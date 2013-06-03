package org.ohmage.query.impl;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.PrivacyState;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignMediaQueries;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * Implements the campaign-video queries.
 *
 * @author John Jenkins
 */
public class CampaignMediaQueries 
		extends Query 
		implements ICampaignMediaQueries {
	
	/**
	 * Creates this object via dependency injection (reflection).
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private CampaignMediaQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignMediaQueries#getCampaignIdsForVideoId(java.util.UUID)
	 */
	@Override
	public List<String> getCampaignIdsForMediaId(
			final UUID mediaId)
			throws DataAccessException {
		
		String sql = 
			"SELECT DISTINCT(c.urn) " +
			"FROM " +
				"url_based_resource ubr, " +
				"prompt_response pr, " +
				"survey_response sr, " +
				"campaign c " +
			"WHERE ubr.uuid = ? " +
			"AND pr.response = ubr.uuid " +
			"AND pr.survey_response_id = sr.id " +
			"AND sr.campaign_id = c.id";
		
		try {
			return getJdbcTemplate().query(
				sql, 
				new Object[] { mediaId.toString() },
				new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + 
					sql + 
					"' with parameter: " + 
					mediaId.toString(),
				e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ICampaignMediaQueries#getVideoPrivacyStateInCampaign(java.lang.String, java.util.UUID)
	 */
	@Override
	public PrivacyState getMediaPrivacyStateInCampaign(
			final String campaignId,
			final UUID mediaId)
			throws DataAccessException {
		
		String sql =
			"SELECT DISTINCT(srps.privacy_state) " +
			"FROM " +
				"survey_response_privacy_state srps, " +
				"survey_response sr, " +
				"prompt_response pr, " +
				"url_based_resource ubr, " +
				"campaign c " +
			"WHERE c.urn = ? " +
			"AND ubr.uuid = ? " +
			"AND pr.response = ubr.uuid " +
			"AND pr.survey_response_id = sr.id " +
			"AND sr.campaign_id = c.id " +
			"AND sr.privacy_state_id = srps.id";
		
		try {
			return 
				SurveyResponse.PrivacyState.getValue(
					getJdbcTemplate().queryForObject(
						sql, 
						new Object[] { campaignId, mediaId.toString() },
						String.class));
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException(
				"There is an incosistency between the survey response privacy states that the application knows about and those that the database knows about.",
				e);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
					"A video, '" + 
						mediaId + 
						"' has multiple privacy states in the same campaign: " + 
						campaignId,
					e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + 
					sql + 
					"' with parameters: " + 
					campaignId + ", " +
					mediaId.toString(),
				e);
		}
	}

}
