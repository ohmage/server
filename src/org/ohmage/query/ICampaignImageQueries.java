package org.ohmage.query;

import java.util.List;

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
	List<String> getCampaignIdsForImageId(String imageId)
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
			String campaignId, String imageId) throws DataAccessException;

}