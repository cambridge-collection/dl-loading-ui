package uk.cam.lib.cdl.loading.exceptions;

public class BadRequestException extends Exception {

    public BadRequestException(Exception ex) {
        super ("bad input parameter", ex);
    }

}
