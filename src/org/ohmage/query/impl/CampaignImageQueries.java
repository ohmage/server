package org.ohmage.query.impl;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignImageQueries;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class is responsible for all queries pertaining to image-campaign 
 * associations. While it may read information pertaining other objects, the
 * parameters, return values, and modified objects should pertain only to 
 * image-campaign associations.
 * 
 * @author John Jenkins
 */
public final class CampaignImageQueries extends Query implements ICampaignImageQueries {
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
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private CampaignImageQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignImageQueries#getCampaignIdsForImageId(java.lang.String)
	 */
	public List<String> getCampaignIdsForImageId(String imageId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_IDS_FOR_IMAGE, 
					new Object[] { imageId }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_IDS_FOR_IMAGE + "' with parameter: " + imageId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignImageQueries#getImagePrivacyStateInCampaign(java.lang.String, java.lang.String)
	 */
	public SurveyResponse.PrivacyState getImagePrivacyStateInCampaign(String campaignId, String imageId) throws DataAccessException {
		try {
			return SurveyResponse.PrivacyState.getValue(getJdbcTemplate().queryForObject(
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
