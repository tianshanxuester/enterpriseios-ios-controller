package com.enterpriseios.push;

import java.io.IOException;

public interface CommandHandler
{
    public void setDeviceManager(DeviceManager deviceManager);

    public void process(Request request) throws ProtocolException,IOException;
}
