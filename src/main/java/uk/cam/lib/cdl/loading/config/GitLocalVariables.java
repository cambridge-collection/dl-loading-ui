package uk.cam.lib.cdl.loading.config;

public class GitLocalVariables {
    private final String gitSourcePath;
    private final String gitSourceDataSubpath;
    private final String gitSourceURL;
    private final String gitSourceURLUserame;
    private final String gitSourceURLPassword;
    private final String gitBranch;

    public GitLocalVariables(String gitSourcePath,
                             String gitSourceDataSubpath,
                             String gitSourceURL,
                             String gitSourceURLUserame,
                             String gitSourceURLPassword,
                             String gitBranch
    ) {
        this.gitSourcePath = gitSourcePath;
        this.gitSourceDataSubpath = gitSourceDataSubpath;
        this.gitSourceURL = gitSourceURL;
        this.gitSourceURLUserame = gitSourceURLUserame;
        this.gitSourceURLPassword = gitSourceURLPassword;
        this.gitBranch = gitBranch;
    }

    public String getGitSourcePath() {
        return gitSourcePath;
    }

    public String getGitSourceDataSubpath() {
        return gitSourceDataSubpath;
    }

    public String getGitSourceURL() {
        return gitSourceURL;
    }

    public String getGitSourceURLUsername() {
        return gitSourceURLUserame;
    }

    public String getGitSourceURLPassword() {
        return gitSourceURLPassword;
    }

    public String getGitBranch() {
        return gitBranch;
    }
}
