package com.enterpriseios.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.*;
import com.enterpriseios.client.policies.*;

public class iOSController implements EntryPoint {

    public void onModuleLoad()
    {
        RootPanel.get("DeviceList").add(new DeviceController());
    }
}
