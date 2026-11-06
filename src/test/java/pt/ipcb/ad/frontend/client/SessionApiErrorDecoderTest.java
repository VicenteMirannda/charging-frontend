package pt.ipcb.ad.frontend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SessionApiErrorDecoderTest {

    private final SessionApiErrorDecoder decoder = new SessionApiErrorDecoder(new ObjectMapper());

    @Test
    void usesDetailFromProblemDetailsBody() {
        Response response = response(409, """
                {"status": 409, "detail": "Charging point 1 is not available (current status: IN_USE)"}
                """);

        Exception exception = decoder.decode("startSession", response);

        assertThat(exception).isInstanceOf(SessionApiException.class)
                .hasMessage("Charging point 1 is not available (current status: IN_USE)");
        assertThat(((SessionApiException) exception).getStatus()).isEqualTo(409);
    }

    @Test
    void usesDefaultMessageWhenBodyIsNotProblemDetails() {
        Response response = response(500, "Internal Server Error");

        Exception exception = decoder.decode("startSession", response);

        assertThat(exception).hasMessage("The charging session service returned an error");
    }

    private static Response response(int status, String body) {
        Request request = Request.create(Request.HttpMethod.POST, "/api/sessions",
                Map.of(), null, StandardCharsets.UTF_8, null);
        return Response.builder()
                .status(status)
                .request(request)
                .headers(Map.of())
                .body(body, StandardCharsets.UTF_8)
                .build();
    }
}
