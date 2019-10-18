package uk.cam.lib.cdl.loading;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.cam.lib.cdl.loading.exceptions.BadRequestException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@Controller
public class EditController {

    @Value("${git.sourcedata.checkout.path}")
    String gitSourceDataPath;

    @RequestMapping(method = RequestMethod.GET, value = "/edit/edit.html")
    public String edit(Model model) {

        model.addAttribute("gitSourceDataPath", gitSourceDataPath);

        return "edit";
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
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file.getAbsolutePath()));

        return ResponseEntity.ok()
            .contentLength(file.length())
            .contentType(MediaType.parseMediaType(contentType))
            .body(resource);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/edit/download")
    public ResponseEntity<Resource> editDownload(Model model, @RequestParam String filepath)
        throws BadRequestException, FileNotFoundException {

        File file = new File(filepath);

        System.out.println("file found: "+filepath);

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
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file.getAbsolutePath()));

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
