package uk.cam.lib.cdl.loading.editing.itemcreation;

public class ModelCreationException extends RuntimeException {
    public ModelCreationException() {
        super();
    }

    public ModelCreationException(String message) {
        super(message);
    }

    public ModelCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelCreationException(Throwable cause) {
        super(cause);
    }

    protected ModelCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
