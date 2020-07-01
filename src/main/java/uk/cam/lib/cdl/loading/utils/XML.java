package uk.cam.lib.cdl.loading.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSource;
import org.apache.commons.io.input.ReaderInputStream;
import org.springframework.lang.Nullable;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class XML {
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        var dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf;
    }

    public static Document parseString(String xml) {
        try {
            return parse(CharSource.wrap(xml).asByteSource(Charsets.UTF_8));
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (SAXException e) {
            throw new IllegalArgumentException("Failed to parse xml string", e);
        }
    }

    public static Document parse(ByteSource byteSource) throws IOException, SAXException {
        try {
            var doc = getDocumentBuilderFactory().newDocumentBuilder()
                .parse(byteSource.openBufferedStream());
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialise a Document as a String, specifying UTF-8 encoding.
     */
    public static String serialise(Document doc) {
        try {
            var transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, UTF_8.name());
            var sw = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException e) {
            throw new IllegalArgumentException(
                "Document could not be serialised as a String: " + e.getMessageAndLocation(), e);
        }
    }

    public static String getClarkName(Element element) {
        var nsURI = element.getNamespaceURI();
        var localName = getLocalName(element);
        assert !localName.contains(":");
        return nsURI == null ? localName : "{" + nsURI + "}" + localName;
    }

    public static String getLocalName(Element element) {
        var localName = element.getLocalName();
        return localName != null ? localName : element.getTagName();
    }

    public static String getLocalName(String qualifiedName) {
        var separatorPosition = qualifiedName.indexOf(':');
        return separatorPosition < 0 ? qualifiedName : qualifiedName.substring(separatorPosition + 1);
    }

    public static boolean elementIsNamed(@Nullable String namespaceURI, String localName, Element element) {
        Preconditions.checkNotNull(localName, "localName is null");
        Preconditions.checkArgument(!localName.contains(":"), "localName is not an XML name");
        Preconditions.checkNotNull(element, "element is null");
        return localName.equals(getLocalName(element)) && Objects.equals(namespaceURI, element.getNamespaceURI());
    }

    public static Predicate<Element> elementIsNamed(@Nullable String namespaceURI, String qname) {
        return el -> elementIsNamed(namespaceURI, qname, el);
    }

    public static Stream<Node> streamChildNodes(Node n) {
        final var children = n.getChildNodes();
        final var position = new AtomicInteger(0);
        final var size = children.getLength();
        return Stream.generate(() -> {
            if(position.get() >= size)
                throw new AssertionError("generator called too many times");
            var i = position.getAndAdd(1);
            return i < size ? children.item(i) : null;
        }).limit(size);
    }

    public static Element appendChild(Element parent, @Nullable String namespaceURI, String qname) {
        var child = parent.getOwnerDocument().createElementNS(namespaceURI, qname);
        parent.appendChild(child);
        return child;
    }

    public static Element getOrAppendChild(Element parent, @Nullable String namespaceURI, String qname) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(qname);
        return streamChildNodes(parent)
            .filter(Element.class::isInstance).map(Element.class::cast)
            .filter(elementIsNamed(namespaceURI, qname))
            .findFirst()
            .orElseGet(() -> appendChild(parent, namespaceURI, qname));
    }

    public static XPathExpression compileXPath(String expression, Map<String, String> namespaces) {
        try {
            var xpath = XPathFactory.newInstance().newXPath();
            var nsContext = new SimpleNamespaceContext();
            nsContext.setBindings(namespaces);
            xpath.setNamespaceContext(nsContext);
            return xpath.compile(expression);
        }
        catch (XPathExpressionException e) {
            throw new IllegalArgumentException(String.format("invalid xpath expression: '%s'", expression), e);
        }
    }

    public static Document deepCopyDocument(Document doc) {
        Document clone;
        try {
            clone = getDocumentBuilderFactory().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to create DocumentBuilder: " + e.getMessage(), e);
        }

        clone.setDocumentURI(doc.getDocumentURI());
        clone.setStrictErrorChecking(doc.getStrictErrorChecking());
        clone.setXmlStandalone(doc.getXmlStandalone());
        clone.setXmlVersion(doc.getXmlVersion());

        streamChildNodes(doc).forEach(node -> clone.appendChild(clone.importNode(node, true)));
        return clone;
    }
}
