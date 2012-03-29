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

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.User;
import org.ohmage.domain.UserInformation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.UserValidators;

/**
 * <p>Gathers all users and then searches through them removing those that do
 * not match the search criteria. A missing or empty string for a parameter 
 * means that it will not be used to filter the list. If no parameters are 
 * given, information about every user in the system will be returned.</p>
 * <p>The requester must be an admin.</p>
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
 *     <td>Only return information about users whose username contains this
 *       value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#EMAIL_ADDRESS}</td>
 *     <td>Only return information about users that have personal information
 *       and their email address contains this value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ADMIN}</td>
 *     <td>Only return information about users whose admin value matches this
 *       parameter.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ENABLED}</td>
 *     <td>Only return information about users whose enabled value matches this
 *       parameter.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NEW_ACCOUNT}</td>
 *     <td>Only return information about users whose new account value matches
 *       this parameter.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_CREATION_PRIVILEGE}
 *     </td>
 *     <td>Only return information about users whose campaign creation 
 *       privilege value matches this parameter.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#FIRST_NAME}</td>
 *     <td>Only return information about users that have personal information
 *       and their first name contains this value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#LAST_NAME}</td>
 *     <td>Only return information about users that have personal information
 *       and their last name contains this value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ORGANIZATION}</td>
 *     <td>Only return information about users that have personal information
 *       and their organization contains this value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PERSONAL_ID}</td>
 *     <td>Only return information about users that have personal information
 *       and their personal ID contains this value.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_SKIP}</td>
 *     <td>The number of users to skip before processing to facilitate paging.
 *       </td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#NUM_TO_RETURN}</td>
 *     <td>The number of users to return after skipping to facilitate paging.
 *       </td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class UserSearchRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(UserSearchRequest.class);
	
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
	
	private final int numToSkip;
	private final int numToReturn;
	
	private final Collection<UserInformation> userInformation;
	private long totalNumResults;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the
	 * 					  parameters to and metadata for this request.
	 */
	public UserSearchRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
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
		
		int tNumToSkip = 0;
		int tNumToReturn = User.MAX_NUM_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user search request.");
			
			String[] t;
			try {
				t = getParameterValues(InputKeys.USERNAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple usernames were given: " +
								InputKeys.USERNAME);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tUsername = null;
					}
					else {
						tUsername = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.EMAIL_ADDRESS);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_EMAIL_ADDRESS,
							"Multiple email address values were given: " +
								InputKeys.EMAIL_ADDRESS);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tEmailAddress = null;
					}
					else {
						tEmailAddress = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.USER_ADMIN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ADMIN_VALUE,
							"Multiple admin values were given: " +
								InputKeys.USER_ADMIN);
				}
				else if(t.length == 1) {
					tAdmin = UserValidators.validateAdminValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.USER_ENABLED);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ENABLED_VALUE,
							"Multiple enabled values were given: " +
								InputKeys.USER_ENABLED);
				}
				else if(t.length == 1) {
					tEnabled = UserValidators.validateEnabledValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.NEW_ACCOUNT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_NEW_ACCOUNT_VALUE,
							"Multiple new account values were given: " +
								InputKeys.NEW_ACCOUNT);
				}
				else if(t.length == 1) {
					tNewAccount = UserValidators.validateNewAccountValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE,
							"Multiple campaign creation values values were given: " +
								InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
				}
				else if(t.length == 1) {
					tCampaignCreationPrivilege = 
						UserValidators.validateCampaignCreationPrivilegeValue(t[0]);
				}
				
				t = getParameterValues(InputKeys.FIRST_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_FIRST_NAME_VALUE,
							"Multiple first name values were given: " +
								InputKeys.FIRST_NAME);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tFirstName = null;
					}
					else {
						tFirstName = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.LAST_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_LAST_NAME_VALUE,
							"Multiple last name values were given: " +
								InputKeys.LAST_NAME);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tLastName = null;
					}
					else {
						tLastName = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.ORGANIZATION);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ORGANIZATION_VALUE,
							"Multiple organization values were given: " +
								InputKeys.ORGANIZATION);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tOrganization = null;
					}
					else {
						tOrganization = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.PERSONAL_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PERSONAL_ID_VALUE,
							"Multiple personal ID values were given: " +
								InputKeys.PERSONAL_ID);
				}
				else if(t.length == 1) {
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tPersonalId = null;
					}
					else {
						tPersonalId = t[0];
					}
				}
				
				t = getParameterValues(InputKeys.NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_SKIP,
							"Multiple number to skip parameters were given: " + 
								InputKeys.NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tNumToSkip = UserValidators.validateNumToSkip(t[0]);
				}
				
				t = getParameterValues(InputKeys.NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_RETURN,
							"Multiple number to return parameters were given: " +
								InputKeys.NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tNumToReturn = UserValidators.validateNumToReturn(t[0]);
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
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
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		userInformation = new ArrayList<UserInformation>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user search request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}

		try {
			LOGGER.info("Checking that the user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Searching for the users that satisfy the parameters.");
			totalNumResults = 
					UserServices.instance().userSearch(
						getUser().getUsername(),
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
						numToSkip,
						numToReturn,
						userInformation);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		LOGGER.info("Responding to a user search request.");
		
		JSONObject metadata = null;
		JSONObject result = null;
		
		if(! isFailed()) {
			metadata = new JSONObject();
			result = new JSONObject();
			
			try {
				metadata.put(JSON_KEY_TOTAL_NUM_RESULTS, totalNumResults);
				
				for(UserInformation userInfo : userInformation) {
					result.put(
							userInfo.getUsername(), 
							userInfo.toJson());
				}
			}
			catch(JSONException e) {
				LOGGER.info("There was an error building the result.", e);
				setFailed();
			}
		}
		
		super.respond(httpRequest, httpResponse, metadata, result);
	}
}