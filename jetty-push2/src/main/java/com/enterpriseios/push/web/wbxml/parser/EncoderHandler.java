package com.enterpriseios.push.web.wbxml.parser;

import com.enterpriseios.push.web.wbxml.TagsTables;
import org.eclipse.jetty.util.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/07/12
 * Time: 10:06:04
 * To change this template use File | Settings | File Templates.
 */
class EncoderHandler extends DefaultHandler {
    private WbxmlEncoder we;
    private ByteArrayOutputStream buf;
    private String defaultNamespace;
    private String currentXmlns;
    private StringBuilder currentCharacter;

    private Stack<String> stackedStarts;

    public EncoderHandler(WbxmlEncoder we, ByteArrayOutputStream buf,
                          String defaultNamespace) throws IOException {
        this.stackedStarts = new Stack<String>();
        this.defaultNamespace = defaultNamespace;
        this.we = we;
        this.buf = buf;
        try {
            switchToNs(defaultNamespace);
        } catch (SAXException e) {
        }
        currentXmlns = defaultNamespace;
    }

    public void startElement(String uri, String localName, String qName,
                             Attributes attr) throws SAXException {

        if (!stackedStarts.isEmpty()) {
            flushNormal();
        }
        flushCharacter();
        try {
            String newNs = null;
            if (!qName.contains(":")) {
                newNs = defaultNamespace;
            } else {
                newNs = qName.substring(0, qName.indexOf(":"));
                qName = qName.substring(qName.indexOf(":") + 1);
            }

            if (!newNs.equals(currentXmlns)) {
                switchToNs(newNs);
            }
            currentXmlns = newNs;

//			we.writeElement(qName);
            queueStart(qName);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    private void queueStart(String qName) {
        stackedStarts.add(qName);
    }

    private void flushNormal() throws SAXException {
        String e = stackedStarts.pop();
        try {
            we.writeElement(e);
        } catch (IOException e1) {
            throw new SAXException(e);
        }
    }

    private void flushEmptyElem() throws SAXException {
        String e = stackedStarts.pop();
        try {
            we.writeEmptyElement(e);
        } catch (IOException e1) {
            throw new SAXException(e);
        }
    }

    private void switchToNs(String newNs) throws IOException, SAXException {
        if (!stackedStarts.isEmpty()) {
            flushNormal();
        }
        Map<String, Integer> table = TagsTables.getElementMappings(newNs);
        we.setStringTable(table);
        we.switchPage(TagsTables.NAMESPACES_IDS.get(newNs));
    }

    public void characters(char[] chars, int start, int len)
            throws SAXException {
        if (!stackedStarts.isEmpty()) {
            flushNormal();
        }
        String s = new String(chars, start, len);
        appendCharacter(s);
//		s = s.trim();
//		if (s.length() == 0) {
//			return;
//		}
//		try {
//			buf.write(Wbxml.STR_I);
//			we.writeStrI(buf, new String(chars, start, len));
//		} catch (IOException e) {
//			throw new SAXException(e);
//		}
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (!stackedStarts.isEmpty()) {
            flushEmptyElem();
        } else {
            flushCharacter();
            buf.write(Wbxml.END);
        }
    }

    private void appendCharacter(String characters) {

        if (this.currentCharacter == null) {
            if (characters.trim().length() == 0) {
                return;
            }
            this.currentCharacter = new StringBuilder();
        }
        currentCharacter.append(characters);
    }

    private void flushCharacter() {
        if (this.currentCharacter != null) {
            try {
                we.writeStrI(buf, currentCharacter.toString());
                currentCharacter = null;
            } catch (IOException e) {
                Log.warn(e.getMessage(), e);
            }
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (!stackedStarts.isEmpty()) {
            flushNormal();
        }
        System.err.println("ignorable whitespace");
    }

}
