package uk.cam.lib.cdl.loading.model.editor.ui;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.cam.lib.cdl.loading.model.editor.Id;

import java.beans.ConstructorProperties;

public class UIStaticPage {

    private Id contributors;
    private Id help;
    private Id news;
    private Id termsConditions;

    @ConstructorProperties({"contributors", "help", "news", "terms-conditions"})
    public UIStaticPage(Id contributors, Id help, Id news, Id termsConditions) {
        this.contributors = contributors;
        this.help = help;
        this.news = news;
        this.termsConditions = termsConditions;
    }

    public Id getContributors() {
        return contributors;
    }

    public Id getHelp() {
        return help;
    }

    public void setHelp(Id help) {
        this.help = help;
    }

    public Id getNews() {
        return news;
    }

    public void setNews(Id news) {
        this.news = news;
    }

    @JsonProperty("terms-conditions")
    public Id getTermsConditions() {
        return termsConditions;
    }
}
