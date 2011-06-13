package com.enterpriseios.client.policies;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/02
 * Time: 16:27:29
 * To change this template use File | Settings | File Templates.
 */
public class TogglePolicy extends HorizontalPanel implements ValueChangeHandler<Boolean>,ValueChangeable
{
    private final Mediator mediator;
    private final String name;
    private RadioButton on;
    private RadioButton off;
    private boolean toggle;

    public TogglePolicy(Mediator mediatorImpl,String policyName)
    {
        mediator=mediatorImpl;
        name=policyName;
        mediator.register(name,this);
        on=new RadioButton(name,"ON");
        off=new RadioButton(name,"OFF");
        on.addValueChangeHandler(this);
        off.addValueChangeHandler(this);
        add(on);
        add(off);
    }

    public void onValueChange(ValueChangeEvent<Boolean> changeEvent)
    {
        mediate(toggle=!toggle);
    }

    public void setValue(int value)
    {
        if(value>0)
        {
            on.setValue(true);
            off.setValue(false);
            mediate(toggle=true);
        }
        else
        {
            on.setValue(false);
            off.setValue(true);
            mediate(toggle=false);
        }
    }

    public void setEnabled(boolean enabled)
    {
        on.setEnabled(enabled);
        off.setEnabled(enabled);
    }

    private void mediate(boolean value)
    {
        if(value)
            mediator.removePolicy(name);
        else
            mediator.addPolicy(name,"0");
    }
}