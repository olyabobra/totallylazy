package com.googlecode.totallylazy.xml;

import com.googlecode.totallylazy.Sequence;
import com.googlecode.totallylazy.Xml;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.StringReader;

import static com.googlecode.totallylazy.Assert.assertThat;
import static com.googlecode.totallylazy.Predicates.is;
import static com.googlecode.totallylazy.xml.StreamingXPath.child;
import static com.googlecode.totallylazy.xml.StreamingXPath.descendant;
import static com.googlecode.totallylazy.xml.StreamingXPath.name;
import static com.googlecode.totallylazy.xml.StreamingXPath.text;

public class ContextTest {
    @Test
    public void shouldSupportChild() throws Exception {
        String xml = "<stream><user><first>Dan &amp; Bod</first><dob>1977</dob></user><user><first>Jason</first><dob>1978</dob></user></stream>";
        Document document = Xml.document(xml);

        Sequence<Node> nodes = Xml.selectNodes(document, "child::*");
        Sequence<Context> locations = XmlReader.locations(new StringReader(xml)).filter(child(name("*")));
        assertThat(locations.size(), is(nodes.size()));

        Node root = nodes.head();
        Context rootContext = locations.head();
        assertThat(rootContext.name(), is(root.getNodeName()));
    }

    @Test
    public void shouldSupportDescendant() throws Exception {
        String xml = "<stream><user><first>Dan &amp; Bod</first><dob>1977</dob></user><user><first>Jason</first><dob>1978</dob></user></stream>";
        Document document = Xml.document(xml);

        Sequence<Node> nodes = Xml.selectNodes(document, "descendant::*");

        Sequence<Context> locations = XmlReader.locations(new StringReader(xml)).filter(descendant(name("*")));
        assertThat(locations.size(), is(nodes.size()));

        Node root = nodes.head();
        Context rootContext = locations.head();
        assertThat(rootContext.name(), is(root.getNodeName()));
    }

    @Test
    public void shouldSupportDescendantWithNames() throws Exception {
        String xml = "<stream><user><first>Dan &amp; Bod</first><dob>1977</dob></user><user><first>Jason</first><dob>1978</dob></user></stream>";
        Document document = Xml.document(xml);

        Sequence<Node> nodes = Xml.selectNodes(document, "descendant::user");

        Sequence<Context> locations = XmlReader.locations(new StringReader(xml)).filter(descendant(name("user")));
        assertThat(locations.size(), is(nodes.size()));

        Node root = nodes.head();
        Context rootContext = locations.head();
        assertThat(rootContext.name(), is(root.getNodeName()));
    }

    @Test
    public void shouldSupportText() throws Exception {
        String xml = "<stream><user><first>Dan &amp; Bod</first><dob>1977</dob></user><user><first>Jason</first><dob>1978</dob></user></stream>";
        Document document = Xml.document(xml);

        Sequence<Node> nodes = Xml.selectNodes(document, "descendant::text()");

        Sequence<Context> locations = XmlReader.locations(new StringReader(xml)).filter(descendant(text()));
        assertThat(locations.size(), is(nodes.size()));

        Node root = nodes.head();
        Context rootContext = locations.head();
        assertThat(rootContext.text(), is(root.getTextContent()));
    }
}