package uk.cam.lib.cdl.loading;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.exceptions.NotFoundException;
import uk.cam.lib.cdl.loading.forms.CollectionForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
public class EditController {

    private final EditAPI editAPI;

    @Autowired
    public EditController(EditAPI editAPI) {
        this.editAPI = editAPI;
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
     * @param model
     * @param collectionId
     * @return
     * @throws NotFoundException
     */
    @GetMapping(value = {"/edit/collection/"})
    public String editCollection(Model model, @RequestParam(required = false) String collectionId)
        throws NotFoundException, IOException {

        // TODO check permissions
        CollectionForm form;
        boolean newCollection = true;
        List<Item> items = new ArrayList<>();

        if (collectionId == null) {
            form = new CollectionForm();
        } else {
            Collection collection = editAPI.getCollection(collectionId);
            if (model.asMap().get("form") == null) {

                File collectionFile = new File(editAPI.getCollectionPath(collectionId));
                File fullDescription = new File(collectionFile.getParentFile(),
                    collection.getDescription().getFull().getId());
                String collectionRelativePath =
                    fullDescription.getParent().replaceAll(editAPI.getDataLocalPath()
                        , "") + "/";
                String descriptionHTML = FileUtils.readFileToString(fullDescription, "UTF-8");

                // Need to parse relative links.
                // TODO Make more robust.
                descriptionHTML = descriptionHTML.replaceAll("src\\s*=\\s*'\\s*(?!http)",
                    "src='/edit/source/" + collectionRelativePath);
                descriptionHTML = descriptionHTML.replaceAll("src\\s*=\\s*\"\\s*(?!http)",
                    "src=\"/edit/source/" + collectionRelativePath);


                File credit = new File(collectionFile.getParentFile(),
                    collection.getCredit().getProse().getId());
                String creditHTML = FileUtils.readFileToString(credit, "UTF-8");

                form = new CollectionForm(collection, descriptionHTML, creditHTML);
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
            thumbnailURL = "/pages/images/collectionsView/collection" +
                "-blank.jpg"; // TODO read from UI properties.
        }

        // Get HTML
        model.addAttribute("newCollection", newCollection);
        model.addAttribute("thumbnailURL", thumbnailURL);
        model.addAttribute("form", form);
        model.addAttribute("items", items);
        model.addAttribute("dataLocalPath", editAPI.getDataLocalPath());

        return "edit-collection";
    }

    /**
     * Returns the requested file contents specified by filepath if it exists in the checkedout source repo.
     *
     * @param request
     * @return
     * @throws BadRequestException
     * @throws IOException
     */
    @GetMapping(value = "/edit/source/**")
    public ResponseEntity<Resource> editDownload(HttpServletRequest request)
        throws BadRequestException, IOException {

        String filepath = request.getRequestURI().split(request.getContextPath() + "/edit/source/")[1];

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

        //return response;
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file.getCanonicalPath()));

        return ResponseEntity.ok()
            .contentLength(file.length())
            .header("Content-Disposition", "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    /**
     * TODO validate the changes against the JSON schema.
     *
     * @param attributes
     * @param collectionForm
     * @return
     * @throws BadRequestException
     */
    @PostMapping("/edit/collection/update")
    public RedirectView updateCollection(RedirectAttributes attributes,
                                         @Valid @ModelAttribute CollectionForm collectionForm,
                                         final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", collectionForm);
            attributes.addAttribute("collectionId", collectionForm.getUrlSlugName());

            return new RedirectView("/edit/collection/");
        }

        Collection collection = collectionForm.toCollection();

        if (collection.getName() == null || collection.getName().getUrlSlug() == null) {
            attributes.addFlashAttribute("error", "Failed to update collection, missing collection name.");
            attributes.addAttribute("collectionId", collectionForm.getUrlSlugName());
            return new RedirectView("/edit/collection/");
        }

/*        Collection existingCollection = editAPI.getCollection(collection.getName().getUrlSlug());
        if (existingCollection == null) {

            attributes.addFlashAttribute("error", "Unknown collection with id: " + collection.getName().getUrlSlug());
            attributes.addAttribute("collectionId", collectionForm.getUrlSlugName());
            return new RedirectView("/edit/collection/");
        }*/

        // TODO make sure we have permission to edit the collection being edited.
        boolean success = editAPI.updateCollection(collection);

        if (success) {
            attributes.addFlashAttribute("message", "Collection Updated.");
        } else {
            attributes.addFlashAttribute("error", "Failed to update collection.");
        }

        attributes.addAttribute("collectionId", collectionForm.getUrlSlugName());
        return new RedirectView("/edit/collection/");
    }

    @PostMapping("/edit/collection/{collectionId}/addItem")
    public RedirectView addCollectionItem(RedirectAttributes attributes, @PathVariable String collectionId,
                                          @RequestParam("file") MultipartFile file) throws IOException {

        if (file.getContentType() == null || !(file.getContentType().equals("text/xml"))) {
            attributes.addFlashAttribute("error", "Item needs to be in TEI XML format.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        String itemName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!editAPI.validateFilename(itemName)) {
            attributes.addFlashAttribute("error", "Item name not valid. Should be for example: MS-TEST-00001. Using " +
                " characters A-Z or numbers 0-9 and the - character delimiting sections.  Should have at least 3 " +
                " sections, group (MS = Manuscripts, PR = printed etc) then the collection, then a five digit number.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        if (!editAPI.validate(file)) {
            attributes.addFlashAttribute("error", "Item is not valid XML/JSON or does not validate against the required schema.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        if (!editAPI.addItemToCollection(itemName, fileExtension, file.getInputStream(), collectionId)) {
            attributes.addFlashAttribute("error", "Problem adding item to collection");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        attributes.addFlashAttribute("message", "Item updated/added to collection.");

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

    @PostMapping("/edit/collection/{collectionId}/deleteItem")
    public RedirectView deleteCollectionItem(RedirectAttributes attributes, @PathVariable String collectionId,
                                             @RequestParam String itemName) {

        boolean success = editAPI.deleteItemFromCollection(itemName, collectionId);
        if (success) {
            attributes.addFlashAttribute("message", "Item deleted from collection.");
        } else {
            attributes.addFlashAttribute("error", "Problem deleting item");
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");

    }

}

