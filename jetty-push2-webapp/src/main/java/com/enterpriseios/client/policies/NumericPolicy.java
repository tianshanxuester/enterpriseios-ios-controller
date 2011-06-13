package com.enterpriseios.client.policies;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.ui.*;

import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/12/01
 * Time: 23:23:53
 * To change this template use File | Settings | File Templates.
 */
public class NumericPolicy extends TextBox implements ValueChangeable, KeyPressHandler, BlurHandler
{
    private final Mediator mediator;
    private final String name;

    public NumericPolicy(Mediator mediatorImpl,String policyName,int length)
    {
        super();
        mediator=mediatorImpl;
        name=policyName;
        mediator.register(name,this);
        setMaxLength(length);
        setVisibleLength(length);
        addKeyPressHandler(this);
        addBlurHandler(this);
    }
    
    public void setValue(int value)
    {
        if(value>0)
            setValue(String.valueOf(value));
        else
            setValue(null);
        mediate(getValue());
    }

    public void onKeyPress(KeyPressEvent keyPressEvent)
    {
        if(!keyPressEvent.isAnyModifierKeyDown())
        {
            TextBox textBox=(TextBox) keyPressEvent.getSource();
            char code=keyPressEvent.getCharCode();
          
            String value=textBox.getValue();
            if(((value==null||value.isEmpty()) && code=='0')|| !(0x30<=code && code<=0x39) && !(0x00<=code && code<=0x1f))
                textBox.cancelKey();

        }
    }

    public void onBlur(BlurEvent blurEvent)
    {
        TextBox textBox=(TextBox)blurEvent.getSource();
        String value=textBox.getValue();
        for(int i=0;i<value.length();i++)
        {
            if(value.charAt(i)> 0x80)
                return;   
        }
        mediate(textBox.getValue());
    }

    private void mediate(String value)
    {
        if(value!=null && !value.isEmpty())
            mediator.addPolicy(name,value);
        else
            mediator.removePolicy(name);
    }
}
