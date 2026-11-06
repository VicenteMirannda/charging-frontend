package pt.ipcb.ad.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * HTTP client (OpenFeign) for the charging-session-service.
 * Errors returned by the API are converted into {@link SessionApiException} by {@link SessionApiErrorDecoder}.
 */
@FeignClient(name = "charging-session-service",
        url = "${clients.charging-session-service.url}",
        configuration = SessionApiClientConfig.class)
public interface SessionApiClient {

    @PostMapping("/api/sessions")
    SessionView startSession(@RequestBody StartSessionCommand command);

    @PostMapping("/api/sessions/{id}/stop")
    SessionView stopSession(@PathVariable("id") Long id);

    @GetMapping("/api/sessions")
    List<SessionView> getSessionsByVehicle(@RequestParam("vehicleId") Long vehicleId);
}
