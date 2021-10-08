package uk.cam.lib.cdl.loading.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import org.eclipse.jgit.api.AddCommand;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.cam.lib.cdl.loading.config.GitLocalVariables;
import uk.cam.lib.cdl.loading.exceptions.GitHelperException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for help with git requests
 */
public class GitHelper {
    private static final Logger LOG = LoggerFactory.getLogger(GitHelper.class);

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

    public GitHelper(GitLocalVariables gitSourceVariables) throws GitHelperException {
        this(findOrCreateRepoLock(gitSourceVariables.getGitSourcePath()), gitSourceVariables, null);
    }

    public GitHelper(Git git, GitLocalVariables gitSourceVariables) throws GitHelperException {
        this(findOrCreateRepoLock(gitSourceVariables.getGitSourcePath()), gitSourceVariables, Preconditions.checkNotNull(git));
    }

    private GitHelper(Map.Entry<String, Object> lock, GitLocalVariables gitSourceVariables, Git git) throws GitHelperException {
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

    private Git setupRepo(
        String gitSourcePath, String gitSourceURL, String gitSourceURLUsername,
        String gitSourceURLPassword
    ) throws GitHelperException { synchronized (lock) {
        File dir = new File(gitSourcePath);
        if (dir.exists()) {
            try {
                return Git.init().setDirectory(dir).call();
            } catch (GitAPIException e) {
                throw new GitHelperException(String.format(
                    "Failed to initialise git repo in dir '%s': %s", dir, e.getMessage()), e);
            }
        } else {
            try {
                return Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setBranch(gitSourceVariables.getGitBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(
                        gitSourceURLUsername, gitSourceURLPassword))
                    .setDirectory(dir)
                    .call();
            } catch (GitAPIException e) {
                throw new GitHelperException(String.format(
                    "Failed to clone '%s' into dir '%s': %s", gitSourceURL, dir, e.getMessage()), e);
            }
        }
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
    public <T, Err extends Exception> T writeFilesAndPushOrRollBack(ThrowingSupplier<T, Err> operation) throws GitHelperException { synchronized (lock) {
        T result;
        ObjectId initialRevision = null;
        try {
            try {
                initialRevision = git.getRepository().resolve(Constants.HEAD);
            } catch (IOException e) {
                throw new GitHelperException("Failed to resolve HEAD revision: " + e.getMessage(), e);
            }
            try {
                result = operation.get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new GitHelperException("The supplied operation failed: " + e.getMessage(), e);
            }
            pushGitChanges();
            return result;
        } catch (RuntimeException | GitHelperException e) {
            rollbackUncommittedChanges(initialRevision == null ? Constants.HEAD : initialRevision.getName());

            if(e instanceof GitHelperException) {
                throw (GitHelperException)e;
            }
            throw (RuntimeException)e;
        }
    }}

    /**
     * Wipe uncommitted changes in the git repository, returning it to the
     * committed state of the current branch HEAD.
     */
    public void rollbackUncommittedChanges(String ref) throws GitHelperException { synchronized (lock) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(ref).call();
        } catch (GitAPIException e) {
            throw new GitHelperException(
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
    public void pullGitChanges() throws GitHelperException { synchronized (lock) {
        try {
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
                    LOG.error("Merging remote and local changes during `git pull` failed - human intervention is " +
                        "currently required to resolve this situation. pull result: {}", pullResult);
                    throw new GitHelperException(
                        "Failed to update repository from remote: Updating local copy after fetch failed: " + pullResult);
                }
            }
        } catch (GitAPIException e) {
            throw new GitHelperException("Failed to update repository from remote: " + e.getMessage(), e);
        }
    }}

    /**
     * @return
     */
    public void pushGitChanges() throws GitHelperException { synchronized (lock) {
        try {
            try {
                pullGitChanges();
            } catch (GitHelperException e) {
                throw new GitHelperException("Failed to pull changes before pushing: " + e.getMessage(), e);
            }

            // Remove files that have been deleted from the working copy
            var status = git.status().call();
            if(!status.getMissing().isEmpty()) {
                var rmCmd = git.rm();
                status.getMissing().forEach(rmCmd::addFilepattern);
                rmCmd.call();
            }

            AddCommand addCommand = git.add();
            for (String pattern: status.getModified()) {
                addCommand = addCommand.addFilepattern(pattern);
            }
            for (String pattern: status.getUntracked()) {
                addCommand = addCommand.addFilepattern(pattern);
            }
            addCommand.call();

            git.commit().setMessage("Changed from Loading UI").call();
            Iterable<PushResult> results = git.push().setCredentialsProvider(
                new UsernamePasswordCredentialsProvider(gitSourceVariables.getGitSourceURLUsername(),
                    gitSourceVariables.getGitSourceURLPassword())).call();

            var nonUpdatableRefsDesc = Streams.stream(results).flatMap(pushResult -> pushResult.getRemoteUpdates().stream())
                .filter(remoteRefUpdate -> remoteRefUpdate.getStatus() != RemoteRefUpdate.Status.OK)
                .map(RemoteRefUpdate::toString)
                .collect(Collectors.joining(", "));

            if(!nonUpdatableRefsDesc.isEmpty()) {
                // TODO Handle conflict problems
                LOG.error("Updating remote to match local refs during `git push` failed - human intervention is " +
                    "currently required to resolve this situation. Non-updatable refs: {}", nonUpdatableRefsDesc);
                throw new GitHelperException("Not all refs could be updated: " + nonUpdatableRefsDesc);
            }
        } catch (GitAPIException e) {
            throw new GitHelperException("Pushing changes failed: " + e.getMessage(), e);
        } catch (GitHelperException e) {
            // Flatten exceptions thrown from this function
            throw new GitHelperException("Pushing changes failed: " + e.getMessage(), e.getCause());
        }
    }}

    public List<RevObject> getTags() throws GitHelperException { synchronized (lock) {
        try {
            List<Ref> refs = git.tagList().call();
            List<RevObject> output = new ArrayList<>();

            for (Ref ref : refs) {
                RevWalk walk = new RevWalk(git.getRepository());
                output.add(walk.parseAny(ref.getObjectId()));
            }

            return output;
        } catch (GitAPIException | IOException e) {
            throw new GitHelperException("Listing repository tags failed: " + e.getMessage(), e);
        }
    }}

}

