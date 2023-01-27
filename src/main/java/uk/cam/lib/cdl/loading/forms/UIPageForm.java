package uk.cam.lib.cdl.loading.forms;

import javax.validation.constraints.NotBlank;

public class UIPageForm {

    @NotBlank(message = "Must specify a websiteId.")
    private String websiteId;
    @NotBlank(message = "Must specify html")
    private String html;

    public UIPageForm(String websiteId, String html) {

        this.websiteId = websiteId;
        this.html = html;

    }

    public UIPageForm() { }

    public String getHtml() {
        return html;
    }

    public String getWebsiteId() {
        return websiteId;
    }

}

