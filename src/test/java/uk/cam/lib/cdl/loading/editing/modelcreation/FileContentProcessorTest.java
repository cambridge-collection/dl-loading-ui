package uk.cam.lib.cdl.loading.editing.modelcreation;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FileContentProcessorTest {

    @Test
    public void pipedThrough() throws IOException {
        DefaultFileContentCreationStrategy.FileContentProcessor<String, Integer> a = s ->
            ImmutableCreationResult.successful(s.mapRepresentation(Integer::parseInt));

        DefaultFileContentCreationStrategy.FileContentProcessor<Integer, Integer> b = s ->
            s.representation() % 2 == 0 ?
                ImmutableCreationResult.successful(s.mapRepresentation(i -> i * 2)) :
                ImmutableCreationResult.unsuccessful(ImmutableIssue.of(ImmutableCreationResultTest.ExampleIssueType.EXAMPLE, "rejected: " + s.representation()));

        DefaultFileContentCreationStrategy.FileContentProcessor<String, Integer> c = a.pipedThrough(b);

        var fc = ImmutableTextFileContent.<String>builder().fileData("foo").representation("42").build();
        var success = c.processFileContent(fc);
        Truth.assertThat(success.isSuccessful()).isTrue();
        Truth.assertThat(success.value().orElseThrow().representation()).isEqualTo(84);

        var fail = c.processFileContent(fc.withRepresentation("21"));
        Truth.assertThat(fail.isSuccessful()).isFalse();
        Truth.assertThat(fail.issues()).containsExactly(ImmutableIssue.of(ImmutableCreationResultTest.ExampleIssueType.EXAMPLE, "rejected: 21"));
    }
}
