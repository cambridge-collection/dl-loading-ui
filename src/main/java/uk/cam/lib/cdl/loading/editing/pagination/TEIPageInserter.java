package uk.cam.lib.cdl.loading.editing.pagination;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.cam.lib.cdl.loading.utils.XML;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static uk.cam.lib.cdl.loading.utils.XML.elementIsNamed;

public class TEIPageInserter {
    private static final String XMLNS_TEI = "http://www.tei-c.org/ns/1.0";
    public static final String GENERATED_PAGINATION_FACSIMILE_ID = "__generated_pagination_facsimile__";
    public static final String GENERATED_PAGINATION_PB_CONTAINER_ID = "__generated_pagination_pb_container__";

    public void insertPages(Document tei, List<TEIPage> teiPages) {
        Preconditions.checkNotNull(tei, "tei cannot be null");
        Preconditions.checkNotNull(teiPages, "teiPages cannot be null");
        validateTEIDoc(tei);
        if(teiPages.isEmpty())
            return;

        insertFacsimile(tei, teiPages);
        insertPageBreaks(tei, teiPages);
    }

    private void insertFacsimile(Document tei, List<TEIPage> pages) {
        Preconditions.checkNotNull(tei);
        Preconditions.checkNotNull(pages);
        Preconditions.checkArgument(!pages.isEmpty());
        var teiEl = tei.getDocumentElement();
        assert elementIsNamed(XMLNS_TEI, "TEI", teiEl);
        var facsimileEl = getOrCreateFacsimileInsertionPoint(tei);

        // A single thumbnail is created from the first page
        var thumbnailGraphicEl = XML.appendChild(facsimileEl, XMLNS_TEI, "graphic");
        // The original code set rend="portrait" but I don't think it's used, and we don't have access to the
        // actual aspect ratio of the image to know what it should be.
        thumbnailGraphicEl.setAttribute("decls", "#document-thumbnail");
        thumbnailGraphicEl.setAttribute("url", pages.get(0).page().image().toString());

        pages.stream().map(page -> createSurface(tei, page))
            .forEach(facsimileEl::appendChild);
    }

    private Element getOrCreateFacsimileInsertionPoint(Document doc) {
        return getPlaceholderFacsimileEl(doc)
            .orElseGet(() -> createFacsimileEl(doc));
    }

    private final Supplier<XPathExpression> GENERATED_PAGINATION_FACSIMILE_EXPR = () ->
        XML.compileXPath(String.format("//tei:facsimile[@xml:id='%s']", GENERATED_PAGINATION_FACSIMILE_ID),
            ImmutableMap.of("tei", XMLNS_TEI));

    /**
     * Get a facsimile element explicitly marked with the xml:id
     * {@link #GENERATED_PAGINATION_FACSIMILE_ID} if it exists.
     */
    private Optional<Element> getPlaceholderFacsimileEl(Document doc) {
        try {
            return Optional.ofNullable((Element)(GENERATED_PAGINATION_FACSIMILE_EXPR.get().evaluate(doc, XPathConstants.NODE)))
                .map(TEIPageInserter::removeXmlIdAttribute);
        }
        catch (XPathExpressionException e) {
            throw new PaginationException("Failed to execute XPath expression on doc", e);
        }
    }

    /**
     * Insert and return a facsimile element to hold page data.
     *
     * <p>The element is created just before the {@code <text>} element
     * (if one exists), otherwise it'll be the last child of the root
     * {@code <TEI>} element.
     */
    private Element createFacsimileEl(Document tei) {
        var teiEl = tei.getDocumentElement();
        assert elementIsNamed(XMLNS_TEI, "TEI", teiEl);

        var facsimileEl = tei.createElementNS(XMLNS_TEI, "facsimile");

        // We insert prior to any text node if there is one
        XML.streamChildNodes(teiEl)
            .filter(Element.class::isInstance)
            .map(Element.class::cast)
            .filter(elementIsNamed(XMLNS_TEI, "text"))
            .findFirst()
            .ifPresentOrElse(
                textEl -> teiEl.insertBefore(facsimileEl, textEl),
                () -> teiEl.appendChild(facsimileEl));

        return facsimileEl;
    }

    private Element createSurface(Document doc, TEIPage page) {
        var surfaceEl = doc.createElementNS(XMLNS_TEI, "surface");
        surfaceEl.setAttribute("n", page.page().label());
        surfaceEl.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", surfaceId(page));
        var graphicEl = XML.appendChild(surfaceEl, XMLNS_TEI, "graphic");

        String jp2_id = page.page().image().toString();

        graphicEl.setAttribute("decls", String.join(" ", page.tags()));
        graphicEl.setAttribute("url", jp2_id);

        // Here we query the image server and set rend, height and width.
        // Note if the image changes this will be inaccurate.
        try {
            IIIFImageInfo info = IIIFImageQuerierFactory.get().getImageInfo(jp2_id);

            graphicEl.setAttribute("height", info.getHeight()+"px");
            graphicEl.setAttribute("width", info.getWidth()+"px");
            if (info.getHeight()> info.getWidth()) {
                graphicEl.setAttribute("rend", "portrait");
            } else {
                graphicEl.setAttribute("rend", "landscape");
            }
        } catch (IOException e) {
            System.err.println("Unable to get image height and width.");
            e.printStackTrace();
        }

        return surfaceEl;
    }

    private void insertPageBreaks(Document tei, List<TEIPage> pages) {
        var pbContainerEl = getOrCreatePagebreakInsertionPoint(tei);
        generatePageBreaks(pbContainerEl, pages);
    }

    private Element getOrCreatePagebreakInsertionPoint(Document tei) {
        return getPlaceholderPagebreakContainerEl(tei)
            .orElseGet(() -> createPagebreakContainerEl(tei));
    }

    private Element createPagebreakContainerEl(Document tei) {
        var teiEl = tei.getDocumentElement();
        assert elementIsNamed(XMLNS_TEI, "TEI", teiEl);
        var textEl = XML.getOrAppendChild(teiEl, XMLNS_TEI, "text");
        var bodyEl = XML.getOrAppendChild(textEl, XMLNS_TEI, "body");
        return XML.appendChild(bodyEl, XMLNS_TEI, "div");
    }

    private final Supplier<XPathExpression> GENERATED_PAGINATION_PB_CONTAINER_EXPR = () ->
        XML.compileXPath(String.format("//tei:*[@xml:id='%s']", GENERATED_PAGINATION_PB_CONTAINER_ID),
            ImmutableMap.of("tei", XMLNS_TEI));

    /**
     * Get a facsimile element explicitly marked with the xml:id
     * {@link #GENERATED_PAGINATION_FACSIMILE_ID} if it exists.
     */
    private Optional<Element> getPlaceholderPagebreakContainerEl(Document doc) {
        try {
            return Optional.ofNullable((Element)(GENERATED_PAGINATION_PB_CONTAINER_EXPR.get().evaluate(doc, XPathConstants.NODE)))
                .map(TEIPageInserter::removeXmlIdAttribute);
        }
        catch (XPathExpressionException e) {
            throw new PaginationException("Failed to execute XPath expression on doc", e);
        }
    }

    private static Element removeXmlIdAttribute(Element el) {
        el.removeAttributeNS(XMLConstants.XML_NS_URI, "id");
        return el;
    }

    private void generatePageBreaks(Element parent, List<TEIPage> pages) {
        pages.stream().map(page -> createPageBreak(parent.getOwnerDocument(), page))
            .forEach(parent::appendChild);
    }

    private static Element createPageBreak(Document doc, TEIPage page) {
        var pbEl = doc.createElementNS(XMLNS_TEI, "pb");
        pbEl.setAttribute("n", page.page().label());
        pbEl.setAttribute("facs", "#" + surfaceId(page));
        pbEl.setAttributeNS(XMLConstants.XML_NS_URI, "xml:id", pageBreakId(page));
        return pbEl;
    }

    private static String surfaceId(TEIPage page) {
        return String.format("page-surface-%s", page.identifier());
    }

    private static String pageBreakId(TEIPage page) {
        return String.format("page-pb-%s", page.identifier());
    }

    /**
     * Check if an XML document is in a structure that we can insert pages into.
     */
    private void validateTEIDoc(Document tei) {
        var root = tei.getDocumentElement();

        if(!elementIsNamed(XMLNS_TEI, "TEI", root))
            throw new UserInputPaginationException(String.format(
                "Unable to insert pages into XML: Root element must be {%s}TEI but was %s",
                XMLNS_TEI, XML.getClarkName(root)));
    }
}
