package pt.ipcb.ad.frontend.client;

/** The API answered with an error (e.g. charging point unavailable). The message is safe to show to the user. */
public class SessionApiException extends RuntimeException {

    private final int status;

    public SessionApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
