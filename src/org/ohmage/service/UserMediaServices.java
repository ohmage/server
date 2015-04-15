package org.ohmage.service;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Audio;
import org.ohmage.domain.DocumentP;
import org.ohmage.domain.Video;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignMediaQueries;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.IMediaQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserMediaQueries;
import org.ohmage.query.IUserQueries;

public class UserMediaServices {
	private static UserMediaServices instance;
	
	private final IUserQueries userQueries;
	private final ICampaignQueries campaignQueries;
	private final IUserCampaignQueries userCampaignQueries;
	private final IMediaQueries mediaQueries;
	private final IUserMediaQueries userMediaQueries;
	private final ICampaignMediaQueries campaignVideoQueries;
	
	/**
	 * Private constructor called by reflection via Spring.
	 */
	private UserMediaServices(
			final IUserQueries iUserQueries,
			final ICampaignQueries iCampaignQueries,
			final IMediaQueries iMediaQueries,
			final IUserMediaQueries iUserMediaQueries,
			final IUserCampaignQueries iUserCampaignQueries,
			final ICampaignMediaQueries iCampaignVideoQueries) {
		
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
		else if(iMediaQueries == null) {
			throw new IllegalArgumentException(
				"The IMediaQueries is null.");
			
		}
		else if(iUserMediaQueries == null) {
			throw new IllegalArgumentException(
				"The IUserMediaQueries is null.");
		}
		else if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException(
				"The IUserCampaignQueries is null.");
			
		}
		else if(iCampaignVideoQueries == null) {
			throw new IllegalArgumentException(
				"The ICampaignMediaQueries is null.");
			
		}
		
		userQueries = iUserQueries;
		campaignQueries = iCampaignQueries;
		userCampaignQueries = iUserCampaignQueries;
		mediaQueries = iMediaQueries;
		userMediaQueries = iUserMediaQueries;
		campaignVideoQueries = iCampaignVideoQueries;
		
		instance = this;
	}
	
	/**
	 * Returns the singleton instance of this class.
	 * 
	 * @return The singleton instance of this class.
	 */
	public static UserMediaServices instance() {
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
	public void verifyUserCanReadMedia(
			final String username, 
			final UUID id)
			throws ServiceException {
		
		try {
			// If it is their own image, they can read it.
			if(username.equals(userMediaQueries.getMediaOwner(id))) {
				return;
			}
			
			// If they are an admin, they can read it.
			if(userQueries.userIsAdmin(username)) {
				return;
			}
			
			// Retrieve all of the campaigns associated with an image.
			List<String> campaignIds = 
				campaignVideoQueries.getCampaignIdsForMediaId(id);
			
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
					campaignVideoQueries.getMediaPrivacyStateInCampaign(
						campaignId, id);
				
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
	 * Returns an Audio object representing the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return An Audio object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Audio getAudio(final UUID id) throws ServiceException {
		try {
			URL result = mediaQueries.getMediaUrl(id);
			
			if(result == null) {
				throw new ServiceException("The media does not exist.");
			}
			
			return new Audio(id, result);
		}
		catch(DomainException e) {
			throw new ServiceException(e);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns a Video object representing the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return A Video object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public Video getVideo(final UUID id) throws ServiceException {
		try {
			URL result = mediaQueries.getMediaUrl(id);
			
			if(result == null) {
				throw new ServiceException("The media does not exist.");
			}
			
			return new Video(id, result);
		}
		catch(DomainException e) {
			throw new ServiceException(e);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns a DocumentP object representing the media.
	 * 
	 * @param id The media's unique identifier.
	 * 
	 * @return A DocumentP object.
	 * 
	 * @throws ServiceException There was an error.
	 */
	public DocumentP getDocumentP(final UUID id) throws ServiceException {
		try {
			URL result = mediaQueries.getMediaUrl(id);
			
			if(result == null) {
				throw new ServiceException("The media does not exist.");
			}
			
			return new DocumentP(id, result);
		}
		catch(DomainException e) {
			throw new ServiceException(e);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}