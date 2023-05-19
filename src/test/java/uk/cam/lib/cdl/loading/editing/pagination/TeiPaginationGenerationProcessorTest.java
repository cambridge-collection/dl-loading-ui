package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Document;
import org.xmlunit.builder.DiffBuilder;
import uk.cam.lib.cdl.loading.editing.modelcreation.CreationResult;
import uk.cam.lib.cdl.loading.editing.modelcreation.FileContent;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableBinaryFileContent;
import uk.cam.lib.cdl.loading.editing.modelcreation.ImmutableIssue;
import uk.cam.lib.cdl.loading.editing.modelcreation.ModelAttributes;
import uk.cam.lib.cdl.loading.utils.XML;

import java.io.IOException;

import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;

@SpringBootTest
public class TeiPaginationGenerationProcessorTest {

    public static TeiPaginationGenerationProcessor newTestInstance() {
        return TeiPaginationGenerationProcessor.builder()
            .teiPageListFactory(TeiPageListFactoryTest.newTestInstance())
            .build();
    }

    private TeiPaginationGenerationProcessor processor = newTestInstance();

    public static final String PAGINATION_CSV =
        "Label,Image\n" +
        "1r,MS-FOO-000-00001\n" +
        "1v,MS-FOO-000-00002\n";

    public static final String TEI_EMPTY = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "</TEI>";

    public static final String PAGES_FACSIMILE_XML = "" +
        "  <facsimile>" +
        "    <graphic decls=\"#document-thumbnail\"\n" +
        "             url=\"MS-FOO-000-00001\"/>" +
        "    <surface n=\"1r\" xml:id=\"page-surface-1\">\n" +
        "      <graphic decls=\"#example\"\n" +
        "               url=\"MS-FOO-000-00001\"/>\n" +
        "    </surface>" +
        "    <surface n=\"1v\" xml:id=\"page-surface-2\">\n" +
        "      <graphic decls=\"#example\"\n" +
        "               url=\"MS-FOO-000-00002\"/>\n" +
        "    </surface>" +
        "  </facsimile>";
    public static final String PAGES_PB_XML = "" +
        "      <div>" +
        "        <pb xml:id=\"page-pb-1\" n=\"1r\" facs=\"#page-surface-1\"/>" +
        "        <pb xml:id=\"page-pb-2\" n=\"1v\" facs=\"#page-surface-2\"/>" +
        "      </div>";

    public static final String TEI_EMPTY_WITH_PAGES = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        PAGES_FACSIMILE_XML +
        "  <text>" +
        "    <body>" +
        PAGES_PB_XML +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    private CreationResult<FileContent<Document>> process(String tei, String paginationCsv) throws IOException {
        return process(XML.parseString(tei), paginationCsv);
    }
    private CreationResult<FileContent<Document>> process(Document tei, String paginationCsv) throws IOException {
        var attributes = ImmutableSet.of(
            TeiPaginationGenerationProcessor.Attribute.PAGINATION_ATTRIBUTES.containing(ImmutableSet.of(
                ModelAttributes.StandardFileAttributes.TEXT.containing(paginationCsv)
            ))
        );

        var fc = ImmutableBinaryFileContent.<Document>builder()
            .attributes(attributes)
            .fileData(ByteSource.wrap(XML.serialise(tei).getBytes(UTF_8)))
            .representation(tei)
            .build();

        return processor.processFileContent(fc);
    }

    @Test
    public void processFileContent_returnsSuccessfulResult() throws IOException {
        var inputDoc = XML.parseString(TEI_EMPTY);
        var result = process(inputDoc, PAGINATION_CSV);

        Truth.assertThat(result.isSuccessful()).isTrue();
        var fc = result.value().orElseThrow();
        var outputDoc = fc.representation();
        Truth.assertThat(outputDoc).isNotSameInstanceAs(inputDoc);

        // Output Document is as expected
        var loadedDiff = DiffBuilder.compare(TEI_EMPTY_WITH_PAGES)
            .withTest(outputDoc)
            .ignoreWhitespace()
            .build();
        assertWithMessage("%s", loadedDiff).that(loadedDiff.hasDifferences()).isFalse();

        // The FileContent should contain both text and bytes
        Truth8.assertThat(fc.text()).isPresent();
        Truth8.assertThat(fc.bytes()).isPresent();
        Truth8.assertThat(fc.charset()).isPresent();
        Truth.assertThat(fc.text().get().read())
            .isEqualTo(fc.bytes().get().asCharSource(fc.charset().get()).read());

        // And file content matches loaded Document
        var serialisedDiff = DiffBuilder.compare(TEI_EMPTY_WITH_PAGES)
            .withTest(fc.text().get().read())
            .ignoreWhitespace()
            .build();
        assertWithMessage("%s", serialisedDiff).that(serialisedDiff.hasDifferences()).isFalse();
    }

    private static final String NON_TEI_XML = "<foo/>";

    @Test
    public void processFileContent_returnsUnsuccessfulResult_whenTeiIsInvalid() throws IOException {
        var result = process(NON_TEI_XML, PAGINATION_CSV);

        Truth.assertThat(result.isSuccessful()).isFalse();
        Truth.assertThat(result.issues()).containsExactly(
            ImmutableIssue.of(
                PaginationIssue.INAVLID_TEI,
                "Unable to insert pages into XML: Root element must be " +
                    "{http://www.tei-c.org/ns/1.0}TEI but was foo")
        );
    }

    @Test
    public void processFileContent_returnsUnsuccessfulResult_whenPaginationIsInvalid() throws IOException {
        var result = process(TEI_EMPTY, "Foo,Bar\n");

        Truth.assertThat(result.isSuccessful()).isFalse();
        Truth.assertThat(result.issues()).hasSize(1);
        var issue = result.issues().stream().findFirst().orElseThrow();
        Truth.assertThat(issue.type()).isEqualTo(PaginationIssue.INVALID_PAGE_DEFINITIONS);
        Truth.assertThat(issue.description()).isNotEmpty();
    }
}
