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
package org.ohmage.request.user;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.UserValidators;

/**
 * <p>Creates a new user. The requester must be an admin.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER}</td>
 *     <td>The username for the new user.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PASSWORD}</td>
 *     <td>The password for the new user.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#EMAIL_ADDRESS}</td>
 *     <td>The user's email address.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ADMIN}</td>
 *     <td>Whether or not the new user should be an admin.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ENABLED}</td>
 *     <td>Whether or not the new user's account should be enabled.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NEW_ACCOUNT}</td>
 *     <td>Whether or not the user must change their password before using any
 *       other APIs. The default value is "true".</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_PRIVILEGE}</td>
 *     <td>Whether or not the new user is allowed to create campaigns. The 
 *       default value is based on the current system and can be discovered
 *       through the /config/read API.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserCreationRequest.class);
	
	private final String newUsername;
	private final String newPassword;
	private final String emailAddress;
	private final Boolean newIsAdmin;
	private final Boolean newIsEnabled;
	private final Boolean newIsNewAccount;
	private final Boolean newCampaignCreationPrivilege;
	
	/**
	 * Creates a user creation request.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the required and
	 * 					  optional parameters for creating this request.
	 */
	public UserCreationRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		String tNewUsername = null;
		String tNewPassword = null;
		String tEmailAddress = null;
		Boolean tNewIsAdmin = null;
		Boolean tNewIsEnabled = null;
		Boolean tNewIsNewAccount = null;
		Boolean tNewCampaignCreationPrivilege = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user creation request.");
		
			try {
				String[] t;
				
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple username parameters were given: " +
								InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					tNewUsername = UserValidators.validateUsername(t[0]);
					
					if(tNewUsername == null) {
						throw new ValidationException(
								ErrorCode.USER_INVALID_USERNAME,
								"The username is invalid: " + t[0]);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Missing the required username for the new user: " + 
								InputKeys.USERNAME);
				}
				
				t = getParameterValues(InputKeys.PASSWORD);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PASSWORD,
							"Multiple password parameters were given: " +
								InputKeys.PASSWORD);
				}
				else if(t.length == 1) {
					tNewPassword = 
							UserValidators.validatePlaintextPassword(t[0]);
					
					if(tNewPassword == null) {
						throw new ValidationException(
								ErrorCode.USER_INVALID_PASSWORD,
								"The password is invalid: " + t[0]);
					}
				}
				else {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PASSWORD,
							"Missing the required password for the new user: " + 
								InputKeys.PASSWORD);
				}
				
				t = getParameterValues(InputKeys.EMAIL_ADDRESS);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_EMAIL_ADDRESS,
							"Multiple email address parameters were given: " +
								InputKeys.EMAIL_ADDRESS);
				}
				else if(t.length == 1) {
					tEmailAddress = UserValidators.validateEmailAddress(t[0]);
				}
				
				t = getParameterValues(InputKeys.USER_ADMIN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ADMIN_VALUE,
							"Multiple admin parameters were given: " +
								InputKeys.USER_ADMIN);
				}
				else if(t.length == 1) {
					tNewIsAdmin = UserValidators.validateAdminValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.USER_ENABLED);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ENABLED_VALUE,
							"Multiple enabled parameters were given: " +
								InputKeys.USER_ENABLED);
				}
				else if(t.length == 1) {
					tNewIsEnabled = UserValidators.validateEnabledValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.NEW_ACCOUNT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_NEW_ACCOUNT_VALUE,
							"Multiple new account parameters were given: " +
								InputKeys.NEW_ACCOUNT);
				}
				else if(t.length == 1) {
					tNewIsNewAccount = 
							UserValidators.validateNewAccountValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE,
							"Multiple campaign creation privilege parameters were given: " +
								InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
				}
				else if(t.length == 1) {
					tNewCampaignCreationPrivilege = 
							UserValidators.validateCampaignCreationPrivilegeValue(
									t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		newUsername = tNewUsername;
		newPassword = tNewPassword;
		emailAddress = tEmailAddress;
		newIsAdmin = tNewIsAdmin;
		newIsEnabled = tNewIsEnabled;
		newIsNewAccount = tNewIsNewAccount;
		newCampaignCreationPrivilege = tNewCampaignCreationPrivilege;
	}

	/**
	 * Services this request if an existing user is making the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user creation request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requesting user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Verifying that a user with the username doesn't already exist.");
			UserServices.instance().checkUserExistance(newUsername, false);
			
			LOGGER.info("Creating the user.");
			UserServices.instance().createUser(newUsername, newPassword, emailAddress, newIsAdmin, newIsEnabled, newIsNewAccount, newCampaignCreationPrivilege);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success or failure and a message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}
