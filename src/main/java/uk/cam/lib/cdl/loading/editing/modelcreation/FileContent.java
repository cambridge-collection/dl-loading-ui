package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.apache.commons.lang3.NotImplementedException;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.utils.ThrowingFunction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public interface FileContent<T> {
    Set<ModelAttribute<?>> attributes();
    Optional<ByteSource> bytes();
    Optional<CharSource> text();
    Optional<Charset> charset();
    T representation();

    <U> FileContent<U> withAlternateRepresentation(U representation, CharSource fileData, Optional<Charset> charset);
    default <U> FileContent<U> withAlternateRepresentation(U representation, String fileData, Optional<Charset> charset) {
        return withAlternateRepresentation(representation, CharSource.wrap(fileData), charset);
    }
    default <U> FileContent<U> withAlternateRepresentation(U representation, String fileData) {
        return withAlternateRepresentation(representation, CharSource.wrap(fileData));
    }
    default <U> FileContent<U> withAlternateRepresentation(U representation, CharSource fileData) {
        return withAlternateRepresentation(representation, fileData, charset());
    }

    <U> FileContent<U> withAlternateRepresentation(U representation, ByteSource fileData, Optional<Charset> charset);
    default <U> FileContent<U> withAlternateRepresentation(U representation, byte[] fileData, Optional<Charset> charset) {
        return withAlternateRepresentation(
            representation, ByteSource.wrap(Arrays.copyOf(fileData, fileData.length)), charset);
    }
    default <U> FileContent<U> withAlternateRepresentation(U representation, byte[] fileData) {
        return withAlternateRepresentation(representation, ByteSource.wrap(Arrays.copyOf(fileData, fileData.length)));
    }
    default <U> FileContent<U> withAlternateRepresentation(U representation, ByteSource fileData) {
        return withAlternateRepresentation(representation, fileData, charset());
    }

    <U> FileContent<U> withAlternateRepresentation(U representation);

    <U> FileContent<U> flatMap(Function<? super FileContent<? extends T>, ? extends FileContent<? extends U>> mapper);
    <U> FileContent<U> flatMapRepresentation(Function<? super T, ? extends FileContent<? extends U>> mapper);
    <U> FileContent<U> map(Function<? super FileContent<? extends T>, ? extends U> mapper);
    <U> FileContent<U> mapRepresentation(Function<? super T, ? extends U> mapper);

    default boolean contentEquals(FileContent<?> other) throws IOException {
        boolean propertiesEqual = this == other || (
            attributes().equals(other.attributes()) &&
            charset().equals(other.charset()) &&
            representation().equals(other.representation()));

        if(!propertiesEqual) { return false; }

        boolean bytesContentEqual = bytes()
            .flatMap(thisBytes -> other.bytes()
                .map(ThrowingFunction.dangerouslyMakeUnchecked(thisBytes::contentEquals)))
            .orElse(bytes().isPresent() == other.bytes().isPresent());

        if(!bytesContentEqual) { return false; }

        return text()
            .flatMap(thisText -> other.text().map(ThrowingFunction.dangerouslyMakeUnchecked(CharSource::read))
                .map(otherTextString ->
                    ThrowingFunction.dangerouslyMakeUnchecked(CharSource::read).apply(thisText)
                        .equals(otherTextString)))
            .orElse(text().isPresent() == other.text().isPresent());
    }
}

abstract class AbstractFileContent<T, FileData> implements FileContent<T> {
    protected abstract FileData fileData();

    protected <U> FileContent<U> withoutCovariance(FileContent<? extends U> fileContent) {
        if (fileContent instanceof AbstractFileContent) {
            @SuppressWarnings("unchecked")
            var cast = (AbstractFileContent<? extends U, FileData>) fileContent;
            // return same instance
            return cast.withAlternateRepresentation(cast.representation());
        }
        throw new NotImplementedException("TODO: return a (Text|Binary)FileContent instance");
    }

    public abstract FileContent<T> withRepresentation(T representation);

    public <U> FileContent<U> withAlternateRepresentation(U representation) {
        @SuppressWarnings("unchecked")
        var cast = (AbstractFileContent<U, FileData>)this;
        return cast.withRepresentation(representation);
    }

    @Override
    public <U> FileContent<U> withAlternateRepresentation(
        U representation, CharSource fileData, Optional<Charset> charset) {
        return ImmutableTextFileContent.<U>builder()
            .addAllAttributes(this.attributes())
            .charset(charset)
            .fileData(fileData)
            .representation(representation)
            .build();
    }

    @Override
    public <U> FileContent<U> withAlternateRepresentation(U representation, ByteSource fileData, Optional<Charset> charset) {
        return ImmutableBinaryFileContent.<U>builder()
            .addAllAttributes(this.attributes())
            .charset(charset)
            .fileData(fileData)
            .representation(representation)
            .build();
    }

    @Override
    public <U> FileContent<U> flatMap(Function<? super FileContent<? extends T>, ? extends FileContent<? extends U>> mapper) {
        return withoutCovariance(mapper.apply(this));
    }

    @Override
    public <U> FileContent<U> flatMapRepresentation(Function<? super T, ? extends FileContent<? extends U>> mapper) {
        return flatMap(fc -> mapper.apply(fc.representation()));
    }

    @Override
    public <U> FileContent<U> map(Function<? super FileContent<? extends T>, ? extends U> mapper) {
        return flatMap(mapper.andThen(this::withAlternateRepresentation));
    }

    @Override
    public <U> FileContent<U> mapRepresentation(Function<? super T, ? extends U> mapper) {
        return flatMapRepresentation(mapper.andThen(this::withAlternateRepresentation));
    }
}

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
abstract class AbstractTextFileContent<T> extends AbstractFileContent<T, CharSource> {
    @Override
    @Value.Derived
    @Value.Auxiliary
    public Optional<ByteSource> bytes() {
        return charset().map(cs -> fileData().asByteSource(cs));
    }

    @Override
    @Value.Derived
    @Value.Auxiliary
    public Optional<CharSource> text() {
        return Optional.of(fileData());
    }

    interface Builder<T> {
        ImmutableTextFileContent.Builder<T> fileData(CharSource fileData);
        default ImmutableTextFileContent.Builder<T> fileData(String fileData) {
            Preconditions.checkNotNull(fileData);
            return fileData(CharSource.wrap(fileData));
        }
    }
}

@Value.Immutable
@Value.Style(visibility = Value.Style.ImplementationVisibility.PUBLIC)
abstract class AbstractBinaryFileContent<T> extends AbstractFileContent<T, ByteSource> {
    @Override
    @Value.Derived
    @Value.Auxiliary
    public Optional<ByteSource> bytes() {
        return Optional.of(fileData());
    }

    @Override
    @Value.Derived
    @Value.Auxiliary
    public Optional<CharSource> text() {
        return charset().map(cs -> fileData().asCharSource(cs));
    }

    interface Builder<T> {
        ImmutableBinaryFileContent.Builder<T> fileData(ByteSource fileData);
        default ImmutableBinaryFileContent.Builder<T> fileData(byte[] fileData) {
            Preconditions.checkNotNull(fileData);
            return fileData(ByteSource.wrap(Arrays.copyOf(fileData, fileData.length)));
        }
    }
}
