package com.enterpriseios.client.policies;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/12/02
 * Time: 10:55:26
 * To change this template use File | Settings | File Templates.
 */
public interface Mediator
{
    void register(String policyName,ValueChangeable changeable);

    void addPolicy(String policyName);

    void addPolicy(String policyName, String value);

    void removePolicy(String policyName);

    void setPolicies(Map<String,String> received);

    Map<String,String> getPolicies();

    void setEnabled(boolean enabled);

    boolean isEnabled();

}
