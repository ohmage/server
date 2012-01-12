package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.PrivacyState;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.query.ISurveyResponseQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class is responsible for creating, reading, updating, and deleting
 * survey responses.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class SurveyResponseQueries extends Query implements ISurveyResponseQueries {
	// Retrieves all of the survey response privacy states.
	private static final String SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES =
		"SELECT privacy_state " +
		"FROM survey_response_privacy_state";
	
	// Retrieves the ID for all survey responses in a campaign.
	private static final String SQL_GET_IDS_FOR_CAMPAIGN =
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id";
	
	// Retrieves the survey response ID for all of the survey responses made by
	// a given user in a given campaign.
	private static final String SQL_GET_IDS_FOR_USER = 
		"SELECT sr.uuid " +
		"FROM user u, campaign c, survey_response sr " +
		"WHERE u.username = ? " +
		"AND u.id = sr.user_id " +
		"AND c.urn = ? " +
		"AND c.id = sr.campaign_id";
	
	// Retrieves the survey response ID for all of the survey responses made by
	// a given client in a given campaign.
	private static final String SQL_GET_IDS_WITH_CLIENT = 
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND sr.client = ?";
	
	// Retrieves the survey response ID for all of the survey responses made on 
	// or after some date in a given campaign.
	private static final String SQL_GET_IDS_AFTER_DATE =
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND sr.epoch_millis >= ?";
	
	// Retrieves the survey response ID for all of the survey responses made on 
	// or before some date in a given campaign.
	private static final String SQL_GET_IDS_BEFORE_DATE =
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND sr.epoch_millis <= ?";
	
	// Retrieves the survey response ID for all of the survey responses with a
	// given privacy state in a given campaign.
	private static final String SQL_GET_IDS_WITH_PRIVACY_STATE = 
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr, survey_response_privacy_state srps " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND srps.privacy_state = ? " +
		"AND srps.id = sr.privacy_state_id";
	
	// Retrieves the survey response ID for all of the survey responses with a
	// given survey ID in a given campaign.
	private static final String SQL_GET_IDS_WITH_SURVEY_ID = 
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND sr.survey_id = ?";
	
	// Retrieves the survey response ID for all of the survey responses with a
	// given prompt ID in a given campaign.
	private static final String SQL_GET_IDS_WITH_PROMPT_ID = 
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr, prompt_response pr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND pr.prompt_id = ? " +
		"AND sr.id = pr.survey_response_id";
	
	// Retrieves the survey response ID for all of the survey responses with a
	// given prompt type in a given campaign.
	private static final String SQL_GET_IDS_WITH_PROMPT_TYPE = 
		"SELECT sr.uuid " +
		"FROM campaign c, survey_response sr, prompt_response pr " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND pr.prompt_type = ? " +
		"AND sr.id = pr.survey_response_id";
	
	// Retrieves all of the information about a single survey response.
	private static final String SQL_GET_SURVEY_RESPONSE = 
		"SELECT u.username, c.urn, sr.uuid, sr.client, " +
				"sr.epoch_millis, sr.phone_timezone, " +
				"sr.survey_id, sr.launch_context, " +
				"sr.location_status, sr.location, srps.privacy_state " +
		"FROM user u, campaign c, survey_response sr, survey_response_privacy_state srps " +
		"WHERE sr.uuid = ? " +
		"AND u.id = sr.user_id " +
		"AND c.id = sr.campaign_id " +
		"AND srps.id = sr.privacy_state_id";
	
	// Retrieves all of the information about a single survey response.
	private static final String SQL_GET_SURVEY_RESPONSES = 
		"SELECT u.username, c.urn, sr.id, sr.uuid, sr.client, " +
			"sr.epoch_millis, sr.phone_timezone, " +
			"sr.survey_id, sr.launch_context, " +
			"sr.location_status, sr.location, srps.privacy_state " +
		"FROM user u, campaign c, survey_response sr, survey_response_privacy_state srps " +
		"WHERE u.id = sr.user_id " +
		"AND c.id = sr.campaign_id " +
		"AND srps.id = sr.privacy_state_id " +
		"AND sr.uuid in ";
	
	private static final String SQL_GET_SURVEY_RESPONSES_DYNAMICALLY=
		"SELECT u.username, c.urn, sr.id, sr.uuid, sr.client, " +
			"sr.epoch_millis, sr.phone_timezone, " +
			"sr.survey_id, sr.launch_context, " +
			"sr.location_status, sr.location, srps.privacy_state " +
		"FROM user u, campaign c, survey_response sr, survey_response_privacy_state srps " +
		"WHERE c.urn = ? " +
		"AND c.id = sr.campaign_id " +
		"AND u.id = sr.user_id " +
		"AND srps.id = sr.privacy_state_id";

	private static final String SQL_WHERE_USERNAMES =
		" AND u.username IN ";

	private static final String SQL_WHERE_ON_OR_AFTER =
		" AND sr.epoch_millis >= ?";

	private static final String SQL_WHERE_ON_OR_BEFORE =
		" AND sr.epoch_millis <= ?";

	private static final String SQL_WHERE_PRIVACY_STATE =
		" AND srps.privacy_state = ?";

	private static final String SQL_WHERE_SURVEY_IDS =
		" AND sr.survey_id IN ";

	private static final String SQL_WHERE_PROMPT_TYPE =
		" AND pr.prompt_type = ?";

	// Retrieves all of the information about all prompt responses that pertain
	// to a single survey response.
	private static final String SQL_GET_PROMPT_RESPONSES = 
		"SELECT pr.prompt_id, pr.prompt_type, pr.repeatable_set_id, pr.repeatable_set_iteration, pr.response " +
		"FROM survey_response sr, prompt_response pr " +
		"WHERE pr.survey_response_id = sr.id " +
		"AND sr.uuid = ?";
	
	// Retrieves all of the information about all prompt responses that pertain
	// to a single survey response.
	private static final String SQL_GET_PROMPT_RESPONSES_WITH_ID = 
		"SELECT pr.prompt_id, pr.prompt_type, pr.repeatable_set_id, pr.repeatable_set_iteration, pr.response " +
		"FROM survey_response sr, prompt_response pr " +
		"WHERE pr.survey_response_id = sr.id " +
		"AND sr.uuid = ? " +
		"AND pr.prompt_id in ";
	
	// Updates a survey response's privacy state.
	private static final String SQL_UPDATE_SURVEY_RESPONSE_PRIVACY_STATE = 
		"UPDATE survey_response " +
		"SET privacy_state_id = (SELECT id FROM survey_response_privacy_state WHERE privacy_state = ?) " +
		"WHERE uuid = ?";
	
	// Deletes a survey response and subsequently all prompt response 
	// references.
	private static final String SQL_DELETE_SURVEY_RESPONSE =
		"DELETE FROM survey_response " +
		"WHERE uuid = ?";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private SurveyResponseQueries(DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.ISurveyResponseQueries#retrieveSurveyResponsePrivacyStates()
	 */
	@Override
	public List<PrivacyState> retrieveSurveyResponsePrivacyStates()
			throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES,
					new RowMapper<SurveyResponse.PrivacyState>() {
						/**
						 * Reads the survey response privacy states, converts
						 * them into a SurveyResponse.PrivacyState object, and
						 * returns it.
						 */
						@Override
						public PrivacyState mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							try {
								return SurveyResponse.PrivacyState.getValue(
										rs.getString("privacy_state"));
							}
							catch(IllegalArgumentException e) {
								throw new SQLException(
										"The privacy state was unknown.",
										e);
							}
						}					
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES +
						"'.",
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsFromCampaign(java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsFromCampaign(final String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_FOR_CAMPAIGN, 
					new Object[] { campaignId }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_FOR_CAMPAIGN + 
						"' with parameter: " + 
							campaignId,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsFromUser(java.lang.String, java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsFromUser(final String campaignId, final String username) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_FOR_USER, 
					new Object[] { username, campaignId }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_FOR_USER + 
						"' with parameters: " + 
							username + ", " + 
							campaignId,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsWithClient(java.lang.String, java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsWithClient(final String campaignId, final String client) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_WITH_CLIENT, 
					new Object[] { campaignId, client }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_FOR_USER + 
						"' with parameters: " + 
							campaignId + ", " + 
							client,
					e);
		}
	}

	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsAfterDate(java.lang.String, java.util.Date)
	 */
	public List<UUID> retrieveSurveyResponseIdsAfterDate(final String campaignId, final Date startDate) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_AFTER_DATE, 
					new Object[] { campaignId, startDate.getTime() }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_AFTER_DATE + 
						"' with parameters: " + 
							campaignId + ", " + 
							startDate.getTime(),
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsBeforeDate(java.lang.String, java.util.Date)
	 */
	public List<UUID> retrieveSurveyResponseIdsBeforeDate(final String campaignId, final Date endDate) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_BEFORE_DATE, 
					new Object[] { campaignId, endDate.getTime() }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_BEFORE_DATE + 
						"' with parameters: " + 
							campaignId + ", " + 
							endDate.getTime(),
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsWithPrivacyState(java.lang.String, org.ohmage.domain.campaign.SurveyResponse.PrivacyState)
	 */
	public List<UUID> retrieveSurveyResponseIdsWithPrivacyState(final String campaignId, 
			final SurveyResponse.PrivacyState privacyState) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_WITH_PRIVACY_STATE, 
					new Object[] { campaignId, privacyState.toString() }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_WITH_PRIVACY_STATE + 
						"' with parameters: " + 
							campaignId + ", " + 
							privacyState,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsWithSurveyId(java.lang.String, java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsWithSurveyId(final String campaignId, final String surveyId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_WITH_SURVEY_ID, 
					new Object[] { campaignId, surveyId }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +  
							SQL_GET_IDS_WITH_SURVEY_ID + 
						"' with parameters: " + 
							campaignId + ", " + 
							surveyId,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsWithPromptId(java.lang.String, java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsWithPromptId(final String campaignId, final String promptId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_WITH_PROMPT_ID, 
					new Object[] { campaignId, promptId }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_WITH_PROMPT_ID + 
						"' with parameters: " + 
							campaignId + ", " + 
							promptId,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseIdsWithPromptType(java.lang.String, java.lang.String)
	 */
	public List<UUID> retrieveSurveyResponseIdsWithPromptType(final String campaignId, final String promptType) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_IDS_WITH_PROMPT_TYPE, 
					new Object[] { campaignId, promptType }, 
					new RowMapper<UUID>() {
						@Override
						public UUID mapRow(
								ResultSet rs, 
								int rowNum) 
								throws SQLException {
							
							return UUID.fromString(rs.getString("uuid"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_IDS_WITH_PROMPT_TYPE + 
						"' with parameters: " + 
							campaignId + ", " + 
							promptType,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseFromId(org.ohmage.domain.campaign.Campaign, java.lang.Long)
	 */
	public SurveyResponse retrieveSurveyResponseFromId(
			final Campaign campaign,
			final UUID surveyResponseId) 
			throws DataAccessException {
		
		try {
			// Create the survey response information object from the database.
			final SurveyResponse result = getJdbcTemplate().queryForObject(
					SQL_GET_SURVEY_RESPONSE,
					new Object[] { surveyResponseId.toString() },
					new RowMapper<SurveyResponse>() {
						@Override
						public SurveyResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
							try {
								JSONObject locationJson = null;
								String locationString = rs.getString("location");
								if(locationString != null) {
									locationJson = new JSONObject(locationString);
								}
								
								return new SurveyResponse(
										campaign.getSurveys().get(rs.getString("survey_id")),
										UUID.fromString(rs.getString("uuid")),
										rs.getString("username"),
										rs.getString("urn"),
										rs.getString("client"),
										rs.getLong("epoch_millis"),
										TimeZone.getTimeZone(rs.getString("phone_timezone")),
										new JSONObject(rs.getString("launch_context")),
										rs.getString("location_status"),
										locationJson,
										SurveyResponse.PrivacyState.getValue(rs.getString("privacy_state")));
							}
							catch(JSONException e) {
								throw new SQLException("Error creating a JSONObject.", e);
							}
							catch(ErrorCodeException e) {
								throw new SQLException("Error creating the survey response information object.", e);
							}
							catch(IllegalArgumentException e) {
								throw new SQLException("Error creating the survey response information object.", e);
							}
						}
					}
				);
			
			final String surveyId = result.getSurvey().getId();
			
			final Map<String, Class<?>> typeMapping = new HashMap<String, Class<?>>();
			typeMapping.put("tinyint", Integer.class);
			
			// Retrieve all of the prompt responses for the survey response and
			// add them to the survey response information object.
			getJdbcTemplate().query(
					SQL_GET_PROMPT_RESPONSES,
					new Object[] { surveyResponseId.toString() },
					new RowMapper<Object>() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							try {
								Prompt prompt = campaign.getPrompt(surveyId, rs.getString("prompt_id"));
								result.addPromptResponse(prompt.createResponse(rs.getString("response"), (Integer) rs.getObject("repeatable_set_iteration", typeMapping)));
							}
							catch(IllegalArgumentException e) {
								throw new SQLException("The prompt response value from the database is not a valid response value for this prompt.", e);
							}
							
							// TODO: instead of returning null here we should
							// just wrap the query() call in response.addPromptResponse()
							
							return null;
						}
					}
				);
			
			return result;
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException(
						"Multiple survey response's have the same database ID.", 
						e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + SQL_GET_SURVEY_RESPONSE + 
						"' with parameter: " + surveyResponseId,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#retrieveSurveyResponseFromIds(org.ohmage.domain.campaign.Campaign, java.util.Collection)
	 */
	public List<SurveyResponse> retrieveSurveyResponseFromIds(
			final Campaign campaign,
			final Collection<UUID> surveyResponseIds) throws DataAccessException {
		
		try {
			final Map<String, Class<?>> typeMapping = new HashMap<String, Class<?>>();
			typeMapping.put("tinyint", Integer.class);

			String[] surveyResponseIdsString = new String[surveyResponseIds.size()];
			int numIdsConverted = 0;
			for(UUID surveyResponseId : surveyResponseIds) {
				surveyResponseIdsString[numIdsConverted] = 
						surveyResponseId.toString();
				numIdsConverted++;
			}
			
			final List<SurveyResponse> result = 
				getJdbcTemplate().query(
					SQL_GET_SURVEY_RESPONSES + StringUtils.generateStatementPList(surveyResponseIds.size()),
					surveyResponseIdsString,
					new RowMapper<SurveyResponse>() {
						@Override
						public SurveyResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
							try {
								JSONObject locationJson = null;
								String locationString = rs.getString("location");
								if(locationString != null) {
									locationJson = new JSONObject(locationString);
								}
								
								// Create an object for this survey response.
								final SurveyResponse currResult = 
									new SurveyResponse(
										campaign.getSurveys().get(rs.getString("survey_id")),
										UUID.fromString(rs.getString("uuid")),
										rs.getString("username"),
										rs.getString("urn"),
										rs.getString("client"),
										rs.getLong("epoch_millis"),
										TimeZone.getTimeZone(rs.getString("phone_timezone")),
										new JSONObject(rs.getString("launch_context")),
										rs.getString("location_status"),
										locationJson,
										SurveyResponse.PrivacyState.getValue(rs.getString("privacy_state")));
								
								final String surveyId = currResult.getSurvey().getId();
								
								// Retrieve all of the prompt responses for the
								// current survey response.
								try {
									getJdbcTemplate().query(
											SQL_GET_PROMPT_RESPONSES,
											new Object[] { rs.getString("uuid") },
											new RowMapper<Object>() {
												@Override
												public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
													try {
														// Retrieve the prompt
														// from the 
														// configuration.
														Prompt prompt = 
															campaign.getPrompt(
																	surveyId,
																	rs.getString("prompt_id")
																);
														
														currResult.addPromptResponse(
																prompt.createResponse(
																		rs.getString("response"), 
																		(Integer) rs.getObject("repeatable_set_iteration", typeMapping)
																	)
															);
													}
													catch(IllegalArgumentException e) {
														throw new SQLException("The prompt response value from the database is not a valid response value for this prompt.", e);
													}
													
													return null;
												}
											}
										);
								}
								catch(org.springframework.dao.DataAccessException e) {
									throw new SQLException(
											"Error executing SQL '" + SQL_GET_PROMPT_RESPONSES + 
												"' with parameter: " + rs.getLong("id"),
											e);
								}
								
								return currResult;
							}
							catch(JSONException e) {
								throw new SQLException("Error creating a JSONObject.", e);
							}
							catch(ErrorCodeException e) {
								throw new SQLException("Error creating the survey response information object.", e);
							}
							catch(IllegalArgumentException e) {
								throw new SQLException("Error creating the survey response information object.", e);
							}
						}
					}
				);
			
			return result;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + SQL_GET_SURVEY_RESPONSES + 
						StringUtils.generateStatementPList(surveyResponseIds.size()) +
						"' with parameter: " + surveyResponseIds,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.ISurveyResponseQueries#retrieveSurveyResponseDynamically(org.ohmage.domain.campaign.Campaign, java.util.Collection, java.util.Date, java.util.Date, org.ohmage.domain.campaign.SurveyResponse.PrivacyState, java.util.Collection, java.lang.String)
	 */
	@Override
	public List<SurveyResponse> retrieveSurveyResponseDynamically(
			final Campaign campaign,
			final Collection<String> usernames, 
			final Date startDate,
			final Date endDate, 
			final PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType) 
			throws DataAccessException {
		
		// Begin with the default SQL string.
		StringBuilder sqlBuilder = 
				new StringBuilder(SQL_GET_SURVEY_RESPONSES_DYNAMICALLY);
		
		// Begin with only the campaign's ID.
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(campaign.getId());
		
		// Check all of the criteria and if any are non-null add their SQL and
		// append the parameters.
		if((usernames != null) && (usernames.size() > 0)) {
			sqlBuilder.append(SQL_WHERE_USERNAMES);
			sqlBuilder.append(StringUtils.generateStatementPList(usernames.size()));
			parameters.addAll(usernames);
		}
		if(startDate != null) {
			sqlBuilder.append(SQL_WHERE_ON_OR_AFTER);
			parameters.add(startDate.getTime());
		}
		if(endDate != null) {
			sqlBuilder.append(SQL_WHERE_ON_OR_BEFORE);
			parameters.add(endDate.getTime());
		}
		if(privacyState != null) {
			sqlBuilder.append(SQL_WHERE_PRIVACY_STATE);
			parameters.add(privacyState.toString());
		}
		if((surveyIds != null) && (surveyIds.size() > 0)) {
			sqlBuilder.append(SQL_WHERE_SURVEY_IDS);
			sqlBuilder.append(StringUtils.generateStatementPList(surveyIds.size()));
			parameters.addAll(surveyIds);
		}
		if(promptType != null) {
			sqlBuilder.append(SQL_WHERE_PROMPT_TYPE);
			parameters.add(promptType);
		}
		
		final List<SurveyResponse> result;
		try {
			result =
					getJdbcTemplate().query(
						sqlBuilder.toString(),
						parameters.toArray(),
						new RowMapper<SurveyResponse>() {
							@Override
							public SurveyResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
								try {
									JSONObject locationJson = null;
									String locationString = rs.getString("location");
									if(locationString != null) {
										locationJson = new JSONObject(locationString);
									}
									
									// Create an object for this survey response.
									final SurveyResponse currResult = 
										new SurveyResponse(
											campaign.getSurveys().get(rs.getString("survey_id")),
											UUID.fromString(rs.getString("uuid")),
											rs.getString("username"),
											rs.getString("urn"),
											rs.getString("client"),
											rs.getLong("epoch_millis"),
											TimeZone.getTimeZone(rs.getString("phone_timezone")),
											new JSONObject(rs.getString("launch_context")),
											rs.getString("location_status"),
											locationJson,
											SurveyResponse.PrivacyState.getValue(rs.getString("privacy_state")));
									
									return currResult;
								}
								catch(JSONException e) {
									throw new SQLException("Error creating a JSONObject.", e);
								}
								catch(ErrorCodeException e) {
									throw new SQLException("Error creating the survey response information object.", e);
								}
								catch(IllegalArgumentException e) {
									throw new SQLException("Error creating the survey response information object.", e);
								}
							}
						}
					);

		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + SQL_GET_SURVEY_RESPONSES + 
						sqlBuilder.toString() +
						"' with parameters: " + 
						campaign.getId() + " (campaign ID), " +
						usernames + " (usernames), " +
						startDate + " (start date), " +
						endDate + " (end date), " +
						privacyState + " (privacy state), " + 
						surveyIds + " (survey IDs), " +
						promptType + " (prompt type)",
					e);
		}
		
		final Map<String, Class<?>> typeMapping = new HashMap<String, Class<?>>();
		typeMapping.put("tinyint", Integer.class);
		
		for(final SurveyResponse surveyResponse : result) {
			final String surveyId = surveyResponse.getSurvey().getId();
			
			// Build the prompt SQL and its 
			// corresponding array of parameters.
			String promptSql;
			List<Object> promptParameters = new LinkedList<Object>();
			if((promptIds == null) || (promptIds.size() == 0)) {
				promptSql = SQL_GET_PROMPT_RESPONSES;
				promptParameters.add(
						surveyResponse.getSurveyResponseId().toString());
			}
			else {
				promptSql = 
						SQL_GET_PROMPT_RESPONSES_WITH_ID +
						StringUtils.generateStatementPList(promptIds.size());
				promptParameters.add(surveyResponse.getSurveyResponseId().toString());
				promptParameters.addAll(promptIds);
			}
			
			// Retrieve all of the prompt responses for the
			// current survey response.
			try {
				getJdbcTemplate().query(
						promptSql,
						promptParameters.toArray(),
						new RowMapper<Object>() {
							@Override
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								try {
									// Retrieve the prompt
									// from the 
									// configuration.
									Prompt prompt = 
										campaign.getPrompt(
												surveyId,
												rs.getString("prompt_id")
											);
									
									surveyResponse.addPromptResponse(
											prompt.createResponse(
													rs.getString("response"),
													(Integer) rs.getObject(
															"repeatable_set_iteration", 
															typeMapping)
												)
										);
								}
								catch(IllegalArgumentException e) {
									throw new SQLException("The prompt response value from the database is not a valid response value for this prompt.", e);
								}
								
								return null;
							}
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				throw new DataAccessException(
						"Error executing SQL '" + 
							promptSql + 
							"' with parameter(s): " + 
							promptParameters,
						e);
			}
		}
				
		return result;
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#updateSurveyResponsePrivacyState(java.lang.Long, org.ohmage.domain.campaign.SurveyResponse.PrivacyState)
	 */
	public void updateSurveyResponsePrivacyState(
			final UUID surveyResponseId, 
			final SurveyResponse.PrivacyState newPrivacyState)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a survey response.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
					new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(
						SQL_UPDATE_SURVEY_RESPONSE_PRIVACY_STATE, 
						new Object[] { 
								newPrivacyState.toString(), 
								surveyResponseId.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing SQL '" + 
								SQL_UPDATE_SURVEY_RESPONSE_PRIVACY_STATE + 
								"' with parameters: " + 
								newPrivacyState + ", " + 
								surveyResponseId, 
						e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#deleteSurveyResponse(java.lang.Long)
	 */
	public void deleteSurveyResponse(
			final UUID surveyResponseId) 
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a survey response.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
					new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(
						SQL_DELETE_SURVEY_RESPONSE, 
						new Object[] { surveyResponseId.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing SQL '" + 
								SQL_DELETE_SURVEY_RESPONSE + 
								"' with parameter: " + 
								surveyResponseId.toString(), 
						e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
}