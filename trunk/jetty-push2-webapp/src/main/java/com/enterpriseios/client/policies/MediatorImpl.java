package com.enterpriseios.client.policies;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/02
 * Time: 13:42:47
 * To change this template use File | Settings | File Templates.
 */
public class MediatorImpl implements Mediator
{
    private Map<String,ValueChangeable> changeables=new HashMap<String,ValueChangeable>();
    
    private Map<String,String> policies=new HashMap<String,String>();

    private boolean enabled;

    public void register(String policyName,ValueChangeable changeable)
    {
        changeables.put(policyName,changeable);
    }

    public void addPolicy(String policyName)
    {
        policies.put(policyName,"1");
    }

    public void addPolicy(String policyName, String value)
    {
        policies.put(policyName,value);
    }

    public void removePolicy(String policyName)
    {
        policies.remove(policyName);
    }

    public void setPolicies(Map<String,String> received)
    {
        resetValues();
        if(received!=null)
        {
            for(Map.Entry<String,String> entry:received.entrySet())
            {
                ValueChangeable changeable=changeables.get(entry.getKey());
                if(changeable!=null)
                    changeable.setValue(Integer.valueOf(entry.getValue()));
            }
        }
    }

    public Map<String,String> getPolicies()
    {
        return policies;
    }

    public void setEnabled(boolean enabled)
    {
        for(ValueChangeable changeable:changeables.values())
            changeable.setEnabled(enabled);
        this.enabled=enabled;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    private void resetValues()
    {
        for(ValueChangeable changeable:changeables.values())
            changeable.setValue(changeable instanceof TogglePolicy ? 1 : 0);          
    }
}
