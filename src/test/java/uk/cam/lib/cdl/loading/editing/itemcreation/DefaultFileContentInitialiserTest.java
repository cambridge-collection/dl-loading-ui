package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.cam.lib.cdl.loading.testutils.FileContentSubject.assertThat;

public class DefaultFileContentInitialiserTest {
    DefaultFileContentInitialiser initialiser = DefaultFileContentInitialiser.getInstance();

    @Test
    public void initialiseFileContent_bytesWithNoCharset() {
        var attrs = ImmutableSet.of(
            ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes(UTF_8)))
        );
        var expected = ImmutableBinaryFileContent.builder()
            .attributes(attrs)
            .fileData("foo".getBytes(UTF_8))
            .representation(Optional.empty())
            .build();

        var result = initialiser.initialiseFileContent(attrs);
        assertThat(result.isSuccessful());
        assertThat(result.value().orElseThrow()).isContentEqualTo(expected);
    }
    @Test
    public void initialiseFileContent_containingBytesWithCharset() {
        var attrs = ImmutableSet.of(
            ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes())),
            ModelAttributes.StandardFileAttributes.CHARSET.containing("UTF-8")
        );
        var expected = ImmutableBinaryFileContent.builder()
            .attributes(attrs)
            .fileData("foo".getBytes(UTF_8))
            .charset(UTF_8)
            .representation(Optional.empty())
            .build();

        var result = initialiser.initialiseFileContent(attrs);
        assertThat(result.isSuccessful());
        assertThat(result.value().orElseThrow()).isContentEqualTo(expected);
    }

    @Test
    public void initialiseFileContent_containingTextWithNoCharset() {
        var attrs = ImmutableSet.of(
            ModelAttributes.StandardFileAttributes.TEXT.containing(CharSource.wrap("foo"))
        );
        var expected = ImmutableTextFileContent.builder()
            .attributes(attrs)
            .fileData("foo")
            .representation(Optional.empty())
            .build();

        var result = initialiser.initialiseFileContent(attrs);
        assertThat(result.isSuccessful());
        assertThat(result.value().orElseThrow()).isContentEqualTo(expected);
    }


    @Test
    public void initialiseFileContent_containingTextWithCharset() {
        var attrs = ImmutableSet.of(
            ModelAttributes.StandardFileAttributes.TEXT.containing(CharSource.wrap("foo")),
            ModelAttributes.StandardFileAttributes.CHARSET.containing("UTF-8")
        );
        var expected = ImmutableTextFileContent.builder()
            .attributes(attrs)
            .fileData("foo")
            .charset(UTF_8)
            .representation(Optional.empty())
            .build();

        var result = initialiser.initialiseFileContent(attrs);
        assertThat(result.isSuccessful());
        assertThat(result.value().orElseThrow()).isContentEqualTo(expected);
    }

    @Test
    public void initialiseFileContent_containingTextAndBytes_isInvalid() {
        Assertions.assertThrows(IllegalStateException.class, () ->
            initialiser.initialiseFileContent(ImmutableSet.of(
                ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap("foo".getBytes())),
                ModelAttributes.StandardFileAttributes.TEXT.containing("foo")
            )))
        ;
    }

    @Test
    public void initialiseFileContent_containingNoAttributes_isInvalid() {
        Assertions.assertThrows(IllegalStateException.class, () ->
            initialiser.initialiseFileContent(ImmutableSet.of()));
    }
}
