package uk.cam.lib.cdl.loading.exceptions;

public class NotFoundException extends Exception {

    public NotFoundException (Exception ex) {
        super ("unknown instanceid", ex);
    }

}
