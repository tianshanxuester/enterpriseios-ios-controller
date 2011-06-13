package com.enterpriseios.client;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class Device implements Serializable
{
    private String id;
    private String type;
    private long version;
    private Map<String,String> policies;
    private Date lastUpdated;
    private boolean online;
  
    public Device(String id,String type,long version,Map<String,String> policies,Date lastUpdated)
    {
        this.id = id;
        this.type = type;
        this.version = version;
        this.policies = policies;
        this.lastUpdated = lastUpdated;
    }

    public Device(){}

    public String getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public long getVersion()
    {
        return version;
    }

    public Map<String,String> getPolicies()
    {
        return policies;
    }

    public void setPolicies(Map<String,String> policies)
    {
        this.policies=policies;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date date)
    {
        this.lastUpdated=date;
    }

    public boolean isOnline()
    {
        return online;
    }

    public void setOnline(boolean online)
    {
        this.online = online;
    }

    public String toString()
    {
        return "{id:"+id+ " type:"+type+" version:"+version+
                " policies:"+policies+" lastUpdated:"+lastUpdated+" online:"+online+"}";
    }

}