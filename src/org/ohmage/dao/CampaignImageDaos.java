package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class is responsible for all DAOs pertaining to image-campaign 
 * associations. While it may read information pertaining other objects, the
 * parameters, return values, and modified objects should pertain only to 
 * image-campaign associations.
 * 
 * @author John Jenkins
 */
public final class CampaignImageDaos extends Dao {
	// Retrieves the unique identifiers for all of the campaigns for which an
	// image is associated.
	private static final String SQL_GET_CAMPAIGN_IDS_FOR_IMAGE =
		"SELECT DISTINCT(c.urn) " +
		"FROM url_based_resource ubr, prompt_response pr, survey_response sr, campaign c " +
		"WHERE ubr.uuid = ? " +
		"AND pr.response = ubr.uuid " +
		"AND pr.survey_response_id = sr.id " +
		"AND sr.campaign_id = c.id";
	
	// Retrieves the privacy state of an image in a campaign.
	private static final String SQL_GET_IMAGE_PRIVACY_STATE_IN_CAMPAIGN =
		"SELECT DISTINCT(srps.privacy_state) " +
		"FROM survey_response_privacy_state srps, survey_response sr, prompt_response pr, url_based_resource ubr, campaign c " +
		"WHERE c.urn = ? " +
		"AND ubr.uuid = ? " +
		"AND pr.response = ubr.uuid " +
		"AND pr.survey_response_id = sr.id " +
		"AND sr.campaign_id = c.id " +
		"AND sr.privacy_state_id = srps.id";
	
	private static CampaignImageDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private CampaignImageDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
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
	public static List<String> getCampaignIdsForImageId(String imageId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_IDS_FOR_IMAGE, 
					new Object[] { imageId }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_IDS_FOR_IMAGE + "' with parameter: " + imageId, e);
		}
	}
	
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
	public static SurveyResponsePrivacyStateCache.PrivacyState getImagePrivacyStateInCampaign(String campaignId, String imageId) throws DataAccessException {
		try {
			return SurveyResponsePrivacyStateCache.PrivacyState.getValue(instance.getJdbcTemplate().queryForObject(
					SQL_GET_IMAGE_PRIVACY_STATE_IN_CAMPAIGN, 
					new Object[] { campaignId, imageId }, 
					String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("An image, '" + imageId + "' has multiple privacy states in the same campaign: " + campaignId, e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_IMAGE_PRIVACY_STATE_IN_CAMPAIGN + 
					"' with parameters: " + campaignId + ", " + imageId, e);
		}
	}
}
