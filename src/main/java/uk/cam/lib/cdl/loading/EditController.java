package uk.cam.lib.cdl.loading;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
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
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.forms.CollectionForm;
import uk.cam.lib.cdl.loading.forms.ItemForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.model.editor.ModelOps;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.ValidationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static uk.cam.lib.cdl.loading.model.editor.ModelOps.ModelOps;


@Controller
public class EditController {
    private static final Logger LOG = LoggerFactory.getLogger(EditController.class);

    private final EditAPI editAPI;
    private final Path pathForDataDisplay;

    @Autowired
    public EditController(EditAPI editAPI, @Value("${data.url.display}") String pathForDataDisplay) {
        this.editAPI = editAPI;
        this.pathForDataDisplay = Path.of(pathForDataDisplay);
        Preconditions.checkArgument(
            this.pathForDataDisplay.isAbsolute() &&
                this.pathForDataDisplay.normalize().equals(this.pathForDataDisplay),
            "pathForDataDisplay must start with / and not contain relative segments");
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
                                         @Valid @ModelAttribute CollectionForm collectionForm,
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

    @PostMapping("/edit/collection/addItem")
    public RedirectView addCollectionItem(RedirectAttributes attributes, @RequestParam String collectionId,
                                          @RequestParam("file") MultipartFile file) throws IOException {

        attributes.addAttribute("collectionId", collectionId);

        if (file.getContentType() == null || !(file.getContentType().equals("text/xml"))) {
            attributes.addFlashAttribute("error", "Item needs to be in TEI XML format.");
            return new RedirectView("/edit/collection/");
        }

        String itemName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!editAPI.validateFilename(itemName)) {
            attributes.addFlashAttribute("error", "Item name not valid. Should be for example: MS-TEST-00001. Using " +
                " characters A-Z or numbers 0-9 and the - character delimiting sections.  Should have at least 3 " +
                " sections, group (MS = Manuscripts, PR = printed etc) then the collection, then a five digit number.");
            return new RedirectView("/edit/collection/");
        }

        if (!editAPI.validate(file)) {
            attributes.addFlashAttribute("error", "Item is not valid XML/JSON or does not validate against the required schema.");
            return new RedirectView("/edit/collection/");
        }

        try {
            editAPI.addItemToCollection(itemName, fileExtension, file.getInputStream(), collectionId);
        }
        catch (EditApiException e) {
            LOG.error("Failed to add item to collection", e);
            attributes.addFlashAttribute("error", "Problem adding item to collection");
            return new RedirectView("/edit/collection/");
        }

        attributes.addFlashAttribute("message", "Item updated/added to collection.");

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
            editAPI.deleteItemFromCollection(_itemId, _collectionId);
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

    @PostMapping("/edit/item")
    public Object saveItem(
        RedirectAttributes attributes,
        @RequestParam(required = false, name = "id") Optional<String> itemId,
        @Valid @ModelAttribute ItemForm itemForm
    ) throws IOException {
        Preconditions.checkNotNull(itemForm.metadataFile());
        Preconditions.checkNotNull(itemForm.paginationFile());

        try {
            if(itemId.isEmpty()) {
                if(!isMultipartFileProvided(itemForm.metadataFile())) {
                    throw new ValidationException("Please select an item metadata file to upload.");
                }

                // TODO: Create item and add to selected collections
            }
        }
        catch(ValidationException e) {
            var mav = getEditItemMav(itemId, Optional.of(itemForm));
            mav.getModel().put("error", e.getMessage());
            return mav;
        }
//        return new RedirectView("/edit/item");
        throw new AssertionError();
    }

    @GetMapping("/edit/item")
    public ModelAndView editItem(@RequestParam(required = false, name = "id") Optional<String> itemId) {
        return getEditItemMav(itemId, Optional.empty());
    }

    ModelAndView getEditItemMav(Optional<String> itemId, Optional<ItemForm> populatedItemForm) {
        Preconditions.checkNotNull(itemId);
        Preconditions.checkNotNull(populatedItemForm);

        var item = itemId.map(id -> {
            var _item = editAPI.getItem(id);
            if(_item == null) {
                throw new NotFoundException(String.format("Item does not exist: '%s'", id));
            }
            return _item;
        });
        var form = populatedItemForm
            .or(() -> item.map(i -> ItemForm.forItem(editAPI, i)))
            .orElseGet(ItemForm::new);

        var mav = new ModelAndView("edit-item");
        var model = mav.getModel();
        model.put("mode", item.isEmpty() ? "create" : "update");
        model.put("modeLabel", item.isEmpty() ? "Create" : "Edit");
        model.put("itemId", itemId.orElse(null));
        model.put("item", item);
        model.put("form", form);
        model.put("collections", editAPI.getCollections());
        return mav;
    }
}
