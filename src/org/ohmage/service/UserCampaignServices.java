package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserCampaignDaos;
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
				request.setFailed(ErrorCodes.CAMPAIGN_USER_DOES_NOT_BELONG, "The user does not belong to the campaign: " + campaignId);
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
	 * the campaign's existance be validated before this is run.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param requesterUsername The requesting user's username.
	 * 
	 * @param userUsername The username of the user about which the requester 
	 *					   is attempting to read data.
	 *
	 * @throws ServiceException Thrown if none of the rules are true or there 
	 * 							is an error.
	 */
	public static void requesterCanViewUsersSharedSurveyResponses(Request request, String campaignId, String requesterUsername, String userUsername) throws ServiceException {
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