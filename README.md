# Charging Frontend

A small web front-end (Spring MVC + Thymeleaf) for the
[charging-session-service](https://github.com/VicenteMirannda/charging-session-service).

It lets a user start and stop electric vehicle charging sessions and see the session history
of a vehicle. It has **no business logic of its own**: every action is a REST call to the
charging-session-service through an OpenFeign client. This follows the course project
architecture, where the front-end was a separate microservice.

## Architecture

```
 Browser ──HTML forms──► charging-frontend (:8080) ──REST/JSON (OpenFeign)──► charging-session-service (:8012)
```

| Package      | Responsibility                                                               |
|--------------|------------------------------------------------------------------------------|
| `controller` | `SessionController` (pages and forms) and the "service unavailable" handler. |
| `client`     | Feign client for the API, its DTOs and the error decoder.                    |
| `form`       | `StartSessionForm`: what the user types, with validation rules.               |

Main ideas:

- **Post/Redirect/Get**: after a form is submitted the browser is redirected, so refreshing the page does not submit it again. Success and error messages travel with the redirect as *flash attributes*.
- **Validation in two places**: the form is validated here to give quick feedback next to each field. The API validates again, because a service must never trust its clients.
- **API errors become user messages**: `SessionApiErrorDecoder` reads the `detail` field of the API's Problem Details response (for example *"Charging point 1 is not available"*) and shows it on the page.
- **Service down**: if the API cannot be reached, a friendly page is shown instead of a stack trace.

## Running

1. Start the charging-session-service (port 8012). In standalone mode it has demo data.
2. Start this application:

   ```bash
   ./mvnw spring-boot:run
   ```

3. Open http://localhost:8080

The API URL can be changed with `clients.charging-session-service.url`.

## Tests

```bash
./mvnw test
```

- `SessionControllerTest`: pages, form validation, redirects and error messages (`@WebMvcTest`, API mocked).
- `SessionApiErrorDecoderTest`: converting API error responses into messages.
- `FrontendApplicationTests`: the Spring context starts.
