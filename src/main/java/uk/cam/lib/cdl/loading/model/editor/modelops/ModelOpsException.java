package uk.cam.lib.cdl.loading.model.editor.modelops;

public class ModelOpsException extends RuntimeException {
    public ModelOpsException() {
        super();
    }

    public ModelOpsException(String message) {
        super(message);
    }

    public ModelOpsException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModelOpsException(Throwable cause) {
        super(cause);
    }

    protected ModelOpsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
