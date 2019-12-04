package uk.cam.lib.cdl.loading.apis;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for help with git requests
 * package-private
 */
public class GitHelper {

    private final Git git;
    private final GitLocalVariables gitSourceVariables;

    GitHelper(GitLocalVariables gitSourceVariables) {
        this.gitSourceVariables = gitSourceVariables;
        git = setupRepo(gitSourceVariables.getGitSourcePath(), gitSourceVariables.getGitSourceURL(),
            gitSourceVariables.getGitSourceURLUserame(),
            gitSourceVariables.getGitSourceURLPassword());
    }

    GitHelper(Git git, GitLocalVariables gitSourceVariables) {
        this.gitSourceVariables = gitSourceVariables;
        this.git = git;
    }

    public Git getGitInstance() {
        return git;
    }

    private synchronized Git setupRepo(String gitSourcePath, String gitSourceURL, String gitSourceURLUserame,
                                       String gitSourceURLPassword) {
        try {
            File dir = new File(gitSourcePath);
            if (dir.exists()) {

                return Git.init().setDirectory(dir).call();

            } else {

                return Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setBranch(gitSourceVariables.getGitBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitSourceURLUserame,
                        gitSourceURLPassword))
                    .setDirectory(new File(gitSourcePath))
                    .call();

            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns true if there were any changes.
     * Runs on a schedule form EditConfig
     *
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public boolean pullGitChanges() throws GitAPIException {

        FetchResult fetchResult = git.fetch().setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUserame(),
                gitSourceVariables.getGitSourceURLPassword())).call();

        // Check for changes, and pull if there have been.
        if (!fetchResult.getTrackingRefUpdates().isEmpty()) {
            PullResult pullResult = git.pull().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUserame(),
                    gitSourceVariables.getGitSourceURLPassword())).call();
            if (!pullResult.isSuccessful()) {
                // TODO Handle conflict problems
                System.err.println("Pull Request Failed: " + pullResult.toString());
                return false;
            }
        }
        return true;

    }

    /**
     * @return
     */
    public boolean pushGitChanges() {
        try {
            boolean pullSuccess = pullGitChanges();
            if (!pullSuccess) {
                System.err.println("Problem pulling changes");
                return false;
            }
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Changed from Loading UI").call();
            Iterable<PushResult> results = git.push().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUserame(),
                    gitSourceVariables.getGitSourceURLPassword())).call();

            for (PushResult pushResult : results) {
                java.util.Collection<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate ref : remoteUpdates) {
                    if (ref.getStatus() != RemoteRefUpdate.Status.OK) {
                        // TODO Handle conflict problems
                        System.err.println("Problem pushing changes");
                        return false;
                    }
                }
            }
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RevObject> getTags() {
        try {
            List<Ref> refs = git.tagList().call();
            List<RevObject> output = new ArrayList<>();

            for (Ref ref : refs) {
                RevWalk walk = new RevWalk(git.getRepository());
                output.add(walk.parseAny(ref.getObjectId()));
            }

            return output;
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

        public String getDataLocalPath() {
        return gitSourceVariables.getGitSourcePath() + gitSourceVariables.getGitSourceDataSubpath();
    }
}

