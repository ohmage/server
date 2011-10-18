package org.ohmage.test.user;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.test.Controller;

/**
 * This class is responsible for testing the user APIs.
 * 
 * @author John Jenkins
 */
public class UserApiTests {
	private final OhmageApi api;
	private final String authenticationToken;
	
	// This keeps track of the user whose information we are testing.
	private String username;
	private String password;
	
	/**
	 * Creates a new API tester.
	 * 
	 * @param api The ohmage API connection that is already setup.
	 * 
	 * @param authenticationToken The user's authentication token.
	 */
	public UserApiTests(final OhmageApi api, final String authenticationToken) {
		this.api = api;
		this.authenticationToken = authenticationToken;
		
		username = null;
		password = null;
	}
	
	/**
	 * Tests all parameters and then begins creating, editing, and deleting
	 * usernames.
	 * 
	 * @throws ApiException Thrown if there is a library error which should
	 * 						never happen.
	 */
	public void test() throws ApiException{
		//testUserCreation();
		
		try {
			// Test change password.
			testPasswordChange();
			
			// TODO: Test info read.
			// TODO: Test update.
		}
		finally {
			// If any of the above tests fail, we still want to cleanup our 
			// mess, so we test user deletion in here. 
			// TODO: Test user deletion.
		}
	}
	
	/**
	 * Tests user creation and returns the username of the newly created user.
	 * 
	 * @return The username of the newly created user.
	 * 
	 * @throws ApiException The server returned an unexpected error.
	 */
	private void testUserCreation() throws ApiException {
		// Test no token.
		try {
			api.createUser(null, Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: No authentication token. Expected: " + ErrorCodes.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Test an invalid token.
		try {
			api.createUser("not a token", Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Invalid authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Invalid authentication token. Expected: " + ErrorCodes.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Test a missing client value.
		try {
			api.createUser(authenticationToken, null, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: No client value.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: No client value. Expected: " + ErrorCodes.SERVER_INVALID_CLIENT, e);
			}
		}
		
		// Test a missing new username.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, null, "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: No new username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: No new username. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is too short.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "123", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Username is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username is too short. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is too long.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "12345678901234567890123456", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Username is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username is too long. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is using an invalid character.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "invalid.username.#", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Username uses invalid character, '#'.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username uses invalid character, '#'. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is using only valid characters, but not one of
		// the required characters.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "._@+-", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Username doesn't contain a required character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username doesn't contain a required character. Expected: " + ErrorCodes.USER_INVALID_USERNAME);
			}
		}
		
		// Test the password is missing.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", null, null, null, null, null);
			throw new IllegalStateException("Failed: User: Password is missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password is missing. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password is too short.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "1234567", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password is too short. Expected: " + ErrorCodes.USER_INVALID_PASSWORD);
			}
		}
		
		// Test the password is too long.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "12345678901234567", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password is too long. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no lower case character.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "AAAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password missing lower case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password missing lower case character. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no upper case character.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "aaaa00..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password missing upper case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password missing upper case character. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no numeric character.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "aaAAAA..", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password missing numeric character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password missing numeric character. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no special character.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "aaAA0000", null, null, null, null);
			throw new IllegalStateException("Failed: User: Password missing special character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Password missing special character. Expected: " + ErrorCodes.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Creates the user.
		try {
			api.createUser(authenticationToken, Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: User: Creation failed.", e);
		}
		
		username = "valid.username";
		password = "aaAA00..";
	}
	
	public void testPasswordChange() throws ApiException {
		// Username is missing.
		try {
			api.changePassword(null, password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: User: Username is missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username is missing. Expected: " + ErrorCodes.AUTHENTICATION_FAILED, e);
			}
		}

		// Username is too short.
		try {
			api.changePassword("123", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: User: Username is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username is too short. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username is too long.
		try {
			api.changePassword("12345678901234567890123456", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: User: Username is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username is too long. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username uses and invalid character.
		try {
			api.changePassword("invalid.username.#", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: User: Username users invalid character, '#'.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username users invalid character, '#'. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username uses valid characters, but 
		try {
			api.changePassword("invalid.username.#", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: User: Username users invalid character, '#'.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCodes.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: User: Username users invalid character, '#'. Expected: " + ErrorCodes.USER_INVALID_USERNAME, e);
			}
		}
		
		// Change the password.
		try {
			api.changePassword(username, password, Controller.CLIENT, "bbBB11,,");
		}
		catch(ApiException e) {
			throw new IllegalArgumentException("Failed to change the test user's password.", e);
		}
		
		password = "bbBB11,,";
	}
}