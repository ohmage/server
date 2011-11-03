package org.ohmage.query;

import java.util.List;
import java.util.Map;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;

public interface IUserCampaignQueries {

	/**
	 * Retrieves whether or not a user belongs to a campaign in any capacity.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignId The campaign ID of the campaign in question.
	 * 
	 * @return Whether or not the user exists in a campaign.
	 */
	boolean userBelongsToCampaign(String username, String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves all of the users from a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for the users in the campaign.
	 */
	List<String> getUsersInCampaign(String campaignId)
			throws DataAccessException;

	/**
	 * Returns a List of roles for this user in this campaign.
	 * 
	 * @param username The username of the user that whose roles are desired.
	 * 
	 * @param campaignId The campaign ID for the campaign that the user's roles
	 * 					 are being requested.
	 * 
	 * @return A possibly empty List of roles for this user in this campaign.
	 */
	List<Campaign.Role> getUserCampaignRoles(String username, String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves all of the campaign IDs and their respective names to which a
	 * user is associated.
	 * 
	 * @param username The username of the user.
	 * 
	 * @return A Map of campaign IDs to campaign names for all of the campaigns
	 * 		   to which the user is associated.
	 */
	Map<String, String> getCampaignIdsAndNameForUser(String username)
			throws DataAccessException;

	/**
	 * Retrieves all of the campaign IDs that are associated with a user and
	 * the user has a specific role.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param role The campaign role that the user must have.
	 * 
	 * @return A List of unique identifiers for all campaigns with which the 
	 * 		   user is associated and has the given role. 
	 */
	List<String> getCampaignIdsForUserWithRole(String username,
			Campaign.Role role) throws DataAccessException;

}