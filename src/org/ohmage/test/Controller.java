package org.ohmage.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.test.auth.AuthenticationApiTest;
import org.ohmage.test.user.UserApiTests;

/**
 * This class is the main controller for testing. There is no constructor and 
 * is meant to be called from the command line or an IDE.
 * 
 * @author John Jenkins
 */
public class Controller {
	public static final String CLIENT = "TestSuiteClient";
	
	private static String adminUsername = null;
	private static String adminPassword = null;
	
	/**
	 * Default constructor made private to prevent instantiation.
	 */
	private Controller() {}
	
	/**
	 * Initializes the server connection and runs the tests.
	 * 
	 * @param args The arguments as defined in {@link #printUsage()}.
	 */
	public static void main(final String[] args) throws ApiException {
		OhmageApi api = parseArguments(args);
		if(api == null) {
			return;
		}

		long start = (new Date()).getTime();
		
		// TODO: We will want some sort of cascading cleanup here.
		try {
			// Begin by testing authentication with the admin information and then
			// testing authentication tokens.
			AuthenticationApiTest authTest = new AuthenticationApiTest(api, adminUsername, adminPassword);
			authTest.test();
		}
		catch(ApiException e) {
			// There is nothing to cleanup, we output the exception and quit.
			e.printStackTrace();
			return;
		}
		
		// Now that we know they work, get the hashed password and an 
		// authentication token for the rest of the tests to use.
		String hashedPassword = api.getHashedPassword(adminUsername, adminPassword, CLIENT);
		String authToken = api.getAuthenticationToken(adminUsername, adminPassword, CLIENT);
		
		// Now, start testing the users and end with some users left in the
		// database.
		try {
			UserApiTests userTest = new UserApiTests(api, authToken);
			userTest.test();
		}
		catch(ApiException e) {
			// There is nothing to cleanup, we output the exception and quit.
			e.printStackTrace();
			return;
		}
		
		// Now, test the classes. Again, leave some with different assortments
		// of users with different assortments of permissions.
		
		// Next, begin testing the documents. Create some, share some, delete
		// some.
		
		// Test Mobility. Create points, and test the readability and 
		// writability of certain users.
		
		// Test the campaigns. Create some, connect them to classes, test the
		// readability and writability of users.
		
		// Finally, test the survey responses. Create some, change privacy
		// states, test readability and writability.
		
		// Note: UserApiTests cannot test user/stats/read until there are 
		// Mobility points and survey responses for the current user.
		
		// Note: UserApiTests cannot test user/read until some user(s) has been
		// assigned to classes and campaigns.
		
		// Cleanup.
		
		long end = (new Date()).getTime();
		
		System.out.println("All tests passed in " + (end - start) + " milliseconds.");
	}
	
	/**
	 * The arguments necessary to create the server connection.
	 * 
	 * @param args The string array of arguments given to us by the user.
	 * 
	 * @return An OhmageApi object ready to make requests to a server or null 
	 * 		   if there was an error.
	 */
	private static OhmageApi parseArguments(final String[] args) {
		String address = null;
		Integer port = null;
		boolean secure = false;
		
		for(int i = 0; i < args.length; i++) {
			if("-d".equals(args[i]) || "--domain".equals(args[i])){
				if(args.length < i+1) {
					System.out.println("The domain is missing.");
					printUsage();
					return null;
				}
				
				i++;
				address = args[i];
			}
			else if("-u".equals(args[i]) || "--username".equals(args[i])) {
				if(args.length < i+1) {
					System.out.println("The admin username is missing.");
					printUsage();
					return null;
				}
				
				i++;
				adminUsername = args[i];
			}
			else if("-p".equals(args[i]) || "--password".equals(args[i])) {
				if(args.length < i+1) {
					System.out.println("The admin password is missing.");
					printUsage();
					return null;
				}
				
				i++;
				adminPassword = args[i];
			}
			else if("-o".equals(args[i]) || "--port".equals(args[i])) {
				if(args.length < i+1) {
					System.out.println("The port flag was set, but the port was not given.");
					printUsage();
					return null;
				}
				
				try {
					i++;
					port = new Integer(Short.decode(args[i]));
				}
				catch(NumberFormatException e) {
					System.out.println("The port is not a valid port number.");
					printUsage();
					return null;
				}
			}
			else if("-s".equals(args[i]) || "--secure".equals(args[i])) {
				secure = true;
			}
		}
		
		if(address == null) {
			System.out.println("The server domain is missing.");
			printUsage();
			return null;
		}
		else if(adminUsername == null) {
			System.out.println("The admin username is missing.");
			printUsage();
			return null;
		}
		else if(adminPassword == null) {
			System.out.println("The admin password is missing.");
			printUsage();
			return null;
		}
		
		return new OhmageApi(address, port, secure);
	}
	
	/**
	 * Prints the application's usage to the standard output.
	 */
	private static void printUsage() {
		System.out.println("Parameters: <serer domain> (-u || --username) <admin username> (-p || --password) <admin password> [(-o || --port) <port>] [(-s || --secure)]");
		System.out.println("\t-u --username The username of an admin user.");
		System.out.println("\t-p --password The plain-text password of that admin user.");
		System.out.println("\t-p --port  Use this port instead of the standard HTTP or HTTPS port.");
		System.out.println("\t-s --secure Use a secure connection (HTTPS); otherwise, HTTP is used.");
	}
	
	/**
	 * Returns the contents of a file as a byte array.
	 * 
	 * @param filename The XML's filename.
	 * 
	 * @return The XML as a String.
	 * 
	 * @throws FileNotFoundException Thrown if the filename does not point to a
	 * 								 file on the filesystem.
	 * 
	 * @throws IOException Thrown if there is an issue reading from the file.
	 */
	private static byte[] readFile(final String filename) throws FileNotFoundException, IOException {
		File file = new File(filename);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] chunk = new byte[4096];
		InputStream is = new FileInputStream(file);
		int amountRead;
		while((amountRead = is.read(chunk)) != -1) {
			baos.write(chunk, 0, amountRead);
		}
		
		return baos.toByteArray();
	}
}
