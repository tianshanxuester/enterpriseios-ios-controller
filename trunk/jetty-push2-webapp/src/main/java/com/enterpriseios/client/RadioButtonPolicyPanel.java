package com.enterpriseios.client;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

public class RadioButtonPolicyPanel extends Composite implements ValueChangeHandler
{
    private boolean toggle;
    private String policy;
    private RadioButton on;
    private RadioButton off;

    public RadioButtonPolicyPanel(String file,String policy,boolean init)
    {
        this.policy=policy;
        on=new RadioButton(policy,"ON");
        off=new RadioButton(policy,"OFF");
        on.addValueChangeHandler(this);
        off.addValueChangeHandler(this);
        if(init)
            on.setValue(true);
        else
            off.setValue(true);
        FlowPanel flowPanel = new FlowPanel();
        Image image=new Image(file);
        image.setWidth("30px");
        image.setHeight("30px");
        flowPanel.add(image);
        flowPanel.add(on);
        flowPanel.add(off);
        toggle=init;
        initWidget(flowPanel);
    }

    public void onValueChange(ValueChangeEvent valueChangeEvent)
    {
        toggle=!toggle;
    }

    public String getPolicy()
    {
        return policy;
    }

    public boolean isPolicyEnabled()
    {
        return toggle;
    }

    public void setEnabled(boolean enabled)
    {
        on.setEnabled(enabled);
        off.setEnabled(enabled);
    }
}


