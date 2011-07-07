package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.request.Request;

public class UserCampaignDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserCampaignDocumentServices() {}
	
	/**
	 * Verifies that a user can associate documents with a campaign. The only 
	 * restriction is that the user must belong to the campaign in some 
	 * capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignId The campaign ID of the campaign in question.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to associate documents with this
	 * 							campaign.
	 */
	public static void userCanAssociateDocumentsWithCampaign(Request request, String username, String campaignId) throws ServiceException {
		try {
			List<String> roles = UserCampaignDaos.getUserCampaignRoles(username, campaignId);
			
			if(roles.size() == 0) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_ASSOCIATE_CAMPAIGN, "The user is not a member of the following campaign and, therefore, cannot associate documents with it: " + campaignId);
				throw new ServiceException("The user is not a member of the following campaign and, therefore, cannot associate documents with it: " + campaignId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a user can associate documents with all of the campaigns
	 * in a list. The only restriction is that the user must belong to each of
	 * the campaigns.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignIds A List of campaign IDs where each campaign ID will be
	 * 					  checked that the user is a member of it.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user is
	 * 							not allowed to associate documents with any of
	 * 							the campaigns.
	 */
	public static void userCanAssociateDocumentsWithCampaigns(Request request, String username, List<String> campaignIds) throws ServiceException {
		for(String campaignId : campaignIds) {
			userCanAssociateDocumentsWithCampaign(request, username, campaignId);
		}
	}
}
