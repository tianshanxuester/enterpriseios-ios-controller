package com.enterpriseios.client.policies;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.*;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/01
 * Time: 19:31:12
 * To change this template use File | Settings | File Templates.
 */
public class DropBoxPolicy extends ListBox implements ChangeHandler,ValueChangeable
{
    private final Mediator mediator;
    private final String name;

    public DropBoxPolicy(Mediator mediatorImpl,String policyName,int[] values)
    {
        super(false);
        mediator=mediatorImpl;
        name=policyName;
        mediator.register(name,this);
        addItem("-Select-","0");
        for(int i=0; i<values.length;i++)
        {                        
            String value=String.valueOf(values[i]);     
            addItem(value);
        }
        addChangeHandler(this);
    }

    public DropBoxPolicy(Mediator mediatorImpl,String policyName,int[][] values)
    {
        super(false);
        mediator=mediatorImpl;
        name=policyName;
        mediator.register(name,this);
        addItem("-Select-","0");
        for(int i=0; i<values.length;i++)
        {
            String key=String.valueOf(values[i][0]);
            String value=String.valueOf(values[i][1]);
            addItem(key,value);
        }
        addChangeHandler(this);
    }

    public void onChange(ChangeEvent changeEvent)
    {
        ListBox listBox=((ListBox)changeEvent.getSource());
        mediate(listBox.getSelectedIndex());
    }

    public void setValue(int value)
    {
        String valueReceived=String.valueOf(value);
        for(int i=0; i<getItemCount();i++)
        {
            String dropBoxValue=getValue(i);
            if(dropBoxValue.equals(valueReceived))
            {
                setSelectedIndex(i);
                mediate(i);
            }
        }
    }

    private void mediate(int index)
    {
        if(index>0)
            mediator.addPolicy(name,getValue(index));
        else
            mediator.removePolicy(name);
    }
}
