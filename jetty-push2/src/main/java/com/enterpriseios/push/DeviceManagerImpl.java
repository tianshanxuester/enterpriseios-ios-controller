package com.enterpriseios.push;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeviceManagerImpl implements DeviceManager
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<String, Device> devices = new ConcurrentHashMap<String, Device>();
    private final Map<Command, CommandHandler> commandHandlers;
    private SessionDataDA dataAccess;

    public DeviceManagerImpl(Map<Command, CommandHandler> commandHandlers)
    {
        this.commandHandlers = commandHandlers;
    }

    public void init()
    {
        for (CommandHandler commandHandler : commandHandlers.values())
            commandHandler.setDeviceManager(this);
    }

    public Device getDevice(String id)
    {
        return devices.get(id);
    }

    public SessionData loadSessionData(String id)
    {
        return dataAccess.sessionData.get(id);
    }

    public void storeSessionData(SessionData data)
    {
        dataAccess.sessionData.put(data);
    }

    public Device createDevice(Request request)
    {
        String id=request.getDeviceId();
        Device device=getDevice(id);
        synchronized (this)
        {
            if (device==null)
            {
                SessionData data=loadSessionData(id);
                if(data!=null)
                {
                    device=new DeviceImpl(data);
                    logger.debug("Known Device: {} Policies: {}",device,device.getPolicyData());
                }
                else
                {   data=new SessionData(id,request.getDeviceType(),request.getUser());
                    device=new DeviceImpl(data);
                    storeSessionData(data);
                    logger.debug("New Device: {}",device);
                }
                devices.put(id,device);

            }
            return device;
        }
    }

    public Device removeDevice(String id,boolean store)
    {
        Device device=devices.remove(id);

        if(device!=null && store)
        {
            SessionData data=dataAccess.sessionData.get(id);
            if (data!=null)
            {
                data.setHeartbeat(device.getHeartBeat());
                data.setPolicyKey(device.getPolicyKey());
                data.setPolicies(device.getPolicyData());
                data.setLastUpdated(device.getLastUpdated());
                data.setVersion(device.getVersion());
            }
            else
                data=device.getSessionData();
            storeSessionData(data);
        }

        if(!store)
            dataAccess.sessionData.delete(id);
        return device;
    }

    public void handle(Request request) throws ProtocolException,IOException
    {
        Command command = request.getCommand();
        CommandHandler commandHandler = commandHandlers.get(command);
        commandHandler.process(request);
    }

    public void setDataAccess(SessionDataDA dataAccess)
    {
        this.dataAccess = dataAccess;
    }
}
