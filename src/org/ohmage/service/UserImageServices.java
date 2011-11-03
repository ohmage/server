package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignImageQueries;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserImageQueries;

/**
 * This class contains the services that create, read, update, and delete
 * user-image associations.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class UserImageServices {
	private static UserImageServices instance;
	
	private ICampaignImageQueries campaignImageQueries;
	private ICampaignQueries campaignQueries;
	private IUserImageQueries userImageQueries;
	private IUserCampaignQueries userCampaignQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignImageQueries or
	 * iCampaignQueries or iUserImageQueries or iUserCampaignQueries is null
	 */
	private UserImageServices(ICampaignImageQueries iCampaignImageQueries,
			ICampaignQueries iCampaignQueries, IUserImageQueries iUserImageQueries,
			IUserCampaignQueries iUserCampaignQueries) {
		
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}

		if(iCampaignImageQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignImageQueries is required.");
		}
		if(iCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignQueries is required.");
		}
		if(iUserImageQueries == null) {
			throw new IllegalArgumentException("An instance of IUserImageQueries is required.");
		}
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is required.");
		}
		
		campaignImageQueries = iCampaignImageQueries;
		campaignQueries = iCampaignQueries;
		userImageQueries = iUserImageQueries;
		userCampaignQueries = iUserCampaignQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserImageServices instance() {
		return instance;
	}
	
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
	public void verifyPhotoPromptResponseExistsForUserAndImage(
			final String username, final String imageId) 
			throws ServiceException {
		
		try {
			if(! userImageQueries.responseExistsForUserWithImage(username, imageId)) {
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
	public void verifyUserCanReadImage(final String requesterUsername, 
			final String imageId) throws ServiceException {
		
		try {
			// If it is their own image, they can read it.
			if(requesterUsername.equals(userImageQueries.getImageOwner(imageId))) {
				return;
			}
			
			// Retrieve all of the campaigns associated with an image.
			List<String> campaignIds = campaignImageQueries.getCampaignIdsForImageId(imageId);
			
			// For each of the campaigns, see if the requesting user has 
			// sufficient permissions.
			for(String campaignId : campaignIds) {
				List<Campaign.Role> roles = userCampaignQueries.getUserCampaignRoles(requesterUsername, campaignId);

				// If they are a supervisor.
				if(roles.contains(Campaign.Role.SUPERVISOR)) {
					return;
				}
				
				// Retrieves the privacy state of the image in this campaign. 
				// If null is returned, something has changed since the list of
				// campaign IDs was retrieved, so we need to just error out.
				SurveyResponse.PrivacyState imagePrivacyState = campaignImageQueries.getImagePrivacyStateInCampaign(campaignId, imageId);
				
				// They are an author and the image is shared
				if(roles.contains(Campaign.Role.AUTHOR) && 
						SurveyResponse.PrivacyState.SHARED.equals(imagePrivacyState)) {
					return;
				}
				
				// Retrieve the campaign's privacy state.
				Campaign.PrivacyState campaignPrivacyState = campaignQueries.getCampaignPrivacyState(campaignId);
				
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