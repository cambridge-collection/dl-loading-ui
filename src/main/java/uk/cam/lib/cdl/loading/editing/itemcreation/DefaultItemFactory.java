package uk.cam.lib.cdl.loading.editing.itemcreation;

import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.model.editor.Item;

import java.nio.file.Path;
import java.util.Set;

@Value.Immutable
@Value.Style(typeImmutable = "*")
public abstract class DefaultItemFactory implements ItemFactory {
    protected abstract IdCreationStrategy idCreator();
    protected abstract FileContentCreationStrategy fileContentCreator();
    protected abstract ItemAssembler itemAssembler();

    @Override
    public CreationResult<Item> createItem(Set<ItemAttribute<?>> itemAttributes) {
        return itemAssembler().assembleItem(
            idCreator().createId(itemAttributes),
            fileContentCreator().createFileContent(itemAttributes));
    }

    public interface FileContentCreationStrategy {
        CreationResult<String> createFileContent(Set<ItemAttribute<?>> itemAttributes);
    }

    public interface IdCreationStrategy {
        CreationResult<Path> createId(Set<ItemAttribute<?>> itemAttributes);
    }

    public interface ItemAssembler {
        CreationResult<Item> assembleItem(CreationResult<Path> path, CreationResult<String> fileContent);
    }
}
