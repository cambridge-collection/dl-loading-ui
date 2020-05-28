package uk.cam.lib.cdl.loading.exceptions;

public class EditApiException extends RuntimeException {
    public EditApiException() {
        super();
    }

    public EditApiException(String message) {
        super(message);
    }

    public EditApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public EditApiException(Throwable cause) {
        super(cause);
    }

    protected EditApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
