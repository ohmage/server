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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.InvalidRequestException;
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
public class UserSetupRequestCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserSetupRequestCreationRequest.class);
	
	private final String emailAddress;
	private final String requestContent; 
	
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
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserSetupRequestCreationRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);

		String tEmailAddress = null;
		String tRequestContent = null; 

		String tNewUsername = null;
		String tNewPassword = null;

		Boolean tNewIsAdmin = null;
		Boolean tNewIsEnabled = null;
		Boolean tNewIsNewAccount = null;
		Boolean tNewCampaignCreationPrivilege = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user creation request.");
		
			try {
				String[] t;
				
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
				
				t = getParameterValues(InputKeys.USER_SETUP_REQUEST_CONTENT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_SETUP_REQUEST_INVALID_PARAMETER,
							"Multiple request content were given: " +
								InputKeys.USER_SETUP_REQUEST_CONTENT);
				}
				else if(t.length == 1) {
					tRequestContent = UserValidators.validateUserSetupRequestContent(t[0]);
				}
				
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		emailAddress = tEmailAddress;
		requestContent = tRequestContent; 		
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

			LOGGER.info("Creating the UserSetupRequest.");
			UserSetupRequestServices.instance().createUserSetupRequest(this.getUser().getUsername(), emailAddress, requestContent);
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
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}