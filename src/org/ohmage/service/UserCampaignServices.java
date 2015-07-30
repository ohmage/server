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
package org.ohmage.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.CampaignMask;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignClassQueries;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.ICampaignSurveyResponseQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserQueries;
import org.ohmage.query.impl.QueryResultsList;
import org.ohmage.util.StringUtils;

/**
 * This class contains the services for user-campaign relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserCampaignServices {
	private static UserCampaignServices instance;
	
	private ICampaignQueries campaignQueries;
	private ICampaignClassQueries campaignClassQueries;
	private IUserCampaignQueries userCampaignQueries;
	private IUserQueries userQueries;
	private ICampaignSurveyResponseQueries campaignSurveyResponseQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignClassQueries or 
	 * iCampaignQueries or iUserCampaignQueries or iUserCampaignQueries or 
	 * iCampaignSurveyResponseQueries is null
	 */
	private UserCampaignServices(ICampaignClassQueries iCampaignClassQueries, 
			                     ICampaignQueries iCampaignQueries,
			                     IUserCampaignQueries iUserCampaignQueries,
			                     IUserQueries iUserQueries,
			                     ICampaignSurveyResponseQueries iCampaignSurveyResponseQueries) {
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(iCampaignClassQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignClassQueries is required.");
		}
		if(iCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignQueries is required.");
		}
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is required.");
		}
		if(iUserQueries == null) {
			throw new IllegalArgumentException("An instance of IUserQueries is required.");
		}
		if(iCampaignSurveyResponseQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignSurveyResponseQueries is required.");
		}

		
		campaignClassQueries = iCampaignClassQueries;
		campaignQueries = iCampaignQueries;
		userCampaignQueries = iUserCampaignQueries;
		userQueries = iUserQueries;
		campaignSurveyResponseQueries = iCampaignSurveyResponseQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserCampaignServices instance() {
		return instance;
	}
	

	/**
	 * Creates a campaign mask for the given user. When the campaign is
	 * queried, only the surveys given by this list will be shown to the user.
	 * 
	 * @param mask The campaign mask to save.
	 * 
	 * @throws DataAccessException There was an error saving the data.
	 */
	public void createUserCampaignMask(
		final CampaignMask mask)
		throws ServiceException {
		
		try {
			userCampaignQueries.createUserCampaignMask(mask);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a campaign exists and that a user belongs to the campaign
	 * in some capacity.
	 * 
	 * @param campaignId The campaign ID for the campaign in question.
	 * 
	 * @param username The username of the user that must belong to the 
	 * 				   campaign.
	 * 
	 * @throws ServiceException Thrown if the campaign doesn't exist, the user
	 * 							doesn't belong to the campaign, or if there is
	 * 							an error.
	 */
	public void campaignExistsAndUserBelongs(final String campaignId, 
			final String username) throws ServiceException {
		
		CampaignServices.instance().checkCampaignExistence(campaignId, true);
		
		try {
			if(! userCampaignQueries.userBelongsToCampaign(username, campaignId)) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user does not belong to the campaign: " + 
							campaignId);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
		
	/**
	 * Ensures that all of the campaigns in a List exist and that the user 
	 * belongs to each of them in some capacity.
	 * 
	 * @param campaignIds A List of campaign IDs that all must exist.
	 * 
	 * @param username The username of the user that must exist in all of the
	 * 				   campaigns.
	 * 
	 * @throws ServiceException Thrown if any of the campaigns don't exist, if
	 * 							the user doesn't belong to any of the 
	 * 							campaigns, or if there is an error. 
	 */
	public void campaignsExistAndUserBelongs(
			final List<String> campaignIds, final String username) 
			throws ServiceException {
		
		for(String campaignId : campaignIds) {
			campaignExistsAndUserBelongs(campaignId, username);
		}
	}
	
	/**
	 * Verifies that the user is allowed to upload survey responses.
	 * 
	 * @param username The username of the user that is attempting to upload
	 * 				   this data.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to upload 
	 * 							survey responses or if there is an error.
	 */
	public void verifyUserCanUploadSurveyResponses( 
			final String username, final String campaignId) 
			throws ServiceException {
		
		try {
			if(! userCampaignQueries.getUserCampaignRoles(username, campaignId).contains(Campaign.Role.PARTICIPANT)) {
				throw new ServiceException(
						ErrorCode.SURVEY_INSUFFICIENT_PERMISSIONS, 
						"The user is not a participant in the campaign and, therefore, cannot upload responses.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the personal information of all
	 * of the users in a campaign.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to view the
	 * 							personal information about all of the users in
	 * 							the class or if there is an error.
	 */
	public void verifyUserCanReadUsersInfoInCampaign(
			final String username, final String campaignId) 
			throws ServiceException  {
		
		try {
			if(! userCampaignQueries.getUserCampaignRoles(username, campaignId).contains(Campaign.Role.SUPERVISOR)) {
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user is not allowed to read the personal information of the users in the following campaign: " + 
							campaignId);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the personal information of all
	 * of the users in all of the campaigns.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignIds A Collection of unique identifiers for the campaigns.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to read the
	 * 							personal information of the users in one of the
	 * 							classes or if there is an error.
	 */
	public void verifyUserCanReadUsersInfoInCampaigns(
			final String username, final Collection<String> campaignIds) 
			throws ServiceException {
		
		for(String campaignId : campaignIds) {
			verifyUserCanReadUsersInfoInCampaign(username, campaignId);
		}
	}
	
	/**
	 * Verifies that some user is allowed to update some campaign.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to update 
	 * 							this campaign or if there is an error.
	 */
	public void verifyUserCanUpdateCampaign(final String username, 
			final String campaignId) throws ServiceException {
		
		try {
			List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(Campaign.Role.SUPERVISOR) ||
			   roles.contains(Campaign.Role.AUTHOR)) {
				return;
			}
			
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user is not allowed to update the campaign.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to update a campaign's XML.
	 *  
	 * @param username The username of the user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param id The ID from the new XML.
	 * 
	 * @param name The name from the new XML.
	 * 
	 * @throws ServiceException Thrown if the user isn't allowed to modify the
	 * 							campaign, if the user is allowed to modify the
	 * 							campaign but responses exist, or if there is an
	 * 							error.
	 */
	public void verifyUserCanUpdateCampaignXml(
			final String username,
			final String campaignId,
			final String id,
			final String name) 
			throws ServiceException {
		
		try {
			// Get the user's roles for this campaign.
			List<Campaign.Role> roles = 
				userCampaignQueries
					.getUserCampaignRoles(username, campaignId);
			
			// If the user isn't a supervisor or an author, then they aren't 
			// allowed to update it.
			if(!
				(roles.contains(Campaign.Role.SUPERVISOR) ||
				roles.contains(Campaign.Role.AUTHOR))) {

				throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user is not allowed to modify the campaign's XML.");
			}
			
			// If the campaign already has survey responses, then it cannot be
			// updated by anyone.
			if(campaignSurveyResponseQueries
				.getNumberOfSurveyResponsesForCampaign(campaignId) != 0) {
				
				throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"Survey responses exist; therefore the XML can no longer be modified.");
			}
			
			// Check to ensure that the ID of the campaign hasn't changed.
			if(! campaignId.equals(id)) {
				throw new ServiceException(
					ErrorCode.CAMPAIGN_XML_HEADER_CHANGED, 
					"The campaign's ID in the new XML must be the same as the original XML.");
			}
			
			// Check to ensure that the name of the campaign hasn't changed.
			if(! campaignQueries.getName(campaignId).equals(name)) {
				throw new ServiceException(
					ErrorCode.CAMPAIGN_XML_HEADER_CHANGED, 
					"The campaign's name in the new XML must be the same as the original XML.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user is allowed to grant or revoke all of the roles in
	 * the collection.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign where the user
	 * 					 is attempting to add or revoke roles.
	 * 
	 * @param roles The roles to check if the user can grant or revoke.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to grant or
	 * 							revoke some role or if there is an error.
	 */
	public void verifyUserCanGrantOrRevokeRoles(final String username, 
			final String campaignId, final Collection<Campaign.Role> roles) 
			throws ServiceException {
		
		try {
			List<Campaign.Role> usersRoles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			if(usersRoles.contains(Campaign.Role.SUPERVISOR)) {
				return;
			}
			
			if(usersRoles.contains(Campaign.Role.AUTHOR)) {
				if(! roles.contains(Campaign.Role.SUPERVISOR)) {
					return;
				}
				
				throw new ServiceException(
						ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
						"The user is not allowed to grant the supervisor privilege.");
			}
			
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user is not allowed to grant privileges.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the user can delete the campaign. At least one of the 
	 * following must be true to delete a campaign:
	 * <ul>
	 *   <li>The user is a supervisor.</li>
	 *   <li>The user is an author and there are no responses.</li>
	 * </ul>
	 * <br />
	 * 
	 * @param username The user's username.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to delete the campaign or if there
	 * 							is an error.
	 */
	public void userCanDeleteCampaign(final String username, 
			final String campaignId) throws ServiceException {
		
		try {
			List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(Campaign.Role.SUPERVISOR)) {
				return;
			}
			
			if(roles.contains(Campaign.Role.AUTHOR)) {
				long numberOfResponses = campaignSurveyResponseQueries.getNumberOfSurveyResponsesForCampaign(campaignId);
				
				if(numberOfResponses == 0) {
					return;
				}
				else {
					throw new ServiceException(
							ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
							"The campaign has responses; therefore, you can no longer delete it.");
				}
			}
			
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"You do not have sufficient permissoins to delete this campaign.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of usernames for the users in a campaign. The List will
	 * be empty if there are no users in the campaign or if the campaign 
	 * doesn't exist.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for all of the users in a class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getUsersInCampaign(final String campaignId) 
			throws ServiceException {
		
		try {
			return userCampaignQueries.getUsersInCampaign(campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns a Set of distinct usernames for all of the users in a Collection 
	 * of campaign IDs.
	 * 
	 * @param campaignIds The unique identifier of the campaign.
	 * 
	 * @return Returns a Set of distinct usernames from all of the campaigns.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Set<String> getUsersInCampaigns(
			final Collection<String> campaignIds) throws ServiceException {
		
		Set<String> usernames = new HashSet<String>();
		for(String campaignId : campaignIds) {
			usernames.addAll(getUsersInCampaign(campaignId));
		}
		return usernames;
	}
	
	/**
	 * Aggregates all of the personal information for all of the users in all 
	 * of the campaigns without duplicates.
	 * 
	 * @param campaignIds A Collection of unique identifiers for campaigns.
	 * 
	 * @return A Map of usernames to personal information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Map<String, UserPersonal> getPersonalInfoForUsersInCampaigns(
			final Collection<String> campaignIds) throws ServiceException {
		
		try {
			Map<String, UserPersonal> result = new HashMap<String, UserPersonal>();
			Collection<String> usernames = getUsersInCampaigns(campaignIds);
			
			for(String username : usernames) {
				result.put(username, userQueries.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks that the requesting user can view survey responses for some 
	 * collection of users. There may not actually be any responses to read or
	 * the responses may need to be made public first. This only guarantees 
	 * that, if the other users have any public responses that the requesting
	 * user is allowed to view them. Therefore, this will pass as long as any 
	 * of the following are true:
	 * <br />
	 * <br />
	 * - If the user is an admin
	 * - If the user is a supervisor or an author.<br />
	 * - If the user is an analyst and the campaign is shared.<br />
	 * - If the user is the same as all of the requesting users.<br />
	 * <br />
	 * If you want to check if a user can read survey responses from every user
	 * in a campaign, don't pass in any user usernames.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param requesterUsername The requesting user's username.
	 * 
	 * @param userUsernames The array of usernames of specific users to check
	 * 						if the requesting user has permission to read their
	 * 						information.
	 *
	 * @throws ServiceException Thrown if none of the rules are true or there 
	 * 							is an error.
	 */
	public void requesterCanViewUsersSurveyResponses(
			final String campaignId, final String requesterUsername, 
			final String... userUsernames) throws ServiceException {
		try {
			
			// If the requester is an admin, he/she can read it.
			if(userQueries.userIsAdmin(requesterUsername)) {
				return;
			}
						
			// If the requester is asking about other users.
			if(userUsernames.length != 0) {
				// If the requester is the same as all of the users in question.
				boolean otherUsers = false;
				for(String username : userUsernames) {
					if(! requesterUsername.equals(username)) {
						otherUsers = true;
					}
				}
				if(! otherUsers) {
					return;
				}
			}
			
			List<Campaign.Role> requesterRoles = 
				userCampaignQueries.getUserCampaignRoles(requesterUsername, campaignId);
			
			// If the requester's role list contains supervisor, return.
			if(requesterRoles.contains(Campaign.Role.SUPERVISOR)) {
				return;
			}
			
			// If the requester's role list contains author, return.
			if(requesterRoles.contains(Campaign.Role.AUTHOR)) {
				return;
			}
			
			// If the requester's role list contains analyst,
			if(requesterRoles.contains(Campaign.Role.ANALYST)) {
				Campaign.PrivacyState privacyState = campaignQueries.getCampaignPrivacyState(campaignId);
				
				if((privacyState != null) && 
				   (Campaign.PrivacyState.SHARED.equals(privacyState))) {
					return;
				}
			}
				
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user does not have sufficient permissions to read information about other users.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Generates a List of unique identifiers for campaigns based on the
	 * parameters. The List is based on the 'campaignIds' parameter unless it 
	 * is null in which case the List is based on all campaigns visible to the 
	 * user. All parameters except 'request' and 'username' are optional and
	 * each will filter the resulting List of campaign identifiers.<br />
	 * <br />
	 * <br />
	 * For example, if 'campaignIds' was null as were 'endDate' and 
	 * 'privacyState', then what would be returned would be the intersection of
	 * the following lists:<br />
	 * - All of the campaigns to which the user was associated (because 
	 * 'campaignIds' was null).<br />
	 * - All of the campaigns that are associated with any of the classes whose
	 * unique identifier was in the 'classIds' list.<br />
	 * - All of the campaigns whose creation timestamp was equal to or after
	 * 'startDate'<br />
	 * - All of the campaigns whose running state equaled 'runningState'.<br />
	 * - All of the campaigns to which the user had the campaign role 'role'.
	 * <br />
	 * <br />
	 * Therefore, if a campaign was associated with a user only through a 
	 * single class, but that class wasn't in the 'classIds' list, then that
	 * campaign ID would not be returned even if all of the other parameters
	 * matched.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignIds An optional Collection of campaign identifiers from 
	 * 					  which to base the List. If this is empty, the 
	 * 					  resulting List will be empty. If this is null, the
	 * 					  base List will be all campaigns to which the user is
	 * 					  associated.
	 * 
	 * @param classIds A Collection of unique identifiers for classes where the
	 * 				   resulting list will only contain campaign identifiers
	 * 				   for campaigns that are associated with any of these
	 * 				   classes.
	 * 
	 * @param startDate A Calendar where only campaigns whose creation 
	 * 					timestamp is equal to or after this date.
	 * 
	 * @param endDate A Calendar where only campaigns whose creation timestamp
	 * 				  is equal to or before this date.
	 * 
	 * @param privacyState A campaign privacy state that trims the resulting 
	 * 					   list of campaigns to only those that have this 
	 * 					   privacy state.
	 * 					   
	 * @param runningState A campaign running state that trims the resulting
	 * 					   list of campaigns to only those that have this
	 * 					   running state.
	 * 
	 * @param role A campaign role that trims the resulting list of campaigns 
	 * 			   to only those where the user has that role in the campaign.
	 *  
	 * @return A Set of campaign unique identifiers based on the 'campaignIds'
	 * 		   parameter and trimmed by the rest of the parameters.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Set<String> getCampaignsForUser(final String username, 
			final Collection<String> campaignIds, 
			final Collection<String> classIds,
			final DateTime startDate, final DateTime endDate, 
			final Campaign.PrivacyState privacyState, 
			final Campaign.RunningState runningState, 
			final Campaign.Role role) throws ServiceException {
		
		try {
			Set<String> desiredCampaignIds = new HashSet<String>();
			
			if(campaignIds == null) {
				// Initializes the list with all of the campaign IDs for the 
				// requesting user.
				desiredCampaignIds.addAll(userCampaignQueries.getCampaignIdsAndNameForUser(username).keySet());
			}
			else {
				// Initializes the list with the campaign IDs in the query.
				desiredCampaignIds.addAll(campaignIds);
			}
			
			if(desiredCampaignIds.size() == 0) {
				return Collections.emptySet();
			}
			
			if(classIds != null) {
				// Get all of the campaigns associated with all of the classes in
				// the list.
				for(String classId : classIds) {
					desiredCampaignIds.retainAll(campaignClassQueries.getCampaignsAssociatedWithClass(classId));
				}
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			if(startDate != null) {
				// Get all of the campaigns whose creation timestamp is greater
				// than or equal to the start date.
				desiredCampaignIds.retainAll(campaignQueries.getCampaignsOnOrAfterDate(startDate));
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			if(endDate != null) {
				// Get all of the campaigns whose creation timestamp is less than
				// or equal to the end date.
				desiredCampaignIds.retainAll(campaignQueries.getCampaignsOnOrBeforeDate(endDate));
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			if(privacyState != null) {
				// Get all of the campaigns with a privacy state of 'privacyState'.
				desiredCampaignIds.retainAll(campaignQueries.getCampaignsWithPrivacyState(privacyState));
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			if(runningState != null) {
				// Get all of the campaigns with a running state of 'runningState'.
				desiredCampaignIds.retainAll(campaignQueries.getCampaignsWithRunningState(runningState));
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			if(role != null) {
				// Get all of the campaigns where the user's role is 'role'.
				desiredCampaignIds.retainAll(userCampaignQueries.getCampaignIdsForUserWithRole(username, role));
				
				if(desiredCampaignIds.size() == 0) {
					return Collections.emptySet();
				}
			}
			
			return desiredCampaignIds;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Gathers the requested information about a campaign. This will be at 
	 * least its name, description (possibly null), running state, privacy 
	 * state, creation timestamp, and all of the requesting user's roles in the
	 * campaign.<br />
	 * <br />
	 * The extras include the campaign's XML, all of the users associated with 
	 * the campaign and their roles, and all of the classes associated with the
	 * campaign.
	 * 
	 * @param username The username of the user whose roles in the campaign are
	 * 				   desired.
	 * 
	 * @param campaignIds The IDs for the campaigns whose information is 
	 * 					  desired.
	 * 
	 * @param withExtras A flag to indicate if the extra information should be
	 * 					 included in each Campaign object.
	 * 
	 * @return A map of campaigns and their information to the list of roles
	 * 		   for this user in the campaign.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Map<Campaign, List<Campaign.Role>> getCampaignAndUserRolesForCampaigns(
			final String username, final Collection<String> campaignIds, 
			final boolean withExtras) throws ServiceException {
		
		try {
			Map<Campaign, List<Campaign.Role>> result = new HashMap<Campaign, List<Campaign.Role>>();
			
			for(String campaignId : campaignIds) {
				// Create the Campaign object with the campaign's ID.
				Campaign campaign = campaignQueries.getCampaignInformation(campaignId);
				
				// Get the user's roles.
				List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
				
				// If we are supposed to get the extra information as well.
				if(withExtras) {
					
					// Add the classes that are associated with the campaign.
					try {
						campaign.addClasses(campaignClassQueries.getClassesAssociatedWithCampaign(campaignId));
					} 
					catch(DomainException e) {
						throw new ServiceException(
								"There was a problem adding a class.",
								e);
					}
					
					// Add the users and their roles to the campaign.
					campaign.addUsers(userCampaignQueries.getUsersAndRolesForCampaign(campaignId));
				}

				// Add the user's roles.
				result.put(campaign, roles);
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the information about a campaign.
	 * 
	 * @param campaignId The campaign's unqiue identifier.
	 * 
	 * @param withClasses Whether or not to populate the campaign with its list
	 * 					  of classes.
	 * 
	 * @param withUsers Whether or not to populate the campaign with its list 
	 * 					of users and their respective roles.
	 * 
	 * @return The campaign object with the specified information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Campaign getCampaignInformation(
			final String campaignId,
			final boolean withClasses,
			final boolean withUsers)
			throws ServiceException {
		
		try {
			Campaign result = campaignQueries.findCampaignConfiguration(campaignId);

			if(withClasses) {
				try {
					result.addClasses(
							campaignClassQueries.getClassesAssociatedWithCampaign(
									campaignId
								)
						);
				}
				catch(DomainException e) {
					throw new ServiceException(
							"There was a problem adding a class.",
							e);
				}
			}
			
			if(withUsers) {
				List<String> campaignUsernames = 
					userCampaignQueries.getUsersInCampaign(campaignId);
				
				for(String campaignUsername : campaignUsernames) {
					List<Campaign.Role> userRoles = 
						userCampaignQueries.getUserCampaignRoles(
								campaignUsername, 
								campaignId
							);
					
					for(Campaign.Role userRole : userRoles) {
						try {
							result.addUser(campaignUsername, userRole);
						}
						catch(DomainException e) {
							throw new ServiceException(
									"There was a problem adding a user.",
									e);
						}
					}
				}
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Gathers the information about the classes that match the criteria based
	 * on the user's permissions. If the requesting user is an admin, they will 
	 * see all campaigns; otherwise, they will only see the campaigns to which
	 * they belong.
	 * 
	 * @param username The requesting user's username. This parameter is 
	 * 				   required.
	 * 
	 * @param campaignIds A list of campaign unique identifiers. This is 
	 * 					  optional and may be null. It limits the results to 
	 * 					  only those campaigns to which the user belongs.
	 * 
	 * @param classIds A list of class unique identifiers. This is optional and
	 * 				   may be null. It limits the results to only those 
	 * 				   campaigns that are associated with any class in this
	 * 				   list.
	 * 
	 * @param nameTokens A collection of token strings which limit the results
	 * 					 to only those campaigns whose name contains at least 
	 * 					 one of the tokens.
	 * 
	 * @param descriptionTokens A collection of token strings which limit the
	 * 							results to only those campaigns that have a 
	 * 							description and whose description contains at
	 * 							least one of the tokens.
	 * 
	 * @param startDate A date that limits the results to only those campaigns
	 * 					that were created on or after this date.
	 * 
	 * @param endDate A date that limits the results to only those campaigns 
	 * 				  that were created on or before this date.
	 * 
	 * @param privacyState A campaign privacy state the limits the results to
	 * 					   only those campaigns that have this privacy state.
	 * 
	 * @param runningState A campaign running state that limits the results to
	 * 					   only those campaigns that have this running state.
	 * 
	 * @param role A campaign role which limits the results to only those 
	 * 			   campaigns where the requesting user has this role in the 
	 * 			   campaign.
	 * 
	 * @param withClasses Whether or not to aggregate all of the classes 
	 * 					  associated with this campaign.
	 * 					  
	 * @param withUsers Whether or not to aggregate all of the users and their
	 * 					respective roles for this campaign.
	 * 
	 * @return A map of Campaign objects to the requesting user's respective
	 * 		   roles.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Map<Campaign, Collection<Campaign.Role>> getCampaignInformation(
			final String username, 
			final Collection<String> campaignIds, 
			final Collection<String> classIds,
			final Collection<String> nameTokens,
			final Collection<String> descriptionTokens,
			final DateTime startDate, final DateTime endDate, 
			final Campaign.PrivacyState privacyState, 
			final Campaign.RunningState runningState, 
			final Campaign.Role role,
			final boolean withClasses,
			final boolean withUsers) 
			throws ServiceException {
		
		try {
			QueryResultsList<Campaign> queryResult = 
					campaignQueries.getCampaignInformation(
							username, 
							campaignIds, 
							classIds, 
							nameTokens,
							descriptionTokens,
							startDate, 
							endDate, 
							privacyState, 
							runningState, 
							role);
			List<Campaign> campaignResults = queryResult.getResults();
			
			Map<Campaign, Collection<Campaign.Role>> result =
					new HashMap<Campaign, Collection<Campaign.Role>>(
							campaignResults.size());
			
			for(Campaign campaign : campaignResults) {
				result.put(
						campaign, 
						userCampaignQueries.getUserCampaignRoles(
								username, 
								campaign.getId()));
				try {
				// get authors
					campaign.addAuthorList(userCampaignQueries.getAuthorsForCampaign(campaign.getId()));
				} catch(DomainException e) {
						throw new ServiceException(
								"There was a problem adding the author list.",
								e);
				}
				
				if(withClasses) {
					try {
						campaign.addClasses(
								campaignClassQueries.getClassesAssociatedWithCampaign(
										campaign.getId()));
					}
					catch(DomainException e) {
						throw new ServiceException(
								"There was a problem adding the classes.",
								e);
					}
				}
				
				if(withUsers) {
					// Add the users and their roles to the campaign.
					campaign.addUsers(
							userCampaignQueries.getUsersAndRolesForCampaign(
									campaign.getId()));
				}
				
				// Get and apply the masks for this campaign.
				campaign
					.addMasks(
						userCampaignQueries
							.getCampaignMasks(
								null, 
								null, 
								null, 
								null, 
								username, 
								campaign.getId()));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that some user is allowed to read the list of users in a 
	 * campaign.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have permissions to
	 * 							read the users of a campaign and their roles or
	 * 							if there is an error.
	 */
	public void verifyUserCanReadUsersInCampaign(final String username,
			final String campaignId) throws ServiceException {
		
		try {
			List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(Campaign.Role.SUPERVISOR) || 
					roles.contains(Campaign.Role.AUTHOR)) {
				return;
			}
			
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + 
						campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of users in a 
	 * Collection of campaigns.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignIds The Collection of campaign unique identifiers.
	 * 
	 * @throws ServiceException Thrown if the user is not allowed to read the
	 * 							list of users in one of the campaigns or if 
	 * 							there is an error.
	 */
	public void verifyUserCanReadUsersInCampaigns(final String username,
			final Collection<String> campaignIds) throws ServiceException {
		
		for(String campaignId : campaignIds) {
			verifyUserCanReadUsersInCampaign(username, campaignId);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of classes associated 
	 * with a campaign.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the list of classes for a
	 * 							campaign.
	 */
	public void verifyUserCanReadClassesAssociatedWithCampaign(
			final String username, final String campaignId) 
			throws ServiceException {
		
		try {
			List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			if(roles.contains(Campaign.Role.SUPERVISOR) || 
					roles.contains(Campaign.Role.AUTHOR)) {
				return;
			}
			
			throw new ServiceException(
					ErrorCode.CAMPAIGN_INSUFFICIENT_PERMISSIONS, 
					"The user doesn't have sufficient permissions to read the users and their roles for a campaign: " + 
						campaignId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user is allowed to read the list of classes associated
	 * with a Collection of campaigns.
	 * 
	 * @param username The username of the user whose permissions are being
	 * 				   checked.
	 * 
	 * @param campaignIds A Collection of unique identifiers for the campaigns.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the list of classes for any
	 * 							of the campaigns.
	 */
	public void verifyUserCanReadClassesAssociatedWithCampaigns(
			final String username, final Collection<String> campaignIds) 
			throws ServiceException {
		
		for(String campaignId : campaignIds) {
			verifyUserCanReadClassesAssociatedWithCampaign(username, campaignId);
		}
	}
	
	/**
	 * Verifies that each username in usernameList belongs to the campaign
	 * specified by the campaignId.
	 * 
	 * @param campaignId  The campaign to check each user against.
	 * @param usernameList  The users in question
	 * @throws ServiceException  If any user in usernameList does not belong
	 * to the campaign or if an IO problem occurs.
	 * @throws IllegalArgumentException if the request is null; if the 
	 * campaignId is empty or null; or if the usernameList is null.
	 */
	public void verifyUsersExistInCampaign(final String campaignId, 
			final Collection<String> usernameList) throws ServiceException {
		
		// check for logical errors
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId) || usernameList == null) {
			throw new IllegalArgumentException("null request, empty campaignId, or null usernameList");
		}
		
		// check each username in usernameList
		try {
			for(String username : usernameList) {
				if(! userCampaignQueries.userBelongsToCampaign(username, campaignId)) {
					StringBuilder sb = new StringBuilder();
					sb.append("User in usernameList does not belong to campaign. Username: ");
					sb.append(username);
					sb.append(" Campaign ID: ");
					sb.append(campaignId);
					String msg = sb.toString();
					throw new ServiceException(ErrorCode.USER_NOT_IN_CAMPAIGN, msg);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}