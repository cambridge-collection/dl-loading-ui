package uk.cam.lib.cdl.loading.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

public class XMLTest {
    @Test
    public void parseString() {
        var doc = XML.parseString("<root xmlns=\"foo\"/>");
        var root = doc.getDocumentElement();
        assertThat(root.getTagName()).isEqualTo("root");
        assertThat(root.getNamespaceURI()).isEqualTo("foo");
    }

    @ParameterizedTest
    @CsvSource({
        "<foo/>,foo",
        "<x:foo xmlns:x=\"ns\"/>,foo",
        "<foo xmlns=\"ns\"/>,foo"
    })
    public void getLocalName(String elementXml, String localName) {
        var el = XML.parseString(elementXml).getDocumentElement();
        assertThat(XML.getLocalName(el)).isEqualTo(localName);
    }

    @ParameterizedTest
    @CsvSource({
        "<foo/>,foo",
        "<x:foo xmlns:x=\"ns\"/>,{ns}foo",
        "<foo xmlns=\"ns\"/>,{ns}foo"
    })
    public void getClarkName(String elementXml, String clarkName) {
        var el = XML.parseString(elementXml).getDocumentElement();
        assertThat(XML.getClarkName(el)).isEqualTo(clarkName);
    }

    @ParameterizedTest
    @CsvSource({
        "<foo/>,,foo,true",
        "<foo/>,,bar,false",
        "<foo xmlns=\"abc\"/>,abc,foo,true",
        "<bar xmlns=\"abc\"/>,abc,foo,false",
        "<foo xmlns=\"def\"/>,abc,foo,false",
        "<x:foo xmlns:x=\"abc\"/>,abc,foo,true",
        "<x:bar xmlns:x=\"abc\"/>,abc,foo,false",
        "<x:foo xmlns:x=\"def\"/>,abc,foo,false",
    })
    public void elementIsNamed(String xml, String ns, String name, boolean isTrue) {
        var el = XML.parseString(xml).getDocumentElement();
        assertThat(XML.elementIsNamed(ns, name, el)).isEqualTo(isTrue);
        assertThat(XML.elementIsNamed(ns, name).test(el)).isEqualTo(isTrue);
    }

    @ParameterizedTest
    @CsvSource({
        "<a></a>,",
        "<a foo=\"a\">_<!--sdf-->_</a>,#text(_)|#comment(sdf)|#text(_)",
        "<a foo=\"a\">_<x/><x/>x<y/>_<!--sdf--></a>,#text(_)|x(null)|x(null)|#text(x)|y(null)|#text(_)|#comment(sdf)",
    })
    public void streamChildNodes(String xml, String expected) {
        var el = XML.parseString(xml).getDocumentElement();
        assertThat(
            XML.streamChildNodes(el)
                .map(n -> String.format("%s(%s)", n.getNodeName(), n.getNodeValue()))
                .collect(Collectors.joining("|")))
            .isEqualTo(expected == null ? "" : expected);
    }

    @Test
    public void appendChild() {
        var doc = XML.parseString("<a><b/>...</a>");
        XML.appendChild(doc.getDocumentElement(), "abc", "x:foo");
        Diff diff = DiffBuilder.compare("<a><b/>...<x:foo xmlns:x=\"abc\"/></a>")
            .withTest(doc)
            .build();

        assertWithMessage("%s", diff).that(diff.hasDifferences()).isFalse();
    }

    @Test
    public void compileXPath() throws XPathExpressionException {
        var expr = XML.compileXPath("//*/@info:msg", ImmutableMap.of("info", "example"));
        var el = XML.parseString("<foo xmlns:i=\"example\"><bar i:msg=\"hi\"/></foo>").getDocumentElement();
        assertThat(expr.evaluate(el, XPathConstants.STRING)).isEqualTo("hi");
    }

    @Test
    public void deepCopyDocument() {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<?foo a=\"123\"?>" +
            "<el> <hi a=\"1\"/> </el>";
        var doc = XML.parseString(xml);
        var clone = XML.deepCopyDocument(doc);

        Truth.assertThat(XML.serialise(doc)).isEqualTo(xml);
        Truth.assertThat(XML.serialise(clone)).isEqualTo(xml);

        var el = (Element)doc.getDocumentElement();
        el.setAttribute("foo", "bar");

        var modifiedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<?foo a=\"123\"?>" +
            "<el foo=\"bar\"> <hi a=\"1\"/> </el>";

        Truth.assertThat(XML.serialise(doc)).isEqualTo(modifiedXml);
        Truth.assertThat(XML.serialise(clone)).isEqualTo(xml);
    }
}
