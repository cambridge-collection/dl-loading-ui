package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttribute;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes.StandardFileAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes.StandardModelAttributes;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class TeiIdCreationStrategyTest {
    TeiIdCreationStrategy strategy = TeiIdCreationStrategy.of();

    @ParameterizedTest
    @MethodSource("createId_ignoresFileName_whenExistingIdIsAvailable_examples")
    public void createId_ignoresFileName_whenExistingIdIsAvailable(Path expected, Set<ModelAttribute<?>> attrs) {
        var result = strategy.createId(attrs);
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.value()).hasValue(expected);
    }

    public static Stream<Arguments> createId_ignoresFileName_whenExistingIdIsAvailable_examples() {
        var expected = Path.of("items/data/tei/MS-BAR-00001.txt");
        return Stream.of(
            Arguments.of(
                expected,
                ImmutableSet.of(
                    StandardModelAttributes.MODEL_ID.containing(expected.toString()),
                    StandardFileAttributes.FILENAME.containing("MS-FOO-00001.xml"))),
            Arguments.of(
                expected,
                ImmutableSet.of(
                    StandardModelAttributes.MODEL_ID.containing(expected),
                    StandardFileAttributes.FILENAME.containing("MS-FOO-00001.xml"))),
            Arguments.of(
                expected,
                ImmutableSet.of(
                    StandardModelAttributes.MODEL_ID.containing(expected)))
        );
    }

    @Test
    public void createId_throwsIllegalStateException_whenInvalidModelIdIsProvided() {
        Assertions.assertThrows(IllegalStateException.class, () ->
            strategy.createId(StandardModelAttributes.MODEL_ID.containing("/invalid/id")));
    }

    @ParameterizedTest
    @CsvSource({
        "MS-FOO-00001.xml,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml",
        "MS-FOO-BAR-00001.xml,items/data/tei/MS-FOO-BAR-00001/MS-FOO-BAR-00001.xml",
        "MS-FOO-00001.XML,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml",
        "MS-FOO-00001.XmL,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml",
        "MS-FOO-00001-362-AT.XmL,items/data/tei/MS-FOO-00001-362-AT/MS-FOO-00001-362-AT.xml"
    })
    public void createId_returnsPathGivenValidFilename(String filename, Path expected) {
        var result = strategy.createId(
            ModelAttributes.filename(filename)
        );
        assertThat(result.isSuccessful()).isTrue();
        assertThat(result.value()).hasValue(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "name.xml",
        "AB-00000.xml",
        "AB-ABC-000$0.xml",
        "AB--ABC-00000.xml",
        "MS-FOO-00001.xml ",
        " MS-FOO-00001.xml",
        "MS-FOO 00001.xml",
    })
    public void createId_returnsUnsuccessfulResultForInvalidPaths(String invalidFilename) {
        var result = strategy.createId(
            ModelAttributes.filename(invalidFilename)
        );
        assertThat(result.isSuccessful()).isFalse();
        assertThat(result.issues()).hasSize(1);
        var issue = result.issues().iterator().next();
        assertThat(issue.type()).isEqualTo(TeiIdCreationStrategy.Issue.INVALID_FILENAME);
        assertThat(issue.description()).startsWith(String.format("Item name '%s' not valid.", invalidFilename));
    }
}
