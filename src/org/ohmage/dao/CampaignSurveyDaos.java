package org.ohmage.dao;

import javax.sql.DataSource;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting campaign-survey relationships. While it may read 
 * information pertaining to other entities, the information it takes and 
 * provides should pertain to campaign-survey relationships only.
 * 
 * @author John Jenkins
 */
public class CampaignSurveyDaos extends Dao {
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
	
	private static CampaignSurveyDaos instance;
	
	/**
	 * Private constructor that is used by Spring to setup this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private CampaignSurveyDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves the total number of survey responses for a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The total number of survey responses for a campaign. If the
	 * 		   campaign doesn't exist, 0 is returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static long getNumberOfSurveyResponsesForCampaign(String campaignId) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForLong(SQL_COUNT_SURVEY_RESPONSES, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error running SQL '" + SQL_COUNT_SURVEY_RESPONSES + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Retrieves the total number of prompt responses for a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The total number of prompt responses for a campaign. If the 
	 * 		   campaign doesn't exist, 0 is returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static long getNumberOfPromptResposnesForCampaign(String campaignId) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForLong(SQL_COUNT_PROMPT_RESPONSES, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error running SQL '" + SQL_COUNT_PROMPT_RESPONSES + "' with parameter: " + campaignId, e);
		}
	}
}