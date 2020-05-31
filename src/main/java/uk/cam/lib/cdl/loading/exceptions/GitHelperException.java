package uk.cam.lib.cdl.loading.exceptions;

public class GitHelperException extends Exception {
    public GitHelperException() {
        super();
    }

    public GitHelperException(String message) {
        super(message);
    }

    public GitHelperException(String message, Throwable cause) {
        super(message, cause);
    }

    public GitHelperException(Throwable cause) {
        super(cause);
    }
}
