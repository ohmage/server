package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.CampaignImageDaos;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserImageDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services that create, read, update, and delete
 * user-image associations.
 * 
 * @author John Jenkins
 */
public final class UserImageServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserImageServices() {}
	
	/**
	 * Verifies that some photo prompt response exists and the image ID for
	 * the response is the same as the given image ID.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @throws ServiceException Thrown if no such photo prompt response exists
	 * 							or if there is an error.
	 */
	public static void verifyPhotoPromptResponseExistsForUserAndImage(Request request, String username, String imageId) throws ServiceException {
		try {
			if(! UserImageDaos.responseExistsForUserWithImage(username, imageId)) {
				request.setFailed(ErrorCodes.IMAGE_INVALID_ID, "No such photo prompt response exists with the given image ID.");
				throw new ServiceException("No such photo prompt response exists with the given image ID.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that an user has sufficient permissions to read an image.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param requesterUsername The username of the user making this request.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the image.
	 */
	public static void verifyUserCanReadImage(Request request, String requesterUsername, String imageId) throws ServiceException {
		try {
			// If it is their own image, they can read it.
			if(requesterUsername.equals(UserImageDaos.getImageOwner(imageId))) {
				return;
			}
			
			// Retrieve all of the campaigns associated with an image.
			List<String> campaignIds = CampaignImageDaos.getCampaignIdsForImageId(imageId);
			
			// For each of the campaigns, see if the requesting user has 
			// sufficient permissions.
			for(String campaignId : campaignIds) {
				List<CampaignRoleCache.Role> roles = UserCampaignDaos.getUserCampaignRoles(requesterUsername, campaignId);

				// If they are a supervisor.
				if(roles.contains(CampaignRoleCache.Role.SUPERVISOR)) {
					return;
				}
				
				// Retrieves the privacy state of the image in this campaign. 
				// If null is returned, something has changed since the list of
				// campaign IDs was retrieved, so we need to just error out.
				SurveyResponsePrivacyStateCache.PrivacyState imagePrivacyState = CampaignImageDaos.getImagePrivacyStateInCampaign(campaignId, imageId);
				
				// They are an author and the image is shared
				if(roles.contains(CampaignRoleCache.Role.AUTHOR) && 
						SurveyResponsePrivacyStateCache.PrivacyState.SHARED.equals(imagePrivacyState)) {
					return;
				}
				
				// Retrieve the campaign's privacy state.
				CampaignPrivacyStateCache.PrivacyState campaignPrivacyState = CampaignDaos.getCampaignPrivacyState(campaignId);
				
				// They are an analyst, the image is shared, and the campaign is shared.
				if(roles.contains(CampaignRoleCache.Role.ANALYST) && 
						SurveyResponsePrivacyStateCache.PrivacyState.SHARED.equals(imagePrivacyState) &&
						CampaignPrivacyStateCache.PrivacyState.SHARED.equals(campaignPrivacyState)) {
					return;
				}
			}
			
			// If we made it to this point, the requesting user doesn't have
			// sufficient permissions to read the image.
			request.setFailed(ErrorCodes.IMAGE_INSUFFICIENT_PERMISSIONS, "The user doesn't have sufficient permissions to read the image.");
			throw new ServiceException("The user doesn't have sufficient permissions to read the image.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}