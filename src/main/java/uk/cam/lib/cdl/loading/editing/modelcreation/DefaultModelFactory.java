package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.CharSource;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiFunction;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PRIVATE)
public abstract class DefaultModelFactory<T, R> implements ModelFactory<T> {
    public static <T, R> DefaultModelFactoryBuilder<T, R> builder() {
        return new DefaultModelFactoryBuilder<>();
    }

    protected abstract IdCreationStrategy idCreator();
    protected abstract FileContentCreationStrategy<R> fileContentCreator();
    protected abstract ResultAssembler<R, T> resultAssembler();

    public static <A, B> ResultAssembler<A, B> assembleResultFromFileContent(
        BiFunction<Path, ? super FileContent<? extends A>, ? extends B> idContentHandler
    ) {
        return (idResult, contentResult) -> idResult.biMapValue(contentResult, idContentHandler);
    }

    public static <Any, Result> ResultAssembler<Any, Result> assembleResultFromFileContentString(
        String modelName, BiFunction<Path, String, ? extends Result> constructor) {
        return assembleResultFromFileContent((id, fileContent) ->
            constructor.apply(id, fileContent.text().map(ThrowingFunction.dangerouslyMakeUnchecked(CharSource::read))
                .orElseThrow(() -> new IllegalStateException(String.format(
                    "Cannot assemble %s: created FileContent instance does not contain accessible text: %s",
                    modelName, fileContent)))));
    }

    @Override
    public CreationResult<T> createFromAttributes(Set<? extends ModelAttribute<?>> modelAttributes) throws IOException {
        return resultAssembler().assembleResult(
            idCreator().createId(modelAttributes),
            fileContentCreator().createFileContent(modelAttributes));
    }

    public interface FileContentCreationStrategy<T> {
        CreationResult<FileContent<T>> createFileContent(Set<? extends ModelAttribute<?>> modelAttributes) throws IOException;
        default CreationResult<FileContent<T>> createFileContent(ModelAttribute<?> attribute, ModelAttribute<?>... attributes) throws IOException {
            return createFileContent(ImmutableSet.<ModelAttribute<?>>builder().add(attribute).add(attributes).build());
        }
    }

    public interface IdCreationStrategy {
        CreationResult<Path> createId(Set<? extends ModelAttribute<?>> modelAttributes);
        default CreationResult<Path> createId(ModelAttribute<?>... modelAttributes) {
            return createId(ImmutableSet.copyOf(modelAttributes));
        }
    }

    public interface ResultAssembler<I, R> {
        CreationResult<R> assembleResult(
            CreationResult<Path> path,
            CreationResult<? extends FileContent<? extends I>> fileContent) throws IOException;
    }
}
