package com.enterpriseios.push.web.wbxml;

import com.enterpriseios.push.web.wbxml.parser.WbxmlEncoder;
import com.enterpriseios.push.web.wbxml.parser.WbxmlExtensionHandler;
import com.enterpriseios.push.web.wbxml.parser.WbxmlParser;
import org.eclipse.jetty.util.IO;
import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;

public class WBXMLTools {
    private static final Logger logger = LoggerFactory.getLogger(WBXMLTools.class);
    /**
     * Transforms a wbxml byte array into the corresponding DOM representation
     *
     * @param wbxml
     * @return
     * @throws java.io.IOException
     */
    public static Document toXml(InputStream wbxml) throws IOException {

        WbxmlParser parser = new WbxmlParser();
        parser.setTagTable(0, TagsTables.CP_0); // AirSync
        parser.setTagTable(1, TagsTables.CP_1); // Contacts
        parser.setTagTable(2, TagsTables.CP_2); // Email
        parser.setTagTable(3, TagsTables.CP_3); // AirNotify
        parser.setTagTable(4, TagsTables.CP_4); // Calendar
        parser.setTagTable(5, TagsTables.CP_5); // Move
        parser.setTagTable(6, TagsTables.CP_6); // ItemEstimate
        parser.setTagTable(7, TagsTables.CP_7); // FolderHierarchy
        parser.setTagTable(8, TagsTables.CP_8); // MeetingResponse
        parser.setTagTable(9, TagsTables.CP_9); // Tasks
        parser.setTagTable(10, TagsTables.CP_10); // ResolveRecipients
        parser.setTagTable(11, TagsTables.CP_11); // ValidateCert
        parser.setTagTable(12, TagsTables.CP_12); // Contacts2
        parser.setTagTable(13, TagsTables.CP_13); // Ping
        parser.setTagTable(14, TagsTables.CP_14); // Provision
        parser.setTagTable(15, TagsTables.CP_15); // Search
        parser.setTagTable(16, TagsTables.CP_16); // GAL
        parser.setTagTable(17, TagsTables.CP_17); // AirSyncBase
        parser.setTagTable(18, TagsTables.CP_18); // Settings
        parser.setTagTable(19, TagsTables.CP_19); // DocumentLibrary
        parser.setTagTable(20, TagsTables.CP_20); // ItemOperations
        parser.setTagTable(21, TagsTables.CP_21); // ComposeMail
        parser.switchPage(0);
        try {
            parser.setWbxmlExtensionHandler(new WbxmlExtensionHandler(){

                public void ext_i(int id, String par) throws SAXException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public void ext_t(int id, int par) throws SAXException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

               
                public void ext(int id) throws SAXException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }


                public void opaque(byte[] data) throws SAXException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
            return parser.parse(wbxml);

        } catch (SAXException e) {
            storeWbxml(wbxml);
            logger.warn(e.getMessage(), e);
            throw new IOException(e.getMessage());
        }

    }

    private static void storeWbxml(InputStream wbxml) {
        try {
            File tmp = File.createTempFile("debug_", ".wbxml");
            FileOutputStream fout = new FileOutputStream(tmp);
            IO.copy(wbxml,fout);
            fout.close();
            logger.warn("unparsable wbxml saved in {}", tmp.getAbsolutePath());
        } catch (Throwable t) {
            logger.warn("error storing debug file {}", t);
        }
    }

    public static byte[] toWbxml(String defaultNamespace, Document doc)
            throws IOException {
        WbxmlEncoder encoder = new WbxmlEncoder(defaultNamespace);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            
            XMLOutputter outputter=new XMLOutputter();
            outputter.output(doc,out);
            InputSource is = new InputSource(new ByteArrayInputStream(out
                    .toByteArray()));
            out = new ByteArrayOutputStream();
            encoder.convert(is, out);
            byte[] ret = out.toByteArray();

            // storeWbxml(ret);
            // logger.info("reconverted version");
            // DOMUtils.logDom(toXml(ret));

            return ret;
        } catch (Exception e) {
            throw new IOException(e);
        }

    }

}
