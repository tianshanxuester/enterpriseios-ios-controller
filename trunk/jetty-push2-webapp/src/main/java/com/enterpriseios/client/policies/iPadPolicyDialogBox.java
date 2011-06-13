package com.enterpriseios.client.policies;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.enterpriseios.client.Device;
import com.enterpriseios.client.DeviceController;
import com.enterpriseios.client.DeviceMonitoringServiceAsync;
import com.enterpriseios.client.PolicyDialogBox;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/12/02
 * Time: 23:46:52
 * To change this template use File | Settings | File Templates.
 */
public class iPadPolicyDialogBox extends DialogBox implements ClickHandler, PolicyDialogBox
{

    @UiTemplate("iPadPolicyDialogPanel.ui.xml")
    interface Binder extends UiBinder<Widget, iPadPolicyDialogBox>
    {
    }

    private static Binder uiBinder = GWT.create(Binder.class);

    @UiField(provided=true)
    CheckBoxPolicy devicePasswordEnabled;

    @UiField(provided=true)
    DropBoxPolicy minDevicePasswordLength;

    @UiField(provided=true)
    DropBoxPolicy maxDevicePasswordFailedAttempts;

    @UiField(provided=true)
    DropBoxPolicy minDevicePasswordComplexCharacters;

    @UiField(provided=true)
    CheckBoxPolicy alphanumericDevicePasswordRequired;

    @UiField(provided=true)
    CheckBoxPolicy allowSimpleDevicePassword;

    @UiField(provided=true)
    NumericPolicy devicePasswordExpiration;

    @UiField(provided=true)
    DropBoxPolicy maxInactivityTimeDeviceLock;

    @UiField(provided=true)
    NumericPolicy devicePasswordHistory;

    @UiField
    Image image;

    @UiField
    Image apple;

    @UiField
    CheckBox remoteWipe;

    @UiField
    CheckBox disassociate;

    @UiField
    Button closeDialog;

    @UiField
    SpanElement lastUpdated;

    @UiField
    SpanElement online;

    private final DeviceController parent;
    private Device device;
    private PopupPanel popup=new PopupPanel();
    private HTML REQUEST_IN_PROGRESS=new HTML("Policy change in progress...");
    private HTML REMOTE_WIPE_REQUESTED=new HTML("RemoteWipe in progress...");
    private Mediator mediator=new MediatorImpl();
    private boolean remove;
    private Timer timer;
    private static Logger logger = Logger.getLogger("iPadPolicyDialogBox");

    public iPadPolicyDialogBox(DeviceController controller,boolean autoHide,boolean modal)
    {
        super(autoHide,modal);
        devicePasswordEnabled=new CheckBoxPolicy(mediator,"DevicePasswordEnabled");
        minDevicePasswordLength=new DropBoxPolicy(mediator,"MinDevicePasswordLength", new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16});
        maxDevicePasswordFailedAttempts=new DropBoxPolicy(mediator,"MaxDevicePasswordFailedAttempts",new int[]{4,5,6,7,8,9,10,11,12,13,14,15,16});
        minDevicePasswordComplexCharacters=new DropBoxPolicy(mediator,"MinDevicePasswordComplexCharacters",new int[]{1,2,3,4});
        alphanumericDevicePasswordRequired=new CheckBoxPolicy(mediator,"AlphanumericDevicePasswordRequired");
        allowSimpleDevicePassword=new CheckBoxPolicy(mediator,"AllowSimpleDevicePassword");
        devicePasswordExpiration=new NumericPolicy(mediator,"DevicePasswordExpiration",3);
        maxInactivityTimeDeviceLock=new DropBoxPolicy(mediator,"MaxInactivityTimeDeviceLock",new int[][]{{2,120},{5,300},{10,600},{15,900}});
        devicePasswordHistory=new NumericPolicy(mediator,"DevicePasswordHistory",3);
        setWidget(uiBinder.createAndBindUi(this));

        image.addClickHandler(this);

        remoteWipe.addValueChangeHandler(new ValueChangeHandler<Boolean>()
        {

            public void onValueChange(ValueChangeEvent<Boolean> changeEvent)
            {
                boolean checked=((CheckBox)changeEvent.getSource()).getValue();
                if(checked)
                {
                    image.setUrl("iPad_wipe.png");
                    mediator.setEnabled(false);
                }
                else
                {
                    image.setUrl("iPad_top.png");
                    mediator.setEnabled(true);
                }
                image.setHeight("300px");
                image.setWidth("300px");
            }        
        });
        remoteWipe.setValue(false);
        this.parent=controller;
        closeDialog.addClickHandler(
                new ClickHandler()
                {

                    public void onClick(ClickEvent clickEvent)
                    {
                        if(remove)
                        {
                            hide();
                            parent.disassociateDevice();
                            parent.deselectSelected();
                        }
                        else
                        {
                            hide();
                            parent.deselectSelected();
                        }
                        logger.info("Timer stopped");
                        timer.cancel();
                        parent.updateList(5000);
                    }
                });
        disassociate.addValueChangeHandler(
                new ValueChangeHandler<Boolean>()
                {

                    public void onValueChange(ValueChangeEvent<Boolean> changeEvent)
                    {
                        remove=((CheckBox)changeEvent.getSource()).getValue();
                    }
                });

    }

    public void setDevice(Device device, boolean update)
    {

        Map<String,String> policies=device.getPolicies();

        if(popup.isVisible())
            popup.hide();

        image.setHeight("300px");
        image.setWidth("300px");
        this.device=device;
        this.setText("Device ID: " + device.getId());
        online.setInnerText(device.isOnline()?"ON LINE":"OFF LINE");
        lastUpdated.setInnerText(device.getLastUpdated().toString());
        disassociate.setValue(false, true);
        if(!update)
        {
            if(policies.containsKey("RemoteWipe"))
            {
                remoteWipe.setValue(true);
                mediator.setEnabled(false);
                image.setUrl("iPad_wipe.png");
            }
            else
            {
                remoteWipe.setValue(false);
                mediator.setEnabled(true);
                image.setUrl("iPad_top.png");
            }
            mediator.setPolicies(policies);
            timer=null;
        }

    }

    public void onClick(ClickEvent event)
    {
        if(remove||!closeDialog.isEnabled())
            return;

        Widget source=(Widget)event.getSource();
        int left = source.getAbsoluteLeft() + 15;
        int top = source.getAbsoluteTop() + 15;

        popup.setPopupPosition(left,top);
        popup.setAnimationEnabled(true);

        Map<String,String> policies;
        if(remoteWipe.getValue())
        {
            policies=new HashMap<String,String>();
            policies.put("RemoteWipe","");
        }else
            policies=mediator.getPolicies();
        device.setPolicies(policies);

        final boolean online=device.isOnline();

        DeviceMonitoringServiceAsync service=parent.getDeviceMonitoringService();
        service.updatePolicies(device,
        new AsyncCallback<Void>(){

            public void onFailure(Throwable throwable)
            {

                if(throwable instanceof StaleObjectStateException)
                    popup.setWidget(new HTML("Stale policy state!"));
                else
                    popup.setWidget(new HTML("Communication error with server..."));
            }

            public void onSuccess(Void aVoid)
            {
                updateDeviceInfo(1000,false);
                if(online)
                    updateDeviceInfo(5000,true);
            }
        });

        if(remoteWipe.getValue())
            popup.setWidget(REMOTE_WIPE_REQUESTED);
        else
            popup.setWidget(REQUEST_IN_PROGRESS);
        popup.show();
    }

    private void updateDeviceInfo(final int time,final boolean repeat)
    {

        closeDialog.setEnabled(repeat);


        final AsyncCallback<Device> callback=
            new AsyncCallback<Device>()
            {

                public void onFailure(Throwable throwable)
                {

                }

                public void onSuccess(Device updated)
                {

                    if (updated!=null)
                    {
                        Map<String,String> policies=updated.getPolicies();

                        setDevice(updated,repeat);

                        if(policies!=null && policies.containsKey("RemoteWipe"))
                        {
                            remoteWipe.setValue(true);
                            mediator.setEnabled(false);
                            image.setUrl("iPad_wipe.png");
                            image.setHeight("300px");
                            image.setWidth("300px");
                        }
                    }

                    if(repeat)
                    {
                        if(device.isOnline())
                        {
                            logger.info("Timer canceled");
                        }
                        else
                        {
                            updateDeviceInfo(time,repeat);
                        }
                    }
                    else
                        closeDialog.setEnabled(!repeat);
                }
            };

        timer=new Timer()
        {
            @Override
            public void run()
            {
                DeviceMonitoringServiceAsync service=parent.getDeviceMonitoringService();
                service.getDeviceById(device.getId(),callback);
            }
        };
        timer.schedule(time);
        logger.info("Timer started");
    }
}