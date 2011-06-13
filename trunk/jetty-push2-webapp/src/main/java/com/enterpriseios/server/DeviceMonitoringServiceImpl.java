package com.enterpriseios.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.enterpriseios.client.Device;
import com.enterpriseios.client.DeviceMonitoringService;
import com.enterpriseios.client.DeviceNotFoundException;
import com.enterpriseios.client.policies.StaleObjectStateException;
import com.enterpriseios.push.Change;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.SessionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/24
 * Time: 13:25:50
 * To change this template use File | Settings | File Templates.
 */
public class DeviceMonitoringServiceImpl extends RemoteServiceServlet implements DeviceMonitoringService
{

    private static final Logger logger = LoggerFactory.getLogger(DeviceMonitoringServiceImpl.class);

	private volatile DeviceManager deviceManager;

	@Override
	public void init() throws ServletException
    {
		deviceManager = (DeviceManager) getServletContext().getAttribute(
				DeviceManager.ATTRIBUTE);
	}

    public Device getDeviceById(String deviceId)
    {
        //If found in the deviceManager then, the device is online, if not obtain its persisted record.
        com.enterpriseios.push.Device managed=deviceManager.getDevice(deviceId);

        Device device;
        if (managed!=null)
        {
            SessionData data=managed.getSessionData();
            device=new Device(data.getDeviceId(),data.getDeviceType(),data.getVersion(),
                    data.getPolicies(),data.getLastUpdated());
            device.setOnline(managed.isOnline());
        }
        else
        {
            SessionData data=deviceManager.loadSessionData(deviceId);
            if (data!=null)
            {
                device=new Device(data.getDeviceId(),data.getDeviceType(),data.getVersion(),
                        data.getPolicies(),data.getLastUpdated());
                device.setOnline(false);
            }
            else
                throw new DeviceNotFoundException();

        }
        logger.debug("Device:{}",device);
        return device;
    }

    public void updatePolicies(Device device) throws StaleObjectStateException
    {
        String deviceId=device.getId();
        Map<String,String> policies=device.getPolicies();
        logger.debug("Setting Security Policy:{} on device with ID:{}",policies,deviceId);
        com.enterpriseios.push.Device managed = deviceManager.getDevice(device.getId());

		if (managed != null)
        {

            if(managed.getVersion()!=device.getVersion())
                throw new StaleObjectStateException(managed.getVersion(),device.getVersion());
            logger.debug("Setting Security Policy:{} on Device with deviceId:{}",policies,deviceId);
            managed.setPolicyData(policies);
			managed.enqueue(Change.FOLDER_SYNC_REQUIRED);
		}
        else
        {
            SessionData data=deviceManager.loadSessionData(deviceId);
            if(data.getVersion()!=device.getVersion())
                throw new StaleObjectStateException(data.getVersion(),device.getVersion());
            logger.debug("Storing Security Policy:{} in SesisonData for Device with deviceId:{}",policies,deviceId);
            data.setPolicies(policies);
            deviceManager.storeSessionData(data);
        }
    }
}
