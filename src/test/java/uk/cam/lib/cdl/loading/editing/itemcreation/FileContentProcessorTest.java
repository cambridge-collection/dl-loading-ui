package uk.cam.lib.cdl.loading.editing.itemcreation;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import uk.cam.lib.cdl.loading.editing.itemcreation.DefaultFileContentCreationStrategy.FileContentProcessor;

import java.io.IOException;

import static uk.cam.lib.cdl.loading.editing.itemcreation.ImmutableCreationResultTest.ExampleIssueType.EXAMPLE;

public class FileContentProcessorTest {

    @Test
    public void pipedThrough() throws IOException {
        FileContentProcessor<String, Integer> a = s ->
            ImmutableCreationResult.successful(s.mapRepresentation(Integer::parseInt));

        FileContentProcessor<Integer, Integer> b = s ->
            s.representation() % 2 == 0 ?
                ImmutableCreationResult.successful(s.mapRepresentation(i -> i * 2)) :
                ImmutableCreationResult.unsuccessful(ImmutableIssue.of(EXAMPLE, "rejected: " + s.representation()));

        FileContentProcessor<String, Integer> c = a.pipedThrough(b);

        var fc = ImmutableTextFileContent.<String>builder().fileData("foo").representation("42").build();
        var success = c.processFileContent(fc);
        Truth.assertThat(success.isSuccessful()).isTrue();
        Truth.assertThat(success.value().orElseThrow().representation()).isEqualTo(84);

        var fail = c.processFileContent(fc.withRepresentation("21"));
        Truth.assertThat(fail.isSuccessful()).isFalse();
        Truth.assertThat(fail.issues()).containsExactly(ImmutableIssue.of(EXAMPLE, "rejected: 21"));
    }
}
