package com.enterpriseios.push;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpHeaders;
import org.jdom.Document;

public class Request
{
    public static final String HOST="Host";
    public static final String COMMAND_KEY = "Cmd";
    public static final String AUTHORIZATION=HttpHeaders.AUTHORIZATION;
    public static final String POLICY_KEY="X-MS-PolicyKey";
    public static final String DEVICE_ID_KEY = "DeviceId";
    public static final String DEVICE_TYPE_KEY = "DeviceType";
    public static final String USER_KEY="UserId";
    private final HttpServletRequest httpRequest;
    private final HttpServletResponse httpResponse;
    private final Map<String, String> parameters = new HashMap<String, String>();
    private Document document;


    public Request(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
    {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public void putParameter(String name, String value)
    {
        parameters.put(name, value);
    }

    public String getDeviceId()
    {
        return parameters.get(DEVICE_ID_KEY);
    }

    public String getHost()
    {
        return parameters.get(HOST);
    }

    public String getUser()
    {
        return parameters.get(USER_KEY);
    }

    public String getBasicAuth()
    {
        return parameters.get(AUTHORIZATION);  
    }

    public String getPolicyKey()
    {
        return parameters.get(POLICY_KEY);
    }

    public String getQueryString()
    {
        return httpRequest.getQueryString();
    }

    public String getDeviceType()
    {
        return parameters.get(DEVICE_TYPE_KEY);
    }

    public Document getXML()
    {
        return document;
    }

    public void setXML(Document document)
    {
        this.document = document;
    }

    public Command getCommand()
    {
        String commandName = parameters.get(COMMAND_KEY);
        if (commandName == null)
            return null;
        try
        {
            return Command.valueOf(commandName.toUpperCase());
        }
        catch(IllegalArgumentException e)
        {   e.printStackTrace();
            //requested command not implemented
        }
        return null;
    }

    public HttpServletRequest getHttpServletRequest()
    {
        return httpRequest;
    }

    public HttpServletResponse getHttpServletResponse()
    {
        return httpResponse;
    }
}
