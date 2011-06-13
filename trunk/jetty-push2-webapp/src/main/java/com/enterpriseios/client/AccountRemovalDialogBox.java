package com.enterpriseios.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/12/05
 * Time: 1:04:58
 * To change this template use File | Settings | File Templates.
 */
public class AccountRemovalDialogBox extends DialogBox 
{
    @UiTemplate("AccountRemovalDialogPanel.ui.xml")
    interface Binder extends UiBinder<HTMLPanel, AccountRemovalDialogBox>
    {
    }

    private static Binder ourUiBinder = GWT.create(Binder.class);

    @UiField
    SpanElement user;

    @UiField
    Button cancel;

    @UiField
    Button ok;

    private DeviceController parent;

    public AccountRemovalDialogBox(DeviceController controller,boolean autoHide,boolean modal)
    {
        super(autoHide,modal);
        parent=controller;
        setWidget(ourUiBinder.createAndBindUi(this));
    }

    public void setUser(String user)
    {
        this.user.setInnerText(user);
    }

    @UiHandler("ok")
    public void handleClickOK(ClickEvent event)
    {
        AsyncCallback<Void> callback=
            new AsyncCallback<Void>()
            {

                public void onFailure(Throwable throwable)
                {
                    throwable.printStackTrace();
                }

                public void onSuccess(Void devices)
                {
                    parent.deselectSelected();
                    hide();
                }
            };
        AccountServiceAsync service=parent.getAccountService();
        service.remove(parent.getSelectedAccount(),callback);
    }

    @UiHandler("cancel")
    public void handleClickCancel(ClickEvent event)
    {
        hide();
    }
}