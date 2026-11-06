package pt.ipcb.ad.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;

/**
 * Feign configuration used only by {@link SessionApiClient}.
 * Intentionally not annotated with @Configuration, so it does not apply to other Feign clients.
 */
public class SessionApiClientConfig {

    @Bean
    ErrorDecoder sessionApiErrorDecoder(ObjectMapper objectMapper) {
        return new SessionApiErrorDecoder(objectMapper);
    }
}
