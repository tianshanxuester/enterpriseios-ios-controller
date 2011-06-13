package com.enterpriseios.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/29
 * Time: 15:29:39
 * To change this template use File | Settings | File Templates.
 */
@RemoteServiceRelativePath("accountService")
public interface AccountService extends RemoteService
{
    List<Account> getAllAccount();

    void add(Account account);

    void remove(Account account);

    void disassociateDevice(Account selected);

}
