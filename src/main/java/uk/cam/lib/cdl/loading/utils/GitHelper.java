package uk.cam.lib.cdl.loading.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
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

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class for help with git requests
 */
public class GitHelper {
    // Shared per-repo objects to synchronise against. Both keys and values are
    // weak so no memory is leaked - references are kept alive by active
    // GitHelper instances.
    private static final Map<String, Object> REPO_LOCKS =
        new MapMaker().weakKeys().weakValues().makeMap();

    private static Map.Entry<String, Object> findOrCreateRepoLock(String repositoryPath) {
        Preconditions.checkNotNull(repositoryPath);
        var path = Path.of(repositoryPath);
        Preconditions.checkArgument(
            path.isAbsolute() && path.normalize().equals(path),
            "repository path is not an absolute, normalised path: %s",
            repositoryPath);

        // Could do this without locking if we call ourselves recursively after
        // inserting a lock to return the first (i.e. canonical) match.
        synchronized (REPO_LOCKS) {
            // We can't use normal map get() method to look up our key as it
            // checks via reference equality. We could use interned strings as
            // an alternative to this, but then we might as well just not use a
            // weak map anyway...
            var entry = REPO_LOCKS.entrySet().stream()
                .filter(e -> repositoryPath.equals(e.getKey())).findFirst();
            return entry.orElseGet(() -> {
                // Create a lock with our key
                var lock = new Object();
                REPO_LOCKS.put(repositoryPath, lock);
                return Maps.immutableEntry(repositoryPath, lock);
            });
        }
    }

    private final String lockKey; // retained as lock map keys are weak
    /**
     * An object which will be reference-equal for any GitHelper instance with
     * the same git repo filesystem path. We can synchronise against this object
     * to maintain isolation between multiple instances of GitHelper acting on
     * the same repo.
     */
    private final Object lock;
    private final Git git;
    private final GitLocalVariables gitSourceVariables;

    public GitHelper(GitLocalVariables gitSourceVariables) {
        this(findOrCreateRepoLock(gitSourceVariables.getGitSourcePath()), gitSourceVariables, null);
    }

    public GitHelper(Git git, GitLocalVariables gitSourceVariables) {
        this(findOrCreateRepoLock(gitSourceVariables.getGitSourcePath()), gitSourceVariables, Preconditions.checkNotNull(git));
    }

    private GitHelper(Map.Entry<String, Object> lock, GitLocalVariables gitSourceVariables, Git git) {
        this.lockKey = lock.getKey();
        this.lock = lock.getValue();
        this.gitSourceVariables = gitSourceVariables;

        this.git = git != null ? git : setupRepo(
            gitSourceVariables.getGitSourcePath(),
            gitSourceVariables.getGitSourceURL(),
            gitSourceVariables.getGitSourceURLUsername(),
            gitSourceVariables.getGitSourceURLPassword());
    }

    public Git getGitInstance() {
        return git;
    }

    private Git setupRepo(String gitSourcePath, String gitSourceURL, String gitSourceURLUsername,
                                       String gitSourceURLPassword) { synchronized (lock) {
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
    }}

    /**
     * Execute an operation which creates commits in this git repository, with
     * automatic roll-back to the state of the previous repo HEAD commit if an
     * error occurs.
     *
     * <p>{@link #pushGitChanges()} is invoked after the operation, and the
     * changes are rolled back if pushing fails.
     *
     * @param operation An arbitrary function to execute.
     * @return The value returned from the operation.
     */
    public <T> T writeFilesAndPushOrRollBack(Supplier<T> operation) { synchronized (lock) {
        T result;
        ObjectId initialRevision = null;
        try {
            initialRevision = git.getRepository().resolve(Constants.HEAD);
            result = operation.get();
            if (!pushGitChanges()) {
                throw new RuntimeException("pushGitChanges() failed");
            }
            return result;
        } catch (RuntimeException | IOException e) {
            rollbackUncommittedChanges(initialRevision == null ? Constants.HEAD : initialRevision.getName());

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException("git operation failed: " + e.getMessage());
            }
        }
    }}

    /**
     * Wipe uncommitted changes in the git repository, returning it to the
     * committed state of the current branch HEAD.
     */
    public void rollbackUncommittedChanges(String ref) { synchronized (lock) {
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
    }}

    /**
     * Returns true if there were any changes.
     * Runs on a schedule form EditConfig
     *
     * @return
     * @throws GitAPIException
     * @throws IOException
     */
    public boolean pullGitChanges() throws GitAPIException { synchronized (lock) {

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

    }}

    /**
     * @return
     */
    public boolean pushGitChanges() { synchronized (lock) {
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
    }}

    public List<RevObject> getTags() { synchronized (lock) {
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
    }}

    public String getDataLocalPath() {
        return gitSourceVariables.getGitSourcePath() + gitSourceVariables.getGitSourceDataSubpath();
    }
}

