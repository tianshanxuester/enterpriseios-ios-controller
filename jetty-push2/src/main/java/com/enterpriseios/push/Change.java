package com.enterpriseios.push;

import org.eclipse.jetty.server.Authentication;


public interface Change
{
    /* ------------------------------------------------------------ */
    /**
    *
    */
    // Not used as no folder will be monitored.
    public interface Folder extends Change
    {
        int getId();
        void setId();
    }

    /* ------------------------------------------------------------ */
    /**
     *  The folder hierarchy is out of date and a folder hierarchy sync is required.
     *
     */
    public interface FolderSyncRequired extends Change
    {
    
    }

    public final static Change FOLDER_SYNC_REQUIRED=new Change.FolderSyncRequired()
    {
        @Override
        public String toString(){ return "FolderSyncRequired";}
    };


}
