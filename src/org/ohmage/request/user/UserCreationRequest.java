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
		Boolean tNewIsAdmin = null;
		Boolean tNewIsEnabled = null;
		Boolean tNewIsNewAccount = null;
		Boolean tNewCampaignCreationPrivilege = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user creation request.");
		
			try {
				tNewUsername = UserValidators.validateUsername(httpRequest.getParameter(InputKeys.USERNAME));
				if(tNewUsername == null) {
					setFailed(ErrorCode.USER_INVALID_USERNAME, "Missing the required username for the new user: " + InputKeys.USERNAME);
					throw new ValidationException("Missing the required username for the new user: " + InputKeys.USERNAME);
				}
				else if(httpRequest.getParameterValues(InputKeys.USERNAME).length > 1) {
					setFailed(ErrorCode.USER_INVALID_USERNAME, "Multiple username parameters were given.");
					throw new ValidationException("Multiple username parameters were given.");
				}
				
				tNewPassword = UserValidators.validatePlaintextPassword(httpRequest.getParameter(InputKeys.PASSWORD));
				if(tNewPassword == null) {
					setFailed(ErrorCode.USER_INVALID_PASSWORD, "Missing the required plaintext password for the user: " + InputKeys.PASSWORD);
					throw new ValidationException("Missing the required plaintext password for the user: " + InputKeys.PASSWORD);
				}
				else if(httpRequest.getParameterValues(InputKeys.PASSWORD).length > 1) {
					setFailed(ErrorCode.USER_INVALID_PASSWORD, "Multiple password parameters were given.");
					throw new ValidationException("Multiple password parameters were given.");
				}
				
				tNewIsAdmin = UserValidators.validateAdminValue(httpRequest.getParameter(InputKeys.USER_ADMIN));
				if((tNewIsAdmin != null) && (httpRequest.getParameterValues(InputKeys.USER_ADMIN).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_ADMIN_VALUE, "Multiple admin parameters were given.");
					throw new ValidationException("Multiple admin parameters were given.");
				}
				
				tNewIsEnabled = UserValidators.validateEnabledValue(httpRequest.getParameter(InputKeys.USER_ENABLED));
				if((tNewIsEnabled != null) && (httpRequest.getParameterValues(InputKeys.USER_ENABLED).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_ENABLED_VALUE, "Multiple enabled parameters were given.");
					throw new ValidationException("Multiple enabled parameters were given.");
				}
				
				tNewIsNewAccount = UserValidators.validateNewAccountValue(httpRequest.getParameter(InputKeys.NEW_ACCOUNT));
				if((tNewIsNewAccount != null) && (httpRequest.getParameterValues(InputKeys.NEW_ACCOUNT).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_NEW_ACCOUNT_VALUE, "Multiple new account parameters were given.");
					throw new ValidationException("Multiple new account parameters were given.");
				}
				
				tNewCampaignCreationPrivilege = UserValidators.validateCampaignCreationPrivilegeValue(httpRequest.getParameter(InputKeys.CAMPAIGN_CREATION_PRIVILEGE));
				if((tNewCampaignCreationPrivilege != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_CREATION_PRIVILEGE).length > 1)) {
					setFailed(ErrorCode.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, "Multiple campaign creation privilege parameters were given.");
					throw new ValidationException("Multiple campaign creation privilege parameters were given.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		newUsername = tNewUsername;
		newPassword = tNewPassword;
		newIsAdmin = tNewIsAdmin;
		newIsEnabled = tNewIsEnabled;
		newIsNewAccount = tNewIsNewAccount;
		newCampaignCreationPrivilege = tNewCampaignCreationPrivilege;
	}

	/**
	 * Services this request.
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
			UserServices.instance().createUser(newUsername, null, newPassword, newIsAdmin, newIsEnabled, newIsNewAccount, newCampaignCreationPrivilege);
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
