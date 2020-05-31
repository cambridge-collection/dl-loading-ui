package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

public class DefaultModelFactoryTest {
    public static final DefaultModelFactory.IdCreationStrategy ID_STRATEGY = attrs -> {
        var cr = ModelAttributes.findAttribute(ModelAttributes.StandardFileAttributes.FILENAME, String.class, attrs)
            .map(ModelAttribute::value)
            .map(ImmutableCreationResult::successful)
            .orElseThrow(() -> {
                // not a user error
                throw new IllegalStateException("FILENAME attribute is required");
            });
        return cr.flatMapValue(filename -> Pattern.matches("^\\w+\\.\\w+$", filename) ? cr :
                ImmutableCreationResult.unsuccessful(ImmutableIssue.of(IdIssue.INVALID_FILENAME, "Invalid: " + filename)))
            .mapValue(filename -> Path.of("my/path", filename));
    };

    enum IdIssue implements Issue.Type {
        INVALID_FILENAME
    }

    public static final DefaultModelFactory<Map<String, Object>, Integer> CREATOR = DefaultModelFactory.<Map<String, Object>, Integer>builder()
        .idCreator(ID_STRATEGY)
        .fileContentCreator(DefaultFileContentCreationStrategyTest.CREATOR)
        .resultAssembler((pathResult, numberResult) -> pathResult.biMapValue(numberResult, (path, number) -> ImmutableMap.of(
            "path", path,
            "number", number.representation()
        )))
        .build();

    @Test
    public void idStrategy() {
        var cr = ID_STRATEGY.createId(ModelAttributes.StandardFileAttributes.FILENAME.containing("foo.txt"));
        assertThat(cr.isSuccessful()).isTrue();
        assertThat(cr.value()).hasValue(Path.of("my/path/foo.txt"));
    }

    @Test
    public void createFromAttributes_returnsSuccessfulResult() throws IOException {
        var cr = CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing("foo.txt"),
            ModelAttributes.StandardFileAttributes.TEXT.containing("42")
        );

        assertThat(cr.isSuccessful()).isTrue();
        assertThat(cr.value()).hasValue(ImmutableMap.of(
            "path", Path.of("my/path/foo.txt"),
            "number", 42));
    }

    @Test
    public void createFromAttributes_returnsUnsuccessfulResult_whenIdStrategyIsUnsuccessful() throws IOException {
        var cr = CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing("/invalid path.txt"),
            ModelAttributes.StandardFileAttributes.TEXT.containing("42")
        );

        assertThat(cr.isSuccessful()).isFalse();
        assertThat(cr.issues()).containsExactly(
            ImmutableIssue.of(IdIssue.INVALID_FILENAME, "Invalid: /invalid path.txt"));
    }

    @Test
    public void createFromAttributes_returnsUnsuccessfulResult_whenFileContentCreatorIsUnsuccessful() throws IOException {
        var cr = CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing("foo.txt"),
            ModelAttributes.StandardFileAttributes.TEXT.containing("forty two")
        );

        assertThat(cr.isSuccessful()).isFalse();
        assertThat(cr.issues()).containsExactly(
            ImmutableIssue.of(DefaultFileContentCreationStrategyTest.ExampleIssueType.EXAMPLE, "Invalid number: 'forty two'"));
    }

    @Test
    public void createFromAttributes_returnsUnsuccessfulResult_whenBothIdStrategyAndFileContentCreatorAreUnsuccessful() throws IOException {
        var cr = CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing("/invalid path.txt"),
            ModelAttributes.StandardFileAttributes.TEXT.containing("forty two")
        );

        assertThat(cr.isSuccessful()).isFalse();
        assertThat(cr.issues()).containsExactly(
            ImmutableIssue.of(IdIssue.INVALID_FILENAME, "Invalid: /invalid path.txt"),
            ImmutableIssue.of(DefaultFileContentCreationStrategyTest.ExampleIssueType.EXAMPLE, "Invalid number: 'forty two'"));
    }

    @Test
    public void createFromAttributes_throwsException_whenInvalidInputNotCausedByUserUserIsProvided() {
        Assertions.assertThrows(IllegalStateException.class, () -> CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.TEXT.containing("forty two")));

        Assertions.assertThrows(IllegalStateException.class, () -> CREATOR.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing("/invalid path.txt")));
    }
}
