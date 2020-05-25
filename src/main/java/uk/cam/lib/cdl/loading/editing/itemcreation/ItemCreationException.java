package uk.cam.lib.cdl.loading.editing.itemcreation;

public class ItemCreationException extends RuntimeException {
    public ItemCreationException() {
        super();
    }

    public ItemCreationException(String message) {
        super(message);
    }

    public ItemCreationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemCreationException(Throwable cause) {
        super(cause);
    }

    protected ItemCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
