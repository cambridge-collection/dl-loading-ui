package uk.cam.lib.cdl.loading.editing.modelcreation;

import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;

import java.io.IOException;
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
    public CreationResult<FileContent<T>> createFileContent(Set<? extends ModelAttribute<?>> modelAttributes) throws IOException {
        return initialiser()
            .initialiseFileContent(modelAttributes)
            .flatMapValue(ThrowingFunction.dangerouslyMakeUnchecked(processor()::processFileContent));
    }

    public interface FileContentInitialiser {
        CreationResult<FileContent<Optional<Void>>> initialiseFileContent(Set<? extends ModelAttribute<?>> attributes);
    }

    public interface FileContentProcessor<T, U> {
        CreationResult<FileContent<U>> processFileContent(FileContent<? extends T> content) throws IOException;

        default <V> FileContentProcessor<T, V> pipedThrough(FileContentProcessor<? super U, ? extends V> after) {
            return input -> processFileContent(input)
                    .flatMapValue(ThrowingFunction.dangerouslyMakeUnchecked(after::processFileContent))
                    // Safely remove ? extends V wildcard
                    .mapValue(fcExtendsV -> fcExtendsV.withAlternateRepresentation((V)fcExtendsV.representation()));
        }
    }
}
