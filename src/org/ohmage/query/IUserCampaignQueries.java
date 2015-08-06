/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.ohmage.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.CampaignMask;
import org.ohmage.domain.campaign.Campaign.Role;
import org.ohmage.domain.campaign.CampaignMask.MaskId;
import org.ohmage.exception.DataAccessException;

public interface IUserCampaignQueries {
	/**
	 * Creates a campaign mask for the given user. When the campaign is
	 * queried, only the surveys given by this list will be shown to the user.
	 * 
	 * @param mask The campaign mask to save.
	 * 
	 * @throws DataAccessException There was an error saving the data.
	 */
	void createUserCampaignMask(CampaignMask mask) throws DataAccessException;

	/**
	 * Retrieves whether or not a user belongs to a campaign in any capacity.
	 * 
	 * @param username
	 *        The username of the user in question.
	 * 
	 * @param campaignId
	 *        The campaign ID of the campaign in question.
	 * 
	 * @return Whether or not the user exists in a campaign.
	 */
	boolean userBelongsToCampaign(String username, String campaignId)
		throws DataAccessException;

	/**
	 * Retrieves all of the users from a campaign.
	 * 
	 * @param campaignId
	 *        The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for the users in the campaign.
	 */
	List<String> getUsersInCampaign(String campaignId)
		throws DataAccessException;

	/**
	 * Returns a List of roles for this user in this campaign.
	 * 
	 * @param username
	 *        The username of the user that whose roles are desired.
	 * 
	 * @param campaignId
	 *        The campaign ID for the campaign that the user's roles are being
	 *        requested.
	 * 
	 * @return A possibly empty List of roles for this user in this campaign.
	 */
	List<Campaign.Role> getUserCampaignRoles(String username, String campaignId)
		throws DataAccessException;

	
	/**
	 * Returns a List of roles for this user in all campaigns that fit the 
	 * request criteria.
	 * 
	 * @param username
	 *        The username of the user that whose roles are desired.
	 * 
	 * @param campaignListSubSelect
	 *        The subselect statement that returns a list of campaigns. 
	 *        
	 * @param SubSelectParameters 
	 * 		  The parameters to be used for the subselect statement.
	 * 
	 * @return A possibly empty List of roles for this user in this campaign.
	 */
	public Map<String, Collection<Campaign.Role>> getUserCampaignRolesForCampaignList(
			final String username, 
			final String campaignListSubSelect,
			final Collection<Object> SubSelectParameters) throws DataAccessException;

		
	/**
	 * Retrieves all of the campaign IDs and a respective set of campaign roles
	 * for a given user.
	 * 
	 * @param username
	 *        The user's username.
	 * 
	 * @return A map of campaign IDs to the user's roles in that campaign.
	 * 
	 * @throws DataAccessException
	 *         There was an error.
	 */
	public Map<String, Set<Campaign.Role>> getCampaignAndRolesForUser(
		final String username) throws DataAccessException;

	/**
	 * Retrieves all of the campaign IDs and a respective set of campaign roles
	 * for a given set of users. 
	 * @param userSubSelectStmt TODO
	 * @param userSubSelectParameters TODO
	 * 
	 * @return A map of campaign IDs to the user's roles in that campaign.
	 * 
	 * @throws DataAccessException
	 *         There was an error.
	 */
	public Map<String, Map<String, Set<Campaign.Role>>> getCampaignAndRolesForUsers(
		final String userSubSelectStmt, 
		final Collection<Object> userSubSelectParameters) throws DataAccessException;

	
	/**
	 * Returns a map of usernames to a set of campaign roles for all of the
	 * users in a campaign.
	 * 
	 * @param campaignId
	 *        The campaign's unique identifier.
	 * 
	 * @return A map of usernames to a set of campaign roles.
	 * 
	 * @throws DataAccessException
	 *         Thrown if there is an error.
	 */
	public Map<String, Collection<Campaign.Role>> getUsersAndRolesForCampaign(
		String campaignId) throws DataAccessException;

	/**
	 * Returns a map of campaign urns to a map of users and their roles in those campaigns.
	 * 
	 * @param subSelectStmt
	 *        The sub select statement that returns a list of campaign ids.
	 * 
	 * @param subSelectParameters
	 * 		  The parameters to be used for the above subSelectStmt.
	 * 
	 * @return A map of campaign urns to a map of users and their roles associated 
	 * 		  with each campaign.
	 * 
	 * @throws DataAccessException
	 *         Thrown if there is an error.
	 */
	public Map<String, Map<String, Collection<Role>>> getUsersAndRolesForCampaignList(
			final String subSelectStmt, 
			final Collection<Object> subSelectParameters)
			throws DataAccessException;

	/**
	 * Returns a list of usernames to that are authors in a campaign.
	 * 
	 * @param campaignId
	 *        The campaign's unique identifier.
	 * 
	 * @return A list of authors' usernames.
	 * 
	 * @throws DataAccessException
	 *         Thrown if there is an error.
	 */
	public List<String> getAuthorsForCampaign(
			final String campaignId)
			throws DataAccessException;
	
	/**
	 * Returns a map of campaigns and author lists associated with each campaign. 
	 *  
	 * @param campaignListSubSelect 
	 *        The sub-select statement to be used to retrived a list of valid campaigns
	 * 
	 * @param parameters 
	 * 		  The list of objects to be used as parameters to the sub-select statement. 
	 * 
	 * @return A map of campaigns and author lists assoicated with each campaign.
	 * 
	 * @throws DataAccessException
	 *         Thrown if there is an error.
	 */
	public Map<String, Collection<String>> getAuthorsForCampaignList(
			final String campaignListSubSelect, 
			final Collection<Object> parameters)
			throws DataAccessException; 
			
	/**
	 * Retrieves all of the campaign IDs and their respective names to which a
	 * user is associated.
	 * 
	 * @param username
	 *        The username of the user.
	 * 
	 * @return A Map of campaign IDs to campaign names for all of the campaigns
	 *         to which the user is associated.
	 */
	Map<String, String> getCampaignIdsAndNameForUser(String username)
		throws DataAccessException;

	/**
	 * Retrieves all of the campaign IDs that are associated with a user and
	 * the user has a specific role.
	 * 
	 * @param username
	 *        The username of the user.
	 * 
	 * @param role
	 *        The campaign role that the user must have.
	 * 
	 * @return A List of unique identifiers for all campaigns with which the
	 *         user is associated and has the given role.
	 */
	List<String> getCampaignIdsForUserWithRole(
		String username,
		Campaign.Role role) throws DataAccessException;

	/**
	 * Retrieves the masks that meet the criteria.
	 * 
	 * @param maskId Retrieves the mask with the given mask ID.
	 * 
	 * @param startDate Retrieves all of the masks that have a creation
	 * 					timestamp greater than or equal to this timestamp.
	 * 
	 * @param endDate Retrieves all of the masks that have a creation timestamp
	 * 				  less than or equal to this timestamp.
	 * 
	 * @param assignerUserId Returns all of the masks that were assigned by the
	 * 						 user with this user ID.
	 * 
	 * @param assigneeUserId Returns all of the masks that were assigned to the
	 * 						 user with this user ID.
	 * 
	 * @param campaignId Returns all of the masks for the campaign with this
	 * 					 unique campaign identifier.
	 * 
	 * @return The list of campaign masks that matched the given criteria.
	 */
	List<CampaignMask> getCampaignMasks(
		CampaignMask.MaskId maskId,
		DateTime startDate,
		DateTime endDate,
		String assignerUserId,
		String assigneeUserId,
		String campaignId)
		throws DataAccessException;
	
	
	public Map<String, Collection<CampaignMask>> getCampaignMasksForCampaignList(
			final MaskId maskId,
			final DateTime startDate,
			final DateTime endDate,
			final String assignerUserId,
			final String assigneeUserId,
			final String campaignSubSelectStmt,
			final Collection<Object> subSelectParameters)
			throws DataAccessException;

	
}
