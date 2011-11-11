package org.ohmage.test.auth;

import org.ohmage.annotator.Annotator.ErrorCode;
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
	
	/**
	 * Creates a tester for the authentication APIs.
	 * 
	 * @param api The ohmage API object that should already be setup with a
	 * 			  connection to a server.
	 */
	public AuthenticationApiTest(final OhmageApi api) {
		this.api = api;
	}
	
	/**
	 * Tests all of the possible authentication protocols.
	 * 
	 * @param username The username of the user to test.
	 * 
	 * @param password The password of the user to test.
	 * 
	 * @throws ApiException Thrown if there is a bug in the library.
	 * 
	 * @throws IllegalStateException Thrown if the test fails.
	 */
	public void test(final String username, final String password) 
			throws ApiException {
		
		testAuthentication(username, password);
		testAuthToken(username, password);
	}
	
	/**
	 * Tests the different possibilities for getting an user's hashed password.
	 * 
	 * @throws ApiException Thrown if there is a bug in the library.
	 * 
	 * @throws IllegalStateException Thrown if the test fails.
	 */
	private void testAuthentication(
			final String username, final String password) 
			throws ApiException {

		// No username.
		try {
			api.getHashedPassword(null, password, Controller.CLIENT);
			throw new IllegalStateException("Failed: No username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No username: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Invalid username.
		try {
			api.getHashedPassword("blah", password, Controller.CLIENT);
			throw new IllegalStateException("Failed: Invalid username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid username: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// No password.
		try {
			api.getHashedPassword(username, null, Controller.CLIENT);
			throw new IllegalStateException("Failed: No password.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No password: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Invalid password.
		try {
			api.getHashedPassword(username, "blah", Controller.CLIENT);
			throw new IllegalStateException("Failed: Invalid password.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid password: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// No client.
		try {
			api.getHashedPassword(username, password, null);
			throw new IllegalStateException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No client: " + ErrorCode.SERVER_INVALID_CLIENT, e);
			}
		}
		
		// This request should be valid.
		try {
			api.getHashedPassword(username, password, Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Valid request.", e);
		}
	}
	
	/**
	 * Tests the different possibilities for getting an authentication token.
	 * 
	 * @param username The username of the user to test.
	 * 
	 * @param password The password of the user to test.
	 * 
	 * @throws ApiException Thrown if an incorrect error code is returned or
	 * 						there is a bug in the library.
	 * 
	 * @throws IllegalStateException Thrown if the test fails.
	 */
	private void testAuthToken(final String username, final String password) 
			throws ApiException {
		
		// No username.
		try {
			api.getAuthenticationToken(null, password, Controller.CLIENT);
			throw new IllegalStateException("Failed: No username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No username: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Invalid username.
		try {
			api.getAuthenticationToken("blah", password, Controller.CLIENT);
			throw new IllegalStateException("Failed: Invalid username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid username: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}

		// No password
		try {
			api.getAuthenticationToken(username, null, Controller.CLIENT);
			throw new IllegalStateException("Failed: No password.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No password: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}

		// Invalid password
		try {
			api.getAuthenticationToken(username, "blah", Controller.CLIENT);
			throw new IllegalStateException("Failed: Invalid password.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid password: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}

		// No client.
		try {
			api.getAuthenticationToken(username, password, null);
			throw new IllegalStateException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No client: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}

		// This request should be valid.
		try {
			api.getAuthenticationToken(username, password, Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Valid request.", e);
		}
	}
}