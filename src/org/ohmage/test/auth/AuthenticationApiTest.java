package org.ohmage.test.auth;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.test.Controller;

/**
 * This class is responsible for testing the authentication APIs.
 * 
 * @author John Jenkins
 */
public class AuthenticationApiTest {
	private final OhmageApi api;
	private final String username;
	private final String password;
	
	/**
	 * Creates a tester for the authentication APIs.
	 * 
	 * @param api The ohmage API object that should already be setup with a
	 * 			  connection to a server.
	 * 
	 * @param username The username of the user to test.
	 * 
	 * @param password The password of the user to test.
	 */
	public AuthenticationApiTest(final OhmageApi api, 
			final String username, final String password) {
		
		this.api = api;
		this.username = username;
		this.password = password;
	}
	
	/**
	 * Tests all of the possible authentication protocols.
	 * 
	 * @throws ApiException Thrown if one of the tests fail or if there is a 
	 * 						bug in the library.
	 */
	public void test() throws ApiException {
		testAuthentication();
		testAuthToken();
	}
	
	/**
	 * Tests the different possibilities for getting an user's hashed password.
	 * 
	 * @throws ApiException Thrown if an incorrect error code is returned or
	 * 						there is a bug in the library.
	 */
	private void testAuthentication() throws ApiException {

		// Test no username.
		try {
			api.getHashedPassword(null, null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: No username.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test an invalid username.
		try {
			api.getHashedPassword("blah", null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: Invalid username.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test no password.
		try {
			api.getHashedPassword(username, null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: No password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test no client with invalid password.
		try {
			api.getHashedPassword(username, "blah", null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: No client with invalid password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test invalid password.
		try {
			api.getHashedPassword(username, "blah", Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			// Note: This feels like it should be an invalid password, but
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: Invalid password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test no client with valid password.
		try {
			api.getHashedPassword(username, password, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				System.out.println("Failed: Auth: No client with valid password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// This request should be valid.
		try {
			api.getHashedPassword(username, password, Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			System.out.println("This should not have been an error.");
			throw e;
		}
	}
	
	/**
	 * Tests the different possibilities for getting an authentication token.
	 * 
	 * @throws ApiException Thrown if an incorrect error code is returned or
	 * 						there is a bug in the library.
	 */
	private void testAuthToken() throws ApiException {
		// Test no username.
		try {
			api.getAuthenticationToken(null, null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: No username.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}
		
		// Test an invalid username.
		try {
			api.getAuthenticationToken("blah", null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: Invalid username.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}

		// Test no password
		try {
			api.getAuthenticationToken(username, null, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: No password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}

		// Test no client with invalid password
		try {
			api.getAuthenticationToken(username, "blah", null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: Invalid password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}

		// Test invalid password
		try {
			api.getAuthenticationToken(username, "blah", Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: Invalid password.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}

		// Test no client with valid password.
		try {
			api.getAuthenticationToken(username, password, null);
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				System.out.println("Failed: AuthToken: No client.");
				System.out.println("This should have been error code '" + ErrorCodes.AUTHENTICATION_FAILED + "'.");
				throw e;
			}
		}

		// This request should be valid.
		try {
			api.getAuthenticationToken(username, password, Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			System.out.println("This should not have been an error.");
			throw e;
		}
	}
}