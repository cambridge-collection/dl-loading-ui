package uk.cam.lib.cdl.loading.config;

import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import java.nio.file.Path;

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
        ModelOps.ModelOps().validatePathForIO(Path.of(gitSourcePath));
        ModelOps.ModelOps().validateSubpath(Path.of(gitSourceDataSubpath));
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
