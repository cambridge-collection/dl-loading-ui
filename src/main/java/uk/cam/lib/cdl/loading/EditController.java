package uk.cam.lib.cdl.loading;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.model.editor.Collection;
import uk.cam.lib.cdl.loading.model.editor.Id;
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Controller
public class EditController {

    private final EditAPI editAPI;

    @Autowired
    public EditController(EditAPI editAPI) {
        this.editAPI = editAPI;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/edit.html")
    public String edit(Model model) {

        List<Collection> collections = editAPI.getCollections();
        model.addAttribute("collections", collections);

        return "edit";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/collection/{collectionUrlSlug}")
    public String editCollection(Model model, @PathVariable("collectionUrlSlug") String collectionUrlSlug,
                                 @RequestParam(required = false) String message,
                                 @RequestParam(required = false) String error) {

        Collection collection = editAPI.getCollection(collectionUrlSlug);
        // Get Item names from Ids
        List<Item> items = new ArrayList<>();
        for (Id id : collection.getItemIds()) {
            items.add(editAPI.getItem(id.getId()));
        }

        model.addAttribute("thumbnailURL", editAPI.getDataLocalPath() + collection.getThumbnailURL());
        model.addAttribute("collection", collection);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
        model.addAttribute("items", items);
        return "edit-collection";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/advanced-edit.html")
    public String editAdvanced(Model model) {

        model.addAttribute("gitSourceDataPath", editAPI.getDataLocalPath());
        return "advanced-edit";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/filetree/list")
    @ResponseBody
    public String editFileTreeList(Model model, @RequestParam String dir) throws BadRequestException {

        File parent = new File(dir);

        // Allow access to git dir checkout only.
        if (!parent.exists() || !parent.toPath().toAbsolutePath().startsWith(editAPI.getDataLocalPath())) {
            throw new BadRequestException(new Exception("Dir needs to be subdir of git file source."));
        }

        // Get list from file system
        StringBuilder output = new StringBuilder("<ul class=\"jqueryFileTree\" style=\"display: none;\">");
        if (parent.isDirectory()) {
            for (final File f : Objects.requireNonNull(parent.listFiles())) {
                if (f.isDirectory()) {
                    output.append("<li class=\"directory collapsed\">");
                    output.append("<a href=\"#\" rel=\"")
                            .append(f.toPath().toAbsolutePath() + "/").append("\">")
                            .append(f.toPath().getFileName()).append("</a></li>");
                } else {
                    output.append("<li class=\"file ext_")
                            .append(FilenameUtils.getExtension(f.toPath().toString())).append("\">");
                    output.append("<a href=\"#\" rel=\"")
                            .append(f.toPath().toAbsolutePath()).append("\">")
                            .append(f.toPath().getFileName()).append("</a></li>");
                }
            }
        } else {
            throw new BadRequestException(new Exception("Dir is not a directory"));
        }
        output.append("</ul>");

        return output.toString();

    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/filetree/get")
    public ResponseEntity<Resource> editFileTreeGet(Model model, @RequestParam String filepath,
                                                    HttpServletResponse response) throws BadRequestException,
            IOException {
        File file = new File(filepath);

        // Allow access to git dir checkout only.
        if (!file.exists() || !file.toPath().toAbsolutePath().startsWith(editAPI.getDataLocalPath())) {
            throw new BadRequestException(new Exception("Dir needs to be subdir of git file source."));
        }

        // Interpret data as a stream for display.
        String contentType = "application/octet-stream";

        //return response;
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file.getCanonicalPath()));

        return ResponseEntity.ok()
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
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


    @RequestMapping(method = RequestMethod.POST, value = "/edit/rename")
    public String editRename(Model model, @RequestParam String filepath)
            throws BadRequestException, FileNotFoundException {
        return "";
    }


    @RequestMapping(method = RequestMethod.POST, value = "/edit/delete")
    public String editDelete(Model model, @RequestParam String filepath)
            throws BadRequestException, FileNotFoundException {
        return "";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/upload")
    public String editUpload(Model model, @RequestParam String filepath)
            throws BadRequestException, FileNotFoundException {
        return "";
    }


    /**
     * TODO validate the changes against the JSON schema.
     *
     * @param attributes
     * @param collectionId
     * @param collection
     * @return
     * @throws BadRequestException
     */
    @RequestMapping(method = RequestMethod.POST, value = "/edit/collection/{collectionId}/update")
    public RedirectView updateCollection(RedirectAttributes attributes,
                                         @PathVariable String collectionId,
                                         @ModelAttribute Collection collection) throws BadRequestException {

        if (collection.getName() == null || collection.getName().getUrlSlug() == null) {
            throw new BadRequestException(new Exception());
        }

        Collection existingCollection = editAPI.getCollection(collectionId);
        if (existingCollection == null) {
            throw new BadRequestException(new Exception("Unknown collection with id: " + collectionId));
        }

        // Check values we do not want to allow to edit
        // match the existing collection values
        if (collection.getName().getUrlSlug().equals(collectionId) &&
                collection.getType().equals(existingCollection.getType()) &&
                collection.getFilepath().equals(existingCollection.getFilepath()) &&
                collection.getItemIds().equals(existingCollection.getItemIds())) {

            boolean success = editAPI.updateCollection(collection);
            if (success) {
                attributes.addAttribute("message", "Collection Updated.");
            } else {
                attributes.addAttribute("error", "Failed to update collection.");
            }
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

    @PostMapping("/edit/collection/{collectionId}/addItem")
    public RedirectView addCollectionItem(RedirectAttributes attributes, @PathVariable String collectionId,
                                          @RequestParam("file") MultipartFile file) throws IOException {

        if (file.getContentType() == null || !(file.getContentType().equals("text/xml") ||
                file.getContentType().equals("application/json"))) {
            attributes.addAttribute("error", "Item needs to be in TEI XML format.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        String itemName = FilenameUtils.getBaseName(file.getOriginalFilename());
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!editAPI.validateFilename(itemName)) {
            attributes.addAttribute("error", "Item name not valid.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        if (!editAPI.validate(file)) {
            attributes.addAttribute("error", "Item is not valid XML/JSON or does not validate against the required schema.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        if (!editAPI.addItemToCollection(itemName, fileExtension, file.getInputStream(), collectionId)) {
            attributes.addAttribute("error", "Problem adding item to collection");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        attributes.addAttribute("message", "Item added to collection.");

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

    @PostMapping("/edit/collection/{collectionId}/deleteItem")
    public RedirectView deleteCollectionItem(RedirectAttributes attributes, @PathVariable String collectionId,
                                             @RequestParam String itemName) {

        boolean success = editAPI.deleteItemFromCollection(itemName, collectionId);
        if (success) {
            attributes.addAttribute("message", "Item deleted from collection.");
        } else {
            attributes.addAttribute("error", "Problem deleting item");
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");

    }

}

