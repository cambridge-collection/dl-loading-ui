package uk.cam.lib.cdl.loading.editing.modelcreation;

public class AttributeNotFoundException extends ModelCreationException {
    public AttributeNotFoundException() {
        super();
    }

    public AttributeNotFoundException(String message) {
        super(message);
    }

    public AttributeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttributeNotFoundException(Throwable cause) {
        super(cause);
    }

    protected AttributeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
