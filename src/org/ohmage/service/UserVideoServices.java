package org.ohmage.service;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.ICampaignVideoQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserQueries;
import org.ohmage.query.IUserVideoQueries;
import org.ohmage.query.IVideoQueries;

public class UserVideoServices {
	private static UserVideoServices instance;
	
	private final IUserQueries userQueries;
	private final ICampaignQueries campaignQueries;
	private final IVideoQueries videoQueries;
	private final IUserVideoQueries userVideoQueries;
	private final IUserCampaignQueries userCampaignQueries;
	private final ICampaignVideoQueries campaignVideoQueries;
	
	/**
	 * Private constructor called by reflection via Tomcat.
	 * 
	 * @param iUserQueries The object that implements the user queries.
	 * 
	 * @param iUserVideoQueries The object that implements the user-video
	 * 							queries.
	 */
	private UserVideoServices(
			final IUserQueries iUserQueries,
			final ICampaignQueries iCampaignQueries,
			final IVideoQueries iVideoQueries,
			final IUserVideoQueries iUserVideoQueries,
			final IUserCampaignQueries iUserCampaignQueries,
			final ICampaignVideoQueries iCampaignVideoQueries) {
		
		if(instance != null) {
			throw new IllegalStateException(
				"An instance of this class already exists.");
		}
		
		if(iUserQueries == null) {
			throw new IllegalArgumentException(
				"The IUserQueries is null.");
		}
		else if(iCampaignQueries == null) {
			throw new IllegalArgumentException(
				"The ICampaignQueries is null.");
		}
		else if(iVideoQueries == null) {
			throw new IllegalArgumentException(
				"The IVideoQueries is null.");
			
		}
		else if(iUserVideoQueries == null) {
			throw new IllegalArgumentException(
				"The IUserVideoQueries is null.");
		}
		else if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException(
				"The IUserCampaignQueries is null.");
			
		}
		else if(iCampaignVideoQueries == null) {
			throw new IllegalArgumentException(
				"The ICampaignVideoQueries is null.");
			
		}
		
		userQueries = iUserQueries;
		campaignQueries = iCampaignQueries;
		videoQueries = iVideoQueries;
		userVideoQueries = iUserVideoQueries;
		userCampaignQueries = iUserCampaignQueries;
		campaignVideoQueries = iCampaignVideoQueries;
		
		instance = this;
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return The singleton instance of this class.
	 */
	public static UserVideoServices instance() {
		return instance;
	}
	
	/**
	 * Retrieves the URL of a video based on the user's 
	 * 
	 * @param username The user's username.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The URL of the video.
	 * 
	 * @throws ServiceException The user is not allowed to read the video or
	 * 							there was an error.
	 */
	public void verifyUserCanReadVideo(
			final String username, 
			final UUID videoId)
			throws ServiceException {
		
		try {
			// If it is their own image, they can read it.
			if(username.equals(userVideoQueries.getVideoOwner(videoId))) {
				return;
			}
			
			// If they are an admin, they can read it.
			if(userQueries.userIsAdmin(username)) {
				return;
			}
			
			// Retrieve all of the campaigns associated with an image.
			List<String> campaignIds = 
				campaignVideoQueries.getCampaignIdsForVideoId(videoId);
			
			// For each of the campaigns, see if the requesting user has 
			// sufficient permissions.
			for(String campaignId : campaignIds) {
				List<Campaign.Role> roles = 
					userCampaignQueries.getUserCampaignRoles(
						username, 
						campaignId);

				// If they are a supervisor.
				if(roles.contains(Campaign.Role.SUPERVISOR)) {
					return;
				}
				
				// Retrieves the privacy state of the image in this campaign. 
				// If null is returned, something has changed since the list of
				// campaign IDs was retrieved, so we need to just error out.
				SurveyResponse.PrivacyState imagePrivacyState = 
					campaignVideoQueries.getVideoPrivacyStateInCampaign(
						campaignId, videoId);
				
				// They are an author and the image is shared
				if(roles.contains(Campaign.Role.AUTHOR) && 
						SurveyResponse.PrivacyState.SHARED.equals(imagePrivacyState)) {
					return;
				}
				
				// Retrieve the campaign's privacy state.
				Campaign.PrivacyState campaignPrivacyState = 
					campaignQueries.getCampaignPrivacyState(campaignId);
				
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
	
	/**
	 * Returns the URL for the video.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The URL for the video.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public URL getVideoUrl(final UUID videoId) throws ServiceException {
		try {
			URL result = videoQueries.getVideoUrl(videoId);
			
			if(result == null) {
				throw new ServiceException("The video does not exist.");
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}