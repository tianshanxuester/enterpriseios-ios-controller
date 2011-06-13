/*
 * LEGAL NOTICE
 *
 *  Copyright (C) ${year} InventIt Inc. All rights reserved.
 *
 *  This source code, product and/or document is protected under licenses
 *  restricting its use, copying, distribution, and decompilation.
 *  No part of this source code, product or document may be reproduced in
 *  any form by any means without prior written authorization of InventIt Inc.
 *  and its licensors, if any.
 *
 *  InventIt Inc.
 *  9F Kojimachi DUPLEX B's
 *  4-4-7 Kojimachi, Chiyoda-ku, Tokyo 102-0083
 *  JAPAN
 *  http://www.yourinventit.com/
 */

package com.enterpriseios.push.web.wbxml.parser;

import org.eclipse.jetty.util.log.Log;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/07/12
 * Time: 10:07:33
 * To change this template use File | Settings | File Templates.
 */
public class WbxmlEncoder {

    private SAXParser parser;
    private Map<String, Integer> stringTable;
    private ByteArrayOutputStream buf;
    private String defaultNamespace;

    /**
     * The constructor creates an internal document handler. The given parser is
     * used
     */
    public WbxmlEncoder(String defaultNamespace) {
        this.defaultNamespace = defaultNamespace;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            parser = factory.newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    void setStringTable(Map<String, Integer> table) {
        this.stringTable = table;
    }

    /**
     * converts the XML data from the given SAX InputSource and writes the
     * result to the given OutputStream
     */
    public void convert(InputSource in, OutputStream out) throws SAXException,
            IOException {

        buf = new ByteArrayOutputStream();

        // perform conv.
        parser.parse(in, new EncoderHandler(this, buf, defaultNamespace));

        // ok, write header

        out.write(0x03); // version
        out.write(0x01); // unknown or missing public identifier
        out.write(0x6a); // UTF-8
        out.write(0x00); // no string table

        // write buf

        buf.writeTo(out);

        // ready!

        out.flush();
    }

    // internal methods

    void writeInt(OutputStream out, int i) throws IOException {
        byte[] buf = new byte[5];
        int idx = 0;

        do {
            buf[idx++] = (byte) (i & 0x7f);
            i = i >> 7;
        } while (i != 0);

        while (idx > 1) {
            out.write(buf[--idx] | 0x80);
        }
        out.write(buf[0]);
    }

    void writeStrI(OutputStream out, String s) throws IOException {
        out.write(Wbxml.STR_I);
        out.write(s.getBytes());
        out.write(0);
    }

    @SuppressWarnings("unused")
    private void writeStrT(String s) throws IOException {

        Integer idx = stringTable.get(s);

        if (idx == null) {
            throw new IOException("unknown elem in mapping table: " + s);
        }

        writeInt(buf, idx.intValue());
    }

    public void switchPage(Integer integer) throws IOException {
        if (Log.isDebugEnabled()) {
            Log.debug("switching to page 0x" + Integer.toHexString(integer));
        }
        writeInt(buf, 0x00);
        writeInt(buf, integer);
    }

    public void writeElement(String name) throws IOException {
        Integer mapping = stringTable.get(name);
        if (mapping == null) {
            Log.warn("no mapping for '" + name + "'");
            throw new IOException("no mapping for '" + name + "'");
        }
        writeInt(buf, stringTable.get(name) + 64);
    }

    public void writeEmptyElement(String name) throws IOException {
        if (Log.isDebugEnabled()) {
            Log.debug("write empty tag " + name);
        }
        Integer mapping = stringTable.get(name);
        if (mapping == null) {
            Log.warn("no mapping for '" + name + "'");
            throw new IOException("no mapping for '" + name + "'");
        }
        writeInt(buf, stringTable.get(name));
    }
}

