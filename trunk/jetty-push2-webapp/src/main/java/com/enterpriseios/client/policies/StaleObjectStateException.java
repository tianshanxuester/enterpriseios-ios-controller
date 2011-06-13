package com.enterpriseios.client.policies;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/12/04
 * Time: 21:30:16
 * To change this template use File | Settings | File Templates.
 */
public class StaleObjectStateException extends Exception implements Serializable
{

    public StaleObjectStateException(long actual,long expected)
    {
        super("expected:"+expected+" actual:"+actual);
    }

    public StaleObjectStateException()
    {

    }
    
}
