package uk.cam.lib.cdl.loading.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(Exception ex) {
        super ("bad input parameter", ex);
    }

}
