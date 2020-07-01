package uk.cam.lib.cdl.loading.editing.pagination;

import org.immutables.value.Value;
import org.w3c.dom.Document;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy.FileContentProcessor;
import uk.cam.lib.cdl.loading.editing.modelcreation.FileContent;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableCreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation.XmlFileContentProcessor;
import uk.cam.lib.cdl.loading.utils.XML;

import java.io.IOException;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public abstract class TeiPaginationGenerationProcessor implements FileContentProcessor<Document, Document> {

    public static TeiPaginationGenerationProcessorBuilder builder() {
        return new TeiPaginationGenerationProcessorBuilder();
    }

    protected abstract TeiPageListFactory teiPageListFactory();

    @Value.Derived
    protected TEIPageInserter teiPageInserter() {
        return new TEIPageInserter();
    }

    @Override
    public CreationResult<FileContent<Document>> processFileContent(
        FileContent<? extends Document> content) throws IOException {

        var doc = XML.deepCopyDocument(content.representation());
        var paginationAttributes = ModelAttributes.requireNestedAttributes(
            Attribute.PAGINATION_ATTRIBUTES, content.attributes());
        var teiPageResult = teiPageListFactory().createFromAttributes(paginationAttributes);

        return teiPageResult.flatMapValue(teiPages -> {
            try {
                teiPageInserter().insertPages(doc, teiPages);
                return ImmutableCreationResult.successful(XmlFileContentProcessor.fileContentForXml(content, doc));
            } catch (UserInputPaginationException e) {
                return ImmutableCreationResult.unsuccessful(
                    ImmutableIssue.of(PaginationIssue.INAVLID_TEI, e.getMessage()));
            }
        });
    }

    public enum Attribute implements ModelAttribute.Type {
        PAGINATION_ATTRIBUTES
    }
}
