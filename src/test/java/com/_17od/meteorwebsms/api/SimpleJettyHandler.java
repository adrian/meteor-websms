package com._17od.meteorwebsms.api;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.handler.AbstractHandler;

public class SimpleJettyHandler extends AbstractHandler {

    private Hashtable<String, String> parameters;
    private String loginRedirectURL = "/mymeteor/system/view/home.cfm";
    private String sendMessageRedirectURL = "/mymeteor/system/view/smssent.cfm";
    private int numFreeMessagesLeft;
    
    
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
        Request baseRequest = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        baseRequest.setHandled(true);

        // Store the parameters so we can query them later
        parameters = new Hashtable<String, String>();
        Enumeration<?> parameterNames = baseRequest.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = (String) parameterNames.nextElement();
            parameters.put(parameterName, baseRequest.getParameter(parameterName));
            //System.out.println("assertEquals(\"" + baseRequest.getParameter(parameterName) + "\", handler.getParameters().get(\"" + parameterName + "\"));");
        }
        
        if (target.equals("/controller")) {
            String method = request.getParameter("method"); 
            if (method != null && method.equals("logIn")) {
                login(response);
            } else if (method != null && method.equals("SMS")) {
                sendMessage(response);
            }
        } else if (target.equals("/info")) {
            info(response);
        }
    }


    private void login(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("location", loginRedirectURL);
    }


    private void sendMessage(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("location", sendMessageRedirectURL);
    }


    private void info(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("You have <b>" + numFreeMessagesLeft + "</b> Free Web Texts");
    }

    
    public Hashtable<String, String> getParameters() {
        return parameters;
    }


    public void setLoginRedirectURL(String loginRedirectURL) {
        this.loginRedirectURL = loginRedirectURL;
    }

    
    public void setSendMessageRedirectURL(String sendMessageRedirectURL) {
        this.sendMessageRedirectURL = sendMessageRedirectURL;
    }

    
    public void setNumFreeMessagesLeft(int numFreeMessagesLeft) {
        this.numFreeMessagesLeft = numFreeMessagesLeft;
    }
    
}
