package com.enterpriseios.push.handler;

import com.enterpriseios.push.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;


public class ProvisionCommandHandler extends WBXMLResponseHandler implements CommandHandler
{
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Random random=new Random();

    private volatile DeviceManager deviceManager;

    private volatile AccountDA accountDA;

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void process(Request request) throws ProtocolException, IOException
    {

        HttpServletResponse response = request.getHttpServletResponse();
        String deviceId = request.getDeviceId();

        Account account=accountDA.account.get(request.getUser());
        if(account==null)
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if(account.getDeviceId()==null)
        {
            account.setDeviceId(deviceId);
            accountDA.account.put(account);
        }
        else if(!account.getDeviceId().equals(deviceId))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        //create a device using DeviceId from the request parameter
        Device device = deviceManager.createDevice(request);
        
        Document document=request.getXML();
        Element provision=document.getRootElement();

        if (!provision.getName().equals("Provision"))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        //Is its child element Policies or RemoteWipe?
        String remoteWipe=evaluate("Provision/RemoteWipe/Status",document);
        if (remoteWipe.length()>0)//has a RemoveWipe Status
        {
            /**
             * Status "1" if the client remote wipe operation was successful.
             * Status "2" if the remote wipe operation failed.
             */
            provision.removeContent(new ElementFilter("RemoteWipe"));

            /**
             * The <Status> element can be one of the following
             * 1 Success
             * 2 Protocol error
             * 3 General Server error
             * 4 The device is externally managed
             */
            String status="1";
            provision.addContent(new Element("Status").addContent(status));
            sendWBXML(response,"Provision",document);
            return;
        }

        String policyType=evaluate("/Provision/Policies/Policy/PolicyType",document);

        if (!policyType.equals("MS-EAS-Provisioning-WBXML"))
        {
            provision.addContent(new Element("Status").addContent("1"));
            Element policy=selectSingleNode("/Provision/Policies/Policy",document);
            // return unknown policy type which is 3
            policy.addContent(new Element("Status").addContent("3"));
            sendWBXML(response,"Provision", document);
            return;
        }
        
        /**
         * The <Status> element that is returned as a child of <Policy> element indicates
         * whether the policy settings were applied correctly.
         * server -> client
         * 1 Success
         * 2 There is no policy for this client
         * 3 Unknown <PolicyType>
         * 4 The policy data on the server is corrupted(possibly tampered with)
         * 5 The client is acknowledging the wrong policy key.
         *
         * client -> server
         * 1 Success
         * 2 Partial success(at least the PIN was enabled)
         * 3 The client did not apply the policy at all
         * 4 The client claims to have been provisioned by a third party.
         */
        String policyKey=evaluate("/Provision/Policies/Policy/PolicyKey",document);
        String pKey="0";
        if (policyKey != null && policyKey.length()>0)
            pKey = policyKey;

        logger.info("Device with id {} requested PolicyType {} with policyKey {}",new String[]{deviceId,policyType,pKey});

        if ("0".equals(pKey))
        {

            provision.removeContent();

            Map<String,String> data=device.getPolicyData();

            if (data!=null && data.containsKey("RemoteWipe"))
            {
                provision.addContent(new Element("Status").addContent("1"));
                provision.addContent(new Element("RemoteWipe"));
                sendWBXML(response,"Provision",document);
                return;
            }

            provision.addContent(new Element("Status").addContent("1"));
            Element policies = new Element("Policies");
            Element policy = new Element("Policy");

            policy.addContent(new Element("PolicyType").addContent(policyType));
            policy.addContent(new Element("Status").addContent("1"));
            pKey = "" + Math.abs((random.nextInt() >> 2));
            device.setPolicyKey(pKey);
            policy.addContent(new Element("PolicyKey").addContent(pKey));

            Element policyData = new Element("Data");
            Element eas = new Element("EASProvisionDoc");

            if (data != null)
            {
                for (Iterator<Map.Entry<String, String>> itr = data.entrySet().iterator(); itr.hasNext();)
                {
                    Map.Entry<String, String> entry = itr.next();
                    eas.addContent(new Element(entry.getKey()).addContent(entry.getValue()));
                }
            }
            policy.addContent(policyData.addContent(eas));
            policies.addContent(policy);
            provision.addContent(policies);
            sendWBXML(response,"Provision", document);
            return;
        }
        else
        {
            final String pKeyStored=device.getPolicyKey();

            if (pKeyStored==null||!pKeyStored.equals(pKey))
            {
                // The client is acknowledging the wrong policy key.
                provision.removeContent();
                provision.addContent(new Element("Status").addContent("1"));
                Element policies = new Element("Policies");
                Element policy = new Element("Policy");
                policy.addContent(new Element("PolicyType").addContent(policyType));
                policy.addContent(new Element("Status").addContent("5"));
                policy.addContent(new Element("PolicyKey").addContent(pKey));
                policies.addContent(policy);
                provision.addContent(policies);
                sendWBXML(response,"Provision", document);
                return;
            }

            String policyStatus=evaluate("/Provision/Policies/Policy/Status",document);
            if (policyStatus.length()>0)
            {
                //client applied the policy successfully
                provision.removeContent();

                String status="1";

                provision.addContent(new Element("Status").addContent(status));
                Element policies = new Element("Policies");
                Element policy = new Element("Policy");
                policy.addContent(new Element("PolicyType").addContent(policyType));
                policy.addContent(new Element("Status").addContent("1"));
                pKey = "" + Math.abs((random.nextInt() >> 2));
                device.setPolicyKey(pKey);
                policy.addContent(new Element("PolicyKey").addContent(pKey));
                policies.addContent(policy);
                provision.addContent(policies);

                sendWBXML(response,"Provision", document);
            }
        }
    }

    public AccountDA getAccountDA()
    {
        return accountDA;
    }

    public void setAccountDA(AccountDA accountDA)
    {
        this.accountDA = accountDA;
    }
}
