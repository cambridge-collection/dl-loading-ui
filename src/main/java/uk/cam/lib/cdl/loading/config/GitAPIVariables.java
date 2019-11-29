package uk.cam.lib.cdl.loading.config;

import org.springframework.beans.factory.annotation.Value;

import java.net.URL;

public class GitAPIVariables {

    private final URL gitAPIURL;
    private final String gitBranch;
    private final String tagsURL;
    private final String repoURL;
    private final String pipelinesURL;
    private final String gitUsername;
    private final String gitPassword;

    public GitAPIVariables(@Value("${git.api.url}") URL gitAPIURL,
                           @Value("${git.sourcedata.branch}") String gitBranch,
                           @Value("${git.api.url.part.tags}") String tagsURL,
                           @Value("${git.sourcedata.api.url.part.repo}") String repoURL,
                           @Value("${git.api.url.part.pipelines}") String pipelinesURL,
                           @Value("${git.api.username}") String gitUsername,
                           @Value("${git.api.password}") String gitPassword
    ) {
        this.gitAPIURL = gitAPIURL;
        this.gitBranch = gitBranch;
        this.tagsURL = tagsURL;
        this.repoURL = repoURL;
        this.pipelinesURL = pipelinesURL;
        this.gitUsername = gitUsername;
        this.gitPassword = gitPassword;
    }

    public URL getGitAPIURL() {
        return gitAPIURL;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getTagsURL() {
        return tagsURL;
    }

    public String getRepoURL() {
        return repoURL;
    }

    public String getPipelinesURL() {
        return pipelinesURL;
    }

    public String getGitUsername() {
        return gitUsername;
    }

    public String getGitPassword() {
        return gitPassword;
    }
}
