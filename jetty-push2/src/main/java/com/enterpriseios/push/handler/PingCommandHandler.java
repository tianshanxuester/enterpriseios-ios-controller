package com.enterpriseios.push.handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletResponse;

import com.enterpriseios.push.Change;
import com.enterpriseios.push.CommandHandler;
import com.enterpriseios.push.Device;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.ProtocolException;
import com.enterpriseios.push.Request;
import org.jdom.Document;
import org.jdom.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PingCommandHandler extends WBXMLResponseHandler implements CommandHandler
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<String, Future<?>> expirations = new ConcurrentHashMap<String, Future<?>>();
    
    private final ScheduledExecutorService scheduler;
    private volatile DeviceManager deviceManager;
    private volatile long timeout = 120000;

    public PingCommandHandler(ScheduledExecutorService scheduler)
    {
        this.scheduler = scheduler;
    }

    public DeviceManager getDeviceManager()
    {
        return deviceManager;
    }

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public long getTimeout()
    {
        return timeout;
    }

    public void setTimeout(long timeout)
    {
        this.timeout = timeout;
    }

    public void process(Request request) throws ProtocolException,IOException
    {

        String deviceId = request.getDeviceId();

        unschedule(deviceId);

        Device device = deviceManager.getDevice(deviceId);
        if (device == null)
            throw new ProtocolException("Device must be already present for Ping requests");
        
        Document document=request.getXML();

        if (document!=null)
        {
            Element ping=document.getRootElement();
            if (!ping.getName().equals("Ping"))
            {
                HttpServletResponse response = request.getHttpServletResponse();
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            String heartbeatValue = evaluate("/Ping/HeartbeatInterval", document);
            if (heartbeatValue.length() > 0)
            {
                int heartbeat = Integer.parseInt(heartbeatValue);
                device.setHeartbeat(heartbeat);
            }
        }
        else if(device.getHeartBeat()<=0)
        {
            //An empty ping but no HeartbeatInterval saved
            logger.warn("Device with id {} issued empty Ping but no HeartbeatInterval saved",deviceId);

            Element ping = new Element("Ping").addContent(new Element("Status").addContent(Status.MISSING_REQUEST_PARAMS.asXmlValue()));
            document=new Document(ping);

            HttpServletResponse response = request.getHttpServletResponse();
            sendWBXML(response,"Ping",document);
            return;
        }

        flush(device, request);

        if (device.isClosed())
            deviceManager.removeDevice(deviceId,true);
    }

    private void flush(Device device, Request request) throws IOException
    {

        List<Change> changes = device.process(request, true);
        if (changes != null)
        {
            // Schedule before writing the changes, to avoid that the remote device
            // reconnects before we have scheduled the expiration timeout.
            schedule(device, timeout);

            // Write the changes
            HttpServletResponse response = request.getHttpServletResponse();
           
            if (changes.size()>0)
            {

                for (Change change:changes)
                {

                    if (change instanceof Change.FolderSyncRequired)
                    {
                        Element ping = new Element("Ping").addContent(new Element("Status").addContent(Status.FOLDER_SYNC_REQUIRED.asXmlValue()));
                        Document doc=new Document(ping);
                        sendWBXML(response,"Ping",doc);
                        break;
                    }
              
                }
            }
            else
            {
                Element ping = new Element("Ping").addContent(new Element("Status").addContent(Status.NO_CHANGES.asXmlValue()));
                Document doc=new Document(ping);
                sendWBXML(response,"Ping",doc);
            }
        }
    }

    private void schedule(Device device, long timeout)
    {
        Future<?> task = scheduler.schedule(new DeviceExpirationTask(device), timeout, TimeUnit.MILLISECONDS);
        Future<?> existing = expirations.put(device.getId(), task);
        assert existing == null;
    }

    private void unschedule(String deviceId)
    {
        Future<?> task = expirations.remove(deviceId);
        if (task != null)
            task.cancel(false);
    }

    private void deviceExpired(Device device, long time)
    {
        String deviceId = device.getId();
        logger.info("Device with id {} missing, last seen {} ms ago, closing it", deviceId, System.currentTimeMillis() - time);
        device.close();
        // If the device expired, means that it did not connect,
        // so there no request to resume, and we cleanup here
        unschedule(deviceId);
        deviceManager.removeDevice(deviceId,true);
    }

    private class DeviceExpirationTask implements Runnable
    {
        private final long time = System.currentTimeMillis();
        private final Device device;

        public DeviceExpirationTask(Device device)
        {
            this.device = device;
        }

        public void run()
        {
            deviceExpired(device, time);
        }
    }

    public enum Status {

        NO_CHANGES, // 1
        CHANGES_OCCURED, // 2
        MISSING_REQUEST_PARAMS, // 3
        SYNTAX_ERROR_IN_REQUEST, // 4
        INVALID_HEARTBEAT_INTERVAL, // 5
        TOO_MANY_FOLDERS, // 6
        FOLDER_SYNC_REQUIRED, // 7
        SERVER_ERROR; // 8

        public String asXmlValue() {
            switch (this) {
                case CHANGES_OCCURED:
                    return "2";
                case MISSING_REQUEST_PARAMS:
                    return "3";
                case SYNTAX_ERROR_IN_REQUEST:
                    return "4";
                case INVALID_HEARTBEAT_INTERVAL:
                    return "5";
                case TOO_MANY_FOLDERS:
                    return "6";
                case FOLDER_SYNC_REQUIRED:
                    return "7";
                case SERVER_ERROR:
                    return "8";

                case NO_CHANGES:
                default:
                    return "1";
            }
        }

    }
}
