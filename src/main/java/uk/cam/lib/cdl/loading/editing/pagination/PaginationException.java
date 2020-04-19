package uk.cam.lib.cdl.loading.editing.pagination;

/** Thrown if something goes wrong when generating/inserting pagination data. */
public class PaginationException extends RuntimeException {
    public PaginationException(String message) {
        super(message);
    }

    public PaginationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaginationException(Throwable cause) {
        super(cause);
    }
}
