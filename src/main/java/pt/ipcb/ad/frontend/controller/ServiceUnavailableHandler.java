package pt.ipcb.ad.frontend.controller;

import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/** Shows a friendly page when the charging-session-service cannot be reached (e.g. it is not running). */
@ControllerAdvice
public class ServiceUnavailableHandler {

    private static final Logger log = LoggerFactory.getLogger(ServiceUnavailableHandler.class);

    @ExceptionHandler(RetryableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String handleServiceUnavailable(RetryableException ex) {
        log.warn("charging-session-service is unreachable: {}", ex.getMessage());
        return "service-unavailable";
    }
}
