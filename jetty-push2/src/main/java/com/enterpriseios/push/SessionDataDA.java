package com.enterpriseios.push;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/11/29
 * Time: 0:30:37
 * To change this template use File | Settings | File Templates.
 */
public class SessionDataDA
{
    public static final String ATTRIBUTE = SessionDataDA.class.getName() + ".ATTRIBUTE";

    public SessionDataDA(EntityStore store)
        throws DatabaseException {

        sessionData = store.getPrimaryIndex(
            String.class, SessionData.class);
    }

    public PrimaryIndex<String,SessionData> sessionData;
}
