package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableList;
import com.google.common.truth.Truth;
import org.apache.commons.csv.CSVFormat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.cam.lib.cdl.loading.editing.pagination.CSVPageLoader.CSVRowAccessor;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class CSVPageLoaderTest {
    @Test
    public void columnMappingIsRequired() {
        var loader = ImmutableCSVPageLoader.builder()
            // No column mappings defined in CSVFormat
            .csvFormat(CSVFormat.DEFAULT)
            .build();

        Exception e = assertThrows(PaginationException.class, () ->
            loader.loadPages(new StringReader("label,image\na,b\nc,d")));

        Truth.assertThat(e).hasMessageThat().isEqualTo("CSV row has no column named 'label'");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "\n",
        "foo,bar\n"
    })
    public void loadPages_throwsUserInputError_whenNoPagesAreFound(String csv) throws IOException {
        var loader = ImmutableCSVPageLoader.builder().build();

        Exception e = assertThrows(UserInputPaginationException.class, () ->
            loader.loadPages(new StringReader(csv)));

        Truth.assertThat(e).hasMessageThat().isEqualTo("CSV contains no page data");
    }

    @ParameterizedTest
    @MethodSource("loadPagesParameters")
    public void loadPages(
        CSVFormat fmt, CSVRowAccessor label,
        CSVRowAccessor image, List<Page> expected, String csv) throws IOException {

        var loader = ImmutableCSVPageLoader.builder()
            .csvFormat(fmt).imageAccessor(image).labelAccessor(label).build();

        Truth.assertThat(loader.loadPages(new StringReader(csv)))
            .containsExactlyElementsIn(expected);
    }

    public static Stream<Arguments> loadPagesParameters() {
        return Stream.of(
            Arguments.of(
                CSVFormat.DEFAULT.withFirstRecordAsHeader(),
                CSVRowAccessor.of("label"), CSVRowAccessor.of("image"),
                ImmutableList.of(
                    ImmutablePage.of("1r", URI.create("MS-FOO-000-00001")),
                    ImmutablePage.of("1v", URI.create("MS-FOO-000-00002"))
                ),
                "label,image\n" +
                "1r,MS-FOO-000-00001\n" +
                "1v,MS-FOO-000-00002\n"
            ),
            // ignored columns
            Arguments.of(
                CSVFormat.DEFAULT.withFirstRecordAsHeader(),
                CSVRowAccessor.of("label"), CSVRowAccessor.of("image"),
                ImmutableList.of(
                    ImmutablePage.of("1r", URI.create("MS-FOO-000-00001")),
                    ImmutablePage.of("1v", URI.create("MS-FOO-000-00002"))
                ),
                "label,foo,image,bar\n" +
                "1r,a,MS-FOO-000-00001,x\n" +
                "1v,b,MS-FOO-000-00002,y\n"
            ),
            // image paths are URIs, but inputs are quite permissive
            Arguments.of(
                CSVFormat.DEFAULT.withFirstRecordAsHeader(),
                CSVRowAccessor.of("label"), CSVRowAccessor.of("image"),
                ImmutableList.of(
                    ImmutablePage.of("1", URI.create("/path%20with%20spaces/img.jpg")),
                    ImmutablePage.of("2", URI.create("file:///path%20with%20spaces/img.jpg")),
                    ImmutablePage.of("3", URI.create("http://images.example.com/foo%20bar/img.jpg"))
                ),
                "label,image\n" +
                "1,/path with spaces/img.jpg\n" +
                "2,file:///path with spaces/img.jpg\n" +
                "3,http://images.example.com/foo bar/img.jpg\n"
            )
        );
    }
}
