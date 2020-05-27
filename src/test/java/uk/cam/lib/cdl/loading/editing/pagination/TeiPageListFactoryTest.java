package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;

import java.io.IOException;
import java.net.URI;

public class TeiPageListFactoryTest {
    public static TeiPageListFactory newTestInstance() {
        return TeiPageListFactory.builder()
            .pageLoader(ImmutableCSVPageLoader.builder()
                .imageAccessor(CSVPageLoader.CSVRowAccessor.of("Image"))
                .labelAccessor(CSVPageLoader.CSVRowAccessor.of("Label"))
                .build())
            .teiPageConverter(new DefaultTEIPageConverter(ImmutableList.of(
                "#example")))
            .build();
    }

    private TeiPageListFactory factory = newTestInstance();

    @Test
    public void createFromAttributes_returnsPageListGivenValidInput() throws IOException {
        var inputFileData = "Label,Image\n" +
            "1r,MS-FOO-000-00001\n" +
            "1v,MS-FOO-000-00002\n";

        var expectedTeiPages = ImmutableList.of(
            ImmutableTEIPage.builder()
                .identifier("1")
                .addTags("#example")
                .page(ImmutablePage.of("1r", URI.create("MS-FOO-000-00001")))
                .build(),
            ImmutableTEIPage.builder()
                .identifier("2")
                .addTags("#example")
                .page(ImmutablePage.of("1v", URI.create("MS-FOO-000-00002")))
                .build());

        var creationResult = factory.createFromAttributes(
            ModelAttributes.StandardFileAttributes.TEXT.containing(inputFileData));

        Truth.assertThat(creationResult.isSuccessful()).isTrue();
        Truth.assertThat(creationResult.value().orElseThrow()).isEqualTo(expectedTeiPages);
    }

    @Test
    public void createFromAttributes_returnsUnsuccessfulResult_onInvalidCsv() throws IOException {
        var inputFileData = "Page Name,Image\n" +
            "1r,MS-FOO-000-00001\n" +
            "1v,MS-FOO-000-00002\n";

        var creationResult = factory.createFromAttributes(
            ModelAttributes.StandardFileAttributes.TEXT.containing(inputFileData));

        Truth.assertThat(creationResult.isSuccessful()).isFalse();
        Truth.assertThat(creationResult.issues()).containsExactly(
            ImmutableIssue.of(PaginationIssue.INVALID_PAGE_DEFINITIONS,
                "CSV row has no column named 'Label'")
        );
    }
}
