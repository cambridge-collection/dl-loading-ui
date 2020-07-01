package uk.cam.lib.cdl.loading.editing.modelcreation;

import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.Item;

import java.util.function.Predicate;

public class ModelCreation {
    private ModelCreation() {}

    public static <T> DefaultFileContentCreationStrategy.FileContentProcessor<T, T> conditionalProcessor(
            DefaultFileContentCreationStrategy.FileContentProcessor<T, T> conditional, Predicate<? super FileContent<? extends T>> predicate) {
        return fc -> predicate.test(fc) ? conditional.processFileContent(fc) :
            ImmutableCreationResult.successful(fc.withAlternateRepresentation(fc.representation()));
    }

    public static <Any> DefaultModelFactory.ResultAssembler<Any, Item> itemAssemblerFromFileContent() {
        return DefaultModelFactory.assembleResultFromFileContentString("Item", ImmutableItem::of);
    }
}
