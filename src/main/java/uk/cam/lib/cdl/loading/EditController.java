package uk.cam.lib.cdl.loading;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;
import uk.cam.lib.cdl.loading.model.editor.Collection;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
public class EditController {

    @Value("${git.sourcedata.checkout.path}")
    String gitSourcePath;

    @Value("${git.sourcedata.checkout.subpath.data}")
    String gitSourceDataSubpath;

    @Value("${git.sourcedata.url}")
    String gitSourceURL;

    @Value("${git.sourcedata.url.username}")
    String gitSourceURLUserame;

    @Value("${git.sourcedata.url.password}")
    String gitSourceURLPassword;

    @Autowired
    private EditAPI editAPI;

    private Git git;
    private String gitSourceDataPath;

    @PostConstruct
    private void setupRepo() {
        try {
            File dir = new File(gitSourcePath);
            if (dir.exists()) {

                git = Git.init().setDirectory(dir).call();

            } else {

                git = Git.cloneRepository()
                    .setURI(gitSourceURL)
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(gitSourceURLUserame,
                        gitSourceURLPassword))
                    .setDirectory(new File(gitSourcePath))
                    .call();

            }

        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }

    @PostConstruct
    private void setupPath() {
        gitSourceDataPath = gitSourcePath + gitSourceDataSubpath;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/edit.html")
    public String edit(Model model) {

        List<Collection> collections = editAPI.getCollections();
        model.addAttribute("collections", collections);

        return "edit";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/collection/{collectionUrlSlug}")
    public String editCollection(Model model, @PathVariable("collectionUrlSlug") String collectionUrlSlug) {

        Collection collection = editAPI.getCollection(collectionUrlSlug);
        model.addAttribute("collection", collection);
        model.addAttribute("gitSourceDataPath", gitSourceDataPath);

        return "edit-collection";
    }

    @RequestMapping(method = RequestMethod.GET, value = "/edit/advanced-edit.html")
    public String editAdvanced(Model model) {

        model.addAttribute("gitSourceDataPath", gitSourceDataPath);
        return "advanced-edit";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/filetree/list")
    @ResponseBody
    public String editFileTreeList(Model model, @RequestParam String dir) throws BadRequestException {

        File parent = new File(dir);

        // Allow access to git dir checkout only.
        if (!parent.exists() || !parent.toPath().toAbsolutePath().startsWith(gitSourceDataPath)) {
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
        if (!file.exists() || !file.toPath().toAbsolutePath().startsWith(gitSourceDataPath)) {
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
        if (!file.exists() || !file.toPath().toAbsolutePath().startsWith(gitSourceDataPath)) {
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

}
