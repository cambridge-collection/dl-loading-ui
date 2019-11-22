package uk.cam.lib.cdl.loading.apis;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import uk.cam.lib.cdl.loading.config.GitSourceVariables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class PackagingAPI {

    private final GitHelper gitHelper;
    private final BitBucketAPI bitBucketAPI;

    public PackagingAPI(GitSourceVariables gitSourceVariables, BitBucketAPI bitBucketAPI) {
        this.gitHelper = new GitHelper(gitSourceVariables);
        this.bitBucketAPI = bitBucketAPI;
    }

    public int commitsSinceLastPackage() {

        // TODO summarise changes instead.
        // Counts number of commits since last tag (or ever if no tag)
        String lastTag = null;
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
            Iterator<RevCommit> iterator = revWalk.iterator();
            while (iterator.hasNext()) {
                RevCommit next = iterator.next();
                if (tagNames.contains(next.getId().getName())) {
                    break;
                }

                commits.add(next);
            }
            revWalk.close();
            return commits.size();

        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * @return UUID String for pipeline
     */
    public String startProcess() {

        // Trigger pipeline and return UUID
        return bitBucketAPI.triggerPipeline();

    }

    public PackagingStatus getStatus(String UUID) {

        return bitBucketAPI.getStatus(UUID);

    }


}
