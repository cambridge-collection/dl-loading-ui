package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import com.google.common.collect.ImmutableList;
import org.immutables.value.Value;
import org.w3c.dom.Document;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultFileContentCreationStrategy.FileContentProcessor;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultModelFactory;
import uk.cam.lib.cdl.loading.editing.modelcreation.DefaultModelFactory.IdCreationStrategy;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelCreation;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelFactory;
import uk.cam.lib.cdl.loading.editing.pagination.DefaultTEIPageConverter;
import uk.cam.lib.cdl.loading.editing.pagination.ImmutableCSVPageLoader;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPageListFactory;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessor;
import uk.cam.lib.cdl.loading.model.editor.Item;

import java.io.IOException;
import java.util.Set;

import static uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessor.Attribute.PAGINATION_ATTRIBUTES;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public abstract class TeiItemFactory implements ModelFactory<Item> {

    public static TeiItemFactoryBuilder builder() {
        return new TeiItemFactoryBuilder();
    }

    @Value.Default
    protected IdCreationStrategy teiIdCreationStrategy() {
        return TeiIdCreationStrategy.of();
    }

    @Value.Default
    protected TeiPageListFactory teiPageListFactory() {
        return TeiPageListFactory.builder()
            .pageLoader(ImmutableCSVPageLoader.builder().build())
            .teiPageConverter(new DefaultTEIPageConverter(ImmutableList.of(
                "#downloadImageRights", "#download")))
            .build();
    }

    @Value.Derived
    protected TeiPaginationGenerationProcessor teiPaginationGenerationProcessor() {
        return TeiPaginationGenerationProcessor.builder()
            .teiPageListFactory(teiPageListFactory())
            .build();
    }

    protected FileContentProcessor<Document, Document> conditionalTeiPaginationProcessor() {
        // Only invoke the TEI pagination processor when pagination attributes are available
        return ModelCreation.conditionalProcessor(teiPaginationGenerationProcessor(), fc ->
                ModelAttributes.findNestedAttributes(PAGINATION_ATTRIBUTES, fc.attributes()).isPresent());
    }

    @Value.Derived
    protected DefaultModelFactory.FileContentCreationStrategy<Document> teiFileContentCreator() {
        return DefaultFileContentCreationStrategy.<Document>builder()
            .processor(
                new XmlFileContentProcessor()
                .pipedThrough(conditionalTeiPaginationProcessor()))
            .build();
    }

    @Value.Derived
    protected DefaultModelFactory<Item, Document> itemFactory() {
        return DefaultModelFactory.<Item, Document>builder()
            .idCreator(teiIdCreationStrategy())
            .fileContentCreator(teiFileContentCreator())
            .resultAssembler(ModelCreation.itemAssemblerFromFileContent())
            .build();
    }

    @Override
    public CreationResult<Item> createFromAttributes(
        Set<? extends ModelAttribute<?>> modelAttributes) throws IOException {
        return itemFactory().createFromAttributes(modelAttributes);
    }
}
