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
package org.ohmage.request.campaign;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignClassServices;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;
import org.ohmage.validator.UserCampaignValidators;

/**
 * <p>A request to update the information about a campaign. To update a 
 * campaign's information the requesting user must be a supervisor or an 
 * author. Furthermore, to update a campaign's XML, there cannot be any 
 * responses.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The unique identifier for the campaign to be deleted.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#XML}</td>
 *     <td>The XML file describing this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RUNNING_STATE}</td>
 *     <td>The initial running state of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The initial privacy state of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes with which this campaign will initially be 
 *       associated.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_ADD}</td>
 *     <td>A list of users to be directly associated with the campaign. The 
 *       user-role pairs should separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and in each
 *       pair the username should be separated from the campaign role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_ROLE_LIST_REMOVE}</td>
 *     <td>A list of users whose association with the campaign should be  
 *       removed. The user-role pairs should separated by 
 *       {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}s and in each
 *       pair the username should be separated from the campaign role by a
 *       {@value org.ohmage.request.InputKeys#ENTITY_ROLE_SEPARATOR}.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignUpdateRequest.class);
	
	private final String campaignId;
	// The 'id' and 'name' are from the new XML, if present, and represent the
	// ID and name in the new XML.
	private final String id;
	private final String name;
	private final String xml;
	private final String description;
	private final Campaign.RunningState runningState;
	private final Campaign.PrivacyState privacyState;
	private final Set<String> classesToAdd;
	private final Set<String> classesToRemove; 
	private final Map<String, Set<Campaign.Role>> usersAndRolesToAdd;
	private final Map<String, Set<Campaign.Role>> usersAndRolesToRemove;
	
	/**
	 * Creates a campaign update request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters to build
	 * 					  this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public CampaignUpdateRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		LOGGER.info("Creating a campaign update request.");
		
		String tCampaignId = null;
		String tId = null;
		String tName = null;
		String tXml = null;
		String tDescription = null;
		Campaign.RunningState tRunningState = null;
		Campaign.PrivacyState tPrivacyState = null;
		Set<String> tClassesToAdd = null;
		Set<String> tClassesToRemove = null;
		Map<String, Set<Campaign.Role>> tUsersAndRolesToAdd = null;
		Map<String, Set<Campaign.Role>> tUsersAndRolesToRemove = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a campaign update request.");
			String[] t;
			
			try {
				tCampaignId = CampaignValidators.validateCampaignId(httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
				if(tCampaignId == null) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The campaign ID is required: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("The campaign ID is required: " + InputKeys.CAMPAIGN_URN);
				}
				else if(httpRequest.getParameterValues(InputKeys.CAMPAIGN_URN).length > 1) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "Multiple campaign IDs were found.");
					throw new ValidationException("Multiple campaign IDs were found.");
				}
				
				t = getParameterValues(InputKeys.RUNNING_STATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_RUNNING_STATE, 
						"Multiple running states were found: " +
							InputKeys.RUNNING_STATE);
				}
				else if(t.length == 1) {
					tRunningState = 
						CampaignValidators.validateRunningState(t[0]);
				}
				
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_PRIVACY_STATE,
						"Multiple privacy states were found: " +
							InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					tPrivacyState = 
						CampaignValidators.validatePrivacyState(t[0]);
				}
				
				t = getParameterValues(InputKeys.DESCRIPTION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_DESCRIPTION,
						"Multiple descriptions were found: " +
							InputKeys.DESCRIPTION);
				}
				if(t.length == 1) {
					tDescription = CampaignValidators.validateDescription(t[0]);
				}
	
				byte[] bXml = getParameter(httpRequest, InputKeys.XML);
				if(bXml != null) {
					String xmlString = new String(bXml);
					
					Map<String, String> idToName = 
						CampaignValidators.validateXml(xmlString);
					if(idToName != null) {
						if(idToName.size() != 1) {
							throw new ValidationException(
								"The validateXml(String) function did not return a map of size exactly 1.");
						}
						
						tId = idToName.keySet().iterator().next();
						tName = idToName.get(tId);
						
						tXml = xmlString;
					}
					
				}
				
				tClassesToAdd = ClassValidators.validateClassIdList(getParameter(InputKeys.CLASS_LIST_ADD));
				if((tClassesToAdd != null) && (getParameterValues(InputKeys.CLASS_ROLE_LIST_ADD).length > 1)) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists to add were found.");
					throw new ValidationException("Multiple class ID lists to add were found.");
				}
				
				tClassesToRemove = ClassValidators.validateClassIdList(getParameter(InputKeys.CLASS_LIST_REMOVE));
				if((tClassesToRemove != null) && (getParameterValues(InputKeys.CLASS_LIST_REMOVE).length > 1)) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists to remove were found.");
					throw new ValidationException("Multiple class ID lists to remove were found.");
				}
				
				tUsersAndRolesToAdd = UserCampaignValidators.validateUserAndCampaignRole(httpRequest.getParameter(InputKeys.USER_ROLE_LIST_ADD));
				if((tUsersAndRolesToAdd != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_ADD).length > 1)) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ROLE, "Multiple username, campaign role add lists were found.");
					throw new ValidationException("Multiple username, campaign role lists were found.");
				}
				
				tUsersAndRolesToRemove = UserCampaignValidators.validateUserAndCampaignRole(httpRequest.getParameter(InputKeys.USER_ROLE_LIST_REMOVE));
				if((tUsersAndRolesToRemove != null) && (httpRequest.getParameterValues(InputKeys.USER_ROLE_LIST_REMOVE).length > 1)) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ROLE, "Multiple username, campaign role remove lists were found.");
					throw new ValidationException("Multiple username, campaign role remove lists were found.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		campaignId = tCampaignId;
		id = tId;
		name = tName;
		xml = tXml;
		description = tDescription;
		runningState = tRunningState;
		privacyState = tPrivacyState;
		classesToAdd = tClassesToAdd;
		classesToRemove = tClassesToRemove;
		usersAndRolesToAdd = tUsersAndRolesToAdd;
		usersAndRolesToRemove = tUsersAndRolesToRemove;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign update request.");
		
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
			
			if(isAdmin) {
				LOGGER.info("Verifying that the campaign exists.");
				CampaignServices.instance().checkCampaignExistence(campaignId, true);
			}
			else {
				LOGGER.info("Verfiying that the campaign exists and that the user belongs.");
				UserCampaignServices.instance().campaignExistsAndUserBelongs(campaignId, getUser().getUsername());
			}
			
			if(! isAdmin) {
				LOGGER.info("Verifying that the user is allowed to update the campaign.");
				UserCampaignServices.instance().verifyUserCanUpdateCampaign(getUser().getUsername(), campaignId);
			}
			
			if(xml != null) {
				LOGGER.info("Verifying that the user is allowed to update the campaign.");
				UserCampaignServices.instance().verifyCampaignXmlCanBeUpdated(getUser().getUsername(), campaignId, id, name);
			}
			
			if((classesToAdd != null) && (classesToRemove != null)) {
				LOGGER.info("Both a list of classes to add and remove were given, so we are truncating the lists to remove items that are in both.");
				Set<String> union = new HashSet<String>(classesToAdd);
				union.retainAll(classesToRemove);
				
				classesToAdd.removeAll(union);
				classesToRemove.removeAll(union);
			}
			
			if(classesToAdd != null) {
				if(isAdmin) {
					LOGGER.info("Verifying that all of the classes to add exist.");
					ClassServices.instance().checkClassesExistence(classesToAdd, true);
				}
				else {
					LOGGER.info("Verifying that all of the classes to add exist and that the user belongs.");
					UserClassServices.instance().classesExistAndUserBelongs(classesToAdd, getUser().getUsername());
				}
			}
			
			if(classesToRemove != null) {
				if(isAdmin) {
					LOGGER.info("Verifying that all of the classes to remove exist.");
					ClassServices.instance().checkClassesExistence(classesToRemove, true);
				}
				else {
					LOGGER.info("Verifying that all of the classes to remove exist and that the user belongs.");
					UserClassServices.instance().classesExistAndUserBelongs(classesToRemove, getUser().getUsername());
				}
				
				LOGGER.info("Verifying that not all of the classes are being disassociated from the campaign.");
				CampaignClassServices.instance().verifyNotDisassocitingAllClassesFromCampaign(campaignId, classesToRemove, classesToAdd);
			}
			
			if(usersAndRolesToAdd != null) {
				LOGGER.info("Verifying that all of the users to add exist.");
				UserServices.instance().verifyUsersExist(usersAndRolesToAdd.keySet(), true);
				
				if(! isAdmin) {
					LOGGER.info("Verifying that the user is allowed to give the permissions they are trying to give.");
					Set<Campaign.Role> roles = new HashSet<Campaign.Role>();
					for(Set<Campaign.Role> currRoles : usersAndRolesToAdd.values()) {
						roles.addAll(currRoles);
					}
					UserCampaignServices.instance().verifyUserCanGrantOrRevokeRoles(getUser().getUsername(), campaignId, roles);
				}
			}
			
			if((usersAndRolesToRemove != null) && (! isAdmin)) {
				LOGGER.info("Verifying that the user is allowed to revoke permissions that they are trying to revoke access.");
				Set<Campaign.Role> roles = new HashSet<Campaign.Role>();
				for(Set<Campaign.Role> currRoles : usersAndRolesToRemove.values()) {
					roles.addAll(currRoles);
				}
				UserCampaignServices.instance().verifyUserCanGrantOrRevokeRoles(getUser().getUsername(), campaignId, roles);
			}
			
			LOGGER.info("Updating the campaign.");
			CampaignServices.instance().updateCampaign(
					campaignId, 
					xml, 
					description, 
					runningState, 
					privacyState, 
					classesToAdd, 
					classesToRemove, 
					usersAndRolesToAdd, 
					usersAndRolesToRemove);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds with success or a failure message and text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, (JSONObject) null);
	}
}
