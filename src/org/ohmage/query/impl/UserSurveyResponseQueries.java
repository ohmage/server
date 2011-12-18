package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserSurveyResponseQueries;
import org.springframework.jdbc.core.RowMapper;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-survey relationships. While it may read 
 * information pertaining to other entities, the information it takes and  
 * provides should pertain to user-survey relationships only.
 * 
 * @author John Jenkins
 */
public final class UserSurveyResponseQueries extends Query implements IUserSurveyResponseQueries {
	// Retrieves the username of the owner of a survey response.
	private static final String SQL_GET_SURVEY_RESPONSE_OWNER =
		"SELECT u.username " +
		"FROM user u, survey_response sr " +
		"WHERE sr.uuid = ? " +
		"AND sr.user_id = u.id";
	
	// Retrieves all of the survey responses for a user that are visible to a
	// requesting user.
	private static final String SQL_GET_SURVEY_RESPONSES_FOR_USER_FOR_REQUESTER = 
		"SELECT c.urn, sr.client, sr.epoch_millis, sr.phone_timezone, sr.upload_timestamp, " +
			"sr.survey_id, pr.prompt_id, pr.prompt_type, pr.repeatable_set_id, pr.repeatable_set_iteration, pr.response, " +
			"sr.launch_context, sr.location_status, sr.location, srps.privacy_state, " +
			"pr.audit_timestamp as prompt_audit_timestamp, sr.audit_timestamp as survey_audit_timestamp " +
		"FROM user u, user ru, user_role ur, user_role_campaign urc, " +
			"campaign c, campaign_running_state crs, campaign_privacy_state cps, " +
			"survey_response sr, survey_response_privacy_state srps, " +
			"prompt_response pr " +
		// Get the user and their survey responses.
		"WHERE u.username = ? " +
		"AND u.id = sr.user_id " +
		// Align the campaign.
		"AND c.id = sr.campaign_id " +
		// Align the survey response's privacy state.
		"AND srps.id = sr.privacy_state_id " +
		// Align the prompt response to the survey response.
		"AND sr.id = pr.survey_response_id " +
		// Get the requester.
		"AND ru.username = ? " +
		"AND (" +
			"(" +
				// The requesting user is a supervisor.
				"ru.id = urc.user_id " +
				"AND c.id = urc.campaign_id " +
				"AND ur.id = urc.user_role_id " +
				"AND ur.role = '" + Campaign.Role.SUPERVISOR + "'" +
			")" +
			" OR " +
			"(" +
				// The requesting user is an analyst, the campaign's privacy
				// state is shared, and the response's privacy state is not
				// "invisible".
				"ru.id = urc.user_id " +
				"AND c.id = urc.campaign_id " +
				"AND ur.id = urc.user_role_id " +
				"AND ur.role = '" + Campaign.Role.ANALYST + "' " +
				// Get the campaign's privacy state.
				"AND c.privacy_state_id = cps.id " +
				"AND cps.privacy_state = '" + Campaign.PrivacyState.SHARED + "' " +
				// Ensure the survey response is not "invisible".
				"AND srps.privacy_state != '" + SurveyResponse.PrivacyState.INVISIBLE + "'" +
			")" +
			" OR " +
			"(" +
				// The requester is an author and the response's privacy state
				// is shared.
				"ru.id = urc.user_id " +
				"AND c.id = urc.campaign_id " +
				"AND ur.id = urc.user_role_id " +
				"AND ur.role = '" + Campaign.Role.AUTHOR + "' " +
				// Get the survey response's privacy state.
				"AND srps.id = sr.privacy_state_id " +
				"AND srps.privacy_state = '" + SurveyResponse.PrivacyState.SHARED + "'" +
			")" +
			" OR " +
			"(" +
				// The requesting user is the same as the user, the campaign is
				// running, and the response's privacy state is not 
				// "invisible".
				// TODO: This may need to be updated with the new ACLs.
				"ru.id = u.id " +
				// Get the campaign's running state.
				"AND c.running_state_id = crs.id " +
				"AND crs.id = '" + Campaign.RunningState.RUNNING + "' " +
				// Ensure the survey response is not "invisible".
				"AND srps.privacy_state != '" + SurveyResponse.PrivacyState.INVISIBLE + "'" +
			")" +
		")";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use when accessing the database.
	 */
	private UserSurveyResponseQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Return the username of the user that created this survey response.
	 * 
	 * @param surveyResponseId The unique identifier for the survey response.
	 * 
	 * @return The username of the user that owns this survey response or null
	 * 		   if the survey response doesn't exist.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public String getSurveyResponseOwner(UUID surveyResponseId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_SURVEY_RESPONSE_OWNER,
					new Object[] { surveyResponseId.toString() },
					String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("One survey response has more than one owner.", e);
			}
		
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_SURVEY_RESPONSE_OWNER + "' with parameter: " + surveyResponseId, e);
		}
	}
	
	/**
	 * Retrieves the milliseconds since epoch of the time that the most recent 
	 * survey was completed.
	 *  
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user to which the data belongs.
	 * 
	 * @return A Long representing the milliseconds since epoch when the most 
	 * 		   recently completed survey was completed or null if the user has
	 * 		   not yet uploaded any surveys.
	 */
	public Long getLastUploadForUser(String requestersUsername, String usersUsername) throws DataAccessException {
		try {
			List<Long> epochMillis = getJdbcTemplate().query(
					SQL_GET_SURVEY_RESPONSES_FOR_USER_FOR_REQUESTER,
					new Object[] { usersUsername, requestersUsername },
					new RowMapper<Long> () {
						@Override
						public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
							return rs.getLong("epoch_millis");
						}
					}
				);
			
			if(epochMillis.size() == 0) {
				return null;
			}
			else {
				Collections.sort(epochMillis);
				return epochMillis.get(epochMillis.size() - 1);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_SURVEY_RESPONSES_FOR_USER_FOR_REQUESTER + "' with parameters: " +
					usersUsername + ", " + requestersUsername, e);
		}
	}
	
	/**
	 * Retrieves the percentage of non-null location values from surveys over
	 * the past 'hours'.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user to which the data belongs.
	 * 
	 * @param hours Defines the timespan for which the information should be
	 * 				retrieved. The timespan is from now working backwards until
	 * 				'hours' hours ago.
	 * 
	 * @return Returns the percentage of non-null location values from surveys
	 * 		   over the last 'hours' or null if there were no surveys.
	 */
	public Double getPercentageOfNonNullSurveyLocations(String requestersUsername, String usersUsername, int hours)
		throws DataAccessException {
		
		long nonNullLocationsCount = 0;
		long totalLocationsCount = 0;
		
		try {
			// Get a time stamp from 24 hours ago.
			Calendar dayAgo = Calendar.getInstance();
			dayAgo.add(Calendar.HOUR_OF_DAY, -hours);
			final Timestamp dayAgoTimestamp = new Timestamp(dayAgo.getTimeInMillis());
			
			final List<String> nonNullLocations = new LinkedList<String>();
			final List<String> allLocations = new LinkedList<String>();
			
			getJdbcTemplate().query(
					SQL_GET_SURVEY_RESPONSES_FOR_USER_FOR_REQUESTER, 
					new Object[] { usersUsername, requestersUsername }, 
					new RowMapper<String>() {
						@Override
						public String mapRow(ResultSet rs, int rowNum) throws SQLException {
							// Get the time the Mobility point was uploaded.
							Timestamp generatedTimestamp = rs.getTimestamp("upload_timestamp");
							
							// If it was uploaded within the last 'hours' it is
							// valid.
							if(! generatedTimestamp.before(dayAgoTimestamp)) {
								String location = rs.getString("location");
								if(location != null) {
									nonNullLocations.add(location);
								}
								allLocations.add(location);
							}
							
							return null;
						}
					}
				);
			
			nonNullLocationsCount += nonNullLocations.size();
			totalLocationsCount += allLocations.size();
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error while executing '" + SQL_GET_SURVEY_RESPONSES_FOR_USER_FOR_REQUESTER + "' with parameters: " + 
					usersUsername + ", " + requestersUsername, e);
		}
		
		if(totalLocationsCount == 0) {
			return null;
		}
		else {
			return new Double(nonNullLocationsCount) / new Double(totalLocationsCount);
		}
	}
}