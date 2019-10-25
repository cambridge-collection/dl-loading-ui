package uk.cam.lib.cdl.loading;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.io.FileUtils;
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
import uk.cam.lib.cdl.loading.model.editor.Item;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
        model.addAttribute("thumbnailURL", editAPI.getDataLocalPath() + collection.getThumbnailURL());
        model.addAttribute("collection", collection);
        model.addAttribute("error", error);
        model.addAttribute("message", message);

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

        // Set fixed values we don't want to allow editing for.
        collection.getName().setUrlSlug(collectionId);
        collection.setType(existingCollection.getType());
        collection.setFilepath(existingCollection.getFilepath());
        collection.setItems(existingCollection.getItems());
        collection.setItemIds(existingCollection.getItemIds());

        try {

            // Write out file
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(editAPI.getDataLocalPath() + collection.getFilepath()), collection);

            // Git Commit and push to remote repo.
            boolean success = editAPI.pushGitChanges();
            if (success) {
                attributes.addAttribute("message", "Collection Updated.");
            } else {
                throw new IOException("Git push failed");
            }

            editAPI.updateModel();

        } catch (IOException e) {
            attributes.addAttribute("error", "There was a problem updating the collection.");
            e.printStackTrace();
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

    @PostMapping("/edit/collection/{collectionId}/addItem")
    public RedirectView addCollectionItem(RedirectAttributes attributes, @PathVariable String collectionId,
                                          @RequestParam("file") MultipartFile file) throws BadRequestException, IOException {

        System.out.println("in addCollectionItem ");
        if (file.getContentType() == null || !file.getContentType().equals("application/xml")) {
            attributes.addAttribute("error", "Item needs to be in TEI XML format.");
            return new RedirectView("/edit/collection/" + collectionId + "/");
        }

        // Check to see if the file already exists
        Item item = editAPI.getItem(file.getName());
        if (item != null) {
            // Overwrite existing Item
            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(item.getFilepath()));

        } else {
            // new Item
            //TODO
        }

        boolean success = editAPI.pushGitChanges();
        if (success) {
            attributes.addAttribute("message", "Item added to collection.");
        } else {
            throw new IOException("Git push failed, on adding new item to collection.");
        }

        return new RedirectView("/edit/collection/" + collectionId + "/");
    }

}
