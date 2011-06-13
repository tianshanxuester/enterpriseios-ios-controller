package com.enterpriseios.push.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.enterpriseios.push.*;
import com.enterpriseios.push.web.wbxml.WBXMLTools;
import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.http.security.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.eclipse.jetty.util.log.Log;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveSyncServlet extends HttpServlet
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private DeviceManager deviceManager;

    @Override
    public void init() throws ServletException
    {
        deviceManager = (DeviceManager)getServletContext().getAttribute(DeviceManager.ATTRIBUTE);
        
    }

    @Override
    protected void doGet(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException
    {
         sendOptionsResponse(httpResponse);    
    }

    @Override
    protected void doOptions(HttpServletRequest httpRequest, HttpServletResponse httpResponse)throws ServletException, IOException
    {
        sendOptionsResponse(httpResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException, IOException
    {

        Request request = (Request)httpRequest.getAttribute(Device.REQUEST_ATTRIBUTE);
        if (request == null)
        {
            try
            {

                request = createRequest(httpRequest, httpResponse);

                String queryString = request.getQueryString();

                if (queryString == null)
                {
                    logger.warn("No queryString");
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                
                Command cmd=request.getCommand();

                if (cmd == null)
                {
                    logger.warn("POST received without explicit command, aborting");
                    httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }

                String policy = httpRequest.getParameter("X-Ms-PolicyKey");

                if (policy != null && policy.equals("0") && !cmd.equals("Provision"))
                {
                    // force device provisioning
                    logger.info("Enforcing Provision on device "+request.getDeviceId());
                    httpResponse.setStatus(449);
                    return;
                }

            }
            catch (Exception e)
            {
                logger.warn("failed to create Request",e);
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
        }

        try
        {
            deviceManager.handle(request);
        }
        catch (ProtocolException e)
        {
            httpResponse.sendError(449);
        }
    }

    private Request createRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException
    {
        Request request = new Request(httpRequest, httpResponse);
        
        //parse the request query string and set
        String queryString=httpRequest.getQueryString();
        String credentials=httpRequest.getHeader(HttpHeaders.AUTHORIZATION);

        if (credentials != null)
        {
            //username may have 'domain' prefix so remove and recompose it.
            credentials = credentials.substring(credentials.indexOf(' ')+1);
            credentials = B64Code.decode(credentials, StringUtil.__ISO_8859_1);
            int i = credentials.indexOf(':');
            String username = removeDomainPart(credentials.substring(0,i));
            String password = credentials.substring(i+1);
            credentials=B64Code.encode(username+":"+password,StringUtil.__ISO_8859_1);
            request.putParameter(Request.AUTHORIZATION,credentials);
        }

        request.putParameter(Request.HOST,httpRequest.getHeader(HttpHeaders.HOST));

        if (queryString!=null && !queryString.contains("Cmd="))
        {
            Base64QueryParser queryParser=new Base64QueryParser(httpRequest);
            request.putParameter(Request.COMMAND_KEY,queryParser.getParameter("Cmd"));
            request.putParameter(Request.USER_KEY,queryParser.getParameter("User"));
            request.putParameter(Request.DEVICE_ID_KEY,queryParser.getParameter("DeviceId"));
            request.putParameter(Request.DEVICE_TYPE_KEY,queryParser.getParameter("DeviceType"));
        }
        else
        {
            request.putParameter(Request.COMMAND_KEY,httpRequest.getParameter("Cmd"));
            request.putParameter(Request.USER_KEY,httpRequest.getParameter("User"));
            request.putParameter(Request.DEVICE_ID_KEY,httpRequest.getParameter("DeviceId"));
            request.putParameter(Request.DEVICE_TYPE_KEY,httpRequest.getParameter("DeviceType"));
        }
        
        //parse the body depending on the content type (wbxml or plain xml)
        Document content=getDocument(httpRequest);
        request.setXML(content);

        return request;
    }

    private Document getDocument(HttpServletRequest httpRequest) throws IOException
    {
		Document document = null;

		String contentType = httpRequest.getContentType();
		if (contentType != null) {
			if (contentType.equalsIgnoreCase("application/vnd.ms-sync.wbxml")) {
				InputStream input = httpRequest.getInputStream();

				if (input!=null && httpRequest.getContentLength()>0) {
					document = WBXMLTools.toXml(input);
				}

				if (document != null) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
				    XMLOutputter outputter=new XMLOutputter(Format.getPrettyFormat());
                    outputter.output(document, out);
		            logger.debug("from device:\n" + out.toString());
			} else {

                SAXBuilder builder=new SAXBuilder();
                    try {
                        document=builder.build(httpRequest.getInputStream());
                    } catch (JDOMException e) {

                    }
                }
            }
        }
		return document;
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

    private void sendOptionsResponse(HttpServletResponse httpResponse)
    {
        httpResponse.setStatus(200);
        httpResponse.setHeader("Server", "Microsoft-IIS/7.5");
        httpResponse.setHeader("MS-Server-ActiveSync", "14.1");
        httpResponse
//                .setHeader("MS-ASProtocolVersions", "1.0,2.0,2.1,2.5,12.0,12.1,14.0");
                .setHeader("MS-ASProtocolVersions", "12.1,14.0,14.1");
        httpResponse
                .setHeader(
                        "MS-ASProtocolCommands",
                        "Sync,SendMail,SmartForward,SmartReply,GetAttachment,GetHierarchy,CreateCollection,DeleteCollection,MoveCollection,FolderSync,FolderCreate,FolderDelete,FolderUpdate,MoveItems,GetItemEstimate,MeetingResponse,Search,Settings,Ping,ItemOperations,Provision,ResolveRecipients,ValidateCert");
        httpResponse.setHeader("Public", "OPTIONS,POST");
        httpResponse.setHeader("Allow", "OPTIONS,POST");
        httpResponse.setHeader("Cache-Control", "private");
        httpResponse.setContentLength(0);
    }

}
