package org.ohmage.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserDaos;
import org.ohmage.domain.UserPersonal;
import org.ohmage.request.Request;

/**
 * This class contains the services for user-campaign relationships.
 * 
 * @author John Jenkins
 */
public class UserCampaignServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserCampaignServices() {}
	
	/**
	 * Ensures that a campaign exists and that a user belongs to the campaign
	 * in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param campaignId The campaign ID for the campaign in question.
	 * 
	 * @param username The username of the user that must belong to the 
	 * 				   campaign.
	 * 
	 * @throws ServiceException Thrown if the campaign doesn't eixst, the user
	 * 							doesn't belong to the campaign, or if there is
	 * 							an error.
	 */
	public static void campaignExistsAndUserBelongs(Request request, String campaignId, String username) throws ServiceException {
		CampaignServices.checkCampaignExistence(request, campaignId, true);
		
		try {
			if(! UserCampaignDaos.userBelongsToCampaign(username, campaignId)) {
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user does not belong to the campaign: " + campaignId);
				throw new ServiceException("The user does not belong to the campaign: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that all of the campaigns in a List exist and that the user 
	 * belongs to each of them in some capacity.
	 * 
	 * @param request The request that is performing this service.
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
	public static void campaignsExistAndUserBelongs(Request request, List<String> campaignIds, String username) throws ServiceException {
		for(String campaignId : campaignIds) {
			campaignExistsAndUserBelongs(request, campaignId, username);
		}
	}
	
	/**
	 * Ensures that the user has all of the roles in the Collection of roles.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose roles in the campaign is
	 * 				   being checked.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param campaignRoles A Collection of campaign roles that the user must
	 * 						possess in the class.
	 * 
	 * @throws ServiceException Thrown if the user is missing any of the roles
	 * 							in the campaign or if there is an error.
	 */
	public static void checkUserHasRolesInCampaign(Request request, String username, String campaignId, Collection<String> campaignRoles) throws ServiceException {
		try {
			if(! UserCampaignDaos.getUserCampaignRoles(username, campaignId).containsAll(campaignRoles)) {
				request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user is doesn't have sufficient permissions in the campaign: " + campaignId);
				throw new ServiceException("The user is doesn't have sufficient permissions in the campaign: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that the user has all of the roles in the Collection of roles 
	 * for each of the campaigns in the Collection of campaign IDs.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose roles in the campagins is
	 * 				   being checked.
	 * 
	 * @param campaignIds The Collection of campaign identifiers.
	 * 
	 * @param campaignRoles The Collection of campaign roles.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have any of the 
	 * 							roles in one of the classes or if there is an
	 * 							error.
	 */
	public static void checkUserHasRolesInCampaigns(Request request, String username, Collection<String> campaignIds, Collection<String> campaignRoles) throws ServiceException {
		for(String campaignId : campaignIds) {
			checkUserHasRolesInCampaign(request, username, campaignId, campaignRoles);
		}
	}
	
	/**
	 * Retrieves a List of usernames for the users in a campaign. The List will
	 * be empty if there are no users in the campaign or if the campaign 
	 * doesn't exist.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for all of the users in a class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getUsersInCampaign(Request request, String campaignId) throws ServiceException {
		try {
			return UserCampaignDaos.getUsersInCampaign(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns a Set of distinct usernames for all of the users in a Collection 
	 * of campaign IDs.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignIds The unique identifier of the campaign.
	 * 
	 * @return Returns a Set of distinct usernames from all of the campaigns.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Set<String> getUsersInCampaigns(Request request, Collection<String> campaignIds) throws ServiceException {
		Set<String> usernames = new HashSet<String>();
		for(String campaignId : campaignIds) {
			usernames.addAll(getUsersInCampaign(request, campaignId));
		}
		return usernames;
	}
	
	/**
	 * Aggregates all of the personal information for all of the users in all 
	 * of the campaigns without duplicates.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignIds A Collection of unique identifiers for campaigns.
	 * 
	 * @return A Map of usernames to personal information.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static Map<String, UserPersonal> getPersonalInfoForUsersInCampaigns(Request request, Collection<String> campaignIds) throws ServiceException {
		try {
			Map<String, UserPersonal> result = new HashMap<String, UserPersonal>();
			Collection<String> usernames = getUsersInCampaigns(request, campaignIds);
			
			for(String username : usernames) {
				result.put(username, UserDaos.getPersonalInfoForUser(username));
			}
			
			return result;
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}

	/**
	 * Checks that the requesting user can view survey responses for some user.
	 * There may not actually be any responses to read or the responses may
	 * need to be made public first. This only guarantees that, if the user has
	 * any public responses that the requesting user is allowed to view them.
	 * Therefore, this will pass as long as any of the following are true:
	 * <br />
	 * <br />
	 * - If the user is a supervisor or an author.<br />
	 * - If the user is an analyst and the campaign is shared.<br />
	 * - If the user is the requester and the campaign is running.<br />
	 * <br />
	 * If you wish to check if the requester can view shared survey responses  
	 * about any arbitrary user in a campaign, pass null for the
	 * 'userUsername'. This is the only parameter that is allowed to be null.
	 * <br />
	 * <br />
	 * If the campaign doesn't exist, this will set the request as failed 
	 * indicating that the user doesn't have sufficient permissions to view
	 * other users' survey responses. Therefore, it is highly recommended that
	 * the campaign's existence be validated before this is run.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param requesterUsername The requesting user's username.
	 * 
	 * @param userUsername The username of the user about which the requester 
	 *					   is attempting to read data. If null is passed, it
	 *					   will check to see if the requesting user can view
	 *					   survey responses about any user in the campaign.
	 *
	 * @throws ServiceException Thrown if none of the rules are true or there 
	 * 							is an error.
	 */
	public static void requesterCanViewUsersSurveyResponses(Request request, String campaignId, String requesterUsername, String userUsername) throws ServiceException {
		try {
			List<String> requesterRoles = UserCampaignDaos.getUserCampaignRoles(requesterUsername, campaignId);

			// If the requester's role list contains supervisor, return.
			if(requesterRoles.contains(CampaignRoleCache.ROLE_SUPERVISOR)) {
				return;
			}

			// If the requester's role list contains author, return.
			if(requesterRoles.contains(CampaignRoleCache.ROLE_AUTHOR)) {
				return;
			}

			// If the requester's role list contains analyst,
			if(requesterRoles.contains(CampaignRoleCache.ROLE_ANALYST)) {
				String privacyState = CampaignDaos.getCampaignPrivacyState(campaignId);

				if((privacyState != null) && 
				   (CampaignPrivacyStateCache.PRIVACY_STATE_SHARED.equals(privacyState))) {
					return;
				}
			}

			// If the requester is the same as the user in question.
			if(requesterUsername.equals(userUsername)) {
				String runningState = CampaignDaos.getCampaignRunningState(campaignId);

				// If the campaign is running, return.
				if((runningState != null) && 
				   (CampaignRunningStateCache.RUNNING_STATE_RUNNING.equals(runningState))) {
					return;
				}
			}

			request.setFailed(ErrorCodes.CAMPAIGN_INSUFFICIENT_PERMISSIONS, "The user does not have sufficient permissions to read information about other users.");
			throw new ServiceException("The user does not have sufficient permissions to read information about other users.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}