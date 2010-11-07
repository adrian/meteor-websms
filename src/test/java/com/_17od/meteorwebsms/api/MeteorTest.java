package com._17od.meteorwebsms.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Server;


public class MeteorTest {

    private static Server server;
    private static SimpleJettyHandler handler;


    @BeforeClass
    public static void startJetty() throws Exception {
        server = new Server(8080);
        server.start();
    }


    @Before
    public void initialiseJetty() {
        handler = new SimpleJettyHandler();
        server.setHandler(handler);
    }


    @Test
    public void shouldSendTheCorrectLoginParameters() throws OperatorException, MSISDNParseException {
        Meteor meteorOperator = new Meteor();
        meteorOperator.setControllerURL("http://localhost:8080/controller");
        
        meteorOperator.login("meteorUsername", "meteorPassword");
        
        assertNotNull(handler.getParameters());
        assertEquals("logIn", handler.getParameters().get("method"));
        assertEquals("/mymeteor/login.cfm", handler.getParameters().get("returnTo"));
        assertEquals("meteorUsername", handler.getParameters().get("msisdn"));
        assertEquals("meteorPassword", handler.getParameters().get("pin"));
    }

    
    @Test (expected = OperatorException.class)
    public void shouldGetInvalidLogin() throws OperatorException, MSISDNParseException {
        Meteor meteorOperator = new Meteor();
        meteorOperator.setControllerURL("http://localhost:8080/controller");
        handler.setLoginRedirectURL("/mymeteor/login.cfm?login=invalid");
        
        meteorOperator.login("meteorUsername", "meteorPassword");
    }


    @Test
    public void shouldGetTheCorrectSendMessageParameters() throws OperatorException, MSISDNParseException {
        Meteor meteorOperator = new Meteor();
        meteorOperator.setControllerURL("http://localhost:8080/controller");
        
        meteorOperator.login("meteorUsername", "meteorPassword");
        meteorOperator.sendMessage("hello world", new MSISDN("0861234567"));
        
        assertNotNull(handler.getParameters());
        assertEquals("hello world", handler.getParameters().get("sms_text"));
        assertEquals("/mymeteor/system/view/sms.cfm", handler.getParameters().get("returnTo"));
        assertEquals("0861234567", handler.getParameters().get("msisdn"));
        assertEquals("Send SMS", handler.getParameters().get("sendSMS"));
        assertEquals("Select from Phonebook", handler.getParameters().get("phonebook"));
        assertEquals("smsForm", handler.getParameters().get("formName"));
        assertEquals("Send SMS", handler.getParameters().get("action"));
        assertEquals("SMS", handler.getParameters().get("method"));
        assertEquals("149", handler.getParameters().get("num_left"));
        assertEquals("1", handler.getParameters().get("clicked"));
    }

    
    @Test (expected = OperatorException.class)
    public void shouldGetMessageNotSendException() throws OperatorException, MSISDNParseException {
        Meteor meteorOperator = new Meteor();
        meteorOperator.setControllerURL("http://localhost:8080/controller");
        handler.setSendMessageRedirectURL("/invalidurl");
        
        meteorOperator.login("meteorUsername", "meteorPassword");
        meteorOperator.sendMessage("hello world", new MSISDN("0861234567"));
    }


    @Test
    public void shouldGetTheCorrectNumberOfMessagesLeft() throws OperatorException, MSISDNParseException {
        Meteor meteorOperator = new Meteor();
        meteorOperator.setControllerURL("http://localhost:8080/controller");
        meteorOperator.setInfoURL("http://localhost:8080/info");
        handler.setNumFreeMessagesLeft(29);
        
        meteorOperator.login("meteorUsername", "meteorPassword");
        int numLeft = meteorOperator.getNumFreeMessagesLeft();
        
        assertEquals(29, numLeft);
    }

    @AfterClass
    public static void logoutOfMeteor() throws Exception {
        server.stop();
    }

}
