package com.enterpriseios.push;

import java.io.IOException;

public interface DeviceManager
{
    public static final String ATTRIBUTE = DeviceManager.class.getName() + ".ATTRIBUTE";

    public Device getDevice(String id);

    public Device createDevice(Request request);

    public Device removeDevice(String id, boolean store);

    public void handle(Request request) throws ProtocolException,IOException;

    public SessionData loadSessionData(String id);

    public void storeSessionData(SessionData data);

}
