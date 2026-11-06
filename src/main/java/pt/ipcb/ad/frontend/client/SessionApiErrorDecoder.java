package pt.ipcb.ad.frontend.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Turns an error response from the API into a {@link SessionApiException}.
 * The API returns errors as Problem Details JSON, e.g. {"status": 409, "detail": "Charging point 1 is not available"},
 * so the "detail" field already has a message we can show to the user.
 */
public class SessionApiErrorDecoder implements ErrorDecoder {

    private static final String DEFAULT_MESSAGE = "The charging session service returned an error";

    private final ObjectMapper objectMapper;

    public SessionApiErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        return new SessionApiException(response.status(), readDetail(response));
    }

    private String readDetail(Response response) {
        if (response.body() == null) {
            return DEFAULT_MESSAGE;
        }
        try (InputStream body = response.body().asInputStream()) {
            JsonNode detail = objectMapper.readTree(body).get("detail");
            return detail != null ? detail.asText() : DEFAULT_MESSAGE;
        } catch (IOException e) {
            return DEFAULT_MESSAGE;
        }
    }
}
