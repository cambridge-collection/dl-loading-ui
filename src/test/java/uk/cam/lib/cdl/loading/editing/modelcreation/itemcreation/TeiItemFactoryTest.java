package uk.cam.lib.cdl.loading.editing.modelcreation.itemcreation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.xmlunit.builder.DiffBuilder;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.editing.pagination.CSVPageLoader.CSVRowAccessor;
import uk.cam.lib.cdl.loading.editing.pagination.DefaultTEIPageConverter;
import uk.cam.lib.cdl.loading.editing.pagination.ImmutableCSVPageLoader;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPageListFactory;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessor;
import uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessorTest;

import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.Truth8.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static uk.cam.lib.cdl.loading.editing.pagination.TeiPaginationGenerationProcessorTest.TEI_EMPTY;

@SpringBootTest
public class TeiItemFactoryTest {
    public static final Path TEI_BASE_PATH = Path.of("example/items/tei");

    public static TeiItemFactory newTestInstance() {
        return TeiItemFactory.builder()
            .teiPageListFactory(TeiPageListFactory.builder()
                .pageLoader(ImmutableCSVPageLoader.builder()
                    .imageAccessor(CSVRowAccessor.of("Image"))
                    .labelAccessor(CSVRowAccessor.of("Label"))
                    .build())
                .teiPageConverter(new DefaultTEIPageConverter(ImmutableList.of("#example")))
                .build())
            .teiIdCreationStrategy(TeiIdCreationStrategy.builder()
                .baseTeiItemPath(TEI_BASE_PATH)
                .build())
            .build();
    }

    private TeiItemFactory itemFactory = newTestInstance();

    private static final Path EXPECTED_ID = TEI_BASE_PATH.resolve("MS-FOO-00001/MS-FOO-00001.xml");

    @Test
    public void createFromAttributes_returnsItemWithExpectedId() throws IOException {
        var result = itemFactory.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing(EXPECTED_ID.getFileName().toString()),
            ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap(TEI_EMPTY.getBytes(UTF_8)))
        );

        assertThat(result.isSuccessful()).isTrue();
        var item = result.value().orElseThrow();

        assertThat(item.id()).isEqualTo(EXPECTED_ID);
    }

    @Test
    public void createFromAttributes_returnsXmlAsProvided_whenPaginationIsNotSpecified() throws IOException {
        var result = itemFactory.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing(EXPECTED_ID.getFileName().toString()),
            ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap(TEI_EMPTY.getBytes(UTF_8)))
        );

        assertThat(result.isSuccessful()).isTrue();
        var item = result.value().orElseThrow();
        assertThat(item.fileData()).isPresent();

        var diff = DiffBuilder.compare(TEI_EMPTY)
            .withTest(item.fileData().get())
            .ignoreWhitespace()
            .build();
        assertWithMessage("%s", diff).that(diff.hasDifferences()).isFalse();
    }

    @Test
    public void createFromAttributes_returnsXmlWithPagination_whenPaginationIsSpecified() throws IOException {
        var result = itemFactory.createFromAttributes(
            ModelAttributes.StandardFileAttributes.FILENAME.containing(EXPECTED_ID.getFileName().toString()),
            ModelAttributes.StandardFileAttributes.BYTES.containing(ByteSource.wrap(TEI_EMPTY.getBytes(UTF_8))),
            TeiPaginationGenerationProcessor.Attribute.PAGINATION_ATTRIBUTES.containing(ImmutableSet.of(
                ModelAttributes.StandardFileAttributes.TEXT.containing(TeiPaginationGenerationProcessorTest.PAGINATION_CSV)
            ))
        );

        Truth.assertWithMessage("%s", result.issues()).that(result.isSuccessful()).isTrue();
        var item = result.value().orElseThrow();
        assertThat(item.fileData()).isPresent();

        var diff = DiffBuilder.compare(TeiPaginationGenerationProcessorTest.TEI_EMPTY_WITH_PAGES)
            .withTest(item.fileData().get())
            .ignoreWhitespace()
            .build();
        assertWithMessage("%s", diff).that(diff.hasDifferences()).isFalse();
    }
}
