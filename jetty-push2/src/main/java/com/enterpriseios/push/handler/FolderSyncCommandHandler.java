package com.enterpriseios.push.handler;

import com.enterpriseios.push.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;


/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/10/25
 * Time: 12:34:59
 * To change this template use File | Settings | File Templates.
 */
public class FolderSyncCommandHandler extends WBXMLResponseHandler implements CommandHandler
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private volatile DeviceManager deviceManager;

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void process(Request request) throws ProtocolException,IOException
    {
        
        HttpServletResponse response = request.getHttpServletResponse();
        Device device=deviceManager.getDevice(request.getDeviceId());

        if (device!=null && !device.isExternallyChanged())
        {

            Document document=request.getXML();

            Element folderSync=document.getRootElement();
            if (!folderSync.getName().equals("FolderSync"))
            {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            device.setLastUpdated(new Date());
            // need to respond something... OK, will add an "inbox" to the client.
            // do we want to keep track of the SyncKey? No, because there is nothing to synchronized
            String syncKey = evaluate("/FolderSync/SyncKey", document);
            folderSync.removeContent(new ElementFilter("SyncKey"));
            folderSync.addContent(new Element("Status").addContent("1"));
            String newSyncKey = Integer.parseInt(syncKey) + 1 + "";
            folderSync.addContent(new Element("SyncKey").addContent(newSyncKey));
            Element changes=new Element("Changes");
            Element count=new Element("Count").addContent("1");
            Element add=new Element("Add");
            add.addContent(new Element("ServerId").addContent("1"));
            add.addContent(new Element("ParentId").addContent("0"));
            add.addContent(new Element("DisplayName","inbox"));
            add.addContent(new Element("Type").addContent("2"));
            changes.addContent(count);
            changes.addContent(add);
            folderSync.addContent(changes);
            sendWBXML(response,"FolderHierarchy",document);
        }
        else
            throw new ProtocolException("Device must be already present for FolderSync requests");
 
    }
}
