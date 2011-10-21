package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.CampaignImageQueries;
import org.ohmage.query.CampaignQueries;
import org.ohmage.query.UserCampaignQueries;
import org.ohmage.query.UserImageQueries;

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
	 * @param username The username of the user.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @throws ServiceException Thrown if no such photo prompt response exists
	 * 							or if there is an error.
	 */
	public static void verifyPhotoPromptResponseExistsForUserAndImage(
			final String username, final String imageId) 
			throws ServiceException {
		
		try {
			if(! UserImageQueries.responseExistsForUserWithImage(username, imageId)) {
				throw new ServiceException(
						ErrorCode.IMAGE_INVALID_ID, 
						"No such photo prompt response exists with the given image ID.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that an user has sufficient permissions to read an image.
	 * 
	 * @param requesterUsername The username of the user making this request.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @throws ServiceException Thrown if the user doesn't have sufficient
	 * 							permissions to read the image.
	 */
	public static void verifyUserCanReadImage(final String requesterUsername, 
			final String imageId) throws ServiceException {
		
		try {
			// If it is their own image, they can read it.
			if(requesterUsername.equals(UserImageQueries.getImageOwner(imageId))) {
				return;
			}
			
			// Retrieve all of the campaigns associated with an image.
			List<String> campaignIds = CampaignImageQueries.getCampaignIdsForImageId(imageId);
			
			// For each of the campaigns, see if the requesting user has 
			// sufficient permissions.
			for(String campaignId : campaignIds) {
				List<Campaign.Role> roles = UserCampaignQueries.getUserCampaignRoles(requesterUsername, campaignId);

				// If they are a supervisor.
				if(roles.contains(Campaign.Role.SUPERVISOR)) {
					return;
				}
				
				// Retrieves the privacy state of the image in this campaign. 
				// If null is returned, something has changed since the list of
				// campaign IDs was retrieved, so we need to just error out.
				SurveyResponse.PrivacyState imagePrivacyState = CampaignImageQueries.getImagePrivacyStateInCampaign(campaignId, imageId);
				
				// They are an author and the image is shared
				if(roles.contains(Campaign.Role.AUTHOR) && 
						SurveyResponse.PrivacyState.SHARED.equals(imagePrivacyState)) {
					return;
				}
				
				// Retrieve the campaign's privacy state.
				Campaign.PrivacyState campaignPrivacyState = CampaignQueries.getCampaignPrivacyState(campaignId);
				
				// They are an analyst, the image is shared, and the campaign is shared.
				if(roles.contains(Campaign.Role.ANALYST) && 
						SurveyResponse.PrivacyState.SHARED.equals(imagePrivacyState) &&
						Campaign.PrivacyState.SHARED.equals(campaignPrivacyState)) {
					return;
				}
			}
			
			// If we made it to this point, the requesting user doesn't have
			// sufficient permissions to read the image.
			throw new ServiceException(
					ErrorCode.IMAGE_INSUFFICIENT_PERMISSIONS, 
					"The user doesn't have sufficient permissions to read the image.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}