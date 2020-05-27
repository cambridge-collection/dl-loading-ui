package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import uk.cam.lib.cdl.loading.editing.itemcreation.ModelAttributes.StandardFileAttributes;

import java.io.IOException;

public class DefaultFileContentCreationStrategyTest {
    public static final DefaultFileContentCreationStrategy<Integer> CREATOR = ImmutableDefaultFileContentCreationStrategy.<Integer>builder()
        .initialiser(DefaultFileContentInitialiser.getInstance())
        .processor(fc -> {
            var text = fc.text()
                .orElseThrow(() -> new IllegalStateException("FileContent contains no text"))
                .read();
            try {
                var number = Integer.parseInt(text);
                return ImmutableCreationResult.successful(fc.withAlternateRepresentation(number));
            } catch (NumberFormatException e) {
                return ImmutableCreationResult.unsuccessful(
                    ImmutableIssue.of(ExampleIssueType.EXAMPLE, String.format("Invalid number: '%s'", text)));
            }
        })
        .build();

    @Test
    public void createFileContent() throws IOException {
        var result = CREATOR.createFileContent(
            StandardFileAttributes.TEXT.containing("42"));

        Truth.assertThat(result.isSuccessful()).isTrue();
        Truth.assertThat(result.value().get().representation()).isEqualTo(42);
    }

    @Test
    public void createFileContent_returnsUnsuccessfulResult() throws IOException {
        var result = CREATOR.createFileContent(
            StandardFileAttributes.TEXT.containing("forty two"));

        Truth.assertThat(result.isSuccessful()).isFalse();
        Truth.assertThat(result.issues()).containsExactly(
            ImmutableIssue.of(ExampleIssueType.EXAMPLE, "Invalid number: 'forty two'"));
    }

    enum ExampleIssueType implements Issue.Type { EXAMPLE }
}
