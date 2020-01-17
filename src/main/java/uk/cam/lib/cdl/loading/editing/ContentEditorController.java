package uk.cam.lib.cdl.loading.editing;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.cam.lib.cdl.loading.apis.EditAPI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Controller for editing content through CKEditor on path /editor
 *
 * @author jennie
 */
@Controller
@RequestMapping("/editor")
public class ContentEditorController {

    protected final String contentHTMLPath;
    protected final String contentImagesPath;
    protected final String contentImagesURL;
    private final String pathForDataDisplay;

    @Autowired
    public ContentEditorController(EditAPI editAPI, @Value("${data.url.display}") String pathForDataDisplay,
                                   @Value("${data.path.images}") String imagePath,
                                   @Value("${data.path.html}") String htmlPath) {

        this.pathForDataDisplay = pathForDataDisplay;
        this.contentImagesURL = pathForDataDisplay + imagePath;
        this.contentImagesPath = editAPI.getDataLocalPath() + imagePath;
        this.contentHTMLPath = editAPI.getDataLocalPath() + htmlPath;
    }

    /**
     * Request on URL /editor/update/html
     * <p>
     * Used by CKEditor to update the HTML on specified sections of the website.
     * Also commits to git.
     *
     * @param response HttpServletResponse
     * @param writeParams
     * @param errors Errors thrown when binding
     * @return
     * @throws IOException
     * @throws JSONException
     */
 /*   //@Secured("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/update/html", method = RequestMethod.POST)
    public synchronized ModelAndView handleUpdateRequest(
        HttpServletResponse response,
        @Valid @ModelAttribute() UpdateHTMLParameters writeParams,
        BindingResult errors) throws IOException, JSONException {

        if (errors.hasErrors()) {
            throw new IOException(
                "HTML Update failed due to invalid parameters. Please check "
                    + "your filename contains only valid characters.");
        }

        boolean success = saveHTML(writeParams);
        if (success) {
*//*            AdminUser adminUser = Users.currentAdminUser(usersDao);

            if (!git.commit(adminUser.getAdminName(),
                    adminUser.getAdminEmail(),
                    "cudl-viewer: Saving HTML changes")
                    || !git.push(gitUsername, gitPassword, localBranch)) {
                success = false;
            }*//*
        }

        JSONObject json = new JSONObject();
        json.put("writesuccess", success);

        response.setContentType("application/json");
        write(json.toString(), response.getOutputStream());
        return null;

    }*/

    /**
     * Request on URL /editor/add/image
     * <p>
     * Used by CKEditor to upload an image file to the server.
     *
     * @param addParams  Information about the file to add
     * @param bindResult Result of data binding
     * @return HTML with JS to set the CKEditor value
     * @throws IOException on a binding error
     */
    //@Secured("hasRole('ROLE_ADMIN')")
    @PostMapping("/add/image")
    public ResponseEntity<String> handleAddImageRequest(@Valid @ModelAttribute() AddImagesParameters addParams,
                                                        BindingResult bindResult) throws IOException {

        // Check file extension and content type are image.
        uploadFileValidation(addParams, bindResult);

        if (bindResult.hasErrors()) {
            throw new IOException(
                "Your image upload failed. Please ensure you have selected a file "
                    + "and that your directory path contains only valid characters.");
        }

        String filename = addParams.getUpload().getOriginalFilename();
        InputStream is = addParams.getUpload().getInputStream();

        // Save the file to disk.
        boolean saveSuccessful = FileSave.save(contentImagesPath
            + File.separator + addParams.getDirectory(), filename, is);

        if (saveSuccessful) {
/*            AdminUser adminUser = Users.currentAdminUser(usersDao);

            if (!git.commit(adminUser.getAdminName(),
                    adminUser.getAdminEmail(), "cudl-viewer: Adding new image")
                    || !git.push(gitUsername, gitPassword, localBranch)) {
                saveSuccessful = false;
            }*/
        }

        String output = "<html><head><script> window.opener.CKEDITOR.tools.callFunction( "
            + addParams.getCKEditorFuncNum()
            + ", '"
            + contentImagesURL
            + "/"
            + addParams.getUpload().getOriginalFilename()
            + "', 'save successful: "
            + saveSuccessful
            + "' );window.close();</script>";

        return ResponseEntity.ok()
            .contentLength(output.length())
            .contentType(MediaType.TEXT_HTML)
            .body(output);

    }

    /**
     * Request on /editor/browse/images
     * <p>
     * Used by CKEditor to browse the images available on the server.
     * Also used by the Collection editor to change the collection thumbnail.
     *
     * @param CKEditor        Reference to the CKEditor that called this
     * @param CKEditorFuncNum Reference to the CKEditor Function Number Parameter
     * @param langCode        language code from CKEditor
     * @return browse image view
     */
    //@Secured("hasRole('ROLE_ADMIN')")
    @GetMapping("/browse/images")
    public String handleBrowseImagesRequest(
        Model model,
        @RequestParam(defaultValue = "None") String CKEditor,
        @RequestParam(defaultValue = "-1") String CKEditorFuncNum,
        @RequestParam(defaultValue = "en") String langCode,
        @RequestParam(required = false) String browseDir) {

        // Get a list of images on the server.
        File imagesDir = new File(contentImagesPath);
        if (browseDir == null) {
            browseDir = imagesDir.getPath();
        }

        // File[] files = imagesDir.listFiles();
        BrowseFile imageFiles = buildFileHierarchy(imagesDir);

        imageFiles = buildBrowseDirectory(browseDir, Objects.requireNonNull(imageFiles));

        model.addAttribute("ckEditor", CKEditor);
        model.addAttribute("ckEditorFuncNum", CKEditorFuncNum);
        model.addAttribute("langCode", langCode);
        model.addAttribute("imageFiles", imageFiles);
        model.addAttribute("browseDir", browseDir);
        model.addAttribute("homeDir", imagesDir.getPath());
        model.addAttribute("currentDir",
            browseDir.replaceFirst(contentImagesPath, ""));
        model.addAttribute("pathForDataDisplay", pathForDataDisplay);

        return "edit-image-browse";
    }

    /**
     * on Path /editor/delete/image
     * <p>
     * Deletes the image at the specified path. Must start with
     * contentImagesPath.
     *
     * @param deleteParams Parameters for image to delete
     * @param bindResult   result of object binding
     * @return JSON response
     * @throws IOException on binding error
     */
    //@Secured("hasRole('ROLE_ADMIN')")
    @PostMapping("/delete/image")
    public ResponseEntity<String> handleDeleteImageRequest(
        @Valid @ModelAttribute() DeleteImagesParameters deleteParams,
        BindingResult bindResult) throws IOException {

        if (bindResult.hasErrors()) {
            throw new IOException(
                "Your image or directory delete failed. Please ensure the filePath exists "
                    + "and has only got allowed characters in.");
        }

        // delete the file.
        String filePath = deleteParams.getFilePath();
        File file = (new File(contentImagesPath + File.separator + filePath))
            .getCanonicalFile();
        boolean successful = false;

        if (file.exists() && !file.isDirectory()) {
            successful = file.delete(); // delete file
        } else if (file.exists() && Objects.requireNonNull(file.list()).length == 0) {
            successful = file.delete(); // delete empty directory.
        }

        if (successful) {
/*            AdminUser adminUser = Users.currentAdminUser(usersDao);

            if (!git.delete(file.getPath(), adminUser.getAdminName(),
                    adminUser.getAdminEmail(), "cudl-viewer: Deleting image")
                    || !git.push(gitUsername, gitPassword, localBranch)) {
                successful = false;
            }*/
        }

        JSONObject json = new JSONObject();
        json.put("deletesuccess", successful);

        return ResponseEntity.status(successful ? 200 : 400)
            .contentLength(json.toString().length())
            .contentType(MediaType.APPLICATION_JSON)
            .body(json.toString());

    }

    /**
     * Builds a list of image files on the server to be displayed by the browse
     * page.
     * <p>
     * Files should be under the directory specified in contentPath.
     *
     * @return BrowseFile object
     */
    private BrowseFile buildFileHierarchy(File file) {

        if (!file.getPath().startsWith(contentImagesPath)) {
            return null;
        }

        String fileURL = contentImagesURL
            + file.getPath().replaceFirst(contentImagesPath, "");

        if (!file.isDirectory()) {
            return new BrowseFile(file.getName(), file.getPath(), fileURL,
                file.isDirectory(), null);
        }

        ArrayList<BrowseFile> children = new ArrayList<BrowseFile>();
        for (int i = 0; i < Objects.requireNonNull(file.listFiles()).length; i++) {

            File child = Objects.requireNonNull(file.listFiles())[i];
            children.add(buildFileHierarchy(child));
        }
        Collections.sort(children);
        return new BrowseFile(file.getName(), file.getPath(), fileURL,
            file.isDirectory(), children);
    }

    /**
     * Picks out the directory currently being viewed and it's children.
     *
     * @return BrowseFile object
     */
    private BrowseFile buildBrowseDirectory(String browseDir,
                                            BrowseFile imageFiles) {

        if (imageFiles.isDirectory()
            && imageFiles.getFilePath().equals(browseDir)) {
            return imageFiles;
        }

        List<BrowseFile> children = imageFiles.getChildren();
        if (children == null)
            return null;

        for (BrowseFile child : children) {
            BrowseFile f = buildBrowseDirectory(browseDir, child);
            if (f != null) {
                return f;
            }
        }

        return null;
    }
    /*
     */

    /**
     * Note: Does not validate params. This should occur before calling this
     * method.
     *
     * @return
     * @throws IOException
     *//*
    private synchronized boolean saveHTML(UpdateHTMLParameters writeParams)
        throws IOException {

        // Read in data from request
        String html = writeParams.getHtml();
        String filename = writeParams.getFilename();

        // Save the file to disk.
        return FileSave.save(contentHTMLPath, filename,
            new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8)));

    }*/

    // Performs validation on parameters used for writing images.
    public static class AddImagesParameters {

        @NotNull
        private String CKEditor;

        @NotNull
        private String CKEditorFuncNum;

        @NotNull
        private String langCode;

        // File validation is separate.
        private MultipartFile file;

        @NotNull
        @Pattern(regexp = "^[-_/A-Za-z0-9]*$", message = "Invalid directory")
        private String directory;

        public String getCKEditor() {
            return CKEditor;
        }

        public void setCKEditor(String CKEditor) {
            this.CKEditor = CKEditor;
        }

        public String getCKEditorFuncNum() {
            return CKEditorFuncNum;
        }

        public void setCKEditorFuncNum(String CKEditorFuncNum) {
            this.CKEditorFuncNum = CKEditorFuncNum;
        }

        public String getLangCode() {
            return langCode;
        }

        public void setLangCode(String langCode) {
            this.langCode = langCode;
        }

        public MultipartFile getUpload() {
            return file;
        }

        public void setUpload(MultipartFile file) {
            this.file = file;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }

    // Performs validation on parameters used for deleting images.
    public static class DeleteImagesParameters {

        @NotNull
        @Pattern(regexp = "^[-_/A-Za-z0-9]+(\\.(?i)(jpg|jpeg|png|gif|bmp))??$", message = "Invalid characters in filePath")
        private String filePath;

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFilePath() {
            return filePath;
        }

    }

    private void uploadFileValidation(AddImagesParameters uploadParams,
                                      BindingResult bindResult) throws IOException {
        if (uploadParams.getUpload().getSize() == 0) {
            bindResult.rejectValue("upload", "Upload file required.");
        }

        // check file extension / content type for image.
        java.util.regex.Pattern pattern = java.util.regex.Pattern
            .compile("^[-_A-Za-z0-9]+\\.(?i)(jpg|jpeg|png|gif|bmp)$");
        boolean isImageFile = pattern.matcher(
            Objects.requireNonNull(uploadParams.getUpload().getOriginalFilename())).matches();
        boolean isImageContent = Objects.requireNonNull(uploadParams.getUpload().getContentType())
            .startsWith("image");

        if (!isImageFile && isImageContent) {
            bindResult.rejectValue("upload",
                "Upload filename should be jpg, png, gif or bmp.");
        }
    }

    // Performs validation on parameters used for writing html.
    /*public static class UpdateHTMLParameters {

        private static final Log logger = LogFactory.getLog(UpdateHTMLParameters.class);

        @NotNull
        @Pattern(regexp = "^[-_/A-Za-z0-9]+\\.html$", message = "Invalid filename")
        private String filename;

        @NotNull
        private String html;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            logger.debug("setHtml() before tidy: " + html);
            this.html = tidyHTML(html);
            logger.debug("setHtml() after tidy: " + this.html);
        }

        private String tidyHTML(String input) {

            try {
                Tidy tidy = new Tidy();
                tidy.setXHTML(false);
                tidy.setInputEncoding("UTF-8");
                tidy.setOutputEncoding("UTF-8");
                tidy.setMakeClean(false);
                tidy.setTidyMark(false);
                tidy.setIndentContent(true);
                tidy.setSmartIndent(true);
                tidy.setDropEmptyParas(false);
                tidy.setDocType("omit");
                tidy.setQuiet(true);
                tidy.setPrintBodyOnly(true);
                tidy.setShowWarnings(false);

                Writer writer = new StringBuilderWriter(input.length());
                tidy.parse(new StringReader(input), writer);

                String output = writer.toString();
                if (output != null && !output.trim().equals("")) {
                    return output;
                }

            } catch (Exception e) {
                logger.error("Tidying HTML failed", e);
            }

            // default to return input in event of any
            // error.
            return input;

        }

    }*/

/*    private boolean write(String text, OutputStream outputStream) {
        try (PrintStream out = new PrintStream(new BufferedOutputStream(outputStream), true,
            "UTF-8")) {
            out.print(text);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }*/

}
