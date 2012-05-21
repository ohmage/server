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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.User;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.UserValidators;

/**
 * <p>Reads the information about all of the users in all of the campaigns and
 * classes in the lists. The requester must be a supervisor in all of the
 * campaigns and privileged in all of the classes. Internally, the results are
 * sorted by username, which is important for those doing paging.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN_LIST}</td>
 *     <td>A list of campaign identifiers where the identifiers are separated 
 *       by {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of class identifiers where the identifiers are separated by
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s</td>
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
public class UserReadRequest extends UserRequest {
	private static final Logger LOGGER = 
			Logger.getLogger(UserReadRequest.class);

	// In 3.x, this parameter would be unnecessary. If you want to query about
	// a specific user then you should use something like "/user/<username>".
	// However, this doesn't lend itself to a single server call to query 
	// information about multiple people. If you want to do that, you would 
	// need to come back to this call, "/user", with the 'usernameTokens' 
	// parameter where each username is separated by a space. But, that is the
	// expected use of the 3.x system. If you want information specifically 
	// about one user or want to perform an action on a specific user, you 
	// should use their specific API, but if you want information about 
	// multiple users then you use its list API variant.
	private final Collection<String> usernames;
	private final Collection<String> usernameTokens;
	private final Collection<String> emailAddressTokens;
	private final Boolean admin;
	private final Boolean enabled;
	private final Boolean newAccount;
	private final Boolean campaignCreationPrivilege;
	private final Collection<String> firstNameTokens;
	private final Collection<String> lastNameTokens;
	private final Collection<String> organizationTokens;
	private final Collection<String> personalIdTokens;
	// These parameters aren't generic / search-able. At this point, you should
	// know exactly what campaign/class you are curious about and should not be
	// fishing for those IDs. Going further, these parameters probably 
	// shouldn't even be here. Instead, you should read the information about
	// the campaign/class, which will give you the list of users. Then, a user
	// would come here and get more specific information if needed.
	private final List<String> campaignIds;
	private final Collection<String> classIds; 
	
	private final int numToSkip;
	private final int numToReturn;
	
	private List<UserInformation> results;
	private long numResults;
	
	/**
	 * Creates a new user read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public UserReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.EITHER);

		Collection<String> tUsernames = null;
		Collection<String> tUsernameTokens = null;
		Collection<String> tEmailAddressTokens = null;
		Boolean tAdmin = null;
		Boolean tEnabled = null;
		Boolean tNewAccount = null;
		Boolean tCampaignCreationPrivilege = null;
		Collection<String> tFirstNameTokens = null;
		Collection<String> tLastNameTokens = null;
		Collection<String> tOrganizationTokens = null;
		Collection<String> tPersonalIdTokens = null;
		List<String> tCampaignIds = null;
		Set<String> tClassIds = null;
		
		int tNumToSkip = 0;
		int tNumToReturn = User.MAX_NUM_TO_RETURN;
		
		if(! isFailed()) {
			LOGGER.info("Creating a user read request.");
		
			try {
				String[] t;
				
				t = getParameterValues(InputKeys.USER_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME, 
							"Multiple username lists parameters were found: " + 
									InputKeys.USER_LIST);
				}
				else if(t.length == 1) {
					tUsernames = UserValidators.validateUsernames(t[0]);
				}
				
				t = getParameterValues(InputKeys.USERNAME_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_USERNAME,
							"Multiple usernames were given: " +
								InputKeys.USERNAME_SEARCH);
				}
				else if(t.length == 1) {
					tUsernameTokens = 
							UserValidators.validateUsernameSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.EMAIL_ADDRESS_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_EMAIL_ADDRESS,
							"Multiple email address values were given: " +
								InputKeys.EMAIL_ADDRESS_SEARCH);
				}
				else if(t.length == 1) {
					tEmailAddressTokens = 
							UserValidators.validateEmailAddressSearch(t[0]);
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
						UserValidators.validateCampaignCreationPrivilegeValue(
							t[0]);
				}
				
				t = getParameterValues(InputKeys.FIRST_NAME_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_FIRST_NAME_VALUE,
							"Multiple first name values were given: " +
								InputKeys.FIRST_NAME_SEARCH);
				}
				else if(t.length == 1) {
					tFirstNameTokens = 
							UserValidators.validateFirstNameSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.LAST_NAME_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_LAST_NAME_VALUE,
							"Multiple last name values were given: " +
								InputKeys.LAST_NAME_SEARCH);
				}
				else if(t.length == 1) {
					tLastNameTokens =
							UserValidators.validateLastNameSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.ORGANIZATION_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_ORGANIZATION_VALUE,
							"Multiple organizationTokens values were given: " +
								InputKeys.ORGANIZATION_SEARCH);
				}
				else if(t.length == 1) {
					tOrganizationTokens =
							UserValidators.validateOrganizationSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.PERSONAL_ID_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.USER_INVALID_PERSONAL_ID_VALUE,
							"Multiple personal ID values were given: " +
								InputKeys.PERSONAL_ID_SEARCH);
				}
				else if(t.length == 1) {
					tPersonalIdTokens =
							UserValidators.validatePersonalIdSearch(t[0]);
				}
				
				t = getParameterValues(InputKeys.CAMPAIGN_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"Multiple campaign ID list parameters were found: " +
								InputKeys.CAMPAIGN_URN_LIST);
				}
				else if(t.length == 1) {
					tCampaignIds = CampaignValidators.validateCampaignIds(t[0]);
				}
				
				t = getParameterValues(InputKeys.CLASS_URN_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID, 
							"Multiple class ID list parameters were found: " +
								InputKeys.CLASS_URN_LIST);
				}
				else if(t.length == 1) {
					tClassIds = ClassValidators.validateClassIdList(t[0]);
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
				LOGGER.info(e.toString());
			}
		}
		
		usernameTokens = tUsernameTokens;
		usernames = tUsernames;
		emailAddressTokens = tEmailAddressTokens;
		admin = tAdmin;
		enabled = tEnabled;
		newAccount = tNewAccount;
		campaignCreationPrivilege = tCampaignCreationPrivilege;
		firstNameTokens = tFirstNameTokens;
		lastNameTokens = tLastNameTokens;
		organizationTokens = tOrganizationTokens;
		personalIdTokens = tPersonalIdTokens;
		campaignIds = tCampaignIds;
		classIds = tClassIds;
		
		numToSkip = tNumToSkip;
		numToReturn = tNumToReturn;
		
		results = new ArrayList<UserInformation>();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the user read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Reading the database.");
			numResults = 
					UserServices.instance().getUserInformation(
							getUser().getUsername(),
							usernames,
							usernameTokens,
							emailAddressTokens,
							admin,
							enabled,
							newAccount,
							campaignCreationPrivilege,
							firstNameTokens,
							lastNameTokens,
							organizationTokens,
							personalIdTokens,
							campaignIds,
							classIds,
							numToSkip,
							numToReturn,
							results);
			
			LOGGER.info(
					"Returning " + 
						results.size() + 
						" results out of a total of " +
						numResults +
						" results.");	
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Creates a JSONObject from the result object and responds with that 
	 * object.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		JSONObject metadata = new JSONObject();
		JSONObject jsonResult = new JSONObject();
		
		try {
			metadata.put(JSON_KEY_TOTAL_NUM_RESULTS, numResults);
			
			for(UserInformation userInformation : results) {
				JSONObject currResult = userInformation.toJson(false, false);
				
				UserPersonal personalInformation = 
						userInformation.getPersonalInfo();
				if(personalInformation != null) {
					JSONObject personalJson = 
							personalInformation.toJsonObject();
					
					@SuppressWarnings("unchecked")
					Iterator<String> personalJsonIter = personalJson.keys();
					while(personalJsonIter.hasNext()) {
						String currKey = personalJsonIter.next();
						
						currResult.put(currKey, personalJson.get(currKey));
					}
				}
				
				jsonResult.put(userInformation.getUsername(), currResult);
			}
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the respons object.");
			setFailed();
		}
		super.respond(httpRequest, httpResponse, metadata, jsonResult);
	}
}
