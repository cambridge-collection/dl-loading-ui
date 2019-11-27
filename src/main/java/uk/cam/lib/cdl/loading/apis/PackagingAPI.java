package uk.cam.lib.cdl.loading.apis;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.json.JSONObject;
import uk.cam.lib.cdl.loading.config.GitSourceVariables;
import uk.cam.lib.cdl.loading.model.WebResponse;
import uk.cam.lib.cdl.loading.model.packaging.PackagingStatus;
import uk.cam.lib.cdl.loading.model.packaging.Pipeline;
import uk.cam.lib.cdl.loading.model.packaging.Update;

import java.io.IOException;
import java.util.*;

public class PackagingAPI {

    private final GitHelper gitHelper;
    private final BitBucketAPI bitBucketAPI;

    public PackagingAPI(GitSourceVariables gitSourceVariables, BitBucketAPI bitBucketAPI) {
        this.gitHelper = new GitHelper(gitSourceVariables);
        this.bitBucketAPI = bitBucketAPI;
    }

    public List<Pipeline> getHistory() {
        return bitBucketAPI.getPipelines();
    }

    public List<Update> updatesSinceLastPackage() {

        // TODO summarise changes instead.
        // Counts number of commits since last tag (or ever if no tag)
        try {

            Repository repo = gitHelper.getGitInstance().getRepository();
            List<RevCommit> commits = new ArrayList<>();
            List<String> tagNames = new ArrayList<>();

            Collection<Ref> allTags = gitHelper.getGitInstance().tagList().call();
            for (Ref ref : allTags) {
                Ref peeledRef = repo.getRefDatabase().peel(ref);
                if (peeledRef.getPeeledObjectId() != null) {
                    tagNames.add(peeledRef.getPeeledObjectId().getName());
                } else {
                    tagNames.add(ref.getObjectId().getName());
                }
            }

            RevWalk revWalk = new RevWalk(repo);
            revWalk.markStart(revWalk.parseCommit(repo.resolve("HEAD")));
            for (RevCommit next : revWalk) {
                if (tagNames.contains(next.getId().getName())) {
                    break;
                }

                commits.add(next);
            }
            revWalk.close();

            // Convert commits to updates.
            List<Update> updates = new ArrayList<>();
            for (RevCommit commit : commits) {
                updates.add(getUpdate(commit));
            }
            return updates;

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    private Update getUpdate(RevCommit commit) {

        PersonIdent author = commit.getAuthorIdent();
        String name = author.getName();
        Date date = commit.getAuthorIdent().getWhen();
        TimeZone authorTimeZone = commit.getAuthorIdent().getTimeZone();

        List<String> filesChanged = new ArrayList<>();

        try {

            Repository repo = gitHelper.getGitInstance().getRepository();
            ObjectReader reader = repo.newObjectReader();

            // Assumes that there is at least one parent to the current commit.
            RevCommit parent = commit.getParent(0);
            CanonicalTreeParser headTreeIter = new CanonicalTreeParser();
            headTreeIter.reset(reader, parent.getTree());

            CanonicalTreeParser commitTreeIter = new CanonicalTreeParser();
            commitTreeIter.reset(reader, commit.getTree());

            DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
            df.setRepository(gitHelper.getGitInstance().getRepository());
            List<DiffEntry> entries = df.scan(headTreeIter, commitTreeIter);

            for (DiffEntry diffEntry : entries) {
                filesChanged.add(diffEntry.getPath(DiffEntry.Side.NEW));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new Update(name, date, authorTimeZone, filesChanged);

    }

    /**
     * @return UUID String for pipeline
     */
    public String startProcess() {

        // Trigger pipeline and return UUID
        WebResponse response = bitBucketAPI.triggerPipeline();
        // get the UUID
        JSONObject content = new JSONObject(response.getResponse());
        return content.getString("uuid");

    }

    public PackagingStatus getStatus(String UUID) {

        return bitBucketAPI.getStatus(UUID);

    }

}
