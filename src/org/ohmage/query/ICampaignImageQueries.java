package org.ohmage.query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;

public interface ICampaignImageQueries {

	/**
	 * Retrieves all of the campaigns to which an image is associated.
	 * 
	 * @param imageId The image's unique identifier.
	 * 
	 * @return Returns a list of campaign IDs for to the image is associated. 
	 * 		   The list may be empty if the image doesn't exist or isn't
	 * 		   associated with any campaigns, but it will never be null.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getCampaignIdsForImageId(UUID imageId)
			throws DataAccessException;

	/**
	 * Retrieves the privacy state of an image for a specific campaign. If the
	 * image and/or campaign don't exist or the image isn't associated with the
	 * campaign, null is returned.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param imageId The unique identifier for the image.
	 * 
	 * @return Returns the privacy state of the image in the campaign. If the
	 * 		   image and/or campaign don't exist or the image isn't associated
	 * 		   with the campaign, null is returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	SurveyResponse.PrivacyState getImagePrivacyStateInCampaign(
			String campaignId, UUID imageId) throws DataAccessException;
	
	/**
	 * Returns the unique identifier for every image that was uploaded as a 
	 * response to any survey in a campaign.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A collection of unique identifiers pertaining to all of the 
	 * 		   images associated with all of the survey responses from a
	 * 		   campaign.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Collection<UUID> getImageIdsFromCampaign(String campaignId)
			throws DataAccessException;

	/**
	 * Returns the URL for every image that belongs to a survey response from a
	 * given campaign. There may be image responses that don't have references
	 * in the database, such as prompts that were skipped or not displayed or
	 * images that have since been purged but their survey responses remain,
	 * and these are ignored.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return The collection of URLs.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Collection<String> getImageUrlsFromCampaign(String campaignId)
			throws DataAccessException;
}