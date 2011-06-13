package com.enterpriseios.push.handler;

import com.enterpriseios.push.CommandHandler;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.ProtocolException;
import com.enterpriseios.push.Request;
import org.jdom.Document;
import org.jdom.Element;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class SendMailCommandHandler extends WBXMLResponseHandler implements CommandHandler
{

    private volatile DeviceManager deviceManager;

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void process(Request request) throws ProtocolException, IOException
    {
        Document document=request.getXML();

        Element sendMail=document.getRootElement();
        if (!sendMail.getName().equals("SendMail"))
        {
            HttpServletResponse response = request.getHttpServletResponse();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //This is a faux implementation! Dude, I don't support SendMail command sorry.

        sendMail.removeContent();
        //just return 130 which represents that the user is not allowed to perform the requested operation
        sendMail.addContent(new Element("Status").addContent("130"));
        HttpServletResponse response=request.getHttpServletResponse();
        sendWBXML(response,"ComposeMail",document);
    }
}
