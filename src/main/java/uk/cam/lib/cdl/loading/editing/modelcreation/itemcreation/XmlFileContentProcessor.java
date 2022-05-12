package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy.FileContentProcessor;
import uk.cam.lib.cdl.loading.editing.modelcreation.FileContent;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableCreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.utils.XML;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class XmlFileContentProcessor implements FileContentProcessor<Optional<Void>, Document> {
    @Override
    public CreationResult<FileContent<Document>> processFileContent(
        FileContent<? extends Optional<Void>> fc
    ) throws IOException {
        try {
            var byteSource = fc.bytes().orElseThrow(() -> new IllegalStateException(
                "No bytes() are available from FileContent and unable to parse XML from text()"));

            var doc = XML.parse(byteSource);
            return ImmutableCreationResult.successful(fileContentForXml(fc, doc));
        } catch (SAXException e) {
            return ImmutableCreationResult.unsuccessful(
                ImmutableIssue.of(ItemIssue.INVALID_INPUT_FILE, "XML is not valid.")
            );
        } catch (IllegalStateException e) {
            throw new RuntimeException(
                "Failed to invoke XML parser: " + e.getMessage(), e);
        }
    }

    public static FileContent<Document> fileContentForXml(FileContent<?> fc, Document doc) {
        var docAsString = XML.serialise(doc);
        return fc.withAlternateRepresentation(doc, docAsString, Optional.of(StandardCharsets.UTF_8));
    }
}
