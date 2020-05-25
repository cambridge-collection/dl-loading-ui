package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.itemcreation.ItemAttributes.StandardItemAttributes;
import uk.cam.lib.cdl.loading.model.editor.Item;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@Value.Immutable
public abstract class DefaultItemFactory<I extends Item, R> implements ItemFactory {
    protected abstract IdCreationStrategy idCreator();
    protected abstract FileContentCreationStrategy<R> fileContentCreator();
    protected abstract ResultAssembler<I, R> resultAssembler();

    @Override
    public CreationResult<I> createItem(Set<ItemAttribute<?>> itemAttributes) {
        return resultAssembler().assembleResult(
            idCreator().createId(itemAttributes),
            fileContentCreator().createFileContent(itemAttributes));
    }

    public interface FileContentCreationStrategy<T> {
        CreationResult<FileContent<T>> createFileContent(Set<ItemAttribute<?>> itemAttributes);
        default CreationResult<FileContent<T>> createFileContent(ItemAttribute<?>... itemAttributes) {
            return createFileContent(ImmutableSet.copyOf(itemAttributes));
        }
    }

    public interface IdCreationStrategy {
        CreationResult<Path> createId(Set<ItemAttribute<?>> itemAttributes);
        default CreationResult<Path> createId(ItemAttribute<?>... itemAttributes) {
            return createId(ImmutableSet.copyOf(itemAttributes));
        }
    }

    public interface ResultAssembler<I , R> {
        CreationResult<I> assembleResult(CreationResult<Path> path, CreationResult<FileContent<R>> fileContent);
    }

    public interface FileContent<T> {
        Set<ItemAttribute<?>> attributes();
        Optional<String> mimeType();
        Optional<ByteSource> bytes();
        Optional<CharSource> text();
        T representation();
    }

    @Value.Immutable
    abstract static class AbstractInitialFileContent implements FileContent<Optional<Void>> {
        @Override
        @Value.Parameter(order = 0)
        public abstract Set<ItemAttribute<?>> attributes();

        @Value.Derived
        public Optional<String> mimeType() {
            return ItemAttributes.findAttribute(StandardItemAttributes.MIME_TYPE, String.class, attributes())
                .map(ItemAttribute::value);
        }

        @Value.Check
        protected void checkState() {
            Preconditions.checkState(!(providedBytes().isEmpty() && providedText().isEmpty()),
                FILE_CONTENT_NOT_FOUND_MSG);
            Preconditions.checkState(!(providedBytes().isPresent() && providedText().isPresent()),
                "Ambiguous file content: StandardItemAttributes.BYTES and StandardItemAttributes.TEXT are both " +
                    "provided");
        }

        private static final String FILE_CONTENT_NOT_FOUND_MSG =
            "No file content was found amongst attributes. Expected a StandardItemAttributes.BYTES attribute " +
            "containing a com.google.common.io.ByteSource, or a StandardItemAttributes.TEXT attribute " +
            "containing a com.google.common.io.CharSource. Additionally StandardItemAttributes.CHARSET can be " +
            "provided as a String or java.nio.charset.Charset, which enables bytes to be derived from text, or " +
            "vice versa.";

        @Value.Derived
        public Optional<ByteSource> bytes() {
            return providedBytes()
                .or(this::bytesFromProvidedTextAndCharset);
        }

        @Value.Derived
        protected Optional<Charset> providedCharset() {
            return ItemAttributes.findAttribute(StandardItemAttributes.CHARSET, String.class, attributes())
                .map(ItemAttribute::value)
                .map(Charset::forName)
                .or(() -> ItemAttributes.findAttribute(StandardItemAttributes.CHARSET, Charset.class, attributes())
                    .map(ItemAttribute::value));
        }

        @Value.Derived
        protected Optional<ByteSource> providedBytes() {
            return ItemAttributes.findAttribute(StandardItemAttributes.BYTES, ByteSource.class, attributes())
                .map(ItemAttribute::value);
        }

        protected Optional<ByteSource> bytesFromProvidedTextAndCharset() {
            return providedText()
                .flatMap(charSource -> providedCharset().map(charSource::asByteSource));
        }

        protected Optional<CharSource> textFromProvidedBytesAndCharset() {
            return providedBytes()
                .flatMap(byteSource -> providedCharset().map(byteSource::asCharSource));
        }

        @Value.Derived
        protected Optional<CharSource> providedText() {
            return ItemAttributes.findAttribute(StandardItemAttributes.TEXT, CharSource.class, attributes())
                .map(ItemAttribute::value)
                .or(() -> ItemAttributes.findAttribute(StandardItemAttributes.TEXT, String.class, attributes())
                    .map(ItemAttribute::value)
                    .map(CharSource::wrap));
        }

        @Override
        public Optional<CharSource> text() {
            return providedText()
                .or(this::textFromProvidedBytesAndCharset);
        }

        @Override
        public Optional<Void> representation() {
            return Optional.empty();
        }
    }
}
