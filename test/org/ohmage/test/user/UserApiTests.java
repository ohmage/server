/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.test.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.ServerConfig;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.domain.UserSummary;
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
	
	// This keeps track of the user whose information we are testing.
	private String username;
	private String password;
	
	/**
	 * Creates a new API tester. It tests creation and deletion with a single
	 * user to ensure that its tests can create users as needed and cleanup 
	 * after themselves.
	 * 
	 * @param api The ohmage API connection that is already setup.
	 * 
	 * @param adminAuthToken An administrators authentication token to use when
	 * 						 creating and deleting users for testing.
	 * 
	 * @throws ApiException There is an unexpected error.
	 * 
	 * @throws IllegalStateException One of the tests failed.
	 */
	public UserApiTests(final OhmageApi api, final String adminAuthToken) 
			throws ApiException {
		
		this.api = api;
		
		username = null;
		password = null;
		
		testUserCreation(adminAuthToken);
		testUserDeletion(adminAuthToken);
	}
	
	/**
	 * Tests all parameters and then begins creating, editing, and deleting
	 * usernames.
	 * 
	 * @param adminAuthToken An administrators authentication token to use when
	 * 						 creating and deleting users for testing.
	 * 
	 * @throws ApiException Thrown if there is a library error which should
	 * 						never happen.
	 */
	public void test(final String adminAuthToken) throws ApiException {
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, 
					username, password, null, true, null, null);

			// Test change password.
			testPasswordChange();
		}
		finally {
			// Cleanup
			Collection<String> usernames = new ArrayList<String>(1);
			usernames.add(username);
			api.deleteUser(adminAuthToken, Controller.CLIENT, usernames);
		}
		
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, 
					username, password, null, null, null, null);

			// Test update and read.
			testUserUpdateAndPersonalRead(adminAuthToken);
		}
		finally {
			// Cleanup
			Collection<String> usernames = new ArrayList<String>(1);
			usernames.add(username);
			api.deleteUser(adminAuthToken, Controller.CLIENT, usernames);
		}
	}
	
	/**
	 * Tests user creation, then create a user with a password and store those
	 * in the class-level variables.
	 * 
	 * @param adminAuthToken An administrators authentication token to use when
	 * 						 creating a user for testing.
	 * 
	 * @throws ApiException There was an unexpected error.
	 * 
	 * @throws IllegalStateException One of the tests failed.
	 */
	private void testUserCreation(final String adminAuthToken) 
			throws ApiException {
		
		// Test no token.
		try {
			api.createUser(null, Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Test an invalid token.
		try {
			api.createUser("not a token", Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Invalid authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Test a missing client value.
		try {
			api.createUser(adminAuthToken, null, "valid.username", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: No client value.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No client value: " + ErrorCode.SERVER_INVALID_CLIENT, e);
			}
		}
		
		// Test a missing new username.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, null, "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: No new username.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No new username: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is too short.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "123", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Username is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username is too short: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is too long.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "12345678901234567890123456", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Username is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username is too long: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is using an invalid character.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "invalid.username.#", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Username uses invalid character, '#'.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username uses invalid character, '#': " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Test a username that is using only valid characters, but not one of
		// the required characters.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "._@+-", "aaAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Username doesn't contain a required character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username doesn't contain a required character: " + ErrorCode.USER_INVALID_USERNAME);
			}
		}
		
		// Test the password is missing.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", null, null, null, null, null);
			throw new IllegalStateException("Failed: Password is missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password is missing: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password is too short.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "1234567", null, null, null, null);
			throw new IllegalStateException("Failed: Password is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password is too short: " + ErrorCode.USER_INVALID_PASSWORD);
			}
		}
		
		// Test the password is too long.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "12345678901234567", null, null, null, null);
			throw new IllegalStateException("Failed: Password is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password is too long: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no lower case character.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "AAAA00..", null, null, null, null);
			throw new IllegalStateException("Failed: Password missing lower case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing lower case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no upper case character.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "aaaa00..", null, null, null, null);
			throw new IllegalStateException("Failed: Password missing upper case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing upper case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no numeric character.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "aaAAAA..", null, null, null, null);
			throw new IllegalStateException("Failed: Password missing numeric character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing numeric character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Test the password contains no special character.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "aaAA0000", null, null, null, null);
			throw new IllegalStateException("Failed: Password missing special character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing special character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Creates the user.
		try {
			api.createUser(adminAuthToken, Controller.CLIENT, "valid.username", "aaAA00..", null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Creation failed.", e);
		}
		
		username = "valid.username";
		password = "aaAA00..";
	}
	
	/**
	 * Tests user update and reading the personal information about users. 
	 * These must be coupled together because you cannot guarantee that one
	 * succeeded without using the other.
	 * 
	 * @param adminAuthToken An administrators authentication token to use when
	 * 						 updating and reading a user's information.
	 * 
	 * @throws ApiException Thrown if there is an unexpected error.
	 * 
	 * @throws IllegalStateException Thrown if a test fails.
	 */
	private void testUserUpdateAndPersonalRead(final String adminAuthToken) 
			throws ApiException {
		
		Collection<String> usernames = new ArrayList<String>(1);
		usernames.add(username);
		
		// No authentication token.
		try {
			api.getUsersPersonalInformation(null, Controller.CLIENT, null, null, null);
			throw new IllegalArgumentException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No authentication token.", e);
			}
		}
		
		// Invalid authentication token.
		try {
			api.getUsersPersonalInformation("not a token", Controller.CLIENT, null, null, null);
			throw new IllegalArgumentException("Failed: Invalid authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid authentication token.", e);
			}
		}
		
		// No client.
		try {
			api.getUsersPersonalInformation(adminAuthToken, null, null, null, null);
			throw new IllegalArgumentException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No client.", e);
			}
		}
		
		// Test a valid user personal read query.
		try {
			if(api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, null, null, null).size() > 0) {
				throw new IllegalArgumentException("Information was returned but no users were queried.");
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// No authentication token.
		try {
			api.updateUser(null, Controller.CLIENT, username, null, null, null, null, null, null, null, null, null);
			throw new IllegalArgumentException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No authentication token.", e);
			}
		}
		
		// Invalid token.
		try {
			api.updateUser("invalid token", Controller.CLIENT, username, null, null, null, null, null, null, null, null, null);
			throw new IllegalArgumentException("Failed: Invalid token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid token.", e);
			}
		}
		
		// No client.
		try {
			api.updateUser(adminAuthToken, null, username, null, null, null, null, null, null, null, null, null);
			throw new IllegalArgumentException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No client.", e);
			}
		}
		
		// Invalid user.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, "123", null, null, null, null, null, null, null, null, null);
			throw new IllegalArgumentException("Failed: Invalid user.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid user.", e);
			}
		}
		
		// Test a valid user update query.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, null, null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Update with no updates failed.", e);
		}
		
		// Missing the first name when personal entry doesn't exist.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, null, "Last", "Organization", "PersonalId", "email@address.com");
			throw new IllegalArgumentException("Failed: Invalid first name when a personal entry doesn't exist.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_FIRST_NAME_VALUE.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid first name when a personal entry doesn't exist.", e);
			}
		}
		
		// Missing the last name when personal entry doesn't exist.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, "First", null, "Organization", "PersonalId", "email@address.com");
			throw new IllegalArgumentException("Failed: Invalid last name when a personal entry doesn't exist.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_LAST_NAME_VALUE.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid last name when a personal entry doesn't exist.", e);
			}
		}
		
		// Missing the organization when personal entry doesn't exist.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, "First", "Last", null, "PersonalId", "email@address.com");
			throw new IllegalArgumentException("Failed: Invalid organization when a personal entry doesn't exist.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_ORGANIZATION_VALUE.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid organization when a personal entry doesn't exist.", e);
			}
		}
		
		// Missing the personal ID when personal entry doesn't exist.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, "First", "Last", "Organization", null, "email@address.com");
			throw new IllegalArgumentException("Failed: Invalid personal ID when a personal entry doesn't exist.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PERSONAL_ID_VALUE.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid personal ID when a personal entry doesn't exist.", e);
			}
		}
		
		// Creates a user personal entry for the user.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, "First", "Last", "Organization", "PersonalId", null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Creating a new personal entry.", e);
		}

		// Verify the information that was just uploaded.
		try {
			Map<String, UserPersonal> result = 
				api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, usernames, null, null);
			
			if(usernames.size() == 0) {
				throw new IllegalArgumentException("Failed: We just created a personal entry for a user, but it isn't being returned to us.");
			}
			else if(usernames.size() > 1) {
				throw new IllegalArgumentException("Failed: We asked for information about only one user, but multiple were returned.");
			}
			
			for(String username : result.keySet()) {
				if(! this.username.equals(username)) {
					throw new IllegalArgumentException("Failed: We asked about one person, but got information about another.");
				}
				
				UserPersonal personalInfo = result.get(username);
				if(! "First".equals(personalInfo.getFirstName())) {
					throw new IllegalArgumentException("Failed: The user's first name is not what we saved.");
				}
				if(! "Last".equals(personalInfo.getLastName())) {
					throw new IllegalArgumentException("Failed: The user's last name is not what we saved.");
				}
				if(! "Organization".equals(personalInfo.getOrganization())) {
					throw new IllegalArgumentException("Failed: The user's organization is not what we saved.");
				}
				if(! "PersonalId".equals(personalInfo.getPersonalId())) {
					throw new IllegalArgumentException("Failed: The user's personal ID is not what we saved.");
				}
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// Updates the first name to a new value.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, "First1", null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the first name.", e);
		}

		// Verify the information that was just uploaded.
		try {
			Map<String, UserPersonal> result = 
				api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, usernames, null, null);
			
			if(usernames.size() == 0) {
				throw new IllegalArgumentException("Failed: We just created a personal entry for a user, but it isn't being returned to us.");
			}
			else if(usernames.size() > 1) {
				throw new IllegalArgumentException("Failed: We asked for information about only one user, but multiple were returned.");
			}
			
			for(String username : result.keySet()) {
				if(! this.username.equals(username)) {
					throw new IllegalArgumentException("Failed: We asked about one person, but got information about another.");
				}
				
				UserPersonal personalInfo = result.get(username);
				if(! "First1".equals(personalInfo.getFirstName())) {
					throw new IllegalArgumentException("Failed: The user's first name is not what we saved.");
				}
				if(! "Last".equals(personalInfo.getLastName())) {
					throw new IllegalArgumentException("Failed: The user's last name is not what we saved.");
				}
				if(! "Organization".equals(personalInfo.getOrganization())) {
					throw new IllegalArgumentException("Failed: The user's organization is not what we saved.");
				}
				if(! "PersonalId".equals(personalInfo.getPersonalId())) {
					throw new IllegalArgumentException("Failed: The user's personal ID is not what we saved.");
				}
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// Updates the last name to a new value.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, null, "Last1", null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the user's last name.", e);
		}

		// Verify the information that was just uploaded.
		try {
			Map<String, UserPersonal> result = 
				api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, usernames, null, null);
			
			if(usernames.size() == 0) {
				throw new IllegalArgumentException("Failed: We just created a personal entry for a user, but it isn't being returned to us.");
			}
			else if(usernames.size() > 1) {
				throw new IllegalArgumentException("Failed: We asked for information about only one user, but multiple were returned.");
			}
			
			for(String username : result.keySet()) {
				if(! this.username.equals(username)) {
					throw new IllegalArgumentException("Failed: We asked about one person, but got information about another.");
				}
				
				UserPersonal personalInfo = result.get(username);
				if(! "First1".equals(personalInfo.getFirstName())) {
					throw new IllegalArgumentException("Failed: The user's first name is not what we saved.");
				}
				if(! "Last1".equals(personalInfo.getLastName())) {
					throw new IllegalArgumentException("Failed: The user's last name is not what we saved.");
				}
				if(! "Organization".equals(personalInfo.getOrganization())) {
					throw new IllegalArgumentException("Failed: The user's organization is not what we saved.");
				}
				if(! "PersonalId".equals(personalInfo.getPersonalId())) {
					throw new IllegalArgumentException("Failed: The user's personal ID is not what we saved.");
				}
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// Updates the organization to a new value.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, null, null, "Organization1", null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the user's organization.", e);
		}

		// Verify the information that was just uploaded.
		try {
			Map<String, UserPersonal> result = 
				api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, usernames, null, null);
			
			if(usernames.size() == 0) {
				throw new IllegalArgumentException("Failed: We just created a personal entry for a user, but it isn't being returned to us.");
			}
			else if(usernames.size() > 1) {
				throw new IllegalArgumentException("Failed: We asked for information about only one user, but multiple were returned.");
			}
			
			for(String username : result.keySet()) {
				if(! this.username.equals(username)) {
					throw new IllegalArgumentException("Failed: We asked about one person, but got information about another.");
				}
				
				UserPersonal personalInfo = result.get(username);
				if(! "First1".equals(personalInfo.getFirstName())) {
					throw new IllegalArgumentException("Failed: The user's first name is not what we saved.");
				}
				if(! "Last1".equals(personalInfo.getLastName())) {
					throw new IllegalArgumentException("Failed: The user's last name is not what we saved.");
				}
				if(! "Organization1".equals(personalInfo.getOrganization())) {
					throw new IllegalArgumentException("Failed: The user's organization is not what we saved.");
				}
				if(! "PersonalId".equals(personalInfo.getPersonalId())) {
					throw new IllegalArgumentException("Failed: The user's personal ID is not what we saved.");
				}
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// Updates the personal ID to a new value.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, null, null, null, null, "PersonalId1", null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the user's personal ID.", e);
		}

		// Verify the personal ID that was just uploaded.
		try {
			Map<String, UserPersonal> result = 
				api.getUsersPersonalInformation(adminAuthToken, Controller.CLIENT, usernames, null, null);
			
			if(usernames.size() == 0) {
				throw new IllegalArgumentException("Failed: We just created a personal entry for a user, but it isn't being returned to us.");
			}
			else if(usernames.size() > 1) {
				throw new IllegalArgumentException("Failed: We asked for information about only one user, but multiple were returned.");
			}
			
			for(String username : result.keySet()) {
				if(! this.username.equals(username)) {
					throw new IllegalArgumentException("Failed: We asked about one person, but got information about another.");
				}
				
				UserPersonal personalInfo = result.get(username);
				if(! "First1".equals(personalInfo.getFirstName())) {
					throw new IllegalArgumentException("Failed: The user's first name is not what we saved.");
				}
				if(! "Last1".equals(personalInfo.getLastName())) {
					throw new IllegalArgumentException("Failed: The user's last name is not what we saved.");
				}
				if(! "Organization1".equals(personalInfo.getOrganization())) {
					throw new IllegalArgumentException("Failed: The user's organization is not what we saved.");
				}
				if(! "PersonalId1".equals(personalInfo.getPersonalId())) {
					throw new IllegalArgumentException("Failed: The user's personal ID is not what we saved.");
				}
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read personal information.", e);
		}
		
		// Attempts to get an authentication token which should fail because
		// their account is disabled and they need to change their password.
		try {
			api.getAuthenticationToken(username, password, Controller.CLIENT);
			throw new IllegalArgumentException("Failed: Account is disabled and password needs to be changed.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Account is disabled and password needs to be changed.", e);
			}
		}
		
		// Attempt to change the password which should fail because the account
		// is still disabled.
		try {
			api.changePassword(username, password, Controller.CLIENT, "ccCC22!!");
			throw new IllegalArgumentException("Failed: The user shouldn't be able to change their password because their account is disabled.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: The user shouldn't be able to change their password because their account is disabled.", e);
			}
		}
		
		// Enables the account.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, true, null, null, null, null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Enabling the account.", e);
		}
		
		// Attempts to get an authentication token which should still fail
		// because the password needs to be changed.
		try {
			api.getAuthenticationToken(username, password, Controller.CLIENT);
			throw new IllegalArgumentException("Failed: Password needs to be changed.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Password needs to be changed.", e);
			}
		}
		
		// Attempt to change the password which should work because the account
		// has been enabled.
		try {
			api.changePassword(username, password, Controller.CLIENT, "ccCC22!!");
			password = "ccCC22!!";
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: The use should.", e);
		}
		
		// Attempt to get a token which should work because the account has 
		// been enabled and the password has been changed.
		String newUserAuthToken;
		try {
			newUserAuthToken = api.getAuthenticationToken(username, password, Controller.CLIENT);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: The user changed their password and should now be allowed to get a token.");
		}
		
		// No token.
		try {
			api.getUserInformation(null, Controller.CLIENT);
			throw new IllegalArgumentException("Failed: No token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No token.", e);
			}
		}
		
		// Invalid token.
		try {
			api.getUserInformation("invalid token", Controller.CLIENT);
			throw new IllegalArgumentException("Failed: Invalid token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid token.", e);
			}
		}
		
		// No client.
		try {
			api.getUserInformation(newUserAuthToken, null);
			throw new IllegalArgumentException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No client.", e);
			}
		}

		ServerConfig config = api.getServerConfiguration();
		
		// Check that the campaign creation privilege is the default.
		try {
			UserSummary userInformation = api.getUserInformation(newUserAuthToken, Controller.CLIENT);
			
			if(config.getDefaultCampaignCreationPrivilege() != 
				userInformation.getCampaignCreationPrivilege()) {
				
				throw new IllegalArgumentException("Failed: The campaign creation privilege was not the default which is what it was created as.");
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read user information.");
		}
		
		// Updates the campaign creation privilege for the user.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, null, null, null, ! config.getDefaultCampaignCreationPrivilege(), null, null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the user's JSON data.", e);
		}
		
		// Ensure that the campaign creation privilege is the opposite of the
		// default now.
		try {
			UserSummary userInformation = api.getUserInformation(newUserAuthToken, Controller.CLIENT);
			
			if(config.getDefaultCampaignCreationPrivilege() == 
				userInformation.getCampaignCreationPrivilege()) {
				
				throw new IllegalArgumentException("Failed: The campaign creation privilege was not switched as we just attempted to do.");
			}
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Read user information.");
		}
		
		// Have a non-admin user attempt to create a new user.
		try {
			api.createUser(newUserAuthToken, Controller.CLIENT, "valid.username1", "ddDD33##", null, null, null, null);
			
			Collection<String> invalidUsers = new ArrayList<String>(1);
			invalidUsers.add("valid.username1");
			api.deleteUser(adminAuthToken, Controller.CLIENT, invalidUsers);
			throw new IllegalArgumentException("Failed: Non-admin user creating another user.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INSUFFICIENT_PERMISSIONS.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Non-admin user creating another user.");
			}
		}
		
		// Updates the admin status for the user making them an admin.
		try {
			api.updateUser(adminAuthToken, Controller.CLIENT, username, true, null, null, null, null, null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Updating the user's JSON data.", e);
		}
		
		// This should succeed as long as they are now an admin.
		try {
			api.createUser(newUserAuthToken, Controller.CLIENT, "valid.username1", "ddDD33##", null, null, null, null);
			
			Collection<String> invalidUsers = new ArrayList<String>(1);
			invalidUsers.add("valid.username1");
			api.deleteUser(adminAuthToken, Controller.CLIENT, invalidUsers);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: User creating another user after admin status was granted.");
		}
	}
	
	/**
	 * Tests the password change API, changes the user's password, and updates
	 * the class-level variables.
	 * 
	 * @throws ApiException There was an unexpected error.
	 * 
	 * @throws IllegalStateException One of the tests failed.
	 */
	private void testPasswordChange() throws ApiException {
		// Username is missing.
		try {
			api.changePassword(null, password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Username is missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username is missing: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}

		// Username is too short.
		try {
			api.changePassword("123", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Username is too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username is too short: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username is too long.
		try {
			api.changePassword("12345678901234567890123456", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Username is too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username is too long: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username uses an invalid character.
		try {
			api.changePassword("invalid.username.#", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Username users invalid character, '#'.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username users invalid character, '#': " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}
		
		// Username uses valid characters but does not use any of the required
		// ones.
		try {
			api.changePassword("._@+-", password, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Username contains valid characters, but none of the required ones.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Username contains valid characters, but none of the required ones: " + ErrorCode.USER_INVALID_USERNAME, e);
			}
		}

		// Password is missing.
		try {
			api.changePassword(username, null, Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is too short.
		try {
			api.changePassword(username, "1234567", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password too short: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is too long.
		try {
			api.changePassword(username, "12345678901234567", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password too long: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is missing lower case character
		try {
			api.changePassword(username, "BBBB11,,", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing lower case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing lower case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is missing upper case character.
		try {
			api.changePassword(username, "bbbb11,,", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing upper case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing upper case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is missing numeric character.
		try {
			api.changePassword(username, "bbBBBB,,", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing numeric character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing numeric character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Password is missing special character.
		try {
			api.changePassword(username, "bbBB1111", Controller.CLIENT, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing special character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing special character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// Client is missing.
		try {
			api.changePassword(username, "bbBB11,,", null, "bbBB11,,");
			throw new IllegalStateException("Failed: Password missing special character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Password missing special character: " + ErrorCode.SERVER_INVALID_CLIENT, e);
			}
		}

		// New password is missing.
		try {
			api.changePassword(username, password, Controller.CLIENT, null);
			throw new IllegalStateException("Failed: New password missing.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password missing: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is too short.
		try {
			api.changePassword(username, password, Controller.CLIENT, "1234567");
			throw new IllegalStateException("Failed: New password too short.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password too short: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is too long.
		try {
			api.changePassword(username, password, Controller.CLIENT, "12345678901234567");
			throw new IllegalStateException("Failed: New password too long.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password too long: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is missing lower case character
		try {
			api.changePassword(username, password, Controller.CLIENT, "BBBB11,,");
			throw new IllegalStateException("Failed: New password missing lower case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password missing lower case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is missing upper case character.
		try {
			api.changePassword(username, password, Controller.CLIENT, "bbbb11,,");
			throw new IllegalStateException("Failed: New password missing upper case character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password missing upper case character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is missing numeric character.
		try {
			api.changePassword(username, password, Controller.CLIENT, "bbBBBB,,");
			throw new IllegalStateException("Failed: New password missing numeric character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password missing numeric character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}

		// New password is missing special character.
		try {
			api.changePassword(username, password, Controller.CLIENT, "bbBB1111");
			throw new IllegalStateException("Failed: New password missing special character.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_PASSWORD.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: New password missing special character: " + ErrorCode.USER_INVALID_PASSWORD, e);
			}
		}
		
		// Change the password.
		try {
			api.changePassword(username, password, Controller.CLIENT, "bbBB11,,");
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed to change the test user's password.", e);
		}
		
		password = "bbBB11,,";
	}
	
	/**
	 * Tests user deletion and then deletes the user based on the class-level
	 * variables.
	 * 
	 * @param adminAuthToken An administrators authentication token to use when
	 * 						 creating and deleting users for testing.
	 * 
	 * @throws ApiException There was an unexpected error.
	 * 
	 * @throws IllegalStateException One of the tests failed.
	 */
	private void testUserDeletion(final String adminAuthToken) 
			throws ApiException {
		
		Collection<String> usernames = new ArrayList<String>(1);
		usernames.add(username);
		
		// Token is null.
		try {
			api.deleteUser(null, Controller.CLIENT, usernames);
			throw new IllegalStateException("Failed: The authentication token was null.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: The authentication token was null.", e);
			}
		}
		
		// Token is invalid.
		try {
			api.deleteUser("Invalid token", Controller.CLIENT, usernames);
			throw new IllegalStateException("Failed: The authentication token was invalid.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: The authentication token was invalid.", e);
			}
		}
		
		// Client is null.
		try {
			api.deleteUser(adminAuthToken, null, usernames);
			throw new IllegalStateException("Failed: The client was null.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: The client was null.", e);
			}
		}
		
		// Username list is null.
		try {
			api.deleteUser(adminAuthToken, Controller.CLIENT, null);
			throw new IllegalStateException("Failed: The username list was null.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.USER_INVALID_USERNAME.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: The usernmae list was null.", e);
			}
		}
		
		// Deleting the user.
		try {
			api.deleteUser(adminAuthToken, Controller.CLIENT, usernames);
		}
		catch(ApiException e) {
			throw new IllegalArgumentException("Failed: Deleting the user.", e);
		}
	}
}
