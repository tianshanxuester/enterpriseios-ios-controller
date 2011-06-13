package com.enterpriseios.push.handler;

import com.enterpriseios.push.CommandHandler;
import com.enterpriseios.push.DeviceManager;
import com.enterpriseios.push.ProtocolException;
import com.enterpriseios.push.Request;
import org.eclipse.jetty.util.log.Log;
import org.jdom.Document;
import org.jdom.Element;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: inventit
 * Date: 2010/10/25
 * Time: 16:53:57
 * To change this template use File | Settings | File Templates.
 */
public class SettingsCommandHandler extends WBXMLResponseHandler implements CommandHandler
{
    private volatile DeviceManager deviceManager;

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void process(Request request) throws ProtocolException, IOException
    {
            Document document = request.getXML();
            Element sync=document.getRootElement();
            sync.addContent(new Element("Status").addContent("1"));
            Element userInformation=selectSingleNode("/Settings/UserInformation",document);
            userInformation.addContent(new Element("Status").addContent("1"));
            Element get=selectSingleNode("/Settings/UserInformation/Get",document);
            Element accounts=new Element("Accounts");
            Element account=new Element("Account");
            Element emailAddresses=new Element("EmailAddresses");
            emailAddresses.addContent(new Element("SMTPAddress").addContent(request.getUser()+"@"+request.getHost()));
            account.addContent(emailAddresses);
            accounts.addContent(account);
            get.addContent(accounts);
        
            HttpServletResponse response = request.getHttpServletResponse();
            sendWBXML(response,"Settings",document);
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
}
