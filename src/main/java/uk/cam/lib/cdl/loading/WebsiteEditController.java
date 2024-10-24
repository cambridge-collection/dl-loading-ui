package uk.cam.lib.cdl.loading;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.exceptions.EditApiException;
import uk.cam.lib.cdl.loading.forms.UIPageForm;
import uk.cam.lib.cdl.loading.model.editor.ui.UIPage;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeData;
import uk.cam.lib.cdl.loading.utils.HTMLEditingHelper;
import uk.cam.lib.cdl.loading.viewerui.frontend.BuildFactory;
import uk.cam.lib.cdl.loading.viewerui.frontend.PageType;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping ("/websiteedit")
public class WebsiteEditController {
    private static final Logger LOG = LoggerFactory.getLogger(WebsiteEditController.class);

    private final EditAPI editAPI;
    private final Path pathForDataDisplay;
    private final Path pathForHTML;
    private final Path imagePath;
    private ApplicationContext appContext;
    private final UIThemeData uiThemeData;
    private HTMLEditingHelper htmlEditingHelper;

    @Autowired private BuildFactory buildFactory;

    @Autowired
    public WebsiteEditController(
        EditAPI editAPI,
        @Value("${data.url.display}") String pathForDataDisplay,
        @Value("${data.path.images}") String imagePath,
        @Value("${data.path.html}") String pathForHTML,
        UIThemeData uiThemeData,
        ApplicationContext appContext
    ) {
        this.editAPI = editAPI;
        this.pathForDataDisplay = Path.of("/edit", pathForDataDisplay);
        this.imagePath = Path.of(imagePath);
        this.pathForHTML = Path.of(pathForHTML);
        Preconditions.checkArgument(
            this.pathForDataDisplay.isAbsolute() &&
                this.pathForDataDisplay.normalize().equals(this.pathForDataDisplay),
            "pathForDataDisplay must start with / and not contain relative segments");
        this.appContext = appContext;
        this.uiThemeData = uiThemeData;
        this.htmlEditingHelper = new HTMLEditingHelper(editAPI.getDataLocalPath(), this.pathForDataDisplay);
    }

    @PreAuthorize("@roleService.canEditWebsite(authentication)")
    @GetMapping("/edit.html")
    public String edit(Model model, HttpServletRequest request) {

        model.addAttribute("pathForDataDisplay", pathForDataDisplay);
        model.addAttribute("pages", uiThemeData.getPages());

        return "websiteedit";
    }

    @PreAuthorize("@roleService.canEditWebsite(authentication)")
    @GetMapping("/editpage.html")
    public String editPage(Model model, HttpServletRequest request,
                           @RequestParam String websiteName) throws IOException {

        // NOTE: Website Names need to be unique. Should use id?
        UIPage websitePage = null;
        for (UIPage page: uiThemeData.getPages()) {
            if (page.getName().equals(websiteName)) {
                websitePage = page;
            }
        }

        if (websitePage==null) {
            throw new IOException("Invalid name for website to edit");
        }

        // validate website html path
        String websitePath = Paths.get(websitePage.getHtmlPath().getId()).normalize().toString();

        // Read HTML
        Path htmlDataFile = editAPI.getFullPathForId(websitePath);
        String websiteHTML = FileUtils.readFileToString(htmlDataFile.toFile(), "UTF-8");
        websiteHTML = htmlEditingHelper.prepareHTMLForDisplay(websiteHTML, htmlDataFile, buildFactory, PageType.STANDARD);

        UIPageForm form;
        if (model.asMap().get("form") != null) {
            form = (UIPageForm) model.asMap().get("form");
        } else {
            form = new UIPageForm(websiteName, websitePath, websiteHTML);
        }

        model.addAttribute("pathForDataDisplay", pathForDataDisplay);
        model.addAttribute("websitePage", websitePage);
        model.addAttribute("websiteHTML", websiteHTML);
        model.addAttribute("form", form);

        return "websiteedit-page";
    }

    /**
     * TODO validate the changes against the JSON schema.
     * <p>
     * Saves changes to the html presented in the UIPageForm and redirects to
     * show the updated edit html page.
     *
     * @param attributes     Model attributes to be used in the redirect
     * @param uiPageForm Validated uiPageForm from edit HTML page
     * @return RedirectView to the html edit page (after updates have been saved).
     */
    @PostMapping("/update")
    @PreAuthorize("@roleService.canEditWebsite(authentication)")
    public RedirectView updateHTML(RedirectAttributes attributes,
                                         @RequestBody @Valid @org.springframework.web.bind.annotation.ModelAttribute UIPageForm uiPageForm,
                                         final BindingResult bindingResult)
        throws Exception {

        if (bindingResult!=null && bindingResult.hasErrors()) {

            System.err.println("Errors: "+bindingResult.getErrorCount());
            for (ObjectError error : bindingResult.getAllErrors()) {
                System.err.println("binding errors: "+error.toString());
            }

            attributes.addFlashAttribute("error", "There was a problem saving your changes. See form below for " +
                "details.");
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.form", bindingResult);
            attributes.addFlashAttribute("form", uiPageForm);
            attributes.addAttribute("websiteName", uiPageForm.getWebsiteName());

            return new RedirectView("/websiteedit/editpage.html?websiteName="+uiPageForm.getWebsiteName());
        }

        UIPage uiPage = uiPageForm.toUIPage();
        Path htmlFilePath = Path.of(uiPage.getHtmlPath().getId()).normalize();
        Preconditions.checkState(htmlFilePath.startsWith(this.pathForHTML.toString()));
        String preparedHTML = htmlEditingHelper.prepareHTMLForSaving(uiPageForm.getHtml(),
            Paths.get(editAPI.getDataLocalPath().toString(),htmlFilePath.toString()));

        try {
            editAPI.updatePage(uiPage,preparedHTML);
            attributes.addFlashAttribute("message", "Page Updated. Changes should be viewable in a few minutes.");
        }
        catch (EditApiException e) {
            LOG.error("Failed to update page", e);
            attributes.addFlashAttribute("error", "Failed to update page.");
        }


        return new RedirectView("/websiteedit/editpage.html?websiteName="+uiPageForm.getWebsiteName());
    }
}
