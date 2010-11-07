package com._17od.meteorwebsms.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;


/**
 * This class provides a programmatic API to Meteor's (http://www.mymeteor.ie)
 * web based SMS application.
 * <p>
 * At the time of writing, every Meteor subscriber receives 300 free web based
 * texts a month.
 * <p>
 * Example usage with no HTTP proxy:
 * <pre>
 *      Meteor meteor = new Meteor();
 *      MSISDN recipient = new MSISDN("0867654321");
 *      meteor.login("0851234567", "1234");
 *      meteor.sendMessage("hi u k?", recipient);
 *      meteor.logout();
 * </pre>
 * <p>
 * Example usage with HTTP proxy, no username or password required:
 * <pre>
 *      Meteor meteor = new Meteor("www-proxy.domain.com", 8080);
 *      MSISDN recipient = new MSISDN("0867654321");
 *      meteor.login("0851234567", "1234");
 *      meteor.sendMessage("hi u k?", recipient);
 *      meteor.logout();
 * </pre>
 * <p>
 * Example usage with HTTP proxy requiring username or password:
 * <pre>
 *      Meteor meteor = new Meteor("www-proxy.domain.com", 8080, "myuser", "mypass");
 *      MSISDN recipient = new MSISDN("0867654321");
 *      meteor.login("0851234567", "1234");
 *      meteor.sendMessage("hi u k?", recipient);
 *      meteor.logout();
 * </pre>
 * @author Adrian Smith
 */
public class Meteor implements Operator {

    private String meteorHost = "www.mymeteor.ie";
    private int meteorPort = 443;
    private String controllerURL = "https://" + meteorHost + ':' + meteorPort + "/mymeteor/system/controllers/controller.cfc";
    private String infoURL = "https://" + meteorHost + ':' + meteorPort + "/mymeteor/system/view/sms.cfm";
    private String logoutURL = "https://" + meteorHost + ':' + meteorPort + "/mymeteor/logout.cfm";

    private HttpClient httpClient;

    private boolean loggedIn = false;


    public Meteor() {
        this(null, 0, null, null);
    }


    /**
     * 
     * @param proxyHostname
     * @param proxyPort
     */
    public Meteor(String proxyHostname, int proxyPort) {
        this(proxyHostname, proxyPort, null, null);
    }

    
    /**
     * This constructor initialises the proxy if necessary
     * @param proxyHostname
     * @param proxyPort
     * @param proxyUsername
     * @param proxyPassword
     */
    public Meteor(String proxyHostname, int proxyPort, String proxyUsername, String proxyPassword) {

        // Create the Apache HTTPClient object
        httpClient = new HttpClient();
        
        // Initialise the proxy if we're using one
        if (proxyHostname != null) {
            httpClient.getHostConfiguration().setProxy(proxyHostname, proxyPort);
            if (proxyUsername != null) {
            	Credentials credentials = new UsernamePasswordCredentials(proxyUsername, proxyPassword); 
                httpClient.getState().setProxyCredentials(new AuthScope(proxyHostname, proxyPort), credentials);

                // We need to set preemptive authentication to get the proxy authentication to work, 
                // presumably because we'd have to handle 407 responses otherwise?
                // In doing this we also need to provide credentials for the meteor site or we'll get
                // a warning saying there are no default credentials
                UsernamePasswordCredentials blankCredentials = new UsernamePasswordCredentials("", null); 
                httpClient.getState().setCredentials(new AuthScope(meteorHost, meteorPort), blankCredentials);
                httpClient.getParams().setAuthenticationPreemptive(true);
            }
        }

    }
    
    
    /**
     * Login to the Meteor website using the given username and password
     * @param username
     * @param password
     * @throws OperatorException 
     */
    public void login(String username, String password) throws OperatorException {

        // Logon to the Meteor website
        PostMethod loginMethod = new PostMethod(controllerURL);
        loginMethod.addParameter("method", "logIn");
        loginMethod.addParameter("returnTo", "/mymeteor/login.cfm");
        loginMethod.addParameter("msisdn", username);
        loginMethod.addParameter("pin", password);
        
        try {
            int status = httpClient.executeMethod(loginMethod);
            
            // Meteor should have returned a 302 status code with a new location of
            // /mymeteor/system/view/home.cfm
            // If we didn't get this then we're in unknown waters so throw an error
            String redirectURL = null;
            Header header = loginMethod.getResponseHeader("location");
            if (header != null) {
                redirectURL = header.getValue();
            }
            if (header == null || (status != 302 && !redirectURL.equals("/mymeteor/system/view/home.cfm"))) {
                throw new OperatorException("Didn't get the expected login response from Meteor. HTTP status code [" + status + "]. HTTP status message [" + HttpStatus.getStatusText(status) + "]");
            }
            
            // Check for invalid login credentials
            if (redirectURL.equals("/mymeteor/login.cfm?login=invalid")) {
                throw new OperatorException("Invalid username or password"); 
            }
            
            loggedIn = true;
        } catch (IOException e) {
            throw new OperatorException("Problem logging into the Meteor website", e);
        }
    }
    
    
    /**
     * Send the given message to the given msisdn.
     * @param message The message you want to send
     * @param recipient The recipient of the message
     */
    public void sendMessage(String message, MSISDN recipient) throws OperatorException {

        if (!loggedIn) {
            throw new OperatorException("Must be logged into the Meteor website to perform this operation");
        }

        if (message.length() > 160 ) {
            throw new OperatorException("Message must be <= 160 characters in length");
        }

        // Free texts may only be send to irish mobile subscribers
        if (!recipient.isIrishMobile()) {
            throw new OperatorException("Messages can only be sent to irish mobiles");
        }

        // Build up a PostMethod with the parameters exactly as they are on the HTML FORM
        PostMethod sendSMSMethod = new PostMethod(controllerURL);
        sendSMSMethod.addParameter("method", "SMS");
        sendSMSMethod.addParameter("returnTo", "/mymeteor/system/view/sms.cfm");
        sendSMSMethod.addParameter("formName", "smsForm");
        sendSMSMethod.addParameter("action", "Send SMS");
        sendSMSMethod.addParameter("clicked", "1");
        sendSMSMethod.addParameter("sms_text", message);
        sendSMSMethod.addParameter("num_left", String.valueOf(160 - message.length()));
        sendSMSMethod.addParameter("msisdn", recipient.getNationalNumber());
        sendSMSMethod.addParameter("phonebook", "Select from Phonebook");
        sendSMSMethod.addParameter("sendSMS", "Send SMS");
        
        try {
            int status = httpClient.executeMethod(sendSMSMethod);
            
            // Meteor should have returned a 302 status code with a new location of
            // /mymeteor/system/view/smssent.cfm
            // If we didn't get this then we're in unknown waters so throw an error
            String redirectURL = sendSMSMethod.getResponseHeader("location").getValue();
            if (status != 302 || !redirectURL.equals("/mymeteor/system/view/smssent.cfm")) {
                throw new OperatorException("Didn't get the expected response from Meteor's sendSMS URL. HTTP status code [" + status + "]. HTTP status message [" + HttpStatus.getStatusText(status) + "]");
            }
        } catch (IOException e) {
            throw new OperatorException("Problem sending message", e);
        }

	}

    
    /**
     * Get the number of free text messages left
     * <p>
     * This method works by retrieving the page that contains the number
     * of texts you have left and using a regular expression to pull out 
     * the number
     * <p>  
     * @return
     * @throws OperatorException
     */
    public int getNumFreeMessagesLeft() throws OperatorException {
        
        if (!loggedIn) {
            throw new OperatorException("Must be logged into the Meteor website to perform this operation");
        }

        int numFreeMessagesLeft = 0;

        GetMethod messagesLeftMethod = new GetMethod(infoURL);

        try {
            int status = httpClient.executeMethod(messagesLeftMethod);

            // Check we got a good response
            if (status != HttpStatus.SC_OK) {
                throw new OperatorException("Didn't get the expected response from the info URL. HTTP status code [" + status + "]. HTTP status message [" + HttpStatus.getStatusText(status) + "]");
            }
            
            // Parse the response and take out the number of messages left
            InputStream is = messagesLeftMethod.getResponseBodyAsStream();
            String responseText = getStringFromStream(is);
            
            String pattern = "You have <b>(\\d+)</b> Free Web Texts";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(responseText);
            if (m.find()) {
                numFreeMessagesLeft = Integer.parseInt(m.group(1));
            } else {
                throw new OperatorException("Didn't get the expected response HTML " + responseText);
            }
        } catch (IOException e) {
            throw new OperatorException("Problem retrieving the number of free text messages left", e);
        }

        return numFreeMessagesLeft;

    }

    
    /**
     * Logout of the Meteor website. It's probably not absolutely
     * necessary to do this but it's no harm and it may help tidy up
     * session stuff on Meteor's side.
     * @throws OperatorException 
     */
    public void logout() throws OperatorException {

        GetMethod logoutMethod = new GetMethod(logoutURL);
        
        try {
            int status = httpClient.executeMethod(logoutMethod);

            // Check we got a good response
            if (status != HttpStatus.SC_OK) {
                throw new OperatorException("Didn't get the expected response from the logout URL. HTTP status code [" + status + "]. HTTP status message [" + HttpStatus.getStatusText(status) + "]");
            }
            
            loggedIn = false;
        } catch (IOException e) {
            throw new OperatorException("Problem logging out", e);
        }

    }


    public String getControllerURL() {
        return controllerURL;
    }


    public void setControllerURL(String controllerURL) {
        this.controllerURL = controllerURL;
    }


    public String getInfoURL() {
        return infoURL;
    }


    public void setInfoURL(String infoURL) {
        this.infoURL = infoURL;
    }


    public String getLogoutURL() {
        return logoutURL;
    }


    public void setLogoutURL(String logoutURL) {
        this.logoutURL = logoutURL;
    }

    
    private String getStringFromStream(InputStream is) throws IOException {
        StringBuffer buffer = new StringBuffer();
        int c = is.read();
        while (c != -1) {
            buffer.append((char) c);
            c = is.read();
        }
        return buffer.toString();
    }

}
