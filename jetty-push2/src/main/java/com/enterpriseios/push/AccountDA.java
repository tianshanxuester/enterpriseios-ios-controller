package com.enterpriseios.push;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/11/29
 * Time: 0:48:25
 * To change this template use File | Settings | File Templates.
 */
public class AccountDA
{
    public static final String ATTRIBUTE = AccountDA.class.getName() + ".ATTRIBUTE";

    public AccountDA(EntityStore store)
        throws DatabaseException {

        account = store.getPrimaryIndex(
            String.class, Account.class);
    }

    public PrimaryIndex<String,Account> account;
}
