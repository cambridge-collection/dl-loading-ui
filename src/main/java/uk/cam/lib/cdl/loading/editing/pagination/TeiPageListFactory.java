package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableList;
import org.apache.commons.csv.CSVFormat;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.modelcreation.AttributeNotFoundException;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy.FileContentProcessor;
import uk.cam.lib.cdl.loading.editing.modelcreation.FileContent;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableCreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelFactory;
import uk.cam.lib.cdl.loading.editing.pagination.CSVPageLoader.CSVRowAccessor;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public abstract class TeiPageListFactory implements ModelFactory<List<TEIPage>> {
    public static TeiPageListFactoryBuilder builder() {
        return new TeiPageListFactoryBuilder();
    }

    @Value.Default
    protected PageLoader<Reader> pageLoader() {
        return ImmutableCSVPageLoader.builder()
            .imageAccessor(CSVRowAccessor.of("image"))
            .labelAccessor(CSVRowAccessor.of("label"))
            .csvFormat(CSVFormat.DEFAULT.withFirstRecordAsHeader())
            .build();
    }

    @Value.Default
    protected TEIPageConverter teiPageConverter() {
        return new DefaultTEIPageConverter(ImmutableList.of("#downloadImageRights", "#download"));
    }

    protected FileContentProcessor<Optional<Void>, List<Page>> pageLoaderProcessor() {
        return fc -> {
            var textSource = fc.text().orElseThrow(() ->
                new AttributeNotFoundException("Failed to load pagination pages: user input as text is not available"));

            List<Page> pages;
            try {
                pages = pageLoader().loadPages(textSource.openBufferedStream());
            }
            catch (UserInputPaginationException e) {
                return ImmutableCreationResult.unsuccessful(
                    ImmutableIssue.of(PaginationIssue.INVALID_PAGE_DEFINITIONS, e.getMessage()));
            }
            return ImmutableCreationResult.successful(fc.withAlternateRepresentation(pages));
        };
    }

    protected FileContentProcessor<List<Page>, List<TEIPage>> teiPageConverterProcessor() {
        return fc -> ImmutableCreationResult.successful(fc.withAlternateRepresentation(
            teiPageConverter().convert(fc.representation())));
    }

    protected FileContentProcessor<Optional<Void>, List<TEIPage>> processor() {
        return pageLoaderProcessor().pipedThrough(teiPageConverterProcessor());
    }

    @Value.Auxiliary
    @Value.Derived
    protected DefaultFileContentCreationStrategy<List<TEIPage>> fileLoader() {
        return DefaultFileContentCreationStrategy.<List<TEIPage>>builder()
            .processor(processor())
            .build();
    }

    @Override
    public CreationResult<List<TEIPage>> createFromAttributes(Set<? extends ModelAttribute<?>> modelAttributes)
        throws IOException {
        return fileLoader().createFileContent(modelAttributes).mapValue(FileContent::representation);
    }
}
