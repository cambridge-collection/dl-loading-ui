package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import uk.cam.lib.cdl.loading.config.GitVariables;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Dataset;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
TODO This should be refactored out to talk to a external API.
Access info from the git data directly for now.
*/
public class EditAPI {

    @Autowired
    private GitVariables gitVariables;

    private Git git;
    private String dataPath;
    private File datasetFile;
    private Dataset dataset;
    private List<Collection> collections = new ArrayList<Collection>();
    private HashMap<String, Collection> collectionMap = new HashMap<>();

    public EditAPI(String dataPath, String dlDatasetFilename) {
        this.dataPath = dataPath;
        this.datasetFile = new File(dataPath + File.separator + dlDatasetFilename);
    }

    @PostConstruct
    private void setupEditAPI() throws IOException {
        setupRepo(gitVariables.getGitSourcePath(), gitVariables.getGitSourceURL(), gitVariables.getGitSourceURLUserame(),
            gitVariables.getGitSourceURLPassword());

        if (!datasetFile.exists()) {
            throw new FileNotFoundException("Dataset file cannot be found at: " + datasetFile.toPath());
        }

        updateModel();
    }

    private void updateModel() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        dataset = objectMapper.readValue(datasetFile, Dataset.class);

        // Setup collections
        for (Id id : dataset.getCollections()) {

            String collectionPath = id.getId();

            File collectionFile = new File(dataPath + File.separator + collectionPath);
            if (!collectionFile.exists()) {
                throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile.toPath());
            }

            Collection c = objectMapper.readValue(collectionFile, Collection.class);
            c.setFilepath(collectionPath); // is needed to get correct item path
            collections.add(c);

            // Setup collection map
            collectionMap.put(c.getName().getUrlSlug(), c);

            // Items
            List<Item> items = new ArrayList<>();
            for (Id itemId : c.getItemIds()) {
                Item item = this.getItem(itemId.getId(), c);
                items.add(item);
            }
            c.setItems(items);
        }
    }

    public Dataset getDataset() {
        return dataset;
    }

    public List<Collection> getCollections() {
        return collections;
    }

    public Collection getCollection(String urlSlug) {
        return collectionMap.get(urlSlug);
    }

    private Item getItem(String id, Collection parent) throws IOException {
        Item item = new Item();
        File parentFile = new File(parent.getFilepath());
        File f = new File(dataPath, parentFile.getParent());
        f = new File(f.getCanonicalPath(), id);
        item.setFilepath(f.getCanonicalPath());
        item.setName(f.getName());
        return item;
    }

    private void setupRepo(String gitSourcePath, String gitSourceURL, String gitSourceURLUserame, String gitSourceURLPassword) {
        try {
            File dir = new File(gitSourcePath);
            if (dir.exists()) {

                git = Git.init().setDirectory(dir).call();

            } else {

                git = Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setBranch(gitVariables.getGitBranch())
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitSourceURLUserame,
                        gitSourceURLPassword))
                    .setDirectory(new File(gitSourcePath))
                    .call();

            }

        } catch (GitAPIException e) {
            e.printStackTrace();
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
    public boolean pullGitChanges() throws GitAPIException, IOException {

        FetchResult fetchResult = git.fetch().call();

        // Check for changes, and pull if there have been.
        if (!fetchResult.getTrackingRefUpdates().isEmpty()) {
            PullResult pullResult = git.pull().call();
            if (!pullResult.isSuccessful()) {
                // TODO Handle conflict problems
                System.err.println("Pull Request Failed: " + pullResult.toString());
                return false;
            }
            updateModel();
        }
        return true;

    }

    public boolean pushGitChanges() {
        try {
            git.add().addFilepattern(".").call();
            git.commit().setMessage("Changed from Loading UI").call();
            for (PushResult pushResult : git.push().call()) {
                java.util.Collection<RemoteRefUpdate> remoteUpdates = pushResult.getRemoteUpdates();
                for (RemoteRefUpdate ref : remoteUpdates) {
                    if (ref.getStatus() != RemoteRefUpdate.Status.OK) {
                        // TODO Handle conflict problems
                        System.err.println("Problem pushing changes");
                    }
                }
            }
            return true;
        } catch (GitAPIException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getDataLocalPath() {
        return gitVariables.getGitSourcePath() + gitVariables.getGitSourceDataSubpath();
    }
}
