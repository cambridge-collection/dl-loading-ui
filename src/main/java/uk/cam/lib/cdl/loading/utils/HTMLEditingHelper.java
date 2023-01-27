package uk.cam.lib.cdl.loading.utils;

import com.google.common.base.Preconditions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Path;

public class HTMLEditingHelper {

    private Path localDataPath;
    private Path pathForDataDisplay;

    public HTMLEditingHelper(Path localDataPath, Path pathForDataDisplay) {

        this.localDataPath = localDataPath;
        this.pathForDataDisplay = pathForDataDisplay;
    }

    // Need to parse relative links to add in 'pathForDataDisplay' for local viewing.
    public String prepareHTMLForDisplay(String html, Path HTMLFilePath) {
        Document doc = Jsoup.parse(html);
        var fileRelativePath =
            localDataPath.toAbsolutePath().relativize(HTMLFilePath.getParent().toAbsolutePath());

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
        return doc.outerHtml();
    }

    // Need to parse links from display to format to be saved.
    // replace 'pathForDataDisplay' with file path to data
    // Generate relative path from collections
    public String prepareHTMLForSaving(String html, Path HTMLFilePath) throws IOException {
        Preconditions.checkArgument(HTMLFilePath.isAbsolute(), "HTMLFilePath is not absolute: %s", HTMLFilePath);
        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img[src]")) {
            var src = Path.of(img.attr("src"));
            if (src.startsWith(pathForDataDisplay)) {
                var imgPath = pathForDataDisplay.relativize(src);
                var imageFile = localDataPath.resolve(imgPath).normalize();
                Path relativePath = HTMLFilePath.getParent().relativize(imageFile);
                img.attr("src", relativePath.toString());
            }
        }
        return doc.outerHtml();
    }

}
