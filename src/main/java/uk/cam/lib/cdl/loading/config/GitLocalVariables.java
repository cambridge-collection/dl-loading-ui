package uk.cam.lib.cdl.loading.config;

public class GitLocalVariables {
    private final String gitSourcePath;
    private final String gitSourceDataSubpath;
    private final String gitSourceURL;
    private final String gitSourceURLUserame;
    private final String gitSourceURLPassword;
    private final String gitBranch;
    private final String gitDatasetFilename;

    public GitLocalVariables(String gitSourcePath,
                             String gitSourceDataSubpath,
                             String gitSourceURL,
                             String gitSourceURLUserame,
                             String gitSourceURLPassword,
                             String gitBranch,
                             String gitDLDataFilename
    ) {
        this.gitSourcePath = gitSourcePath;
        this.gitSourceDataSubpath = gitSourceDataSubpath;
        this.gitSourceURL = gitSourceURL;
        this.gitSourceURLUserame = gitSourceURLUserame;
        this.gitSourceURLPassword = gitSourceURLPassword;
        this.gitBranch = gitBranch;
        this.gitDatasetFilename = gitDLDataFilename;
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

    public String getGitSourceURLUserame() {
        return gitSourceURLUserame;
    }

    public String getGitSourceURLPassword() {
        return gitSourceURLPassword;
    }

    public String getGitBranch() {
        return gitBranch;
    }

    public String getGitDatasetFilename() {
        return gitDatasetFilename;
    }
}
