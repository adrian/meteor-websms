package com._17od.meteorwebsms.client;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com._17od.meteorwebsms.api.MSISDN;
import com._17od.meteorwebsms.api.MSISDNParseException;
import com._17od.meteorwebsms.api.Meteor;
import com._17od.meteorwebsms.api.OperatorException;

public class MeteorSMSClient {

    // The properties file to use. The leading '/' means look in the root of the classpath
    private static final String PROPERTIES_FILE = "/mwsms.properties";

	private String message;
	private String fileWithMessage;
	private String recipient;
	private String username;
	private String password;
    private String httpProxy;
    private String httpProxyUsername;
    private String httpProxyPassword;
    private boolean help;
    private boolean free;
    private boolean quiet;
    
	private Options options;
	
	private String httpProxyHost = null;
    private int httpProxyPort = 8080;


	public MeteorSMSClient(String args[]) throws ParseException, IOException {
        // Attempt to read properties from the properties file
	    readPropertiesFromFile();
        
	    // Read the parameters given on the command line
	    readPropertiesFromCommandLine(args);

	    // Extract the proxy host and port
	    extractProxyInfo();
	}


	/**
	 * This method attempts to read the properties from a file on the CLASSPATH
	 * called mwsms.properties.
	 * @throws IOException 
	 */
	private void readPropertiesFromFile() throws IOException {
	    InputStream inStream = getClass().getResourceAsStream(PROPERTIES_FILE);
	    if (inStream != null) {
    	    Properties properties = new Properties();
    	    properties.load(inStream);
    	    
            message = properties.getProperty("message");
            fileWithMessage = properties.getProperty("message-file");
            recipient = properties.getProperty("recipient");
            username = properties.getProperty("username");
            password = properties.getProperty("password");
            httpProxy = properties.getProperty("httpProxy");
            httpProxyUsername = properties.getProperty("httpProxyUsername");
            httpProxyPassword = properties.getProperty("httpProxyPassword");
            
            inStream.close();
	    }
	}


	/**
	 * Read the properties given on the command into the instance variables
	 * @throws ParseException 
	 */
	private void readPropertiesFromCommandLine(String args[]) throws ParseException {
        CommandLine cmd = parseCommandLine(args);
        
        if (cmd.hasOption('h')) {
            help = true;
        }

        // There's no point wasting time parsing the rest of the parameters 
        // if the user only wants to see the help message
        if (!help) {
            message = cmd.getOptionValue('m') != null ? cmd.getOptionValue('m') : message;
            fileWithMessage = cmd.getOptionValue('F') != null ? cmd.getOptionValue('F') : fileWithMessage;
            recipient = cmd.getOptionValue('r') != null ? cmd.getOptionValue('r') : recipient;
            username = cmd.getOptionValue('u') != null ? cmd.getOptionValue('u') : username;
            password = cmd.getOptionValue('p') != null ? cmd.getOptionValue('p') : password;
            httpProxy = cmd.getOptionValue("proxy") != null ? cmd.getOptionValue("proxy") : httpProxy;
            httpProxyUsername = cmd.getOptionValue("proxy-username") != null ? cmd.getOptionValue("proxy-username") : httpProxyUsername;
            httpProxyPassword = cmd.getOptionValue("proxy-password") != null ? cmd.getOptionValue("proxy-password") : httpProxyPassword;

            if (cmd.hasOption('f')) {
                free = true;
            }
            
            if (cmd.hasOption('q')) {
                quiet = true;
            }
        }
	}
	

	@SuppressWarnings("static-access")
    private CommandLine parseCommandLine(String args[]) throws ParseException {
		options = new Options();
		
		Option username = OptionBuilder.withLongOpt("username")
            .hasArg()
            .withArgName("username") 
            .withDescription("your mobile number")
            .create('u');
        options.addOption(username);
    
        Option password = OptionBuilder.withLongOpt("password")
            .hasArg()
            .withArgName("password") 
            .withDescription("your password/pin - if not supplied you'll be prompted")
            .create('p');
        options.addOption(password);
		
        Option recipient = OptionBuilder.withLongOpt("recipient")
            .hasArg()
            .withArgName("recipient") 
            .withDescription("the recipient's number, accepts +{CC}{NDC}{SN} or 00{CC}{NDC}{SN} or {NDC}{SN}")
            .create('r');
        options.addOption(recipient);

        Option message = OptionBuilder.withLongOpt("message")
            .hasArg()
            .withArgName("message") 
            .withDescription("the message to send")
            .create('m');
        options.addOption(message);
		
        Option messageFile = OptionBuilder.withLongOpt("messageFile")
            .hasArg()
            .withArgName("file") 
            .withDescription("file containing the message to send")
            .create('F');
        options.addOption(messageFile);
        
        Option httpProxy = OptionBuilder.withLongOpt("proxy")
            .hasArg()
            .withArgName("proxy") 
            .withDescription("http proxy in the format <server>:<port> - port defaults to 8080 if not given")
            .create();
        options.addOption(httpProxy);

        Option httpProxyUsername = OptionBuilder.withLongOpt("proxy-username")
            .hasArg()
            .withArgName("proxy username") 
            .withDescription("http proxy username")
            .create();
        options.addOption(httpProxyUsername);

        Option httpProxyPassword = OptionBuilder.withLongOpt("proxy-password")
            .hasArg()
            .withArgName("proxy password") 
            .withDescription("http proxy password")
            .create();
        options.addOption(httpProxyPassword);

        options.addOption("h", "help", false, "show this help message");
        options.addOption("f", "free", false, "report back the number of free messages left this month");
        options.addOption("q", "quiet", false, "Operate quietly");

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse(options, args);
		
		return cmd;
	}


	private boolean checkForRequiredParameters() {
	    boolean haveRequiredParameters = true;
	    
	    // We always need the user name and password
	    if (username == null || password == null) {
	        haveRequiredParameters = false;
	    }
	    
	    // If we're sending a message we need the recipient and the message
	    if (!free) {
	        if (recipient == null || (message == null && fileWithMessage == null)) {
	            haveRequiredParameters = false;
    	    }
	    }
	    
	    return haveRequiredParameters;
	}

	
	private void printUsage() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("mwsms", options);
        System.out.println("\nusername and password are always required");
        System.out.println("recipient and message OR messageFile are required for sending a message");
	}


    private void extractProxyInfo() {
        if (httpProxy != null) {
            String[] proxyParts = httpProxy.split(":");
            httpProxyHost = proxyParts[0];
            if (proxyParts.length > 1) {
                httpProxyPort = Integer.parseInt(proxyParts[1]);
            }
        }
    }


	private void sendMessage() throws IOException, OperatorException, MSISDNParseException {
        // Get the message
        if (message == null) {
            FileReader fileReader = new FileReader(fileWithMessage);
            StringBuffer buf = new StringBuffer();
            int c = fileReader.read();
            while (c != -1) {
                c = fileReader.read();
                buf.append((char) c);
            }
            fileReader.close();
            message = buf.toString();
        }

        // Send the message
        Meteor meteor = new Meteor(httpProxyHost, httpProxyPort, httpProxyUsername, httpProxyPassword);
        meteor.login(username, password);
        meteor.sendMessage(message, new MSISDN(recipient));
        meteor.logout();
	}


	/**
	 * Retrieve the number of free messages left
	 * @throws OperatorException 
	 */
	private int getNumFreeMessages() throws OperatorException {
	    int numFreeMessagesLeft = 0;
	    
        // Call meter to get the number of free messages left
        Meteor meteor = new Meteor(httpProxyHost, httpProxyPort, httpProxyUsername, httpProxyPassword);
        meteor.login(username, password);
        numFreeMessagesLeft = meteor.getNumFreeMessagesLeft();
        meteor.logout();
        
        return numFreeMessagesLeft;
	}


    public static void main(String args[]) throws ParseException, IOException, OperatorException, MSISDNParseException {
        MeteorSMSClient client = new MeteorSMSClient(args);

        if (!client.checkForRequiredParameters()) {
            System.err.println("Required parameters missing\n");
            client.printUsage();
            System.exit(1);
        } else {
            try {

                // user asked for help
                if (client.help) {
                    
                    client.printUsage();
                    
                // user asked for a report on the number of free messages left                    
                } else if (client.free) {
                    
                        int numFreeMessages = client.getNumFreeMessages();
                        System.out.println(numFreeMessages);
                    
                // user asked to send a message
                } else {
                    
                    client.sendMessage();
                    if (!client.quiet) {
                        System.out.println("Message send successfully");
                    }
                }

            } catch (OperatorException e) {
                if (e.getCause() instanceof SSLHandshakeException) {
                    System.err.println("SSL handshake exception - have you installed the keystore as per the installation instructions?");
                    System.exit(1);
                } else {
                    throw e;
                }
            }

        }

    }

}
