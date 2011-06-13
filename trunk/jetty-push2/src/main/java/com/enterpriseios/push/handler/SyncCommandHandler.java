package com.enterpriseios.push.handler;

import com.enterpriseios.push.CommandHandler;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.ProtocolException;
import com.enterpriseios.push.Request;
import org.jdom.Document;
import org.jdom.Element;

import javax.servlet.http.HttpServletResponse;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/10/25
 * Time: 13:02:17
 * To change this template use File | Settings | File Templates.
 */
public class SyncCommandHandler extends WBXMLResponseHandler implements CommandHandler
{
    private final XPathFactory xpathFactory = XPathFactory.newInstance();

    private volatile DeviceManager deviceManager;

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void process(Request request) throws ProtocolException, IOException
    {
        HttpServletResponse response = request.getHttpServletResponse();
        Element sync = new Element("Sync");
        //TODO test
        if (deviceManager.getDevice(request.getDeviceId())!=null)
        {
            Element collections = new Element("Collections");
            Element collection = new Element("Collection");

            String cls = evaluate("/Sync/Collections/Collection/Class", request.getXML());
            collection.addContent(new Element("Class").addContent(cls));

            String syncKey = evaluate("/Sync/Collections/Collection/SyncKey", request.getXML());
            collection.addContent(new Element("SyncKey").addContent(Integer.parseInt(syncKey) + 1 + ""));

            String collectionId = evaluate("/Sync/Collections/Collection/CollectionId", request.getXML());
            collection.addContent(new Element("CollectionId").addContent(collectionId));
            collection.addContent(new Element("Status").addContent("1"));
            collections.addContent(collection);
            sync.addContent(collections);
            sendWBXML(response,"AirSync",new Document(sync));
        }
        else
        {
            response.sendError(449);
        }

    }

}
