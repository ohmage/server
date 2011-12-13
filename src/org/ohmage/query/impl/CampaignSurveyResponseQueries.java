package org.ohmage.query.impl;

import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignSurveyResponseQueries;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting campaign-survey relationships. While it may read 
 * information pertaining to other entities, the information it takes and 
 * provides should pertain to campaign-survey relationships only.
 * 
 * @author John Jenkins
 */
public class CampaignSurveyResponseQueries extends Query implements ICampaignSurveyResponseQueries {
	// Retrieves the total number of survey responses for a campaign.
	private static final String SQL_COUNT_SURVEY_RESPONSES =
		"SELECT COUNT(Id) " +
		"FROM survey_response " +
		"WHERE campaign_id = (" +
			"SELECT Id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";
	
	// Retrieves the total number of prompt responses for a campaign.
	private static final String SQL_COUNT_PROMPT_RESPONSES = 
		"SELECT COUNT(Id) " +
		"FROM prompt_response " +
		"WHERE survey_response_id in (" +
			"SELECT Id " +
			"FROM survey_response " +
			"WHERE campaign_id = (" +
				"SELECT Id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			")" +
		")";
	
	// Retrieves the campaign ID for the campaign to which the survey response
	// belongs.
	private static final String SQL_GET_CAMPAIGN_ID_FROM_SURVEY_RESPONSE_ID =
		"SELECT c.urn " +
		"FROM campaign c, survey_response sr " +
		"WHERE sr.uuid = ? " +
		"AND sr.campaign_id = c.id";
	
	/**
	 * Private constructor that is used by Spring to setup this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private CampaignSurveyResponseQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignSurveyResponseQueries#getNumberOfSurveyResponsesForCampaign(java.lang.String)
	 */
	public long getNumberOfSurveyResponsesForCampaign(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForLong(SQL_COUNT_SURVEY_RESPONSES, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error running SQL '" + SQL_COUNT_SURVEY_RESPONSES + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignSurveyResponseQueries#getNumberOfPromptResposnesForCampaign(java.lang.String)
	 */
	public long getNumberOfPromptResposnesForCampaign(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForLong(SQL_COUNT_PROMPT_RESPONSES, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error running SQL '" + SQL_COUNT_PROMPT_RESPONSES + "' with parameter: " + campaignId, e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignSurveyResponseQueries#getCampaignIdFromSurveyId(java.lang.Long)
	 */
	public String getCampaignIdFromSurveyId(UUID surveyResponseId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_ID_FROM_SURVEY_RESPONSE_ID,
					new Object[] { surveyResponseId.toString() },
					String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("One survey response belongs to multiple campaigns.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error running SQL '" + SQL_GET_CAMPAIGN_ID_FROM_SURVEY_RESPONSE_ID + "' with parameter: " + surveyResponseId, e);
		}
	}
}