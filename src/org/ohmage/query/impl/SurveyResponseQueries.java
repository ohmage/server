/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import javax.sql.DataSource;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.PrivacyState;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.ISurveyResponseQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
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
	
	private static final String SQL_GET_CAMPAIGN_URN_FOR_SURVEY_ID =
	    "SELECT urn " +
	    "FROM campaign, survey_response " +
	    "WHERE campaign_id = campaign.id and survey_response.uuid = ?";
	
	// Retrieves all of the survey response privacy states.
	private static final String SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES =
		"SELECT privacy_state " +
		"FROM survey_response_privacy_state";
	
	/**
	 * Retrieves all of the necessary information for survey responses. It 
	 * should almost certainly be used with the {@link #SQL_WHERE_ACL} in order
	 * to ensure that a user cannot see more than they are privileged to see.
	 * However, this may not be used in the case of administrators when they
	 * need to see all data regardless of role.<br />
	 * <br />
	 * Note: This should never use a GROUP BY clause as you will lose the count
	 * for the columns that were aggregated together. If you need to group the
	 * results, use {@link #SQL_GET_SURVEY_RESPONSES_AGGREGATED}, instead.
	 * 
	 * @see #SQL_WHERE_ACL
	 * @see #SQL_WHERE_USERNAMES
	 * @see #SQL_WHERE_ON_OR_AFTER
	 * @see #SQL_WHERE_ON_OR_BEFORE
	 * @see #SQL_WHERE_PRIVACY_STATE
	 * @see #SQL_WHERE_SURVEY_IDS
	 * @see #SQL_WHERE_PROMPT_IDS
	 * @see #SQL_WHERE_PROMPT_TYPE
	 * 
	 * @see #SQL_ORDER_BY
	 */
	private static final String SQL_GET_SURVEY_RESPONSES_INDIVIDUAL =
		// Retrieve all of the columns necessary to build a SurveyResponse
		// object.
		"SELECT u.username, c.urn, " +
			"sr.id, sr.uuid, sr.client, " +
			"sr.epoch_millis, sr.phone_timezone, " +
			"sr.survey_id, sr.launch_context, " +
			"sr.location_status, sr.location, srps.privacy_state, " +
			"pr.prompt_id, pr.response, pr.repeatable_set_iteration " +
		// Include as few tables as possible and use sub-queries when possible.
		"FROM user u, campaign c, " +
			"survey_response sr, survey_response_privacy_state srps, " +
			"prompt_response pr " +
		// Begin with exactly one campaign.
		"WHERE c.urn = ? " +
		// Retrieve only the survey response associated with that campaign.
		"AND c.id = sr.campaign_id " +
		// Link the user to the survey response.
		"AND u.id = sr.user_id " +
		// Link the privacy state to the survey response.
		"AND srps.id = sr.privacy_state_id " +
		// Link the prompt responses to the survey response.
		// Note: This means that multiple rows may have the same survey 
		// response information but have unique prompt response information.
		"AND pr.survey_response_id = sr.id";
	
	/**
	 * Retrieves all of the necessary information for survey responses. It also
	 * returns a count meaning that this should be used in conjunction with a
	 * WHERE clause. If it is not used with a WHERE clause, then exactly one
	 * row will be returned which will contain the count for all of the survey
	 * responses that matched the criteria as well as all of the information
	 * about one, random survey response. The count is based on the number of
	 * survey responses returned and should be used when aggregating survey
	 * responses at the granularity of survey responses. It should almost 
	 * certainly be used with the {@link #SQL_WHERE_ACL} in order to ensure 
	 * that a user cannot see more than they are privileged to see. However, 
	 * this may not be used in the case of administrators when they need to see
	 * all data regardless of role.
	 * 
	 * @see #SQL_WHERE_ACL
	 * @see #SQL_WHERE_USERNAMES
	 * @see #SQL_WHERE_ON_OR_AFTER
	 * @see #SQL_WHERE_ON_OR_BEFORE
	 * @see #SQL_WHERE_PRIVACY_STATE
	 * @see #SQL_WHERE_SURVEY_IDS
	 * @see #SQL_WHERE_PROMPT_IDS
	 * @see #SQL_WHERE_PROMPT_TYPE
	 * 
	 * @see #SQL_ORDER_BY
	 */
	private static final String SQL_GET_SURVEY_RESPONSES_AGGREGATED_SURVEY =
		// Retrieve all of the columns necessary to build a SurveyResponse
		// object.
		"SELECT COUNT(DISTINCT(sr.uuid)) as count, " +
			"u.username, c.urn, " +
			"sr.id, sr.uuid, sr.client, " +
			"sr.epoch_millis, sr.phone_timezone, " +
			"sr.survey_id, sr.launch_context, " +
			"sr.location_status, sr.location, srps.privacy_state, " +
			"pr.prompt_id, pr.response, pr.repeatable_set_iteration " +
		// Include as few tables as possible and use sub-queries when possible.
		"FROM user u, campaign c, " +
			"survey_response sr, survey_response_privacy_state srps, " +
			"prompt_response pr " +
		// Begin with exactly one campaign.
		"WHERE c.urn = ? " +
		// Retrieve only the survey response associated with that campaign.
		"AND c.id = sr.campaign_id " +
		// Link the user to the survey response.
		"AND u.id = sr.user_id " +
		// Link the privacy state to the survey response.
		"AND srps.id = sr.privacy_state_id " +
		// Link the prompt responses to the survey response.
		// Note: This means that multiple rows may have the same survey 
		// response information but have unique prompt response information.
		"AND pr.survey_response_id = sr.id";
	
	/**
	 * Retrieves all of the necessary information for survey responses. It also
	 * returns a count meaning that this should be used in conjunction with a
	 * WHERE clause. If it is not used with a WHERE clause, then exactly one
	 * row will be returned which will contain the count for all of the survey
	 * responses that matched the criteria as well as all of the information
	 * about one, random survey response. The count is based on the number of
	 * prompt responses returned and should be used when aggregating survey 
	 * responses at the granularity of prompt responses. It should almost 
	 * certainly be used with the {@link #SQL_WHERE_ACL} in order to ensure 
	 * that a user cannot see more than they are privileged to see. However, 
	 * this may not be used in the case of administrators when they need to see
	 * all data regardless of role.
	 * 
	 * @see #SQL_WHERE_ACL
	 * @see #SQL_WHERE_USERNAMES
	 * @see #SQL_WHERE_ON_OR_AFTER
	 * @see #SQL_WHERE_ON_OR_BEFORE
	 * @see #SQL_WHERE_PRIVACY_STATE
	 * @see #SQL_WHERE_SURVEY_IDS
	 * @see #SQL_WHERE_PROMPT_IDS
	 * @see #SQL_WHERE_PROMPT_TYPE
	 * 
	 * @see #SQL_ORDER_BY
	 */
	private static final String SQL_GET_SURVEY_RESPONSES_AGGREGATED_PROMPT =
		// Retrieve all of the columns necessary to build a SurveyResponse
		// object.
		"SELECT COUNT(sr.uuid) as count, " +
			"u.username, c.urn, " +
			"sr.id, sr.uuid, sr.client, " +
			"sr.epoch_millis, sr.phone_timezone, " +
			"sr.survey_id, sr.launch_context, " +
			"sr.location_status, sr.location, srps.privacy_state, " +
			"pr.prompt_id, pr.response, pr.repeatable_set_iteration " +
		// Include as few tables as possible and use sub-queries when possible.
		"FROM user u, campaign c, " +
			"survey_response sr, survey_response_privacy_state srps, " +
			"prompt_response pr " +
		// Begin with exactly one campaign.
		"WHERE c.urn = ? " +
		// Retrieve only the survey response associated with that campaign.
		"AND c.id = sr.campaign_id " +
		// Link the user to the survey response.
		"AND u.id = sr.user_id " +
		// Link the privacy state to the survey response.
		"AND srps.id = sr.privacy_state_id " +
		// Link the prompt responses to the survey response.
		// Note: This means that multiple rows may have the same survey 
		// response information but have unique prompt response information.
		"AND pr.survey_response_id = sr.id";
	
	/**
	 * This is the WHERE clause that must be included when using either
	 * {@link #SQL_GET_SURVEY_RESPONSES_INDIVIDUAL} or
	 * {@link #SQL_GET_SURVEY_RESPONSES_AGGREGATED}. This includes the 
	 * directives for SQL to properly align the tables and then incorporate the
	 * ACL. Without this section, one will get all of the information about all
	 * of the survey responses in all of the campaigns. No one should ever 
	 * query this much information at once. Even admins should break it down by
	 * campaign at the most course grained level.
	 * 
	 * Note: Maybe we should move the linking aspect of this WHERE clause to 
	 * the two SELECT/FROM clauses and leave the ACL rules as an optional 
	 * addition. This may be necessary for admins that can circumvent the ACLs.
	 */
	private static final String SQL_WHERE_ACL =
		// ACL Rules
		" AND EXISTS (" +
			// Get the requesting user's information.
			"SELECT ru.id " +
			"FROM user ru " +
			"WHERE ru.username = ? " +
			"AND (" +
				// If it is their own,
				"(u.id = ru.id) " +
				"OR " +
				// If they are a supervisor,
				"EXISTS (" +
					"SELECT ur.role " +
					"FROM user_role ur, user_role_campaign urc " +
					"WHERE ru.id = urc.user_id " +
					"AND c.id = urc.campaign_id " +
					"AND ur.id = urc.user_role_id " +
					"AND ur.role = '" + Campaign.Role.SUPERVISOR.toString() + "'" +
				") " +
				"OR " +
				// If they are an author and the survey response is shared,
				"EXISTS (" +
					"SELECT ur.role " +
					"FROM user_role ur, user_role_campaign urc " +
					"WHERE ru.id = urc.user_id " +
					"AND c.id = urc.campaign_id " +
					"AND ur.id = urc.user_role_id " +
					"AND ur.role = '" + Campaign.Role.AUTHOR.toString() + "' " +
					"AND srps.privacy_state = '" + SurveyResponse.PrivacyState.SHARED.toString() + "'" +
				") " +
				"OR " +
				// If they are an analyst, the survey response is shared, and the
				// campaign is shared.
				"EXISTS (" +
					"SELECT ur.role " +
					"FROM user_role ur, user_role_campaign urc, campaign_privacy_state cps " +
					"WHERE ru.id = urc.user_id " +
					"AND c.id = urc.campaign_id " +
					"AND ur.id = urc.user_role_id " +
					"AND ur.role = '" + Campaign.Role.ANALYST.toString() + "' " +
					"AND srps.privacy_state = '" + SurveyResponse.PrivacyState.SHARED.toString() + "' " +
					"AND c.privacy_state_id = cps.id " +
					"AND cps.privacy_state = '" + Campaign.PrivacyState.SHARED + "'" +
				")" +
			")" +
		")";
	
	/**
	 * Limit the responses to only these usernames. This SQL is incomplete and
	 * ends with "IN ". The user will need to fill in a parenthetical of "?"s
	 * and supply an equal number of usernames to the parameters list.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_USERNAMES =
		" AND u.username IN ";

	/**
	 * Limit the responses to only those on or after a date. This is expected
	 * to be a long value representing the number of milliseconds since the 
	 * epoch.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_ON_OR_AFTER =
		" AND sr.epoch_millis >= ?";

	/**
	 * Limit the responses to only those on or before a date. This is expected
	 * to be a long vlaue representing the number of milliseconds since the
	 * epoch.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_ON_OR_BEFORE =
		" AND sr.epoch_millis <= ?";

	/**
	 * Limit the responses to only those with this privacy state. This should
	 * be a lower-case string representing a valid survey response privacy 
	 * state.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_PRIVACY_STATE =
		" AND srps.privacy_state = ?";

	/**
	 * Limit the responses to only those from a list of survey IDs. This SQL is
	 * incomplete and ends with "IN ". The user will need to fill in a
	 * parenthetical of "?"s and supply an equal number of valid survey IDs to
	 * the parameters list.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES 
	 */
	private static final String SQL_WHERE_SURVEY_IDS =
		" AND sr.survey_id IN ";
	
	/**
	 * Limit the responses to only those from a list of prompt IDs. This SQL is
	 * incomplete and ends with "IN ". The user will need to fill in a 
	 * parenthetical of "?"s and supply an equal number of valid prompt IDs to
	 * the parameters list.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_PROMPT_IDS =
		" AND pr.prompt_id IN ";

	/**
	 * Limit the responses to only those with a given prompt type. This should
	 * be the lower-case representation of a known prompt type.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 */
	private static final String SQL_WHERE_PROMPT_TYPE =
		" AND pr.prompt_type = ?";
	
	/**
	 * Order the results first by the number of milliseconds since the epoch at
	 * which time the survey was taken and then, if there is a collision, by
	 * UUID. This guarantees that all prompt responses for a given survey 
	 * response will be grouped together.
	 * 
	 * @see #SQL_GET_SURVEY_RESPONSES
	 *
	private static final String SQL_ORDER_BY =
		" ORDER BY sr.epoch_millis DESC, sr.uuid";
	*/
	
	// Updates a survey response's privacy state.
	private static final String SQL_UPDATE_SURVEY_RESPONSES_PRIVACY_STATE = 
		"UPDATE survey_response " +
		"SET privacy_state_id = (SELECT id FROM survey_response_privacy_state WHERE privacy_state = ?) " +
		"WHERE uuid in ";
	
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
	 * @see org.ohmage.query.ISurveyResponseQueries#getCampaignIdForSurveyResponseId()
	 */
	@Override
	public String getCampaignIdForSurveyResponseId(UUID uuid) 
			throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_GET_CAMPAIGN_URN_FOR_SURVEY_ID, String.class, uuid.toString());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
				"Error executing SQL '" + SQL_GET_CAMPAIGN_URN_FOR_SURVEY_ID + "'.", e);
		}
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
	 * @see org.ohmage.query.ISurveyResponseQueries#retrieveSurveyResponseDynamically(org.ohmage.domain.campaign.Campaign, java.util.Collection, java.util.Date, java.util.Date, org.ohmage.domain.campaign.SurveyResponse.PrivacyState, java.util.Collection, java.lang.String)
	 */
	@Override
	public int retrieveSurveyResponses(
			final Campaign campaign,
			final String username,
			final Collection<String> usernames, 
			final Date startDate,
			final Date endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType,
			final Collection<ColumnKey> columns,
			final List<SortParameter> sortOrder,
			final long surveyResponsesToSkip,
			final long surveyResponsesToProcess,
			List<SurveyResponse> result)
			throws DataAccessException {
		
		if(
			((surveyIds != null) && (surveyIds.size() == 0)) ||
			((promptIds != null) && (promptIds.size() == 0)) ||
			((columns != null) && (columns.size() == 0))) {
			
			return 0;
		}
		
		List<Object> parameters = new LinkedList<Object>();
		String sql = buildSqlAndParameters(
				campaign,
				username,
				usernames, 
				startDate,
				endDate, 
				privacyState,
				surveyIds,
				promptIds,
				promptType,
				columns,
				sortOrder,
				false,
				parameters);

		// This is necessary to map tiny integers in SQL to Java's integer.
		final Map<String, Class<?>> typeMapping = new HashMap<String, Class<?>>();
		typeMapping.put("tinyint", Integer.class);
		
		// This is a silly, hacky way to get the total count, but it is the 
		// only real way I have found thus far.
		final Collection<Integer> totalCount = new ArrayList<Integer>(1);
		
		try {
			result.addAll(getJdbcTemplate().query(
				sql,
				parameters.toArray(),
				new ResultSetExtractor<List<SurveyResponse>>() {
					/**
					 * First, it skips a set of rows based on the parameterized
					 * number of survey responses to skip. Then, it aggregates  
					 * the information from the number of desired survey 
					 * responses.
					 * 
					 * There must be some ordering on the results in order for
					 * subsequent results to skip / process the same rows. The
					 * agreed upon ordering is by time taken time stamp. 
					 * Therefore, if a user were viewing results as they were
					 * being generated and/or uploaded, it could be that
					 * subsequent calls return the same result as a previous
					 * call. This is analogous to viewing a page of feed data
					 * and going to the next page and seeing some feed items
					 * that you just saw on the previous page. It was decided
					 * that this is a common and acceptable way to view live
					 * data.
					 */
					@Override
					public List<SurveyResponse> extractData(ResultSet rs)
							throws SQLException,
							org.springframework.dao.DataAccessException {
						
						// If the result set is empty, we can simply return an
						// empty list.
						if(! rs.next()) {
							totalCount.add(0);
							return Collections.emptyList();
						}
						
						// Keep track of the number of survey responses we have
						// skipped.
						int surveyResponsesSkipped = 0;
						// Continue while there are more survey responses to
						// skip.
						while(surveyResponsesSkipped < surveyResponsesToSkip) {
							// Get the ID for the survey response we are 
							// skipping.
							String surveyResponseId = rs.getString("uuid");
							surveyResponsesSkipped++;
							
							// Continue to skip rows as long as there are rows
							// to skip and those rows have the same survey
							// response ID.
							while(surveyResponseId.equals(rs.getString("uuid"))) {
								// We were skipping the last survey response,
								// therefore, there are no survey responses to
								// return and we can return an empty list.
								if(! rs.next()) {
									totalCount.add(surveyResponsesSkipped);
									return Collections.emptyList();
								}
							}
						}
						
						// Create a list of the results.
						List<SurveyResponse> result =
								new LinkedList<SurveyResponse>();
						
						// Cycle through the rows until the maximum number of
						// rows has been processed or there are no more rows to
						// process.
						int surveyResponsesProcessed = 0;
						while(surveyResponsesProcessed < surveyResponsesToProcess) {
							// We have not yet processed this survey response,
							// so we need to process it and then continue
							// processing this and all of its survey responses.
							
							// First, create the survey response object.
							SurveyResponse surveyResponse;
							try {
								JSONObject locationJson = null;
								String locationString = rs.getString("location");
								if(locationString != null) {
									locationJson = new JSONObject(locationString);
								}
								
								surveyResponse =
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
								
								if(columns != null) {
									surveyResponse.setCount(
											rs.getLong("count"));
								}
							}
							catch(JSONException e) {
								throw new SQLException("Error creating a JSONObject.", e);
							}
							catch(DomainException e) {
								throw new SQLException("Error creating the survey response information object.", e);
							}
							
							// Add the current survey response to the result
							// list and increase the number of survey responses
							// processed.
							result.add(surveyResponse);
							surveyResponsesProcessed++;
							
							// Get a string representation of the survey
							// response's unique identifier.
							String surveyResponseId =
									surveyResponse.getSurveyResponseId().toString();
							
							// Now, process this prompt response and all 
							// subsequent prompt responses.
							do {
								try {
									// Retrieve the corresponding prompt 
									// information from the campaign.
									Prompt prompt = 
										campaign.getPrompt(
												surveyResponse.getSurvey().getId(),
												rs.getString("prompt_id")
											);
									
									// Generate the prompt response and add it to
									// the survey response.
									surveyResponse.addPromptResponse(
											prompt.createResponse(
													(Integer) rs.getObject(
															"repeatable_set_iteration", 
															typeMapping),
													rs.getString("response")
												)
										);
								}
								catch(DomainException e) {
									throw new SQLException(
											"The prompt response value from the database is not a valid response value for this prompt.", 
											e);
								}
							} while(
									// Get the next prompt response unless we
									// just read the last prompt response in
									// the result,
									rs.next() && 
									// and continue as long as that prompt 
									// response pertains to this survey 
									// response.
									surveyResponseId.equals(rs.getString("uuid")));
									
							// If we exited the loop because we passed the last
							// record, break out of the survey response 
							// processing loop.
							if(rs.isAfterLast()) {
								break;
							}
						}
						
						// Now, if we are after the last row, we need to set 
						// the total count to be the total number skipped plus
						// the total number processed.
						if(rs.isAfterLast()) {
							totalCount.add(
									surveyResponsesSkipped + 
									surveyResponsesProcessed);
						}
						else {
							int otherIds = 1;
							String id = rs.getString("uuid");
							
							while(rs.next()) {
								if(! rs.getString("uuid").equals(id)) {
									otherIds++;
									id = rs.getString("uuid");
								}
							}
							
							totalCount.add(
									surveyResponsesSkipped + 
									surveyResponsesProcessed +
									otherIds);
						}
						
						// Finally, return only the survey responses as a list.
						return result;
					}
				}
			));
			
			return totalCount.iterator().next();
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +  
						sql +
						"' with parameters: " + 
						campaign.getId() + " (campaign ID), " +
						usernames + " (usernames), " +
						startDate + " (start date), " +
						endDate + " (end date), " +
						privacyState + " (privacy state), " + 
						surveyIds + " (survey IDs), " +
						promptIds + " (prompt IDs), " +
						promptType + " (prompt type)",
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ISurveyResponseQueries#updateSurveyResponsePrivacyState(java.lang.Long, org.ohmage.domain.campaign.SurveyResponse.PrivacyState)
	 */
	public void updateSurveyResponsesPrivacyState(
			final Set<UUID> surveyResponseIds, 
			final SurveyResponse.PrivacyState newPrivacyState)
			throws DataAccessException {
		
		StringBuilder sqlBuilder = 
				new StringBuilder(SQL_UPDATE_SURVEY_RESPONSES_PRIVACY_STATE);
		sqlBuilder.append(
				StringUtils.generateStatementPList(surveyResponseIds.size()));

		List<Object> parameters = 
				new ArrayList<Object>(surveyResponseIds.size() + 1);
		parameters.add(newPrivacyState.toString());
		for(UUID surveyResponseId : surveyResponseIds) {
			parameters.add(surveyResponseId.toString());
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a survey response.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
					new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			System.out.println("Executing SQL '" + sqlBuilder.toString() + "' with parameters: " + parameters.toArray());
			
			try {
				getJdbcTemplate().update(
						sqlBuilder.toString(), 
						parameters.toArray());
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing SQL '" + 
								sqlBuilder.toString() + 
							"' with parameters: " + 
								parameters.toArray(), 
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
	
	/**
	 * Builds the SQL for the survey response SELECT and generates a parameter
	 * list that corresponds to that SQL. The parameter list is returned and
	 * the SQL is set as the final parameter.
	 * 
	 * @param campaign The campaign to which the survey responses must belong.
	 * 
	 * @param username The username of the user that is making this request.
	 * 				   This is used by the ACLs to limit who sees what.
	 * 
	 * @param usernames Limits the results to only those submitted by any one 
	 * 					of the users in the list.
	 * 
	 * @param startDate Limits the results to only those survey responses that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate Limits the results to only those survey responses that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState Limits the results to only those survey responses
	 * 					   with this privacy state.
	 * 
	 * @param surveyIds Limits the results to only those survey responses that 
	 * 					were derived from a survey in this collection.
	 * 
	 * @param promptIds Limits the results to only those survey responses that 
	 * 					were derived from a prompt in this collection.
	 * 
	 * @param promptType Limits the results to only those survey responses that
	 * 					 are of the given prompt type.
	 * 
	 * @param columns Aggregates the data based on the column keys. If this is
	 * 				  null, no aggregation is performed. If the list is empty,
	 * 				  an empty list is returned.
	 * 
	 * @param parameters This is a list created by the caller to be populated
	 * 					 with the parameters aggregated while generating this
	 * 					 SQL.
	 * 
	 * @return The list of parameters that corresponds with the generated SQL.
	 */
	private String buildSqlAndParameters(
			final Campaign campaign,
			final String username,
			final Collection<String> usernames, 
			final Date startDate,
			final Date endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final String promptType,
			final Collection<ColumnKey> columns,
			final List<SortParameter> sortOrder,
			final boolean forCount,
			final Collection<Object> parameters) {
		
		// Begin with the SQL string which gets all results or the one that
		// aggregates results.
		StringBuilder sqlBuilder = 
				new StringBuilder(SQL_WHERE_ACL);
		
		// Begin with the campaign's ID and the requester's username.
		parameters.add(campaign.getId());
		parameters.add(username);
		
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
		if(surveyIds != null) {
			sqlBuilder.append(SQL_WHERE_SURVEY_IDS);
			sqlBuilder.append(StringUtils.generateStatementPList(surveyIds.size()));
			parameters.addAll(surveyIds);
		}
		if(promptIds != null) {
			sqlBuilder.append(SQL_WHERE_PROMPT_IDS);
			sqlBuilder.append(StringUtils.generateStatementPList(promptIds.size()));
			parameters.addAll(promptIds);
		}
		if(promptType != null) {
			sqlBuilder.append(SQL_WHERE_PROMPT_TYPE);
			parameters.add(promptType);
		}
		
		// Now, collapse the columns if columns is non-null.
		boolean individual = true;
		boolean onSurveyResponse = true;
		if((columns != null) && (! forCount)) {
			individual = false;
			sqlBuilder.append(" GROUP BY ");
			
			boolean firstPass = true;
			for(ColumnKey columnKey : columns) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sqlBuilder.append(", ");
				}
				
				switch(columnKey) {
				case CONTEXT_CLIENT:
					sqlBuilder.append("sr.client");
					break;
					
				case CONTEXT_TIMESTAMP:
				case CONTEXT_UTC_TIMESTAMP:
					sqlBuilder.append("(sr.epoch_millis / 1000)");
					break;
					
				case CONTEXT_EPOCH_MILLIS:
					sqlBuilder.append("sr.epoch_millis");
					break;
					
				case CONTEXT_TIMEZONE:
					sqlBuilder.append("sr.phone_timezone");
					break;
					
				case CONTEXT_LAUNCH_CONTEXT_LONG:
				case CONTEXT_LAUNCH_CONTEXT_SHORT:
					sqlBuilder.append("sr.launch_context");
					break;
					
				case CONTEXT_LOCATION_STATUS:
					sqlBuilder.append("sr.location_status");
					break;
					
				case USER_ID:
					sqlBuilder.append("u.username");
					break;
					
				case SURVEY_ID:
					sqlBuilder.append("sr.survey_id");
					break;
					
				case SURVEY_RESPONSE_ID:
					sqlBuilder.append("sr.uuid");
					break;
					
				case SURVEY_PRIVACY_STATE:
					sqlBuilder.append("srps.privacy_state");
					break;
					
				case REPEATABLE_SET_ID:
					onSurveyResponse = false;
					sqlBuilder.append("pr.repeatable_set_id");
					break;
					
				case REPEATABLE_SET_ITERATION:
					onSurveyResponse = false;
					sqlBuilder.append("pr.repeatable_set_iteration");
					break;
					
				case PROMPT_RESPONSE:
					onSurveyResponse = false;
					sqlBuilder.append("pr.response");
					break;
					
				// This is inaccurate and will only work if the entire 
				// JSONObject is the same. We cannot do this without JSONObject
				// dissection in SQL.
				case CONTEXT_LOCATION_LATITUDE:
				case CONTEXT_LOCATION_LONGITUDE:
				case CONTEXT_LOCATION_TIMESTAMP:
				case CONTEXT_LOCATION_TIMEZONE:
				case CONTEXT_LOCATION_ACCURACY:
				case CONTEXT_LOCATION_PROVIDER:
					sqlBuilder.append("sr.location");
					break;
					
				// This cannot be done without XML manipulation in the SQL. 
				// Instead, we shouldn't dump the XML in the database and 
				// should explode it into its own series of columns and, if
				// necessary, additional tables.
				case SURVEY_TITLE:
					
				case SURVEY_DESCRIPTION:
					
				default:
					int length = sqlBuilder.length();
					sqlBuilder.delete(length - 2, length);
				}
			}
		}
		// Now, go back and insert the correct SELECT clause based on if we are
		// grouping or not and, if so, if we are doing it at the survey level
		// or the prompt level.
		if(individual && (! forCount)) { 
			sqlBuilder.insert(0, SQL_GET_SURVEY_RESPONSES_INDIVIDUAL);
		}
		else if(onSurveyResponse || forCount) {
			sqlBuilder.insert(0, SQL_GET_SURVEY_RESPONSES_AGGREGATED_SURVEY);
		}
		else {
			sqlBuilder.insert(0, SQL_GET_SURVEY_RESPONSES_AGGREGATED_PROMPT);
		}
		
		// Finally, add some ordering to facilitate consistent results in the
		// paging system.
		if(sortOrder == null) {
			sqlBuilder.append(" ORDER BY epoch_millis DESC, uuid");
		}
		else {
			sqlBuilder.append(" ORDER BY ");
			boolean firstPass = true;
			for(SortParameter sortParameter : sortOrder) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sqlBuilder.append(", ");
				}
				
				sqlBuilder.append(sortParameter.getSqlColumn());
			}
			// We must always include the UUID in the order to guarantee that 
			// all survey responses are grouped together.
			if(firstPass) {
				sqlBuilder.append("uuid");
			}
			else {
				sqlBuilder.append(", uuid");
			}
		}
		
		return sqlBuilder.toString();
	}
}
