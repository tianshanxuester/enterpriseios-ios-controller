package com.enterpriseios.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.enterpriseios.client.policies.StaleObjectStateException;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/24
 * Time: 11:50:53
 * To change this template use File | Settings | File Templates.
 */
@RemoteServiceRelativePath("deviceMonitoringService")
public interface DeviceMonitoringService extends RemoteService
{
    Device getDeviceById(String id);
    void updatePolicies(Device device) throws StaleObjectStateException;
}
