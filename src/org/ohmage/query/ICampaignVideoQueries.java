package org.ohmage.query;

import java.util.List;
import java.util.UUID;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;

/**
 * Defines all of the queries between campaigns and videos.
 *
 * @author John Jenkins
 */
public interface ICampaignVideoQueries {
	/**
	 * Gets the ID of all of the campaigns associated with a video.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The list of campaign IDs associated with this video.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	List<String> getCampaignIdsForVideoId(
		final UUID videoId)
		throws DataAccessException;
	
	/**
	 * Gets the privacy state of the survey response that is associated with 
	 * this video.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param videoId The video's unique identifier.
	 * 
	 * @return The privacy state of the survey response associated with this
	 * 		   video.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	SurveyResponse.PrivacyState getVideoPrivacyStateInCampaign(
		final String campaignId,
		final UUID videoId)
		throws DataAccessException;
}
