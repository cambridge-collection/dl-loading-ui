package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import uk.cam.lib.cdl.loading.utils.XML;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class TEIPageInserterTest {

    private TEIPageInserter inserter;

    @BeforeEach
    private void beforeEach() {
        inserter = new TEIPageInserter();
    }

    @ParameterizedTest
    @CsvSource({
        "<foo/>,foo",
        "<foo xmlns=\"abc\"/>,{abc}foo",
        "<TEI/>,TEI"
    })
    public void nonTEIDocumentsAreRejected(String xml, String clarkName) {
        var exc = assertThrows(PaginationException.class, () ->
            inserter.insertPages(XML.parseString(xml), ImmutableList.of()));
        assertThat(exc).hasMessageThat().isEqualTo(String.format(
            "Unable to insert pages into XML: Root element must be {http://www.tei-c.org/ns/1.0}TEI but was %s", clarkName));
    }

    private static final String TEI_EMPTY = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "</TEI>";

    private static final List<TEIPage> PAGES = ImmutableList.of(
        ImmutableTEIPage.builder()
            .identifier("cover")
            .addTags("#downloadImageRights", "#abc")
            .page(ImmutablePage.of("Cover", URI.create("MS-FOO-000-00001")))
            .build(),
        ImmutableTEIPage.builder()
            .identifier("1")
            .addTags("#downloadImageRights", "#download")
            .page(ImmutablePage.of("1r", URI.create("MS-FOO-000-00002")))
            .build(),
        ImmutableTEIPage.builder()
            .identifier("2")
            .addTags("#download", "#foo")
            .page(ImmutablePage.of("1v", URI.create("MS-FOO-000-00003")))
            .build(),
        ImmutableTEIPage.builder()
            .identifier("3")
            .addTags("#downloadImageRights", "#bar")
            .page(ImmutablePage.of("2r", URI.create("MS-FOO-000-00004")))
            .build()
    );

    private static final String PAGES_FACSIMILE_XML = "" +
        "  <facsimile>" +
        "    <graphic decls=\"#document-thumbnail\"\n" +
        "             url=\"MS-FOO-000-00001\"/>" +
        "    <surface n=\"Cover\" xml:id=\"page-surface-cover\">\n" +
        "      <graphic decls=\"#downloadImageRights #abc\"\n" +
        "               url=\"MS-FOO-000-00001\"/>\n" +
        "    </surface>" +
        "    <surface n=\"1r\" xml:id=\"page-surface-1\">\n" +
        "      <graphic decls=\"#downloadImageRights #download\"\n" +
        "               url=\"MS-FOO-000-00002\"/>\n" +
        "    </surface>" +
        "    <surface n=\"1v\" xml:id=\"page-surface-2\">\n" +
        "      <graphic decls=\"#download #foo\"\n" +
        "               url=\"MS-FOO-000-00003\"/>\n" +
        "    </surface>" +
        "    <surface n=\"2r\" xml:id=\"page-surface-3\">\n" +
        "      <graphic decls=\"#downloadImageRights #bar\"\n" +
        "               url=\"MS-FOO-000-00004\"/>\n" +
        "    </surface>" +
        "  </facsimile>";
    private static final String PAGES_PB_XML = "" +
        "      <div>" +
        "        <pb xml:id=\"page-pb-cover\" n=\"Cover\" facs=\"#page-surface-cover\"/>" +
        "        <pb xml:id=\"page-pb-1\" n=\"1r\" facs=\"#page-surface-1\"/>" +
        "        <pb xml:id=\"page-pb-2\" n=\"1v\" facs=\"#page-surface-2\"/>" +
        "        <pb xml:id=\"page-pb-3\" n=\"2r\" facs=\"#page-surface-3\"/>" +
        "      </div>";

    private static final String TEI_EMPTY_WITH_PAGES = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        PAGES_FACSIMILE_XML +
        "  <text>" +
        "    <body>" +
        PAGES_PB_XML +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    @Test
    @DisplayName("Inserting no pages does not change XML")
    public void insertPages_inserting_no_pages() {
        var test = XML.parseString(TEI_EMPTY);
        inserter.insertPages(test, ImmutableList.of());

        var diff = DiffBuilder.compare(TEI_EMPTY).withTest(test).build();
        assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
    }

    @Test
    @DisplayName("Inserting pages into empty TEI doc")
    public void insertPages_empty_tei_doc() {
        var test = XML.parseString(TEI_EMPTY);
        inserter.insertPages(test, PAGES);

        var diff = DiffBuilder.compare(TEI_EMPTY_WITH_PAGES)
            .withTest(test)
            .ignoreWhitespace()
            .build();
        assertWithMessage(diff.toString()).that(diff.hasDifferences()).isFalse();
    }

    private static final String TEI_EXISTING_CONTENT_EXPLICIT = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "  <teiHeader n=\"foo\">" +
        "    <fileDesc>" +
        "      <titleStmt>" +
        "        <title>Example</title>" +
        "      </titleStmt>" +
        "    </fileDesc>" +
        "  </teiHeader>" +
        "  <facsimile xml:id=\"__generated_pagination_facsimile__\"/>" +
        "  <text>" +
        "    <body>" +
        "      <p>Hi</p>" +
        "      <div xml:id=\"__generated_pagination_pb_container__\"/>" +
        "      <p>Bye</p>" +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    private static final String TEI_EXISTING_CONTENT_EXPLICIT_WITH_PAGES = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "  <teiHeader n=\"foo\">" +
        "    <fileDesc>" +
        "      <titleStmt>" +
        "        <title>Example</title>" +
        "      </titleStmt>" +
        "    </fileDesc>" +
        "  </teiHeader>" +
        PAGES_FACSIMILE_XML +
        "  <text>" +
        "    <body>" +
        "      <p>Hi</p>" +
        PAGES_PB_XML +
        "      <p>Bye</p>" +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    @Test
    public void existingXMLContentIsPreservedWithExplicitInsertionPoints() {
        var test = XML.parseString(TEI_EXISTING_CONTENT_EXPLICIT);
        inserter.insertPages(test, PAGES);

        var diff = DiffBuilder.compare(TEI_EXISTING_CONTENT_EXPLICIT_WITH_PAGES)
            .withTest(test)
            .ignoreWhitespace()
            .build();
        assertWithMessage(getAllDifferences(diff)).that(diff.hasDifferences()).isFalse();
    }

    private static final String TEI_EXISTING_CONTENT_IMPLICIT = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "  <teiHeader n=\"foo\">" +
        "    <fileDesc>" +
        "      <titleStmt>" +
        "        <title>Example</title>" +
        "      </titleStmt>" +
        "    </fileDesc>" +
        "  </teiHeader>" +
        "  <facsimile n=\"some-existing-facsimile\"/>" +
        "  <div n=\"foo\"/>" +
        "  <text>" +
        "    <body>" +
        "      <p>Hi</p>" +
        "      <div n=\"foo\"/>" +
        "      <div n=\"bar\"/>" +
        "      <p>Bye</p>" +
        "      <div/>" +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    private static final String TEI_EXISTING_CONTENT_IMPLICIT_WITH_PAGES = "" +
        "<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">" +
        "  <teiHeader n=\"foo\">" +
        "    <fileDesc>" +
        "      <titleStmt>" +
        "        <title>Example</title>" +
        "      </titleStmt>" +
        "    </fileDesc>" +
        "  </teiHeader>" +
        "  <facsimile n=\"some-existing-facsimile\"/>" +
        "  <div n=\"foo\"/>" +
        PAGES_FACSIMILE_XML +
        "  <text>" +
        "    <body>" +
        "      <p>Hi</p>" +
        "      <div n=\"foo\"/>" +
        "      <div n=\"bar\"/>" +
        "      <p>Bye</p>" +
        "      <div/>" +
        PAGES_PB_XML +
        "    </body>" +
        "  </text>" +
        "</TEI>";

    @Test
    public void existingXMLContentIsPreservedWithImplicitInsertionPoints() {
        var test = XML.parseString(TEI_EXISTING_CONTENT_IMPLICIT);
        inserter.insertPages(test, PAGES);

        var diff = DiffBuilder.compare(TEI_EXISTING_CONTENT_IMPLICIT_WITH_PAGES)
            .withTest(test)
            .ignoreWhitespace()
            .build();
        assertWithMessage(getAllDifferences(diff)).that(diff.hasDifferences()).isFalse();
    }

    private static String getAllDifferences(Diff diff) {
        if(!diff.hasDifferences())
            return diff.toString();

        return Streams.stream(diff.getDifferences())
            .map(d -> d.getComparison().toString())
            .map(msg -> "- " + msg)
            .collect(Collectors.joining("\n"));
    }
}
