package com.enterpriseios.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/29
 * Time: 16:09:39
 * To change this template use File | Settings | File Templates.
 */
public interface AccountServiceAsync
{
    void getAllAccount(AsyncCallback<List<Account>> async);

    void add(Account account, AsyncCallback<Void> async);

    void remove(Account selected, AsyncCallback<Void> async);
    
    void disassociateDevice(Account selected, AsyncCallback<Void> async);   
}
