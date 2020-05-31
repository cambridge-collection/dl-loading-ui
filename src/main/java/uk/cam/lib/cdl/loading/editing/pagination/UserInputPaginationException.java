package uk.cam.lib.cdl.loading.editing.pagination;

/**
 * Thrown if invalid user-supplied data causes something to go wrong when
 * generating/inserting pagination data.
 */
public class UserInputPaginationException extends PaginationException {
    public UserInputPaginationException(String message) {
        super(message);
    }

    public UserInputPaginationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserInputPaginationException(Throwable cause) {
        super(cause);
    }
}
