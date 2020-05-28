package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation.TeiIdCreationStrategy;

import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat;

public class TeiIdCreationStrategyTest {
    TeiIdCreationStrategy strategy = TeiIdCreationStrategy.of();

    @ParameterizedTest
    @CsvSource({
        "MS-FOO-00001.xml,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml",
        "MS-FOO-BAR-00001.xml,items/data/tei/MS-FOO-BAR-00001/MS-FOO-BAR-00001.xml",
        "MS-FOO-00001.XML,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml",
        "MS-FOO-00001.XmL,items/data/tei/MS-FOO-00001/MS-FOO-00001.xml"
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
        "AB-ABC--00000.xml",
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
