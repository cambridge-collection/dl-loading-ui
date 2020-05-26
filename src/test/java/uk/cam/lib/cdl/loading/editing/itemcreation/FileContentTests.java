package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.cam.lib.cdl.loading.editing.itemcreation.ModelAttributes.StandardFileAttributes;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

public class FileContentTests {
    @TestInstance(PER_CLASS)
    public abstract static class BaseFileContentTest<T extends FileContent<?>> {
        protected T content = null;

        @Test
        public void attributes() {
            assertThat(content.attributes()).containsExactly(StandardFileAttributes.FILENAME.containing("foo.txt"));
        }

        @Test
        public void flatMap() throws IOException {
            var a = content.flatMap(fc -> fc.withAlternateRepresentation("hi", "hi"));
            var b = content.withAlternateRepresentation("hi", "hi");
            assertWithMessage("%s.contentEquals(%s)", a, b).that(a.contentEquals(b)).isTrue();
        }

        @Test
        public void flatMapRepresentation() throws IOException {
            assertThat(content.flatMapRepresentation(rep -> content.withAlternateRepresentation("number: " + rep, "number: " + rep))
                .contentEquals(content.withAlternateRepresentation("number: 42", "number: 42"))).isTrue();
        }

        @Test
        public void mapRepresentation() throws IOException {
            assertThat(content.mapRepresentation(rep -> "number: " + rep)
                .contentEquals(content.withAlternateRepresentation("number: 42", "42"))).isTrue();
        }

        @Test
        public void map() throws IOException {
            assertThat(content.map(fc -> "number: " + fc.representation())
                .contentEquals(content.withAlternateRepresentation("number: 42", "42"))).isTrue();
        }

        @ParameterizedTest
        @MethodSource("equalityExamples")
        public void equality(FileContent<?> left, FileContent<?> right, boolean sameInstance, boolean equal, boolean contentEqual) throws IOException {
            assertWithMessage("((%s) == (%s)) == %s", left, right, sameInstance)
                .that(left == right).isEqualTo(sameInstance);
            assertWithMessage("(%s).equals(%s) == %s", left, right, equal)
                .that(left.equals(right)).isEqualTo(equal);
            assertWithMessage("(%s).contentEquals(%s) == %s", left, right, contentEqual)
                .that(left.contentEquals(right)).isEqualTo(contentEqual);
        }

        public static ImmutableTextFileContent<List<String>> txtFc() {
            return ImmutableTextFileContent.<List<String>>builder()
                .addAttributes(StandardFileAttributes.FILENAME.containing("foo.txt"))
                .fileData("abc")
                .representation(ImmutableList.of("a", "b", "c"))
                .build();
        }

        public static ImmutableBinaryFileContent<List<String>> binFc() {
            return ImmutableBinaryFileContent.<List<String>>builder()
                .addAttributes(StandardFileAttributes.FILENAME.containing("foo.txt"))
                .fileData("abc".getBytes(UTF_8))
                .representation(ImmutableList.of("a", "b", "c"))
                .build();
        }

        public Stream<Arguments> equalityExamples() {
            var txt = txtFc();
            var bin = binFc();

            // Used to force withFileData() to allocate a new instance
            var tmpCs = CharSource.wrap("");
            var tmpBs = ByteSource.wrap(new byte[0]);

            return Stream.of(
                // Same instance
                Arguments.of(txt, txt, true, true, true),
                Arguments.of(bin, bin, true, true, true),

                // different instance with same properties and same text() instance
                Arguments.of(txt, txt.withFileData(tmpCs).withFileData(txt.text().orElseThrow()), false, true, true),
                // different instance with same properties and same bytes() instance
                Arguments.of(bin, bin.withFileData(tmpBs).withFileData(bin.bytes().orElseThrow()), false, true, true),

                // different instance with equal properties and same text() instance
                Arguments.of(txt, txtFc().withFileData(txt.text().orElseThrow()), false, true, true),
                // different instance with equal properties and same bytes() instance
                Arguments.of(bin, binFc().withFileData(bin.bytes().orElseThrow()), false, true, true),

                // different instance with equal properties and content-equal text()
                Arguments.of(txt, txtFc().withFileData(CharSource.wrap("abc")), false, false, true),
                // different instance with equal properties and content-equal bytes()
                Arguments.of(bin, binFc().withFileData(ByteSource.wrap("abc".getBytes(UTF_8))), false, false, true),

                // TextFileContent and BinaryFileContent with equivalent content
                Arguments.of(txt.withCharset(UTF_8), bin.withCharset(UTF_8), false, false, true),
                // TextFileContent and BinaryFileContent with equivalent content but no charset to allow text/bytes comparison
                Arguments.of(txt, bin, false, false, false),

                // Different properties
                Arguments.of(txt, txt.withCharset(StandardCharsets.US_ASCII), false, false, false),
                Arguments.of(bin, bin.withCharset(StandardCharsets.US_ASCII), false, false, false),
                Arguments.of(txt, txt.withAttributes(ImmutableSet.of()), false, false, false),
                Arguments.of(bin, bin.withAttributes(ImmutableSet.of()), false, false, false),
                Arguments.of(txt, txt.withAlternateRepresentation(42), false, false, false),
                Arguments.of(bin, bin.withAlternateRepresentation(42), false, false, false)
            );
        }
    }

    public static class ImmutableBinaryFileContentTest extends BaseFileContentTest<ImmutableBinaryFileContent<Integer>> {
        @BeforeAll
        public void beforeAll() {
            this.content = ImmutableBinaryFileContent.<Integer>builder()
                .representation(42)
                .fileData("42".getBytes())
                .charset(UTF_8)
                .addAttributes(
                    StandardFileAttributes.FILENAME.containing("foo.txt"))
                .build();
        }

        @Test
        public void bytes() throws IOException {
            assertThat(content.bytes().orElseThrow().read())
                .isEqualTo("42".getBytes(UTF_8));
            assertThat(content.withCharset(Optional.empty()).bytes().orElseThrow().read())
                .isEqualTo("42".getBytes());
        }

        @Test
        public void text() throws IOException {
            assertThat(content.text().orElseThrow().read()).isEqualTo("42");
            assertThat(content.withCharset(Optional.empty()).text()).isEmpty();
        }

        @Test
        public void withAlternateRepresentation_withoutFileData() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"));
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.bytes().orElseThrow().read()).isEqualTo("42".getBytes(UTF_8));
            assertThat(pathContent.charset()).hasValue(UTF_8);
        }

        @Test
        public void withAlternateRepresentation_withFileData() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"), "/foo".getBytes(UTF_8));
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.bytes().orElseThrow().read()).isEqualTo("/foo".getBytes(UTF_8));
            assertThat(pathContent.charset()).hasValue(UTF_8);
        }

        @Test
        public void withAlternateRepresentation_withFileDataAndCharset() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"), "/foo".getBytes(UTF_16), Optional.of(UTF_16));
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.bytes().orElseThrow().read()).isEqualTo("/foo".getBytes(UTF_16));
            assertThat(pathContent.text().orElseThrow().read()).isEqualTo("/foo");
            assertThat(pathContent.charset()).hasValue(UTF_16);
        }
    }

    public static class ImmutableTextFileContentTest extends BaseFileContentTest<ImmutableTextFileContent<Integer>> {
        @BeforeAll
        public void beforeAll() {
            this.content = ImmutableTextFileContent.<Integer>builder()
                .representation(42)
                .fileData("42")
                .charset(UTF_8)
                .addAttributes(
                    StandardFileAttributes.FILENAME.containing("foo.txt"))
                .build();
        }

        @Test
        public void bytes() throws IOException {
            assertThat(content.bytes().orElseThrow().read())
                .isEqualTo("42".getBytes(UTF_8));
            assertThat(content.withCharset(Optional.empty()).bytes())
                .isEmpty();
        }

        @Test
        public void text() throws IOException {
            assertThat(content.text().orElseThrow().read()).isEqualTo("42");
        }

        @Test
        public void withAlternateRepresentation_withoutFileData() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"));
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.text().orElseThrow().read()).isEqualTo("42");
            assertThat(pathContent.charset()).hasValue(UTF_8);
        }

        @Test
        public void withAlternateRepresentation_withFileData() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"), "/foo");
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.text().orElseThrow().read()).isEqualTo("/foo");
            assertThat(pathContent.charset()).hasValue(UTF_8);
        }

        @Test
        public void withAlternateRepresentation_withFileDataAndCharset() throws IOException {
            var pathContent = content.withAlternateRepresentation(Path.of("/foo"), "/foo", Optional.of(UTF_16));
            assertThat(pathContent.representation()).isEqualTo(Path.of("/foo"));
            assertThat(pathContent.text().orElseThrow().read()).isEqualTo("/foo");
            assertThat(pathContent.charset()).hasValue(UTF_16);
        }
    }
}
