package com.enterpriseios.push.web;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.enterpriseios.push.AccountDA;
import com.enterpriseios.push.DeviceManager;
import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

import javax.servlet.Servlet;

public class ActiveSyncServer
{
    private static final String pathActiveSync="/Microsoft-Server-ActiveSync";
    private Server server;
    private int port;
    private LoginService loginService;
    private DeviceManager deviceManager;
    private AccountDA accountDA;

    private Map<String,Class<? extends Servlet>> pathMap=new HashMap<String,Class<? extends Servlet>>();

    public void setServletMappings(Map<String,Class<? extends Servlet>> pathMap)
    {
        this.pathMap.putAll(pathMap);  
    }

    public void addServletMapping(String path,Class<? extends Servlet> servlet)
    {
        this.pathMap.put(path,servlet);
    }


    public void setLoginService(LoginService loginService)
    {
        this.loginService = loginService;
    }

    public void start() throws Exception
    {
        if (server==null)
            server=new Server();

        if (server.getConnectors()==null || server.getConnectors().length==0)
        {
            Connector connector=new SelectChannelConnector();
            if(port>0)
                connector.setPort(port);
            server.setConnectors(new Connector[]{connector});
        }

        HandlerCollection handlers = new HandlerCollection();


        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SECURITY);

        for (Map.Entry<String,Class<? extends Servlet>> entry: pathMap.entrySet())
        {
            String path=entry.getKey();
            Class servlet=entry.getValue();
           
            if (servlet.isAssignableFrom(ActiveSyncServlet.class) && !path.equals(pathActiveSync))
              path=pathActiveSync;

            if(!path.endsWith("/"))
                path=path+"/*";

            context.addServlet(new ServletHolder(servlet),path);
        }
        context.setAttribute(DeviceManager.ATTRIBUTE,deviceManager);
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"*"});
        constraint.setAuthenticate(true);
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");
        ConstraintSecurityHandler securityHandler = (ConstraintSecurityHandler)context.getSecurityHandler();
        securityHandler.setStrict(false);
        securityHandler.setConstraintMappings(Collections.singletonList(constraintMapping));
        securityHandler.setAuthenticator(new BasicAuthenticator());
        securityHandler.setLoginService(loginService);
        handlers.addHandler(context);
        
        WebAppContext webapp=new WebAppContext();
        webapp.setWar("webapps/jetty-push2-webapp.war");
        webapp.setContextPath("/");
        webapp.setAttribute(AccountDA.ATTRIBUTE,accountDA);
        webapp.setAttribute(DeviceManager.ATTRIBUTE,deviceManager);
        webapp.setExtractWAR(true);
        handlers.addHandler(webapp);
        server.setHandler(handlers);
        server.start();
    }

    public void stop() throws Exception
    {
        server.stop();
        server.join();
    }

    public void setServer(Server server)
    {
        this.server=server;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public void setDeviceManager(DeviceManager deviceManager)
    {
        this.deviceManager = deviceManager;
    }

    public void setAccountDA(AccountDA accountDA)
    {
        this.accountDA = accountDA;
    }
}