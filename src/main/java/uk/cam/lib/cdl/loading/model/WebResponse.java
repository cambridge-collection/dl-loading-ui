package uk.cam.lib.cdl.loading.model;

public class WebResponse {

    private final int code;
    private final String response;

    public WebResponse(int code, String response) {

        this.code = code;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public String getResponse() {
        return response;
    }
}
