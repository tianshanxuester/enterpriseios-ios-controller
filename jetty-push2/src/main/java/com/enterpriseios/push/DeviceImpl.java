package com.enterpriseios.push;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class  DeviceImpl implements Device
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Object lock = new Object();
    private final List<Change> changes = new ArrayList<Change>();
    private SessionData sessionData;
    private boolean online;
    private volatile boolean closed;
    private volatile  int changedExternally;
    private Continuation continuation;

    public DeviceImpl(SessionData sessionData)
    {
        this.sessionData = sessionData;
    }

    public String getId()
    {
        return sessionData.getDeviceId();
    }

    public long getVersion()
    {
        return sessionData.getVersion();
    }

    public void setOnline(boolean online)
    {
        this.online=online;
    }

    public boolean isOnline()
    {
        return online;
    }

    public String getType()
    {
        return sessionData.getDeviceType();
    }

    public String getUser()
    {
        return sessionData.getUser();
    }

    public void setHeartbeat(int heartbeat)
    {
        sessionData.setHeartbeat(heartbeat);
    }

    public int getHeartBeat()
    {
        return sessionData.getHeartbeat();
    }

    public void setPolicyKey(String policyKey)
    {
        sessionData.setPolicyKey(policyKey);
    }

    public String getPolicyKey()
    {
        return sessionData.getPolicyKey();
    }

    public void setPolicyData(Map<String,String> devicePolicy)
    {
        sessionData.setPolicies(devicePolicy);
    }

    public Map<String, String> getPolicyData()
    {   if(changedExternally>0)
            changedExternally--;
        return sessionData.getPolicies();
    }

    public boolean isExternallyChanged()
    {
        return changedExternally>0;
    }

    public void setLastUpdated(Date date)
    {
        sessionData.setLastUpdated(date);
    }

    public Date getLastUpdated()
    {
        return sessionData.getLastUpdated();
    }

    public SessionData getSessionData()
    {
        return sessionData;
    }

    public boolean enqueue(Change change)
    {
        if (isClosed())
            return false;

        synchronized (lock)
        {
            changedExternally++;
            changes.add(change);
            resume();
        }

        return true;
    }

    public List<Change> process(Request request, boolean suspend)
    {
        // Synchronization is crucial here, since we don't want to suspend if there is something to deliver
        synchronized (lock)
        {
            setOnline(false);
            if (isClosed())
            {
                logger.debug("Closing for device {}", getId());
                return Collections.emptyList();
            }

            int size = changes.size();
            if (size > 0)
            {
                assert continuation == null;
                List<Change> result = new ArrayList<Change>(size);
                result.addAll(changes);
                changes.clear();
                logger.debug("Resuming for device {}, delivering requests {}", getId(), result);
                return result;
            }

            if (continuation != null)
            {
                // Two cases here: we are resuming because the continuation expired,
                // or we received a second long poll request.

                Request existingRequest = (Request)continuation.getAttribute(REQUEST_ATTRIBUTE);
                assert existingRequest != null;
                if (existingRequest == request)
                {
                    // Continuation expired
                    continuation = null;
                    logger.debug("Expiring for device {}", getId());
                    return Collections.emptyList();
                }
                else
                {
                    // Second long poll, complete the previous long poll, and fall through to decide if suspend
                    try
                    {
                        int errorCode = HttpServletResponse.SC_REQUEST_TIMEOUT;
                        ((HttpServletResponse)continuation.getServletResponse()).sendError(errorCode);
                    }
                    catch (IOException x)
                    {
                        // Ignored
                    }
                    continuation.complete();
                    continuation = null;
                    logger.debug("Completing for device {}", getId());
                }
            }

            if (!suspend)
                return Collections.emptyList();

            // Here we need to suspend
            continuation = ContinuationSupport.getContinuation(request.getHttpServletRequest());
            continuation.setTimeout(TimeUnit.SECONDS.toMillis(getHeartBeat()));
            continuation.suspend();
            continuation.setAttribute(REQUEST_ATTRIBUTE, request);
            logger.debug("Suspending for device {} for {}", getId(),getHeartBeat());
            setOnline(true);
            return null;
        }
    }

    public void close()
    {
        closed = true;
        resume();
    }

    public boolean isClosed()
    {
        return closed;
    }

    private void resume()
    {
        synchronized (lock)
        {
            // Continuation may be null in several cases:
            // 1. there always is something to deliver so we never suspend
            // 2. concurrent calls to enqueue() and close()
            // 3. concurrent close() with a long poll that expired
            // 4. concurrent close() with a long poll that resumed
            if (continuation != null)
            {
                continuation.resume();
                // Null the continuation, as there is no point is resuming multiple times
                continuation = null;
            }
        }
    }
}
