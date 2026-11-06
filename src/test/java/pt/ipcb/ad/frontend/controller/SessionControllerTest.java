package pt.ipcb.ad.frontend.controller;

import feign.Request;
import feign.RetryableException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pt.ipcb.ad.frontend.client.SessionApiClient;
import pt.ipcb.ad.frontend.client.SessionApiException;
import pt.ipcb.ad.frontend.client.SessionView;
import pt.ipcb.ad.frontend.client.StartSessionCommand;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/** Tests the pages and forms. The REST API of the charging-session-service is mocked. */
@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SessionApiClient sessionApi;

    @Test
    void sessionsPageShowsHistoryOfSelectedVehicle() throws Exception {
        when(sessionApi.getSessionsByVehicle(1L)).thenReturn(List.of(inProgressSession(5L)));

        mockMvc.perform(get("/sessions").param("vehicleId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attribute("vehicleId", 1L))
                .andExpect(content().string(containsString("In progress")))
                .andExpect(content().string(containsString("10/01/2025 10:00")));
    }

    @Test
    void validFormStartsSessionAndRedirects() throws Exception {
        when(sessionApi.startSession(any(StartSessionCommand.class))).thenReturn(inProgressSession(5L));

        mockMvc.perform(post("/sessions")
                        .param("vehicleId", "1")
                        .param("chargingPointId", "2")
                        .param("startBatteryPercent", "20")
                        .param("targetBatteryPercent", "80"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/sessions?vehicleId=1"))
                .andExpect(flash().attribute("successMessage", "Session 5 started."));

        verify(sessionApi).startSession(new StartSessionCommand(1L, 2L, 20, 80));
    }

    @Test
    void invalidFormShowsErrorsAndDoesNotCallApi() throws Exception {
        mockMvc.perform(post("/sessions")
                        .param("vehicleId", "1")
                        .param("chargingPointId", "2")
                        .param("startBatteryPercent", "90")
                        .param("targetBatteryPercent", "50"))
                .andExpect(status().isOk())
                .andExpect(view().name("sessions"))
                .andExpect(model().attributeHasFieldErrors("startSessionForm", "targetAboveStart"));

        verify(sessionApi, never()).startSession(any());
    }

    @Test
    void apiErrorIsShownAsMessage() throws Exception {
        when(sessionApi.startSession(any(StartSessionCommand.class)))
                .thenThrow(new SessionApiException(409, "Charging point 3 is not available (current status: OFFLINE)"));

        mockMvc.perform(post("/sessions")
                        .param("vehicleId", "1")
                        .param("chargingPointId", "3")
                        .param("startBatteryPercent", "20")
                        .param("targetBatteryPercent", "80"))
                .andExpect(redirectedUrl("/sessions?vehicleId=1"))
                .andExpect(flash().attribute("errorMessage",
                        "Charging point 3 is not available (current status: OFFLINE)"));
    }

    @Test
    void stopButtonStopsSessionAndRedirects() throws Exception {
        when(sessionApi.stopSession(5L)).thenReturn(inProgressSession(5L));

        mockMvc.perform(post("/sessions/5/stop").param("vehicleId", "1"))
                .andExpect(redirectedUrl("/sessions?vehicleId=1"));

        verify(sessionApi).stopSession(5L);
    }

    @Test
    void showsFriendlyPageWhenServiceIsUnreachable() throws Exception {
        Request request = Request.create(Request.HttpMethod.GET, "/api/sessions",
                Map.of(), null, StandardCharsets.UTF_8, null);
        when(sessionApi.getSessionsByVehicle(1L))
                .thenThrow(new RetryableException(-1, "Connection refused", Request.HttpMethod.GET, (Long) null, request));

        mockMvc.perform(get("/sessions"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(view().name("service-unavailable"));
    }

    private static SessionView inProgressSession(Long id) {
        return new SessionView(id, 1L, 2L, "IN_PROGRESS", 20, 80, 22.0, 36.0, 0.0,
                LocalDateTime.of(2025, 1, 10, 10, 0), null, null);
    }
}
