package com.enterpriseios.push;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface Device
{
    public static final String REQUEST_ATTRIBUTE = Device.class.getName() + ".REQUEST";

    /**
     * <p>Enqueues the given change to the delivery queue so that it will be sent to the
     * remote device on the first occasion.</p>
     * <p>Changes may fail to be enqueued, for example because this device has been
     * concurrently closed.</p>
     *
     * @param change the change to add to the delivery queue
     * @return whether the change has been enqueued or not
     * @see #process(Request, boolean)
     */
    public boolean enqueue(Change change);

    /**
     * Closes this device, returning any pending long poll.
     * @see #isClosed()
     */
    public void close();

    /**
     * @return whether this device is closed
     * @see #close()
     */
    public boolean isClosed();

    /**
     * <p>Flushes the changes that have been {@link #enqueue(Change)} enqueued}.</p>
     * <p>If no changes have been enqueued, then this method may suspend the current request for
     * the long poll heartbeat. <br />
     * The request is suspended only if all these conditions holds true:
     * <ul>
     * <li>this client delegate is not closed</li>
     * <li>no changes have been enqueued</li>
     * <li>the request is not suspended</li>
     * </ul>
     * In all other cases, a response if sent to the remote device, possibly containing no changes.
     *
     * @param request the request wrapping the long poll request and response from the remote device
     * @param suspend whether the request should be suspended if all conditions for suspending hold
     * @return the list of changes to send to the remote device, or null if no response should be sent
     * because the request has been suspended
     * @see #enqueue(Change)
     */
    List<Change> process(Request request, boolean suspend);

    /**
     * @return the device id
     */
    String getId();

    /**
     * @param heartbeat the period of time in seconds that a long poll request must be suspended
     */
    void setHeartbeat(int heartbeat);

    /**
     * @return heartbeat
     */
    int getHeartBeat();

    /**
     *
     * @param policyKey the value of the MS-PolicyKey
     */
    void setPolicyKey(String policyKey);

    /**
     * @return policyKey
     */
    String getPolicyKey();

    /**
     *
     * @return user
     */
    String getUser();

    /**
     *
     * @param devicePolicy
     */
    void setPolicyData(Map<String,String> devicePolicy);

    /**
     *
     * @return policyData
     */
    Map<String, String> getPolicyData();

    void setLastUpdated(Date date);

    Date getLastUpdated();

    boolean isExternallyChanged();

    SessionData getSessionData();

    long getVersion();

    void setOnline(boolean online);

    boolean isOnline();
}
