package uk.cam.lib.cdl.loading;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.forms.CollectionForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@Controller
public class EditController {

    private final EditAPI editAPI;
    private final String pathForDataDisplay;

    @Autowired
    public EditController(EditAPI editAPI, @Value("${data.url.display}") String pathForDataDisplay) {
        this.editAPI = editAPI;
        this.pathForDataDisplay = pathForDataDisplay;
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
        List<Item> items = new ArrayList<>();

        if (collectionId == null) {
            form = new CollectionForm();
        } else {
            Collection collection = editAPI.getCollection(collectionId);
            if (model.asMap().get("form") == null) {

                File collectionFile = new File(editAPI.getCollectionPath(collectionId));

                // Description HTML
                File fullDescription = new File(collectionFile.getParentFile(),
                    collection.getDescription().getFull().getId());
                String descriptionHTML = FileUtils.readFileToString(fullDescription, "UTF-8");
                descriptionHTML = prepareHTMLForDisplay(descriptionHTML, fullDescription.getCanonicalPath());

                // Credit HTML
                File credit = new File(collectionFile.getParentFile(),
                    collection.getCredit().getProse().getId());
                String creditHTML = FileUtils.readFileToString(credit, "UTF-8");
                creditHTML = prepareHTMLForDisplay(creditHTML, credit.getCanonicalPath());

                form = new CollectionForm(collectionId, collection, descriptionHTML, creditHTML);
            } else {
                form = (CollectionForm) model.asMap().get("form");
            }
            // Get Item names from Ids
            if (collection != null) {
                newCollection = false;
                for (Id id : collection.getItemIds()) {
                    items.add(editAPI.getItem(id.getId()));
                }
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
    private String prepareHTMLForDisplay(String html, String HTMLFilePath) {
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("src");

            String collectionRelativePath =
                new File(HTMLFilePath).getParent().replaceAll(editAPI.getDataLocalPath()
                    , "");

            img.attr("src", pathForDataDisplay + collectionRelativePath + "/" + src);
        }
        return doc.outerHtml();
    }

    // Need to parse links from display to format to be saved.
    // replace 'pathForDataDisplay' with file path to data
    // Generate relative path from collections
    private String prepareHTMLForSaving(String html, String HTMLFilePath) throws IOException {
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("src");
            src = src.replace(pathForDataDisplay, "");
            File imageFile = new File(editAPI.getDataLocalPath(), src);
            String relativePath =
                Paths.get(HTMLFilePath).getParent().relativize(Paths.get(imageFile.getCanonicalPath())).toString();

            img.attr("src", relativePath.replace(editAPI.getDataLocalPath(), ""));
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

        String filepath = request.getRequestURI().split(request.getContextPath() + pathForDataDisplay)[1];

        File file = new File(editAPI.getDataLocalPath() + filepath);

        // Allow access to git dir checkout only.
        if (!file.exists() || !file.toPath().toAbsolutePath().startsWith(editAPI.getDataLocalPath())) {
            throw new BadRequestException(new Exception("File needs to be subdir of git file source."));
        }

        // Assume content type from extension
        String ext = FilenameUtils.getExtension(filepath);
        String filename = FilenameUtils.getName(filepath);

        // Interpret data as a stream for display.
        String contentType = "application/octet-stream";

        if (ext.endsWith(".json5")) {
            contentType = "application/json5";
        } else if (ext.endsWith(".json")) {
            contentType = "application/json";
        } else if (ext.endsWith(".xml")) {
            contentType = "application/xml";
        }

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file.getCanonicalPath()));

        return ResponseEntity.ok()
            .contentLength(file.length())
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

            // TODO Allow these paths to be set in properties file
            collectionId = "collections/" + urlSlug + ".collection.json";
            collection.setCollectionId(collectionId);
            collection.getDescription().setFull(new Id("../pages/html/collections/" + urlSlug + "/summary.html"));
            collection.getCredit().setProse(new Id("../pages/html/collections/" + urlSlug + "/sponsors.html"));
        }

        // Update links in HTML
        String summaryId = collection.getDescription().getFull().getId();
        String creditId = collection.getCredit().getProse().getId();

        String collectionPath = new File(editAPI.getDataLocalPath() + File.separator + collectionId).getParent();
        String collectionHTMLPath = new File(collectionPath, summaryId).getCanonicalPath();
        String creditHTMLPath = new File(collectionPath, creditId).getCanonicalPath();

        String fullDescriptionHTML = prepareHTMLForSaving(collectionForm.getFullDescriptionHTML(), collectionHTMLPath);
        String proseCreditHTML = prepareHTMLForSaving(collectionForm.getProseCreditHTML(), creditHTMLPath);

        // Save collection file
        boolean success = editAPI.updateCollection(collection, fullDescriptionHTML,
            proseCreditHTML);

        if (success) {
            attributes.addFlashAttribute("message", "Collection Updated.");
        } else {
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

        if (!editAPI.addItemToCollection(itemName, fileExtension, file.getInputStream(), collectionId)) {
            attributes.addFlashAttribute("error", "Problem adding item to collection");
            return new RedirectView("/edit/collection/");
        }

        attributes.addFlashAttribute("message", "Item updated/added to collection.");

        return new RedirectView("/edit/collection/");
    }

    @PostMapping("/edit/collection/deleteItem")
    public RedirectView deleteCollectionItem(RedirectAttributes attributes, @RequestParam String collectionId,
                                             @RequestParam String itemName) {

        attributes.addAttribute("collectionId", collectionId);

        boolean success = editAPI.deleteItemFromCollection(itemName, collectionId);
        if (success) {
            attributes.addFlashAttribute("message", "Item deleted from collection.");
        } else {
            attributes.addFlashAttribute("error", "Problem deleting item");
        }

        return new RedirectView("/edit/collection/");
    }

}

