package com.enterpriseios.push.web.wbxml.parser;

import org.jdom.Document;
import org.jdom.input.SAXHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class WbxmlParser {

    private InputStream in;

    private WbxmlExtensionHandler eh;

    private String[] attrStartTable;
    private String[] attrValueTable;
    private String[] tagTable;
    private Map<Integer, String[]> tagsTables;
    private Map<Integer, String[]> attrStarTables;
    private Map<Integer, String[]> attrValueTables;
    private char[] stringTable;

    private int publicIdentifierId;
    private Vector<String> stack = new Vector<String>();

    private static CharsetMappings charsetMappings=new CharsetMappings();
    private String docCharset;
    private static final Logger logger = LoggerFactory.getLogger(WbxmlParser.class);
    
    public WbxmlParser() {
        this.tagsTables = new HashMap<Integer, String[]>();
        this.attrStarTables = new HashMap<Integer, String[]>();
        this.attrValueTables = new HashMap<Integer, String[]>();
        switchPage(0);
    }

    /**
     * Sets the tag table for a given page. The first string in the array
     * defines tag 5, the second tag 6 etc. Currently, only page 0 is supported
     */
    public void setTagTable(int page, String[] tagTable) {
        tagsTables.put(page, tagTable);
    }

    /**
     * Sets the attribute start Table for a given page. The first string in the
     * array defines attribute 5, the second attribute 6 etc. Please use the
     * character '=' (without quote!) as delimiter between the attribute name
     * and the (start of the) value
     */
    public void setAttrStartTable(int page, String[] attrStartTable) {
        attrStarTables.put(page, attrStartTable);
    }

    /**
     * Sets the attribute value Table for a given page. The first string in the
     * array defines attribute value 0x85, the second attribute value 0x86 etc.
     */
    public void setAttrValueTable(int page, String[] attrStartTable) {
        attrValueTables.put(page, attrStartTable);
    }

    /**
     * registers a handler for Wap XML extensions
     */

    public void setWbxmlExtensionHandler(WbxmlExtensionHandler eh) {
        this.eh = eh;
    }

    public Document parse(InputStream in) throws SAXException, IOException {

        char entityBuf[] = new char[1];

        this.in = in;

        readByte(); // skip version
        publicIdentifierId = readInt();

        if (publicIdentifierId == 0)
            readInt();

        int charset = readInt();
        docCharset = charsetMappings.getCharset(charset);
        logger.info("document charset is {}",docCharset);

        int strTabSize = readInt();
        stringTable = new char[strTabSize];

        for (int i = 0; i < strTabSize; i++)
            stringTable[i] = (char) readByte();

        // ok, now the real thing....
        SAXHandler saxHandler=new SAXHandler();
        saxHandler.startDocument();

        while (true) {
            int id = in.read();
            if (id == -1)
                break;

            switch (id) {
                case Wbxml.SWITCH_PAGE:
                    int page = readByte();
                    switchPage(page);
                    break;

                case Wbxml.END:
                    saxHandler.endElement(null, null, stack.lastElement());
                    stack.setSize(stack.size() - 1);
                    break;

                case Wbxml.ENTITY:
                    entityBuf[0] = (char) readInt();
                    saxHandler.characters(entityBuf, 0, 1);
                    break;

                case Wbxml.STR_I: {
                    String s = readStrI();
                    saxHandler.characters(s.toCharArray(), 0, s.length());
                    break;
                }

                case Wbxml.EXT_I_0:
                case Wbxml.EXT_I_1:
                case Wbxml.EXT_I_2:
                case Wbxml.EXT_T_0:
                case Wbxml.EXT_T_1:
                case Wbxml.EXT_T_2:
                case Wbxml.EXT_0:
                case Wbxml.EXT_1:
                case Wbxml.EXT_2:
                case Wbxml.OPAQUE:
                    handleExtensions(id);
                    break;

                case Wbxml.PI:
                    throw new SAXException("PI curr. not supp.");
                    // readPI;
                    // break;

                case Wbxml.STR_T: {
                    int pos = readInt();
                    int end = pos;
                    while (stringTable[end] != 0)
                        end++;
                    saxHandler.characters(stringTable, pos, end - pos);
                    break;
                }

                default:
                    readElement(id,saxHandler);
            }
        }
        if (stack.size() != 0)
            throw new SAXException("unclosed elements: " + stack);

        saxHandler.endDocument();
        return saxHandler.getDocument();
    }

    public void switchPage(int page) {
        tagTable = tagsTables.get(page);
        attrStartTable = attrStarTables.get(page);
        attrValueTable = attrValueTables.get(page);
        logger.debug("switching to page 0x{}",page);
        if (tagTable == null) {
            logger.debug("tagsTable not found for page {}",page);
        }
    }

    // -------------- internal methods start here --------------------

    void handleExtensions(int id) throws SAXException, IOException {

        if (eh == null)
            throw new SAXException("No WapExtensionHandler registered!");

        switch (id) {
            case Wbxml.EXT_I_0:
            case Wbxml.EXT_I_1:
            case Wbxml.EXT_I_2:
                eh.ext_i(id - Wbxml.EXT_I_0, readStrI());
                break;

            case Wbxml.EXT_T_0:
            case Wbxml.EXT_T_1:
            case Wbxml.EXT_T_2:
                eh.ext_t(id - Wbxml.EXT_T_0, readInt());
                break;

            case Wbxml.EXT_0:
            case Wbxml.EXT_1:
            case Wbxml.EXT_2:
                eh.ext(id - Wbxml.EXT_0);
                break;

            case Wbxml.OPAQUE: {
                int len = readInt();
                byte[] buf = new byte[len];
                for (int i = 0; i < len; i++)
                    buf[i] = (byte) readByte();

                eh.opaque(buf);
            } // case OPAQUE
        } // SWITCH
    }

    public Attributes readAttr() throws SAXException, IOException {

        AttributesImpl result = new AttributesImpl();

        int id = readByte();

        while (id != 1) {

            String name = resolveId(attrStartTable, id);
            StringBuffer value;

            int cut = name.indexOf('=');

            if (cut == -1)
                value = new StringBuffer();
            else {
                value = new StringBuffer(name.substring(cut + 1));
                name = name.substring(0, cut);
            }

            id = readByte();
            while (id > 128 || id == Wbxml.ENTITY || id == Wbxml.STR_I
                    || id == Wbxml.STR_T
                    || (id >= Wbxml.EXT_I_0 && id <= Wbxml.EXT_I_2)
                    || (id >= Wbxml.EXT_T_0 && id <= Wbxml.EXT_T_2)) {

                switch (id) {
                    case Wbxml.ENTITY:
                        value.append((char) readInt());
                        break;

                    case Wbxml.STR_I:
                        value.append(readStrI());
                        break;

                    case Wbxml.EXT_I_0:
                    case Wbxml.EXT_I_1:
                    case Wbxml.EXT_I_2:
                    case Wbxml.EXT_T_0:
                    case Wbxml.EXT_T_1:
                    case Wbxml.EXT_T_2:
                    case Wbxml.EXT_0:
                    case Wbxml.EXT_1:
                    case Wbxml.EXT_2:
                    case Wbxml.OPAQUE:
                        handleExtensions(id);
                        break;

                    case Wbxml.STR_T:
                        value.append(readStrT());
                        break;

                    default:
                        value.append(resolveId(attrValueTable, id));
                }

                id = readByte();
            }

            result.addAttribute(null, null, name, null, value.toString());
        }

        return result;
    }

    private String resolveId(String[] tab, int id) throws SAXException,
            IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("resolve(0x{})", Integer.toHexString(id & 0x07f));
        }
        int idx = (id & 0x07f) - 5;
        if (idx == -1) {
            return readStrT();
        }
        if (idx < 0 || tab == null || idx >= tab.length || tab[idx] == null) {
            throw new SAXException("id " + id + " idx " + idx + " undef. tab: " + tab);
        }

        String ret = tab[idx];
        if (logger.isDebugEnabled()) {
            logger.debug("resolved as '{}'",ret);
        }

        return ret;
    }

    void readElement(int id,SAXHandler saxHandler) throws IOException, SAXException {
        String tag = resolveId(tagTable, id & 0x03f);

        // ok, now let's care about attrs etc

        saxHandler.startElement("", tag,"", ((id & 128) != 0) ? readAttr()
                : new AttributesImpl());

        if ((id & 64) != 0) {
            stack.addElement(tag);
        } else {
            saxHandler.endElement(null, null, tag);
        }
    }

    int readByte() throws IOException, SAXException {
        int i = in.read();
        if (i == -1)
            throw new SAXException("Unexpected EOF");
        return i;
    }

    int readInt() throws SAXException, IOException {
        int result = 0;
        int i;

        do {
            i = readByte();
            result = (result << 7) | (i & 0x7f);
        } while ((i & 0x80) != 0);

        return result;
    }

    String readStrI() throws IOException, SAXException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (true) {
            int i = in.read();
            if (i == -1)
                throw new SAXException("Unexpected EOF");
            if (i == 0)
                return new String(out.toByteArray(), docCharset);
            out.write(i);
        }
    }

    String readStrT() throws IOException, SAXException {
        int pos = readInt();
        int end = pos;

        while (stringTable[end] != 0)
            end++;

        return new String(stringTable, pos, end - pos);
	}
}
