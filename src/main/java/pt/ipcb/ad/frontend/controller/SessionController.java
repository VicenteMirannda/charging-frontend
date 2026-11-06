package pt.ipcb.ad.frontend.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pt.ipcb.ad.frontend.client.SessionApiClient;
import pt.ipcb.ad.frontend.client.SessionApiException;
import pt.ipcb.ad.frontend.client.SessionView;
import pt.ipcb.ad.frontend.form.StartSessionForm;

/**
 * Web pages for charging sessions. It only handles pages and forms:
 * all business rules live in the charging-session-service, called through {@link SessionApiClient}.
 */
@Controller
public class SessionController {

    private static final long DEFAULT_VEHICLE_ID = 1L;

    private final SessionApiClient sessionApi;

    public SessionController(SessionApiClient sessionApi) {
        this.sessionApi = sessionApi;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/sessions";
    }

    @GetMapping("/sessions")
    public String sessionsPage(@RequestParam(defaultValue = "1") Long vehicleId, Model model) {
        StartSessionForm form = new StartSessionForm();
        form.setVehicleId(vehicleId);
        model.addAttribute("startSessionForm", form);
        addSessionHistory(model, vehicleId);
        return "sessions";
    }

    @PostMapping("/sessions")
    public String startSession(@Valid @ModelAttribute("startSessionForm") StartSessionForm form,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            // Show the same page again, with the errors next to each field
            Long vehicleId = form.getVehicleId() != null ? form.getVehicleId() : DEFAULT_VEHICLE_ID;
            addSessionHistory(model, vehicleId);
            return "sessions";
        }

        try {
            SessionView session = sessionApi.startSession(form.toCommand());
            redirectAttributes.addFlashAttribute("successMessage", "Session " + session.id() + " started.");
        } catch (SessionApiException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        // Post/Redirect/Get: refreshing the page will not submit the form again
        return "redirect:/sessions?vehicleId=" + form.getVehicleId();
    }

    @PostMapping("/sessions/{id}/stop")
    public String stopSession(@PathVariable Long id,
                              @RequestParam Long vehicleId,
                              RedirectAttributes redirectAttributes) {
        try {
            SessionView session = sessionApi.stopSession(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Session " + id + " stopped: " + session.energyDeliveredKwh() + " kWh delivered.");
        } catch (SessionApiException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/sessions?vehicleId=" + vehicleId;
    }

    private void addSessionHistory(Model model, Long vehicleId) {
        model.addAttribute("vehicleId", vehicleId);
        model.addAttribute("sessions", sessionApi.getSessionsByVehicle(vehicleId));
    }
}
