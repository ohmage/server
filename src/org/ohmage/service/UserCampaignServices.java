package org.ohmage.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.domain.Campaign;
import org.ohmage.domain.User;
import org.ohmage.domain.UserRoleCampaignInfo;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services for user-campaign relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserCampaignServices {
	private static Logger LOGGER = Logger.getLogger(UserCampaignServices.class);
	
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
	 * @throws ServiceException Thrown if the campaign doesn't exist, the user
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
	 * Populates the User in the Request with campaign information and the
	 * User's associated roles for each campaign.
	 * 
	 * @param request The request to retrieve the User from.
	 * 
	 * @throws ServiceException Thrown if there is no user in the request, the
	 * user does not belong to any campaigns, or if there is an error. 
	 */
	public static void populateUserWithCampaignRoleInfo(Request request, User user) throws ServiceException {
		LOGGER.info("Populating the user in the request with campaign info and the user's roles for each campaign they belong to");
		
		try {
			List<UserRoleCampaignInfo> userRoleCampaignInfoList 
				= UserCampaignDaos.getAllCampaignRolesAndCampaignInfoForUser(user);
			
			for(UserRoleCampaignInfo info : userRoleCampaignInfoList) {
				Campaign campaign = new Campaign();
				campaign.setCampaignCreationTimestamp(info.getCampaignCreationTimestamp());
				campaign.setDescription(info.getCampaignDescription());
				campaign.setName(info.getCampaignName());
				campaign.setPrivacyState(info.getCampaignPrivacyState());
				campaign.setRunningState(info.getCampaignRunningState());
				campaign.setUrn(info.getCampaignUrn());
				user.addCampaignAndUserRole(campaign, info.getUserRole());
			}
		
		} catch (DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
