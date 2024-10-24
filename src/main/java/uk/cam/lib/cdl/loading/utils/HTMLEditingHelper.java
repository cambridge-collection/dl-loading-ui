package uk.cam.lib.cdl.loading.utils;

import com.google.common.base.Preconditions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import uk.cam.lib.cdl.loading.viewerui.frontend.BuildFactory;
import uk.cam.lib.cdl.loading.viewerui.frontend.PageType;
import uk.cam.lib.cdl.loading.viewerui.frontend.frontend.FrontEndBuild;

import java.nio.file.Path;


public class HTMLEditingHelper {

    private final Path localDataPath;
    private final Path pathForDataDisplay;



    public HTMLEditingHelper(Path localDataPath, Path pathForDataDisplay) {

        this.localDataPath = localDataPath;
        this.pathForDataDisplay = pathForDataDisplay;
    }

    // Need to parse relative links to add in 'pathForDataDisplay' for local viewing.
    public String prepareHTMLForDisplay(String html, Path htmlFilePath, BuildFactory buildFactory, PageType pageType) {
        Document doc = Jsoup.parse(html);
        var fileRelativePath =
            localDataPath.toAbsolutePath().relativize(htmlFilePath.getParent().toAbsolutePath());

        // Translate images
        for (Element img : doc.select("img[src]")) {
            String src = img.attr("src");

            var imageRelativePath = fileRelativePath.resolve(src);
            img.attr("src", pathForDataDisplay.resolve(imageRelativePath).normalize().toString());
        }

        // Translate css
        for (Element link : doc.select("link[src]")) {
            String src = link.attr("src");

            var linkRelativePath = fileRelativePath.resolve(src);
            link.attr("src", pathForDataDisplay.resolve(linkRelativePath).normalize().toString());
        }

        // Add extra css / js  from the viewer ui war, this would be in the cudl page when displayed.
        for(FrontEndBuild.Resource resource: buildFactory.getBuild(pageType).resources()) {
            doc.head().append(resource.render());
        }

        return doc.outerHtml();
    }

    // Need to parse links from display to format to be saved.
    // replace 'pathForDataDisplay' with file path to data
    // Generate relative path from thisHTMLPath
    public String prepareHTMLForSaving(String html, Path thisHTMLPath) {
        Preconditions.checkArgument(thisHTMLPath.isAbsolute(), "thisHTMLPath is not absolute: %s", thisHTMLPath);
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            var src = Path.of(img.attr("src"));
            if (src.startsWith(pathForDataDisplay)) {
                var imgPath = pathForDataDisplay.relativize(src);
                var imageFile = localDataPath.resolve(imgPath).normalize();
                Path relativePath = thisHTMLPath.getParent().relativize(imageFile);
                img.attr("src", relativePath.toString());
            }
        }

        // Remove extra css / js  from the viewer ui war, this would be in the cudl page when displayed.
        // Remove CSS
        for (Element element : doc.head().getElementsByAttributeValueStarting("href", "/ui/")) {
            element.remove();
        }
        // Remove JS
        for (Element element : doc.head().getElementsByAttributeValueStarting("src", "/ui/")) {
            element.remove();
        }

        return doc.outerHtml();
    }

}
