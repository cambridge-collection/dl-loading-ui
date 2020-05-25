package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.itemcreation.DefaultItemFactory.FileContent;

import java.util.Optional;
import java.util.Set;

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
public abstract class DefaultFileContentCreationStrategy<T> implements
    DefaultItemFactory.FileContentCreationStrategy<T> {

    protected abstract FileContentInitialiser initialiser();
    protected abstract FileContentProcessor<Optional<Void>, T> processor();

    @Override
    public CreationResult<FileContent<T>> createFileContent(Set<ItemAttribute<?>> itemAttributes) {
        return null;
    }

    interface FileContentInitialiser {
        FileContent<Optional<Void>> initialiseFileContent(Set<ItemAttribute<?>> attributes);
    }

    interface FileContentProcessor<T, U> {
        FileContent<? extends U> processFileContent(FileContent<? extends T> content);
        default <V> FileContentProcessor<T, V> pipedThrough(FileContentProcessor<? super U, ? extends V> after) {
            return input -> after.processFileContent(processFileContent(input));
        }
    }

//    interface Builder<T> {
//
//        default <U> Builder<U>
//
//        default T fooBar() {
//            return null;
//        }
//    }
}
