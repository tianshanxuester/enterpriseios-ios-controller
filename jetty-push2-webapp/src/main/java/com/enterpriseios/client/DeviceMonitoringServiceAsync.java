package com.enterpriseios.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DeviceMonitoringServiceAsync
{
   
    void updatePolicies(Device device, AsyncCallback<Void> async);

    void getDeviceById(String id, AsyncCallback<Device> async);
}
