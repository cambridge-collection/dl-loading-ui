package uk.cam.lib.cdl.loading;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.UriComponentsBuilder;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableCreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.Issue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes.StandardModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelFactory;
import uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation.ItemIssue;
import uk.cam.lib.cdl.loading.editing.pagination.PaginationIssue;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessor;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.forms.CollectionForm;
import uk.cam.lib.cdl.loading.forms.ItemForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;
import uk.cam.lib.cdl.loading.model.editor.modelops.ModelState;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;
import uk.cam.lib.cdl.loading.utils.sets.SetMembership;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static uk.cam.lib.cdl.loading.EditController.EditIssue.INVALID_FORM_DATA;
import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;


@Controller
public class EditController {
    private static final Logger LOG = LoggerFactory.getLogger(EditController.class);

    private final EditAPI editAPI;
    private final Path pathForDataDisplay;
    private final ModelFactory<Item> teiItemFactory;

    @Autowired
    public EditController(EditAPI editAPI, @Value("${data.url.display}") String pathForDataDisplay, ModelFactory<Item> teiItemFactory) {
        this.editAPI = editAPI;
        this.pathForDataDisplay = Path.of(pathForDataDisplay);
        Preconditions.checkArgument(
            this.pathForDataDisplay.isAbsolute() &&
                this.pathForDataDisplay.normalize().equals(this.pathForDataDisplay),
            "pathForDataDisplay must start with / and not contain relative segments");
        this.teiItemFactory = Preconditions.checkNotNull(teiItemFactory);
    }

    @GetMapping("/edit/edit.html")
    public String edit(Model model) {

        List<Collection> collections = editAPI.getCollections();
        model.addAttribute("collections", collections);

        return "edit";
    }

    /**
     * Display edit collection form
     *
     * @param model        Model object
     * @param collectionId path to collection file
     * @return edit collection view
     * @throws IOException Cannot read collection HTML
     */
    @GetMapping(value = {"/edit/collection/"})
    public String editCollection(Model model, @RequestParam(required = false) String collectionId)
        throws IOException {

        CollectionForm form;
        boolean newCollection = true;
        List<Item> items = ImmutableList.of();

        if (collectionId == null) {
            form = new CollectionForm();
        } else {
            Collection collection = editAPI.getCollection(collectionId);
            if (model.asMap().get("form") == null) {

                var collectionFile = editAPI.getCollectionPath(collectionId);

                // Description HTML
                var fullDescription = collectionFile.getParent().resolve(collection.getDescription().getFull().getId()).normalize();
                String descriptionHTML = FileUtils.readFileToString(fullDescription.toFile(), "UTF-8");
                descriptionHTML = prepareHTMLForDisplay(descriptionHTML, fullDescription);

                // Credit HTML
                var credit = collectionFile.getParent().resolve(collection.getCredit().getProse().getId()).normalize();
                String creditHTML = FileUtils.readFileToString(credit.toFile(), "UTF-8");
                creditHTML = prepareHTMLForDisplay(creditHTML, credit);

                form = new CollectionForm(collectionId, collection, descriptionHTML, creditHTML);
            } else {
                form = (CollectionForm) model.asMap().get("form");
            }
            // Get Item names from Ids
            if (collection != null) {
                newCollection = false;
                items = ModelOps().streamResolvedItemIds(collection)
                    .map(editAPI::getItem)
                    .collect(ImmutableList.toImmutableList());
            }
        }

        // Get thumbnail URL
        String thumbnailURL = form.getThumbnailURL();
        if (thumbnailURL == null || thumbnailURL.isEmpty()) {
            thumbnailURL = "./pages/images/collectionsView/collection" +
                "-blank.jpg"; // TODO read from UI properties.
        }

        model.addAttribute("newCollection", newCollection);
        model.addAttribute("thumbnailURL", thumbnailURL);
        model.addAttribute("form", form);
        model.addAttribute("items", items);
        model.addAttribute("dataLocalPath", editAPI.getDataLocalPath());
        model.addAttribute("pathForDataDisplay", pathForDataDisplay);

        return "edit-collection";
    }

    // Need to parse relative links to add in 'pathForDataDisplay' for local viewing.
    private String prepareHTMLForDisplay(String html, Path HTMLFilePath) {
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("src");

            var collectionRelativePath =
                editAPI.getDataLocalPath().relativize(HTMLFilePath.getParent());
            var imageRelativePath = collectionRelativePath.resolve(src);

            img.attr("src", pathForDataDisplay.resolve(imageRelativePath).normalize().toString());
        }
        return doc.outerHtml();
    }

    // Need to parse links from display to format to be saved.
    // replace 'pathForDataDisplay' with file path to data
    // Generate relative path from collections
    private String prepareHTMLForSaving(String html, Path HTMLFilePath) throws IOException {
        Preconditions.checkArgument(HTMLFilePath.isAbsolute(), "HTMLFilePath is not absolute: %s", HTMLFilePath);
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            var src = Path.of(img.attr("src"));
            Preconditions.checkState(src.startsWith(pathForDataDisplay));
            var imgPath = pathForDataDisplay.relativize(src);
            var imageFile = editAPI.getDataLocalPath().resolve(imgPath).normalize();
            Path relativePath = HTMLFilePath.getParent().relativize(imageFile);

            img.attr("src", relativePath.toString());
        }
        return doc.outerHtml();
    }

    /**
     * Returns the requested file contents specified by filepath if it exists in the checkedout source repo.
     * Currently on /edit/source/
     *
     * @param request HTTPServletRequest
     * @return requested resource
     * @throws BadRequestException If requested resource is not within the dataLocalSourcePath
     * @throws IOException         Unable to find resource
     */
    @GetMapping(value = "${data.url.display}**")
    public ResponseEntity<Resource> editSourceData(HttpServletRequest request)
        throws BadRequestException, IOException {

        var filepath = Path.of(request.getContextPath(), pathForDataDisplay.toString())
            .relativize(Path.of(request.getRequestURI()));

        var file = editAPI.getDataLocalPath().resolve(filepath).normalize();

        // Allow access to git dir checkout only.
        if (!Files.exists(file) || !file.startsWith(editAPI.getDataLocalPath())) {
            throw new BadRequestException(new Exception("File needs to be subdir of git file source."));
        }

        // Assume content type from extension
        String ext = FilenameUtils.getExtension(filepath.toString());
        String filename = FilenameUtils.getName(filepath.toString());

        // Interpret data as a stream for display.
        String contentType = "application/octet-stream";

        if (ext.endsWith(".json5")) {
            contentType = "application/json5";
        } else if (ext.endsWith(".json")) {
            contentType = "application/json";
        } else if (ext.endsWith(".xml")) {
            contentType = "application/xml";
        }

        InputStreamResource resource = new InputStreamResource(new BufferedInputStream(Files.newInputStream(file)));

        return ResponseEntity.ok()
            .contentLength(Files.size(file))
            .header("Content-Disposition", "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    /**
     * TODO validate the changes against the JSON schema.
     * <p>
     * Saves changes to the collection presented in the CollectionForm and redirects to
     * show the updated edit collections page.
     *
     * @param attributes     Model attributes to be used in the redirect
     * @param collectionForm Validated collectionForm from edit Collection page
     * @return RedirectView to the collections page (after updates have been saved).
     */
    // TODO make sure we have permission to edit the collection being edited.
    @PostMapping("/edit/collection/update")
    public RedirectView updateCollection(RedirectAttributes attributes,
                                         @Valid @org.springframework.web.bind.annotation.ModelAttribute CollectionForm collectionForm,
                                         final BindingResult bindingResult) throws IOException {

        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", collectionForm);
            attributes.addAttribute("collectionId", collectionForm.getCollectionId());

            return new RedirectView("/edit/collection/");
        }

        Collection collection = collectionForm.toCollection();


        String collectionId = collectionForm.getCollectionId();
        if (collectionId == null || collectionId.trim().equals("")) {

            // new collection
            String urlSlug = collection.getName().getUrlSlug();
            Preconditions.checkState(!urlSlug.contains("/"));

            // TODO Allow these paths to be set in properties file
            collectionId = "collections/" + urlSlug + ".collection.json";
            collection.setCollectionId(collectionId);
            collection.getDescription().setFull(new Id("../pages/html/collections/" + urlSlug + "/summary.html"));
            collection.getCredit().setProse(new Id("../pages/html/collections/" + urlSlug + "/sponsors.html"));
        }

        // Update links in HTML
        String summaryId = collection.getDescription().getFull().getId();
        String creditId = collection.getCredit().getProse().getId();

        var collectionPath = editAPI.getDataLocalPath().resolve(collectionId).getParent();
        Preconditions.checkState(collectionPath.startsWith(editAPI.getDataLocalPath()));
        var collectionHTMLPath = collectionPath.resolve(summaryId).normalize();
        Preconditions.checkState(collectionHTMLPath.startsWith(editAPI.getDataLocalPath()));
        var creditHTMLPath = collectionPath.resolve(creditId).normalize();
        Preconditions.checkState(creditHTMLPath.startsWith(editAPI.getDataLocalPath()));

        String fullDescriptionHTML = prepareHTMLForSaving(collectionForm.getFullDescriptionHTML(), collectionHTMLPath);
        String proseCreditHTML = prepareHTMLForSaving(collectionForm.getProseCreditHTML(), creditHTMLPath);

        // Save collection file
        try {
            editAPI.updateCollection(collection, fullDescriptionHTML, proseCreditHTML);
            attributes.addFlashAttribute("message", "Collection Updated.");
        }
        catch (EditApiException e) {
            LOG.error("Failed to update collection", e);
            attributes.addFlashAttribute("error", "Failed to update collection.");
        }

        attributes.addAttribute("collectionId", collection.getCollectionId());
        return new RedirectView("/edit/collection/");
    }

    @PostMapping("/edit/collection/deleteItem")
    public RedirectView deleteCollectionItem(RedirectAttributes attributes, @RequestParam String collectionId,
                                             @RequestParam String itemId) {

        Path _itemId, _collectionId;
        try {
            _itemId = ModelOps.ModelOps().validatePathForId(Path.of(itemId));
            _collectionId = ModelOps.ModelOps().validatePathForId(Path.of(collectionId));
        }
        catch (IllegalStateException e) {
            throw new BadRequestException(e);
        }

        attributes.addAttribute("collectionId", collectionId);

        try {
            editAPI.enforceItemState(_itemId, SetMembership.removing(_collectionId));
            attributes.addFlashAttribute("message", "Item deleted from collection.");
        }
        catch (EditApiException e) {
            LOG.error("Failed to delete item from collection", e);
            attributes.addFlashAttribute("error", "Problem deleting item");
        }

        return new RedirectView("/edit/collection/");
    }

    static boolean isMultipartFileProvided(MultipartFile mpf) {
        return !(mpf.isEmpty() && Strings.isNullOrEmpty(mpf.getOriginalFilename()));
    }

    static CreationResult<Optional<Set<ModelAttribute<?>>>> getItemFileContentAttribute(
        Optional<Item> existingItem, ItemForm itemForm
    ) {
        Preconditions.checkNotNull(itemForm.metadataFile());
        Preconditions.checkNotNull(itemForm.paginationFile());
        var directMetadata = Optional.ofNullable(itemForm.metadata());

        Preconditions.checkState(directMetadata.isPresent() == existingItem.isPresent(),
            "directMetadata is expected to co-occur with an item");

        var directMetadataChanged = existingItem
            .map(item -> !(item.fileData().equals(directMetadata))).orElse(false);
        var metadataFileSpecified = isMultipartFileProvided(itemForm.metadataFile());

        // Reject the request if metadata is edited and a metadata file is specified
        if(directMetadataChanged && metadataFileSpecified) {
            return ImmutableCreationResult.unsuccessful(
                ImmutableIssue.of(INVALID_FORM_DATA, "Metadata cannot be edited directly if a metadata file is also uploaded."));
        }

        Set<ModelAttribute<?>> inputAttrs = null;
        if(metadataFileSpecified) {
            var filename = Optional.ofNullable(itemForm.metadataFile().getOriginalFilename())
                .filter(not(String::isEmpty)).orElseThrow(() -> new BadRequestException(
                    new IllegalStateException("No filename available from metadata file upload")));

             inputAttrs = ImmutableSet.of(
                 ModelAttributes.StandardFileAttributes.FILENAME.containing(filename),
                 ModelAttributes.StandardFileAttributes.BYTES.containing(
                 asByteSource(itemForm.metadataFile())));
        }
        else if(directMetadataChanged) {
            inputAttrs = ImmutableSet.of(
                ModelAttributes.StandardFileAttributes.TEXT.containing(CharSource.wrap(directMetadata.orElseThrow())),
                ModelAttributes.StandardFileAttributes.CHARSET.containing(UTF_8));
        }
        else if(isMultipartFileProvided(itemForm.paginationFile())) {
            Preconditions.checkState(existingItem.isPresent(),
                "Pagination provided with no existing item, metadata text or metadata file");
            // If pagination is being updated and no metadata input has been
            // specified then the existing item is re-processed.
            inputAttrs = ImmutableSet.of(
                ModelAttributes.StandardFileAttributes.TEXT.containing(
                    CharSource.wrap(existingItem.orElseThrow().fileData().orElseThrow())),
                ModelAttributes.StandardFileAttributes.CHARSET.containing(UTF_8));
        }
        return ImmutableCreationResult.successful(Optional.ofNullable(inputAttrs));
    }

    /**
     * Get a ByteSource backed by an InputStreamSource (such as a MultiPartFile).
     */
    public static ByteSource asByteSource(InputStreamSource inputStreamSource) {
        return new ByteSource() {
            @Override
            public InputStream openStream() throws IOException {
                return inputStreamSource.getInputStream();
            }
        };
    }

    enum EditIssue implements Issue.Type {
        INVALID_FORM_DATA
    }

    static CreationResult<Set<ModelAttribute<?>>> assembleTeiItemAttributes(Optional<Item> existingItem, ItemForm itemForm) {
        Preconditions.checkArgument(existingItem.map(item -> item.fileData().isPresent()).orElse(true),
            "existingItem must have file data when present");
        Preconditions.checkNotNull(itemForm.paginationFile());

        var attrs = ImmutableSet.<ModelAttribute<?>>builder();
        existingItem.ifPresent(item -> attrs.add(StandardModelAttributes.MODEL_ID.containing(item.id())));

        return getItemFileContentAttribute(existingItem, itemForm).flatMapValue(itemFileAttrs -> {
            itemFileAttrs.ifPresent(attrs::addAll);

            if(isMultipartFileProvided(itemForm.paginationFile())) {
                attrs.add(TeiPaginationGenerationProcessor.Attribute.PAGINATION_ATTRIBUTES.containing(ImmutableSet.of(
                    ModelAttributes.StandardFileAttributes.BYTES.containing(asByteSource(itemForm.paginationFile())),
                    ModelAttributes.StandardFileAttributes.CHARSET.containing(UTF_8)
                )));
            }

            return ImmutableCreationResult.successful(attrs.build());
        });
    }

    static <A, B, E extends Exception> Function<A, B> reThrowing(Class<E> type, ThrowingFunction<A, B, E> f) {
        return a -> {
            try {
                return f.apply(a);
            } catch (Exception e) {
                if(type.isInstance(e)) {
                    throw new RuntimeException(e);
                } else if(e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                throw new AssertionError(
                    "Caught unexpected checked exception: " + e.getClass(), e);
            }
        };
    }

    @PostMapping("/edit/item")
    public Object saveItem(
        @RequestParam(required = false, name = "id") Optional<String> itemId,
        @Valid @org.springframework.web.bind.annotation.ModelAttribute ItemForm itemForm
    ) {
        Preconditions.checkNotNull(itemForm.metadataFile());
        Preconditions.checkNotNull(itemForm.paginationFile());

        var newItemCollections = Arrays.stream(itemForm.collections())
            .map(Path::of)
            .filter(colId -> {
                // Reject the request if any collection IDs are invalid
                try { editAPI.getCollection(colId); }
                catch(NotFoundException | IllegalStateException e) {
                    throw new BadRequestException(e);
                }
                return true;
            })
            .collect(toImmutableSet());

        // Only allow specifying an ID that corresponds to an existing item.
        // FIXME: We currently have no access control to limit who can edit what
        var currentItem = itemId.map(Path::of).map(editAPI::getItemWithData);

        CreationResult<Optional<Void>> validationResult = ImmutableCreationResult.successful(Optional.empty())
            .flatMapValue(ignored -> {
                // New items must have metadata specified
                if(itemId.isEmpty() && !isMultipartFileProvided(itemForm.metadataFile())) {
                    return ImmutableCreationResult.unsuccessful(
                        ImmutableIssue.of(INVALID_FORM_DATA, "Please select an item metadata file to upload."));
                }
                return ImmutableCreationResult.successful(Optional.empty());
            })
            .flatMapValue(ignored -> {
                if(itemId.isEmpty() && newItemCollections.isEmpty()) {
                    return ImmutableCreationResult.unsuccessful(
                        ImmutableIssue.of(INVALID_FORM_DATA, "Please select one or more collections for your item."));
                }
                return ImmutableCreationResult.successful(Optional.empty());
            });

        var itemResult = validationResult
            .flatMapValue(ignored -> assembleTeiItemAttributes(currentItem, itemForm))
            .flatMapValue(reThrowing(IOException.class, teiItemFactory::createFromAttributes))
            .flatMapValue(item -> {
                // Don't allow overwriting existing items when creating new items
                if(itemId.isEmpty() && editAPI.itemExists(item.id())) {
                    return ImmutableCreationResult.unsuccessful(
                        ImmutableIssue.of(INVALID_FORM_DATA,
                            String.format("An item already exists with the name \"%s\"", item.name())));
                }
                return ImmutableCreationResult.successful(item);
            });

        if(itemResult.isSuccessful()) {
            var item = itemResult.value().orElseThrow();
            var outcome = editAPI.enforceItemState(item, SetMembership.onlyMemberOf(newItemCollections));

            return new RedirectView(outcome
                .map(state -> state.ensure() == ModelState.Ensure.ABSENT ? "/edit/edit.html" : getItemEditUrl(state.model()))
                .orElseGet(() -> getItemEditUrl(item)));
        }
        else {
            var errorMessages = itemResult.issues().stream()
                .map(EditController::constructUserErrorMessage)
                .collect(ImmutableList.toImmutableList());
            return getEditItemMavForItem(currentItem, Optional.of(itemForm), Optional.of(errorMessages));
        }
    }

    static String constructUserErrorMessage(Issue issue) {
        if(issue.type() instanceof PaginationIssue) {
            return "TEI pagination could not be generated: " + issue.description();
        }
        else if(issue.type() == ItemIssue.INVALID_INPUT_FILE) {
            return "Your metadata could not be processed: " + issue.description();
        }
        return issue.description();
    }

    protected static String getItemEditUrl(Item item) {
        return UriComponentsBuilder.fromUriString("/edit/item")
            .queryParam("id", item.id().toString())
            .toUriString();
    }

    @GetMapping("/edit/item")
    public ModelAndView createOrEditItem(
        @RequestParam(required = false, name = "id") Optional<String> itemId,
        @RequestParam(required = false, name = "col") Set<String> preSelectedCollectionIds) {
        var mav = getEditItemMavForItemId(itemId.map(Path::of), Optional.empty());

        // Allow pre-selecting collections when creating new items.
        if(itemId.isEmpty() && !preSelectedCollectionIds.isEmpty()) {
            var itemForm = (ItemForm)mav.getModel().get("form");
            var selectedCollections = Sets.intersection(preSelectedCollectionIds,
                editAPI.getCollections().stream().map(Collection::getCollectionId).collect(Collectors.toSet())).toArray(String[]::new);
            itemForm.setCollections(selectedCollections);
        }

        return mav;
    }

    ModelAndView getEditItemMavForItemId(Optional<Path> itemId, Optional<ItemForm> populatedItemForm) {
        return getEditItemMavForItemId(itemId, populatedItemForm, Optional.empty());
    }
    ModelAndView getEditItemMavForItemId(Optional<Path> itemId, Optional<ItemForm> populatedItemForm, Optional<List<String>> errors) {
        var item = itemId.map(id -> {
            var _item = editAPI.getItemWithData(id);
            if(_item == null) {
                throw new NotFoundException(String.format("Item does not exist: '%s'", id));
            }
            return _item;
        });
        return getEditItemMavForItem(item, populatedItemForm, errors);
    }
    ModelAndView getEditItemMavForItem(Optional<Item> item, Optional<ItemForm> populatedItemForm, Optional<List<String>> errors) {
        Preconditions.checkNotNull(item);
        Preconditions.checkNotNull(populatedItemForm);
        Preconditions.checkNotNull(errors);
        Preconditions.checkArgument(item.map(i -> i.fileData().isPresent()).orElse(true),
            "Item must have filedata when present");

        var form = populatedItemForm
            .or(() -> item.map(i -> ItemForm.forItem(editAPI, i)))
            .orElseGet(ItemForm::new);

        var mav = new ModelAndView("edit-item");
        var model = mav.getModel();
        model.put("mode", item.isEmpty() ? "create" : "update");
        model.put("modeLabel", item.isEmpty() ? "Create" : "Edit");
        model.put("itemId", item.map(Item::id).orElse(null));
        model.put("item", item);
        model.put("form", form);
        model.put("collections", editAPI.getCollections());
        model.put("errors", errors);
        model.put("itemDownloadPath", pathForDataDisplay);
        return mav;
    }
}
