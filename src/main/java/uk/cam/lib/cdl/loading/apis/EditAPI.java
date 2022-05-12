package uk.cam.lib.cdl.loading.apis;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.*;
import uk.cam.lib.cdl.loading.model.editor.modelops.*;
import uk.cam.lib.cdl.loading.model.editor.ui.UICollection;
import uk.cam.lib.cdl.loading.utils.ThrowingSupplier;
import uk.cam.lib.cdl.loading.utils.sets.SetMembershipTransformation;

import javax.validation.constraints.NotNull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;

/*
TODO This should be refactored out to talk to a external API.
Access info from the git data directly for now.
*/
public class EditAPI {
    private static final Logger LOG = LoggerFactory.getLogger(EditAPI.class);

    private final Path dataPath;
    private final Path dataItemPath;
    private final Path datasetFile;
    private final Path uiFile;
    private final ModelStateHandlerResolver stateHandlers;
    private final ObjectMapper objectMapper;

    private Map<String, Collection> collectionMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Path> collectionFilepaths = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Item> itemMap = Collections.synchronizedMap(new HashMap<>());
    private Map<String, UICollection> uiCollectionMap = Collections.synchronizedMap(new HashMap<>());
    private final Pattern filenamePattern = Pattern.compile("^[a-zA-Z0-9]+-[a-zA-Z0-9]+[a-zA-Z0-9\\-]*-[0-9]{5}$");

    /**
     * Initialise an EditAPI instance by reading model data from a git repository.
     *
     * @param dataPath          File path to source data
     * @param dlDatasetFilename Filename for the root JSON file for this dataset.
     * @param dataItemPath      File path to the item TEI directory.
     * @throws IllegalStateException If the initial model load from the git repo fails.
     */
    public EditAPI(String dataPath, String dlDatasetFilename, String dlUIFilename, String dataItemPath) throws EditApiException {

        this.dataPath = Path.of(dataPath).normalize();
        Preconditions.checkArgument(this.dataPath.isAbsolute(), "dataPath is not absolute: %s", dataPath);
        this.datasetFile = this.dataPath.resolve(dlDatasetFilename).normalize();
        this.uiFile = this.dataPath.resolve(dlUIFilename).normalize();
        this.dataItemPath = Path.of(dataItemPath).normalize();
        Preconditions.checkArgument(this.dataItemPath.startsWith(this.dataPath), "dataItemPath is not under dataPath");
        this.objectMapper = createObjectMapper();
        this.stateHandlers = createStateHandlers();

        updateModel();
    }

    private ModelStateHandlerResolver createStateHandlers() {
        return DefaultModelStateHandlerResolver.builder()
            .addHandlers(
                ModelStates.itemWriter(ModelOps(), this.dataPath),
                ModelStates.itemRemover(ModelOps(), this.dataPath),
                ModelStates.collectionWriter(ModelOps(), this.objectMapper, this.dataPath),
                ModelStates.collectionRemover(ModelOps(), this.dataPath)
            )
            .build();
    }

    private ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
        return mapper;
    }

    /**
     * Re-reads data from the file system and converts this into the object Model.
     * Helpful to update from Git changes made elsewhere.
     *
     * @throws IOException On problems accessing file system data
     */
    public synchronized void updateModel() throws EditApiException {
        try {
            _updateModel();
        }
        catch (IOException e) {
            throw new EditApiException("Updating model failed: " + e.getMessage(), e);
        }
    }
    private synchronized void _updateModel() throws EditApiException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        Dataset dataset = mapper.readValue(datasetFile.toFile(), Dataset.class);
        Map<String, Collection> newCollectionMap = Collections.synchronizedMap(new HashMap<>());
        Map<String, Path> newCollectionFilepaths = Collections.synchronizedMap(new HashMap<>());
        Map<String, Item> newItemMap = Collections.synchronizedMap(new HashMap<>());

        // Setup collections
        for (Id id : dataset.getCollections()) {

            try {
                var collectionFile = datasetFile.resolveSibling(id.getId()).normalize();
                Preconditions.checkState(collectionFile.startsWith(dataPath), "Collection '%s' is not under dataPath", id.getId());
                String collectionId = dataPath.relativize(collectionFile).toString();
                if (!Files.exists(collectionFile)) {
                    throw new FileNotFoundException("Collection file cannot be found at: " + collectionFile);
                }

                Collection c = mapper.readValue(collectionFile.toFile(), Collection.class);
                c.setCollectionId(collectionId);

                // Setup collection maps
                newCollectionMap.put(collectionId, c);
                newCollectionFilepaths.put(collectionId, collectionFile);

                for (Id relativeItemId : c.getItemIds()) {
                    try {
                        var itemFile = collectionFile.resolveSibling(relativeItemId.getId());
                        var itemId = dataPath.relativize(itemFile);
                        newItemMap.put(itemId.toString(), ImmutableItem.of(itemId));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.collectionMap = newCollectionMap;
        this.collectionFilepaths = newCollectionFilepaths;
        this.itemMap = newItemMap;

        // Update UI
        Map<String, UICollection> newUICollectionMap = Collections.synchronizedMap(new HashMap<>());
        UI ui = mapper.readValue(uiFile.toFile(), UI.class);
        for (UICollection collection : ui.getThemeData().getCollections()) {

            // Get collection Id from the filepath
            String collectionId = collection.getCollection().getId();
            newUICollectionMap.put(collectionId, collection);

        }
        this.uiCollectionMap = newUICollectionMap;

        for (Collection c : collectionMap.values()) {
            String thumbnailURL = getCollectionThumbnailURL(c.getCollectionId());
            c.setThumbnailURL(thumbnailURL);
        }

    }

    /**
     * Get the ThumbnailURL for a collection.
     *
     * @param collectionId relative to the ui file.
     * @return
     */
    private String getCollectionThumbnailURL(@NotNull String collectionId) {

        UICollection uiCollection = uiCollectionMap.get(collectionId);
        return uiCollection.getThumbnail().getId();

    }

    private void setUICollection(UICollection uiCollection, String collectionId) throws EditApiException {
        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
        mapper.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        UI ui = null;
        try {
            ui = mapper.readValue(uiFile.toFile(), UI.class);
        } catch (IOException e) {
            throw new EditApiException(String.format(
                "Failed to load UI file '%s': %s", uiFile, e.getMessage()), e);
        }
        for (UICollection collection : ui.getThemeData().getCollections()) {
            String thisCollectionPath = collection.getCollection().getId();

            if (thisCollectionPath.equals(collectionId)) {

                collection.setThumbnail(uiCollection.getThumbnail());
                collection.setLayout(uiCollection.getLayout());
                ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
                try {
                    writer.writeValue(uiFile.toFile(), ui);
                } catch (IOException e) {
                    throw new EditApiException(String.format(
                        "Failed to save UI file '%s': %s", uiFile, e.getMessage()), e);
                }
                break;
            }
        }
    }
    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public Stream<Collection> streamCollections() {
        return collectionMap.values().stream()
            .map(Collection::copyOf);
    }

    public List<Collection> getCollections() {
        return streamCollections().collect(toImmutableList());
    }

    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public UICollection getCollectionUI(String collectionId) {

        UICollection uiCollection = uiCollectionMap.get(collectionId);
        return uiCollection;
    }

    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public Collection getCollection(Path id) {
        ModelOps().validatePathForId(id);
        var collection = collectionMap.get(id.toString());
        if(collection == null) {
            throw new NotFoundException(String.format("Collection not found: '%s'", id));
        }
        // Collections are mutable, so we need to copy collections we share to
        // keep our data in a predictable state.
        return Collection.copyOf(collection);
    }

    public Collection getCollection(String collectionId) {
        return getCollection(Path.of(collectionId));
    }

    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public java.util.Collection<Item> getItems() {
        return ImmutableList.copyOf(itemMap.values());
    }

    public boolean itemExists(Path id) {
        try {
            getItem(id);
            return true;
        }
        catch (NotFoundException ignored) {}
        return false;
    }

    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public Item getItem(Path id) {
        ModelOps().validatePathForId(id);
        var item = itemMap.get(id.toString());
        if(item == null) {
            throw new NotFoundException(String.format("Item not found: '%s'", id));
        }
        return item;
    }
    public Item getItem(String id) {
        return getItem(Path.of(id));
    }
    public Item getItemWithData(Item item) throws EditApiException {
        return getItemWithData(item.id());
    }
    @PreAuthorize("@roleService.canViewWorkspaces(authentication) || @roleService.canEditWorkspaces(authentication)")
    public Item getItemWithData(Path id) throws EditApiException {
        var item = getItem(id); // validate the item exists
        try {
            return ImmutableItem.copyOf(item)
                .withFileData(ModelOps().readItemMetadataAsString(this.dataPath, item));
        }
        catch (IOException e) {
            throw new EditApiException("Failed to read item file: " + e.getMessage(), e);
        }
    }

    private Path getDataItemPath() {
        return dataItemPath;
    }

    /**
     * Modify an Item's data and or the Collections that it's referenced from.
     *
     * @param item The state of the Item to enforce.
     * @param collectionMembership A transform modifying the Collections referencing the item.
     * @return The state of the Item which was enforced. Will be empty if only collections were changed.
     * @throws EditApiException
     * @see uk.cam.lib.cdl.loading.utils.sets.SetMembership
     */
    // FIXME: This needs permission enforcement
    // @PreAuthorize("@roleService.canEditCollection(#collectionId,authentication)")
    public Optional<ModelState<Item>> enforceItemState(
        Item item,
        SetMembershipTransformation<Path> collectionMembership
    ) throws EditApiException {
        Preconditions.checkArgument(itemExists(item.id()) || item.fileData().isPresent(),
            "item must have fileData if it's being created");

        return updateData(() -> {
            var requiredStateChanges = ModelOps().transformItem(
                item, this.collectionMap.values(), collectionMembership);

            var results = ModelOps().enforceModelState(requiredStateChanges, this.stateHandlers);

            // There won't be any item change enforced if it existed and had no attached file data
            return results.stream()
                .map(ModelStateEnforcementResult::state)
                .flatMap(state -> state.match(Item.class).stream())
                .findFirst();
        });
    }

    public Optional<ModelState<Item>> enforceItemState(
        Path itemId,
        SetMembershipTransformation<Path> collectionMembership
    ) throws EditApiException {
        return enforceItemState(getItem(itemId), collectionMembership);
    }

    protected <T, Err extends EditApiException> T updateData(ThrowingSupplier<T, Err> operation) throws EditApiException {
        Preconditions.checkNotNull(operation);

        var result = operation.get();
        updateModel();
        return result;

    }

    @PreAuthorize("@roleService.canEditCollection(#collection.collectionId, authentication) ||" +
            " @roleService.canEditWorkspace(#workspaceIds, authentication)")
    public void updateCollection(Collection collection, String descriptionHTML, String creditHTML,
                                    UICollection uiCollection, List<Long> workspaceIds) throws EditApiException {
        try {

            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(JsonParser.Feature.ALLOW_COMMENTS);
            mapper.enable(JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature());
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());

            // Set metadata for error tracing


            String collectionId = collection.getCollectionId();

            Path collectionFilepath = collectionFilepaths.get(collectionId);
            if (collectionFilepath == null) {
                // New item so create filepath
                collectionFilepath = dataPath.resolve(collectionId).normalize();
                Preconditions.checkState(collectionFilepath.startsWith(dataPath), "collection path is not under dataPath: '%s'", collectionFilepath);
            }

            // If collection is not in dataset file add it.
            Dataset dataset = mapper.readValue(datasetFile.toFile(), Dataset.class);
            List<Id> collectionIds = dataset.getCollections();
            if (!collectionIds.contains(new Id(collectionId))) {
                collectionIds.add(new Id(collectionId));
                writer.writeValue(datasetFile.toFile(), dataset);
            }

            // If collectionThumbnail is not in uiFile add it
            // TODO: Allow settings or collection form to define UI layout for new collections
            UI ui = mapper.readValue(uiFile.toFile(), UI.class);
            List<UICollection> uiCollections = ui.getThemeData().getCollections();
            if (!UICollectionContains(collectionId, uiCollections)) {
                uiCollections.add(new UICollection(new Id(collectionId),
                    uiCollection.getLayout(), uiCollection.getThumbnail()));
                writer.writeValue(uiFile.toFile(), ui);
            }

            // Write out collection file
            writer.writeValue(collectionFilepath.toFile(), collection);

            // Write out HTML section files
            var descriptionHTMLFile = collectionFilepath.getParent().resolve(collection.getDescription().getFull().getId()).normalize();
            Preconditions.checkState(descriptionHTMLFile.startsWith(dataPath), "descriptionHTMLFile is not under dataPath: %s", descriptionHTMLFile);
            FileUtils.write(descriptionHTMLFile.toFile(), descriptionHTML, "UTF-8");

            var creditHTMLFile = collectionFilepath.getParent().resolve(collection.getCredit().getProse().getId()).normalize();
            Preconditions.checkState(creditHTMLFile.startsWith(dataPath), "creditHTMLFile is not under dataPath: %s", creditHTMLFile);
            FileUtils.write(creditHTMLFile.toFile(), creditHTML, "UTF-8");

            // Update collection thumbnail in the UI
            setUICollection(uiCollection, collectionId);

            updateModel();

        } catch (IOException e) {
            throw new EditApiException("Failed to update Collection: " + e.getMessage(), e);
        }
    }

    public Path getDataLocalPath() {
        return dataPath;
    }

    public Path getCollectionPath(String collectionId) {
        return collectionFilepaths.get(collectionId);
    }

    public Path getDatasetFile() {
        return datasetFile;
    }

    private boolean UICollectionContains(String collectionId, List<UICollection> uiCollections) {
        for (UICollection c : uiCollections) {
            if (c.getCollection().equals(new Id(collectionId))) {
                return true;
            }
        }
        return false;
    }
}

