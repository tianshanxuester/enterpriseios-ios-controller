package com.enterpriseios.client.policies;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/01
 * Time: 19:08:20
 * To change this template use File | Settings | File Templates.
 */
public class CheckBoxPolicy extends CheckBox implements ValueChangeHandler<Boolean>,ValueChangeable
{
    private final Mediator mediator;
    private final String name;

    public CheckBoxPolicy(Mediator mediatorImpl,String policyName)
    {
        super();
        mediator=mediatorImpl;
        name=policyName;
        mediator.register(name,this);
        addValueChangeHandler(this);
    }

    public void onValueChange(ValueChangeEvent<Boolean> changeEvent)
    {
        boolean required=((CheckBox)changeEvent.getSource()).getValue();
        mediate(required);
    }

    public void setValue(int value)
    {
       setValue(value>0);
       mediate(getValue());
    }

    private void mediate(boolean value)
    {
        if(value)
            mediator.addPolicy(name);
        else
            mediator.removePolicy(name);
    }
}