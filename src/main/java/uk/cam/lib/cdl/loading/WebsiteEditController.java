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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.cam.lib.cdl.loading.apis.EditAPI;
import uk.cam.lib.cdl.loading.forms.UIPageForm;
import uk.cam.lib.cdl.loading.model.editor.ui.UIPage;
import uk.cam.lib.cdl.loading.model.editor.ui.UIThemeData;
import uk.cam.lib.cdl.loading.utils.HTMLEditingHelper;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping ("/websiteedit")
public class WebsiteEditController {
    private static final Logger LOG = LoggerFactory.getLogger(WebsiteEditController.class);

    private final EditAPI editAPI;
    private final Path pathForDataDisplay;
    private ApplicationContext appContext;
    private final UIThemeData uiThemeData;
    private HTMLEditingHelper htmlEditingHelper;

    @Autowired
    public WebsiteEditController(
        EditAPI editAPI,
        @Value("${data.url.display}") String pathForDataDisplay,
        @Value("${data.path.html}") String pathForHTML,
        UIThemeData uiThemeData,
        ApplicationContext appContext
    ) {
        this.editAPI = editAPI;
        this.pathForDataDisplay = Path.of("/edit", pathForDataDisplay);
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
        String websitePath = Paths.get(websitePage.getHtml().getId()).normalize().toString();

        // Read HTML
        Path htmlDataFile = editAPI.getFullPathForId(websitePath);
        String websiteHTML = FileUtils.readFileToString(htmlDataFile.toFile(), "UTF-8");
        websiteHTML = htmlEditingHelper.prepareHTMLForDisplay(websiteHTML, htmlDataFile);

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

}
