package com.enterpriseios.client;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/29
 * Time: 16:07:58
 * To change this template use File | Settings | File Templates.
 */
public class Account implements Serializable,Comparable<Account>
{
    private String user;

    private String password;

    private String deviceId;

    public Account(String user,String password)
    {
        this.user=user;
        this.password=password;
    }

    public Account(){}

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public int compareTo(Account data)
    {
        return user.compareTo(data.user);
    }

    public boolean equals(Object o)
    {
        if (o instanceof Account)
            return getUser().equals(((Account)o).getUser());
        return false;
    }

    public int hashCode()
    {
        return getUser().hashCode();
    }

    public String toString()
    {
        return "{user:"+user+ " password:"+password+ " deviceId:"+deviceId+"}";
    }

}
