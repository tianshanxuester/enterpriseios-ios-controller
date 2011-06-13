package com.enterpriseios.push.handler;

import com.enterpriseios.push.web.wbxml.WBXMLTools;
import org.eclipse.jetty.util.log.Log;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/10/25
 * Time: 11:39:47
 * To change this template use File | Settings | File Templates.
 */
public abstract class WBXMLResponseHandler
{

    protected void sendWBXML(HttpServletResponse httpResponse,String defaultNamespace, Document document) throws IOException {
        if (Log.isDebugEnabled()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            XMLOutputter outputter=new XMLOutputter(Format.getPrettyFormat());
            outputter.output(document, out);
            Log.debug("to device:\n" + out.toString());
        }

        byte[] content = WBXMLTools.toWbxml(defaultNamespace, document);
        httpResponse.setHeader("Server", "Microsoft-IIS/6.0");
        httpResponse.setHeader("MS-Server-ActiveSync", "8.1");
        httpResponse.setHeader("Cache-Control", "private");
        httpResponse.setContentType("application/vnd.ms-sync.wbxml");
        httpResponse.setContentLength(content.length);
        ServletOutputStream out = httpResponse.getOutputStream();
        out.write(content);
        out.flush();
    }

    protected String evaluate(String expression, Document xml)
    {
        try
        {
            XPath xpath=XPath.newInstance(expression);
            return xpath.valueOf(xml).trim();
        }
        catch(JDOMException e)
        {   e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected List selectNodes(String expression, Document xml)
    {
        try
        {
            XPath xpath=XPath.newInstance(expression);
            return xpath.selectNodes(xml);
        }
        catch(JDOMException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    protected Element selectSingleNode(String expression, Document xml)
    {
        try
        {
        XPath xpath=XPath.newInstance(expression);
        return (Element)xpath.selectSingleNode(xml);
        }
        catch(JDOMException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
