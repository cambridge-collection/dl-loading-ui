package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.immutables.value.Value;
import uk.cam.lib.cdl.loading.editing.itemcreation.DefaultFileContentCreationStrategy.FileContentInitialiser;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;

@Value.Immutable(singleton = true, builder = false)
@Value.Style(visibility = Value.Style.ImplementationVisibility.PACKAGE)
public abstract class DefaultFileContentInitialiser implements FileContentInitialiser {

    public static DefaultFileContentInitialiser getInstance() {
        return ImmutableDefaultFileContentInitialiser.of();
    }

    private static final String FILE_CONTENT_NOT_FOUND_MSG =
        "No file content was found amongst attributes. Expected a StandardItemAttributes.BYTES attribute " +
            "containing a com.google.common.io.ByteSource, or a StandardItemAttributes.TEXT attribute " +
            "containing a com.google.common.io.CharSource. Additionally StandardItemAttributes.CHARSET can be " +
            "provided as a String or java.nio.charset.Charset, which enables bytes to be derived from text, or " +
            "vice versa.";

    @Override
    public CreationResult<FileContent<Optional<Void>>> initialiseFileContent(
        Set<? extends ModelAttribute<?>> attributes
    ) {
        var providedCharset = getProvidedCharset(attributes);
        var providedBytes = getProvidedBytes(attributes);
        var providedText = getProvidedText(attributes);

        // Note that these are programming errors, not user errors, so we don't report them via the CreationResult.
        Preconditions.checkState(!(providedBytes.isEmpty() && providedText.isEmpty()),
            FILE_CONTENT_NOT_FOUND_MSG);
        Preconditions.checkState(!(providedBytes.isPresent() && providedText.isPresent()),
            "Ambiguous file content: StandardItemAttributes.BYTES and StandardItemAttributes.TEXT are both " +
                "provided");

        if(providedBytes.isPresent()) {
            return ImmutableCreationResult.successful(
                ImmutableBinaryFileContent.<Optional<Void>>builder()
                    .fileData(providedBytes.get())
                    .charset(providedCharset)
                    .addAllAttributes(attributes)
                    .representation(Optional.empty())
                    .build());
        }
        else {
            return ImmutableCreationResult.successful(
                ImmutableTextFileContent.<Optional<Void>>builder()
                    .fileData(providedText.get())
                    .charset(providedCharset)
                    .addAllAttributes(attributes)
                    .representation(Optional.empty())
                    .build());
        }
    }

    protected Optional<Charset> getProvidedCharset(Set<? extends ModelAttribute<?>> attributes) {
        return ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.CHARSET, String.class, attributes)
            .map(ModelAttribute::value)
            .map(Charset::forName)
            .or(() -> ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.CHARSET, Charset.class, attributes)
                .map(ModelAttribute::value));
    }

    protected Optional<ByteSource> getProvidedBytes(Set<? extends ModelAttribute<?>> attributes) {
        return ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.BYTES, ByteSource.class, attributes)
            .map(ModelAttribute::value);
    }

    protected Optional<CharSource> getProvidedText(Set<? extends ModelAttribute<?>> attributes) {
        return ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.TEXT, CharSource.class, attributes)
            .map(ModelAttribute::value)
            .or(() -> ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.TEXT, String.class, attributes)
                .map(ModelAttribute::value)
                .map(CharSource::wrap));
    }
}
