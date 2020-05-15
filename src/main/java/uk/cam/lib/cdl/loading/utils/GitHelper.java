package uk.cam.lib.cdl.loading.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
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
import java.util.function.Supplier;

/**
 * Class for help with git requests
 */
public class GitHelper {
    private final Git git;
    private final GitLocalVariables gitSourceVariables;

    public GitHelper(GitLocalVariables gitSourceVariables) {
        this.gitSourceVariables = gitSourceVariables;
        git = setupRepo(gitSourceVariables.getGitSourcePath(), gitSourceVariables.getGitSourceURL(),
            gitSourceVariables.getGitSourceURLUsername(),
            gitSourceVariables.getGitSourceURLPassword());
    }

    public GitHelper(Git git, GitLocalVariables gitSourceVariables) {
        this.gitSourceVariables = gitSourceVariables;
        this.git = git;
    }

    public Git getGitInstance() {
        return git;
    }

    private synchronized Git setupRepo(String gitSourcePath, String gitSourceURL, String gitSourceURLUsername,
                                       String gitSourceURLPassword) {
        try {
            File dir = new File(gitSourcePath);
            if (dir.exists()) {

                return Git.init().setDirectory(dir).call();

            } else {

                return Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setBranch(gitSourceVariables.getGitBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitSourceURLUsername,
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
     * Execute an operation which creates commits in this git repository, with
     * automatic roll-back to the state of the previous repo HEAD commit if an
     * error occurs.
     *
     * <p>{@link #pushGitChanges()} is invoked after the operation, and the
     * changes are rolled back if pushing fails.
     *
     * <p>Note that this function is synchronised per GitHelper instance, so
     * applications should ensure only one instance is created per repository
     * to ensure mutual exclusion.
     *
     * @param operation An arbitrary function to execute.
     * @return The value returned from the operation.
     */
    public synchronized <T> T writeFilesAndPushOrRollBack(Supplier<T> operation) {
        T result;
        ObjectId initialRevision = null;
        try {
            initialRevision = git.getRepository().resolve(Constants.HEAD);
            result = operation.get();
            if(!pushGitChanges()) {
                throw new RuntimeException("pushGitChanges() failed");
            }
            return result;
        }
        catch (RuntimeException | IOException e) {
            rollbackUncommittedChanges(initialRevision == null ? Constants.HEAD : initialRevision.getName());

            if(e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else {
                throw new RuntimeException("git operation failed: " + e.getMessage());
            }
        }
    }

    /**
     * Wipe uncommitted changes in the git repository, returning it to the
     * committed state of the current branch HEAD.
     */
    public synchronized void rollbackUncommittedChanges(String ref) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
        }
        catch (GitAPIException e) {
            throw new RuntimeException(
                String.format(
                    "Failed to roll back uncommitted changes to git repository: " +
                        "git hard reset to %s failed: %s",
                    ref, e.getMessage()),
                e);
        }
    }

    /**
     * Returns true if there were any changes.
     * Runs on a schedule form EditConfig
     *
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public synchronized boolean pullGitChanges() throws GitAPIException {

        FetchResult fetchResult = git.fetch().setCredentialsProvider(
            new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUsername(),
                gitSourceVariables.getGitSourceURLPassword())).call();

        // Check for changes, and pull if there have been.
        if (!fetchResult.getTrackingRefUpdates().isEmpty()) {
            PullResult pullResult = git.pull().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUsername(),
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
    public synchronized boolean pushGitChanges() {
        try {
            boolean pullSuccess = pullGitChanges();
            if (!pullSuccess) {
                System.err.println("Problem pulling changes");
                return false;
            }
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Changed from Loading UI").call();
            Iterable<PushResult> results = git.push().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUsername(),
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

