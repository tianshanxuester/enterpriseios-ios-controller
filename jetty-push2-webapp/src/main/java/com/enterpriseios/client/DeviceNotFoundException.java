package com.enterpriseios.client;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/06
 * Time: 10:39:54
 * To change this template use File | Settings | File Templates.
 */
public class DeviceNotFoundException extends RuntimeException implements Serializable
{
    public DeviceNotFoundException()
    {
        super();
    }
}
