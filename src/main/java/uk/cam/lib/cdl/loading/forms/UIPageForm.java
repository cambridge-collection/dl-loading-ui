package uk.cam.lib.cdl.loading.forms;

import javax.validation.constraints.NotBlank;

public class UIPageForm {

    @NotBlank(message = "Must specify html")
    private String html;

    @NotBlank(message = "Must specify a website Name.")
    private String websiteName;
    @NotBlank(message = "Must specify a website Path.")
    private String websitePath;

    public UIPageForm(String websiteName, String websitePath, String html) {
        this.websiteName = websiteName;
        this.websitePath = websitePath;
        this.html = html;
    }

    public UIPageForm() { }

    public String getHtml() {
        return html;
    }

    public String getWebsiteName() {
        return websiteName;
    }

    public String getWebsitePath() {
        return websitePath;
    }
}

