package com.enterpriseios.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sleepycat.persist.EntityCursor;
import com.enterpriseios.client.Account;
import com.enterpriseios.client.AccountService;
import com.enterpriseios.push.AccountDA;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.SessionDataDA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/11/29
 * Time: 16:05:27
 * To change this template use File | Settings | File Templates.
 */
public class AccountServiceImpl extends RemoteServiceServlet implements AccountService
{
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);
    private AccountDA accountDA;
    private volatile DeviceManager deviceManager;

    @Override
	public void init() throws ServletException
    {
        accountDA = (AccountDA) getServletContext().getAttribute(AccountDA.ATTRIBUTE);
        deviceManager = (DeviceManager) getServletContext().getAttribute(
				DeviceManager.ATTRIBUTE);
	}
    
    public List<Account> getAllAccount()
    {
        logger.debug("Retrieving all Account");
        List<Account> accounts=new ArrayList<Account>();
        EntityCursor<com.enterpriseios.push.Account> cursor=accountDA.account.entities();
        try{
            for(Iterator<com.enterpriseios.push.Account> iterator=cursor.iterator();iterator.hasNext();)
            {

                com.enterpriseios.push.Account entity=iterator.next();
                logger.debug("Account retrieved: {}",entity.toString());
                Account account=new Account(entity.getUser(),entity.getPassword());
                account.setDeviceId(entity.getDeviceId());
                accounts.add(account);
            }
        }
        finally
        {
            cursor.close();   
        }

        return accounts;
    }

    public void add(Account account)
    {
        com.enterpriseios.push.Account entity=new com.enterpriseios.push.Account(account.getUser(),account.getPassword());
        if(accountDA.account.get(account.getUser())!=null)
            return;
        accountDA.account.put(entity);
        logger.debug("Added Account {}",account);
        
    }

    public void remove(Account selected)
    {
        accountDA.account.delete(selected.getUser());
        logger.debug("Deleted Account {}",selected);
    }

    public synchronized void disassociateDevice(Account selected)
    {
        com.enterpriseios.push.Account entity=accountDA.account.get(selected.getUser());
        deviceManager.removeDevice(selected.getDeviceId(),false);
        entity.setDeviceId(null);
        accountDA.account.put(entity);
    }
}
