package uk.cam.lib.cdl.loading;

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
import uk.cam.lib.cdl.loading.forms.CollectionForm;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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

    @GetMapping("/edit/collection/{collectionUrlSlug}")
    public String editCollection(Model model, @PathVariable("collectionUrlSlug") String collectionUrlSlug) {

        Collection collection = editAPI.getCollection(collectionUrlSlug);

        CollectionForm form;
        if (model.asMap().get("form") == null) {
            form = new CollectionForm(collection);
        } else {
            form = (CollectionForm) model.asMap().get("form");
        }

        // Get Item names from Ids
        List<Item> items = new ArrayList<>();
        for (Id id : collection.getItemIds()) {
            items.add(editAPI.getItem(id.getId()));
        }


        model.addAttribute("thumbnailURL", collection.getThumbnailURL());
        model.addAttribute("form", form);
        model.addAttribute("items", items);
        return "edit-collection";
    }

    @RequestMapping(value = "/edit/download")
    public ResponseEntity<Resource> editDownload(Model model, @RequestParam String filepath)
        throws BadRequestException, IOException {

        File file = new File(filepath);

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
     * @param collectionId
     * @param collectionForm
     * @return
     * @throws BadRequestException
     */
    @PostMapping("/edit/collection/{collectionId}/update")
    public RedirectView updateCollection(RedirectAttributes attributes,
                                         @PathVariable String collectionId,
                                         @Valid @ModelAttribute CollectionForm collectionForm,
                                         final BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", collectionForm);
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        Collection collection = collectionForm.toCollection();

        if (collection.getName() == null || collection.getName().getUrlSlug() == null) {
            attributes.addFlashAttribute("error", "Failed to update collection, missing collection name.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        Collection existingCollection = editAPI.getCollection(collectionId);
        if (existingCollection == null) {
            attributes.addFlashAttribute("error", "Unknown collection with id: " + collectionId);
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        // Check values we do not want to allow to edit
        // match the existing collection values

        boolean success = false;
        if (collection.getName().getUrlSlug().equals(collectionId) &&
            collection.getType().equals(existingCollection.getType()) &&
            collection.getFilepath().equals(existingCollection.getFilepath()) &&
            collection.getThumbnailURL().equals(existingCollection.getThumbnailURL()) &&
            listEqualsIgnoreOrder(collection.getItemIds(), existingCollection.getItemIds())) {

            success = editAPI.updateCollection(collection);

        }

        if (success) {
            attributes.addFlashAttribute("message", "Collection Updated.");
        } else {
            attributes.addFlashAttribute("error", "Failed to update collection.");
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

    private <T> boolean listEqualsIgnoreOrder(List<T> list1, List<T> list2) {
        return new HashSet<>(list1).equals(new HashSet<>(list2));
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

    /*public RedirectView replaceCollectionThumbnail(RedirectAttributes attributes, @PathVariable String collectionId,
                                                   @RequestParam("file") MultipartFile file) throws IOException {

        if (file.getContentType() == null ||
            !(file.getContentType().equals("image/png")) || !(file.getContentType().equals("image/gif")) ||
            !(file.getContentType().equals("image/jpg")) || !(file.getContentType().equals("image/jpeg"))) {

            attributes.addFlashAttribute("error", "Item needs to be in .png, .jpg or .gif format.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }


        return new RedirectView("/edit/collection/" + collectionId + "/");
    }*/

}

