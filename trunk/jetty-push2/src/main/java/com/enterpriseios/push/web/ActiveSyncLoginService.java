package com.enterpriseios.push.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.enterpriseios.push.Account;
import com.enterpriseios.push.AccountDA;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.log.Log;

public class ActiveSyncLoginService extends MappedLoginService
{
    private int cacheTime;
    private long lastHashPurge;
    private Map<String,String> admins=new HashMap<String,String>();
    private AccountDA accountDA;

    protected void doStart() throws Exception
    {
        cacheTime = getCacheTime();
        cacheTime *= 1000;
        lastHashPurge = 0;
        super.doStart();
    }

    public int getCacheTime()
    {
        return cacheTime;
    }

    public void setCacheTime(int sec)
    {
        cacheTime = sec;
    }

    public ActiveSyncLoginService(String name)
    {
        setName(name);
    }

    public void setAdminAccounts(Map<String,String> accounts)
    {
        admins.putAll(accounts);       
    }

    @Override
    public UserIdentity login(String username, Object credentials)
    {
        //Stripping domain part from username
        username=removeDomainPart(username);
        
        long now = System.currentTimeMillis();
        if (now - lastHashPurge > cacheTime || cacheTime == 0)
        {
            _users.clear();
            lastHashPurge = now;

            for(Map.Entry<String,String> entry:admins.entrySet())
                putUser(entry.getKey(),entry.getValue());    
        }
        Log.debug("Logging in the user '{}'",username+":"+credentials);
        return super.login(username,credentials);
    }

    private String removeDomainPart(String username)
    {
        Log.debug("original username is '{}'",username);
		int idx = username.indexOf("\\");
		if (idx > 0)
        {
            username = username.substring(idx + 1);
            Log.debug("username with domain part stripped is'{}'", username);
		}
		return username;
	}


    @Override
    protected UserIdentity loadUser(String username)
    {
        Account account=accountDA.account.get(username);
        if (account!=null)
        {
            return putUser(username,account.getPassword());
        }
        else
            return null;
    }

    @Override
    protected void loadUsers() throws IOException
    {

    }

    public void setAccountDA(AccountDA accountDA)
    {
        this.accountDA = accountDA;
    }
}
