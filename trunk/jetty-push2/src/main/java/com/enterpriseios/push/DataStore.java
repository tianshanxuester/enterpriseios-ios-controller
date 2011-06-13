package com.enterpriseios.push;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;
import org.eclipse.jetty.util.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: hanishi
 * Date: 2010/11/28
 * Time: 23:06:33
 * To change this template use File | Settings | File Templates.
 */
public class DataStore
{
    private Environment myEnv;
    private EntityStore store;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public DataStore() {}

    public DataStore(String envHome, boolean readOnly)
        throws DatabaseException
    {

        EnvironmentConfig myEnvConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        myEnvConfig.setReadOnly(readOnly);
        storeConfig.setReadOnly(readOnly);

        // If the environment is opened for write, then we want to be
        // able to create the environment and entity store if
        // they do not exist.
        myEnvConfig.setAllowCreate(!readOnly);
        storeConfig.setAllowCreate(!readOnly);

        // Open the environment and entity store
        myEnv = new Environment(new File(envHome), myEnvConfig);
        store = new EntityStore(myEnv, "EntityStore", storeConfig);
        Runtime.getRuntime().addShutdownHook(
                new Thread(){
                    public void run()
                    {
                        close();
                        logger.info("DataStore closed.");
                    }
                });

    }

    public EntityStore getEntityStore()
    {
        return store;
    }

    public Environment getEnv()
    {
        return myEnv;
    }

    public void close()
    {
        if (store != null)
        {
            try
            {

                store.close();
            }
            catch(DatabaseException dbe)
            {
                System.err.println("Error closing store: " +
                                    dbe.toString());
                System.exit(-1);
            }
        }

        if (myEnv != null)
        {
            try
            {
                myEnv.close();
            }
            catch(DatabaseException dbe)
            {
                System.err.println("Error closing MyDbEnv: " +
                                    dbe.toString());
                System.exit(-1);
            }
        }
    }

}
