package uk.cam.lib.cdl.loading.apis;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.IOException;

public class MockGitRepo {

    private final Git git;
    private final File cloneDir;
    private final File remoteDir;

    public MockGitRepo() throws IOException, GitAPIException {

        // Create a folder in the temp folder that will act as the remote repository
        remoteDir = File.createTempFile("remote", "");
        remoteDir.delete();
        remoteDir.mkdirs();

        // Create a bare repository
        RepositoryCache.FileKey fileKey = RepositoryCache.FileKey.exact(remoteDir, FS.DETECTED);
        Repository remoteRepo = fileKey.open(false);
        remoteRepo.create(true);

        // Clone the bare repository
        cloneDir = File.createTempFile("clone", "");
        cloneDir.delete();
        cloneDir.mkdirs();

        git = Git.cloneRepository().setURI(remoteRepo.getDirectory().getAbsolutePath()).setDirectory(cloneDir).call();

    }


    public Git getGit() {
        return git;
    }

    public File getCloneDir() {
        return cloneDir;
    }

    public File getRemoteDir() {
        return remoteDir;
    }
}
