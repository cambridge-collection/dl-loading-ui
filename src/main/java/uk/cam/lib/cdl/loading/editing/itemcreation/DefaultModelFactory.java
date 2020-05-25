package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.itemcreation.ModelAttributes.StandardItemAttributes;
import uk.cam.lib.cdl.loading.model.editor.ImmutableItem;
import uk.cam.lib.cdl.loading.model.editor.Item;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

@Value.Immutable
public abstract class DefaultModelFactory<T, R> implements ModelFactory<T> {
    protected abstract IdCreationStrategy idCreator();
    protected abstract FileContentCreationStrategy<R> fileContentCreator();
    protected abstract ResultAssembler<R, T> resultAssembler();

    public static <A, B> ResultAssembler<A, B> assembleResultFromFileContent(BiFunction<Path, ? super FileContent<? extends A>, ? extends B> idContentHandler) {
        return (idResult, contentResult) -> idResult.biMap(contentResult, idContentHandler);
    }

    public static <Result> ResultAssembler<FileContent<?>, Result> assembleResultFromFileContentString(String modelName, BiFunction<Path, String, ? extends Result> constructor) {
        return assembleResultFromFileContent((id, fileContent) ->
            constructor.apply(id, fileContent.text().map(ThrowingFunction.dangerouslyMakeUnchecked(CharSource::read))
                .orElseThrow(() -> new IllegalStateException(String.format(
                    "Cannot assemble %s: created FileContent instance does not contain accessible text: %s",
                    modelName, fileContent)))));
    }

    public static final ResultAssembler<FileContent<?>, Item> ITEM_ASSEMBLER =
        assembleResultFromFileContentString("Item", ImmutableItem::of);

    @Override
    public CreationResult<T> createFromAttributes(Set<ModelAttribute<?>> modelAttributes) throws IOException {
        return resultAssembler().assembleResult(
            idCreator().createId(modelAttributes),
            fileContentCreator().createFileContent(modelAttributes));
    }

    public interface FileContentCreationStrategy<T> {
        CreationResult<? extends FileContent<? extends T>> createFileContent(Set<ModelAttribute<?>> modelAttributes);
        default CreationResult<? extends FileContent<? extends T>> createFileContent(ModelAttribute<?> attribute, ModelAttribute<?>... attributes) {
            return createFileContent(ImmutableSet.<ModelAttribute<?>>builder().add(attribute).add(attributes).build());
        }
    }

    public interface IdCreationStrategy {
        CreationResult<Path> createId(Set<ModelAttribute<?>> modelAttributes);
        default CreationResult<Path> createId(ModelAttribute<?>... modelAttributes) {
            return createId(ImmutableSet.copyOf(modelAttributes));
        }
    }

    public interface ResultAssembler<I , R> {
        CreationResult<R> assembleResult(
            CreationResult<Path> path,
            CreationResult<? extends FileContent<? extends I>> fileContent) throws IOException;
    }

    public interface FileContent<T> {
        Set<ModelAttribute<?>> attributes();
        Optional<ByteSource> bytes();
        Optional<CharSource> text();
        T representation();
    }

    @Value.Immutable
    abstract static class AbstractInitialFileContent implements FileContent<Optional<Void>> {
        @Override
        @Value.Parameter(order = 0)
        public abstract Set<ModelAttribute<?>> attributes();

        @Value.Derived
        public Optional<String> mimeType() {
            return ModelAttributes.findAttribute(StandardItemAttributes.MIME_TYPE, String.class, attributes())
                .map(ModelAttribute::value);
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
            return ModelAttributes.findAttribute(StandardItemAttributes.CHARSET, String.class, attributes())
                .map(ModelAttribute::value)
                .map(Charset::forName)
                .or(() -> ModelAttributes.findAttribute(StandardItemAttributes.CHARSET, Charset.class, attributes())
                    .map(ModelAttribute::value));
        }

        @Value.Derived
        protected Optional<ByteSource> providedBytes() {
            return ModelAttributes.findAttribute(StandardItemAttributes.BYTES, ByteSource.class, attributes())
                .map(ModelAttribute::value);
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
            return ModelAttributes.findAttribute(StandardItemAttributes.TEXT, CharSource.class, attributes())
                .map(ModelAttribute::value)
                .or(() -> ModelAttributes.findAttribute(StandardItemAttributes.TEXT, String.class, attributes())
                    .map(ModelAttribute::value)
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
