package com.enterpriseios.push;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/11/28
 * Time: 23:13:34
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class SessionData implements Serializable
{
    @PrimaryKey
    private String deviceId;
    private String deviceType;
    private String user;
    private int heartbeat;
    private String policyKey;
    private Map<String,String> policies;
    private Date lastUpdated;
    private long version;

    public SessionData(){}

    public SessionData(String deviceId,String deviceType,String user)
    {
        setDeviceId(deviceId);
        setDeviceType(deviceType);
        setUser(user);
        setLastUpdated(new Date());
        setPolicies(new HashMap<String,String>());
    }
    
    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public String getDeviceType()
    {
        return deviceType;
    }

    public void setDeviceType(String deviceType)
    {
        this.deviceType = deviceType;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public int getHeartbeat()
    {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat)
    {
        this.heartbeat = heartbeat;
    }

    public String getPolicyKey()
    {
        return policyKey;
    }

    public void setPolicyKey(String policyKey)
    {
        this.policyKey = policyKey;
    }

    public Map<String, String> getPolicies()
    {
        return policies;
    }

    public void setPolicies(Map<String, String> policies)
    {
        this.policies = policies;
        version+=1;
    }

    public Date getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }

    public String toString()
    {
        return "{deviceId:"+ getDeviceId() + " deviceType:"+getDeviceType()+
               " user:"+getUser()+" heartbeat:"+ getHeartbeat() +
               " policies:"+ getPolicies() +" lastUpdated"+ getLastUpdated() +"}";
    }

    public long getVersion()
    {
        return version;
    }

    public void setVersion(long version)
    {
        this.version = version;
    }
}
