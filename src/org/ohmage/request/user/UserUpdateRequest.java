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
 * <p>Updates a user's information. Only an admin can update a user's 
 * information.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#USERNAME}</td>
 *     <td>The username of the user to update.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#EMAIL_ADDRESS}</td>
 *     <td>The user's email address.</td>
 *     <td>false+</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ADMIN}</td>
 *     <td>Whether or not the user should be an admin.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ENABLED}</td>
 *     <td>Whether or not the user's account should be enabled.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NEW_ACCOUNT}</td>
 *     <td>Whether or not the user needs to change their password the next time
 *       the login.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_PRIVILEGE}
 *       </td>
 *     <td>Whether or not the user is allowed to create campaigns.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#FIRST_NAME}</td>
 *     <td>The first name of the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#LAST_NAME}</td>
 *     <td>The last name of the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ORGANIZATION}</td>
 *     <td>The organization to which the user belongs.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PERSONAL_ID}</td>
 *     <td>The personal identifier for the user.</td>
 *     <td>false*</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_DELETE_PERSONAL_INFO}</td>
 *     <td>If true, deletes all of the user's personal information, which 
 *       includes their first name, last name, organization, and personal ID.
 *       </td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * <br />
 * * If a user does not already have a personal information entry in the 
 * database, then all of these entries must be present in order to create a new
 * one.<br />
 * + These are not required to create a new personal information entry in the
 * database; however, if one does not exist, then adding this fields requires
 * that the ones marked with "*" must be present to create the entry.<br />
 * 
 * @author John Jenkins
 */
public class UserUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserUpdateRequest.class);
	
	private final String username;
	private final String emailAddress;
	
	private final Boolean admin;
	private final Boolean enabled;
	private final Boolean newAccount;
	private final Boolean campaignCreationPrivilege;
	
	private final String firstName;
	private final String lastName;
	private final String organization;
	private final String personalId;
	
	private final boolean deletePersonalInfo;
	
	/**
	 * Creates a new user update request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters to build 
	 * 					  this request.
	 */
	public UserUpdateRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a user update request.");
		
		String tUsername = null;
		String tEmailAddress = null;
		
		Boolean tAdmin = null;
		Boolean tEnabled = null;
		Boolean tNewAccount = null;
		Boolean tCampaignCreationPrivilege = null;
		
		String tFirstName = null;
		String tLastName = null;
		String tOrganization = null;
		String tPersonalId = null;
		
		boolean tDeletePersonalInfo = false;
		
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
				tUsername = UserValidators.validateUsername(t[0]);
			}
			if((tUsername == null) || (t.length == 0)) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_USERNAME, 
					"The username is missing or not a valid username: " +
						InputKeys.USERNAME);
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
				tAdmin = UserValidators.validateAdminValue(t[0]);
			}
			
			t = getParameterValues(InputKeys.USER_ENABLED);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_ENABLED_VALUE, 
					"Multiple enabled parameters were given: " +
						InputKeys.USER_ENABLED);
			}
			else if(t.length == 1) {
				tEnabled = UserValidators.validateEnabledValue(t[0]);
			}
			
			t = getParameterValues(InputKeys.NEW_ACCOUNT);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_NEW_ACCOUNT_VALUE, 
					"Multiple new account parameters were given: " +
						InputKeys.NEW_ACCOUNT);
			}
			else if(t.length == 1) {
				tNewAccount = UserValidators.validateNewAccountValue(t[0]);
			}
			
			t = getParameterValues(InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE, 
					"Multiple campaign creation privilege parameters were given: " +
						InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
			}
			else if(t.length == 1) {
				tCampaignCreationPrivilege = 
					UserValidators.validateCampaignCreationPrivilegeValue(
						t[0]);
			}
			
			t = getParameterValues(InputKeys.FIRST_NAME);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_FIRST_NAME_VALUE, 
					"Multiple first name parameters were given: " +
						InputKeys.FIRST_NAME);
			}
			else if(t.length == 1) {
				tFirstName = UserValidators.validateFirstName(t[0]);
			}
			
			t = getParameterValues(InputKeys.LAST_NAME);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_LAST_NAME_VALUE, 
					"Multiple last name parameters were given: " +
						InputKeys.LAST_NAME);
			}
			else if(t.length == 1) {
				tLastName = UserValidators.validateLastName(t[0]);
			}
			
			t = getParameterValues(InputKeys.ORGANIZATION);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_ORGANIZATION_VALUE, 
					"Multiple organization parameters were given: " +
						InputKeys.ORGANIZATION);
			}
			else if(t.length == 1) {
				tOrganization = UserValidators.validateOrganization(t[0]);
			}

			t = getParameterValues(InputKeys.PERSONAL_ID);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_PERSONAL_ID_VALUE, 
					"Multiple personal ID parameters were given: " +
						InputKeys.PERSONAL_ID);
			}
			else if(t.length == 1) {
				tPersonalId = UserValidators.validatePersonalId(t[0]);
			}
			
			t = getParameterValues(InputKeys.USER_DELETE_PERSONAL_INFO);
			if(t.length > 1) {
				throw new ValidationException(
					ErrorCode.USER_INVALID_DELETE_PERSONAL_INFO, 
					"Multiple delete personal info parameters were given: " +
						InputKeys.USER_DELETE_PERSONAL_INFO);
			}
			else if(t.length == 1) {
				tDeletePersonalInfo = 
					UserValidators.validateDeletePersonalInfo(t[0]);
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		
		username = tUsername;
		emailAddress = tEmailAddress;
		
		admin = tAdmin;
		enabled = tEnabled;
		newAccount = tNewAccount;
		campaignCreationPrivilege = tCampaignCreationPrivilege;
		
		firstName = tFirstName;
		lastName = tLastName;
		organization = tOrganization;
		personalId = tPersonalId;
		
		deletePersonalInfo = tDeletePersonalInfo;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Validating the user update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the requesting user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Verifying that the user to be updated exists.");
			UserServices.instance().checkUserExistance(username, true);
			
			LOGGER.info("Verify that either the user to be updated already has a personal record or that enough information was provided to create a new one.");
			UserServices.instance().verifyUserHasOrCanCreatePersonalInfo(
					username,
					firstName,
					lastName,
					organization,
					personalId);
			
			LOGGER.info("Updating the user.");
			UserServices.instance().updateUser(
					username, 
					emailAddress,
					admin, 
					enabled, 
					newAccount, 
					campaignCreationPrivilege,
					firstName,
					lastName,
					organization,
					personalId,
					deletePersonalInfo);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the user's request.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the user update request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}
