package com.enterpriseios.push;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.SecondaryKey;
import static com.sleepycat.persist.model.Relationship.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/11/29
 * Time: 0:05:39
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Account implements Serializable,Comparable<Account>
{
    @PrimaryKey
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
        return (data==null || data.user ==null) ? -1 : -data.user.compareTo(getUser());
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
