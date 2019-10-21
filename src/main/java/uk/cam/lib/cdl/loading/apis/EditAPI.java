package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Dataset;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

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


    private String dataPath;
    private String dlDatasetFilename;
    private Dataset dataset;
    private List<Collection> collections = new ArrayList<Collection>();
    private HashMap<String, Collection> collectionMap = new HashMap<>();

    public EditAPI(String dataPath, String dlDatasetFilename) throws IOException {
        this.dataPath = dataPath;
        this.dlDatasetFilename = dlDatasetFilename;

        File datasetFile = new File(dataPath + File.separator + dlDatasetFilename);
        if (!datasetFile.exists()) {
            throw new FileNotFoundException("Dataset file cannot be found at: " + datasetFile.toPath());
        }

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

        // TODO Keep this model up to date.

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
}
