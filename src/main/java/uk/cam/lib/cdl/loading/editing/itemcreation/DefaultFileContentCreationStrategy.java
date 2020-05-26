package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
public abstract class DefaultFileContentCreationStrategy<T> implements
    DefaultModelFactory.FileContentCreationStrategy<T> {

    @Value.Default
    protected FileContentInitialiser initialiser() {
        return DefaultFileContentInitialiser.getInstance();
    }
    protected abstract FileContentProcessor<Optional<Void>, T> processor();

    @Override
    public CreationResult<? extends FileContent<? extends T>> createFileContent(Set<ModelAttribute<?>> modelAttributes) {
        return initialiser().initialiseFileContent(modelAttributes).flatMap(processor()::processFileContent);
    }

    interface FileContentInitialiser {
        CreationResult<FileContent<Optional<Void>>> initialiseFileContent(Set<? extends ModelAttribute<?>> attributes);
    }

    interface FileContentProcessor<T, U> {
        CreationResult<? extends FileContent<? extends U>> processFileContent(FileContent<? extends T> content);

        default <V> FileContentProcessor<T, V> pipedThrough(FileContentProcessor<? super U, ? extends V> after) {
            return input -> processFileContent(input).flatMap(after::processFileContent);
        }
    }
}
