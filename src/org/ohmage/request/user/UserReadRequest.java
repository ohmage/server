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
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.User;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
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
	private static final Logger LOGGER = Logger.getLogger(UserReadRequest.class);
	
	private final Collection<String> usernames;
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
	 */
	public UserReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER);
		
		Collection<String> tUsernames = null;
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
					
				tCampaignIds = CampaignValidators.validateCampaignIds(httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST));
				if((tCampaignIds != null) && (httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN_LIST).length > 1)) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "Multiple campaign ID list parameters were found.");
					throw new ValidationException("Multiple campaign ID list parameters were found.");
				}
				
				tClassIds = ClassValidators.validateClassIdList(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
				if((tClassIds != null) && (httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1)) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID list parameters were found.");
					throw new ValidationException("Multiple class ID list parameters were found.");
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
		
		usernames = tUsernames;
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
			boolean isAdmin;
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
				
				LOGGER.info("The user is an admin.");
				isAdmin = true;
			}
			catch(ServiceException e) {
				LOGGER.info("The user is not an admin.");
				isAdmin = false;
			}
			
			ArrayList<String> allUsernames;
			
			if(usernames == null) {
				allUsernames = new ArrayList<String>();
			}
			else {
				if(! isAdmin) {
					LOGGER.info("Verifying that the requester may read the information about the users in the list.");
					UserServices.instance().verifyUserCanReadUsersPersonalInfo(getUser().getUsername(), usernames);
				}

				allUsernames = new ArrayList<String>(usernames);
			}
			
			if(campaignIds != null) {
				LOGGER.info("Verifying that all of the campaigns in the list exist.");
				CampaignServices.instance().checkCampaignsExistence(campaignIds, true);
				
				if(! isAdmin) {
					LOGGER.info("Verifying that the requester may read the information about the users in the campaigns.");
					UserCampaignServices.instance().verifyUserCanReadUsersInfoInCampaigns(getUser().getUsername(), campaignIds);
				}
				
				LOGGER.info("Gathering all of the users in all of the campaigns.");
				allUsernames.addAll(
						UserCampaignServices.instance().getUsersInCampaigns(
								campaignIds));
			}
			
			if(classIds != null) {
				LOGGER.info("Verifying that all of the classes in the list exist.");
				ClassServices.instance().checkClassesExistence(classIds, true);
				
				if(! isAdmin) {
					LOGGER.info("Verifying that the requester is privileged in all of the classes.");
					UserClassServices.instance().userHasRoleInClasses(getUser().getUsername(), classIds, Clazz.Role.PRIVILEGED);
				}
				
				LOGGER.info("Gathering all of the users in all of the classes.");
				allUsernames.addAll(
						UserClassServices.instance().getUsersInClasses(
								classIds));
			}
			
			numResults = UserServices.instance().getUserInformation(
					usernames, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					numToSkip, 
					numToReturn, 
					results);
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
				UserPersonal personalInformation = 
						userInformation.getPersonalInfo();
				
				if(personalInformation == null) {
					jsonResult.put(
							userInformation.getUsername(), 
							new JSONObject());
				}
				else {
					jsonResult.put(
							userInformation.getUsername(), 
							personalInformation.toJsonObject());
				}
			}
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the respons object.");
			setFailed();
		}
		super.respond(httpRequest, httpResponse, metadata, jsonResult);
	}
}
