package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.survey.read.ConfigurationValueMerger;
import org.ohmage.domain.survey.read.SurveyResponseReadResult;
import org.ohmage.exception.DataAccessException;
import org.ohmage.request.Request;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * Contains the query operation for survey response read.
 * 
 * @author Joshua Selsky
 * @see org.ohmage.domain.survey.read.ConfigurationValueMerger
 */
public final class SurveyResponseReadDao extends Dao {
	
	private static SurveyResponseReadDao instance;
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseReadDao.class);
	
	private static final String SQL_SELECT = 
		"SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
        + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id, u.username," +
        		" sr.client, sr.launch_context, sr.id, srps.privacy_state, sr.epoch_millis"
        + " FROM prompt_response pr, survey_response sr, user u, campaign c, survey_response_privacy_state srps"
        + " WHERE pr.survey_response_id = sr.id"
        + " AND c.urn = ?"
        + " AND c.id = sr.campaign_id"
        + " AND sr.user_id = u.id"
        + " AND sr.privacy_state_id = srps.id"; 
	
	private static final String SQL_AND_USERS =
		" AND u.username IN "; 
	
	private static final String SQL_AND_PROMPT_IDS = 
		" AND pr.prompt_id IN ";  
	
	private static final String SQL_AND_SURVEY_IDS = 
		" AND sr.survey_id IN ";
	
	private static final String SQL_AND_DATES_BETWEEN = 
		" AND sr.msg_timestamp BETWEEN ? AND ?";
	
	private static final String SQL_ORDER_BY_USER_TIMESTAMP_SURVEY =
		" ORDER BY u.username, sr.msg_timestamp, sr.survey_id, " +
	    "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id";
	
	private static final String SQL_ORDER_BY_USER_SURVEY_TIMESTAMP = 
		" ORDER BY u.username, sr.survey_id, " +
	    "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id, sr.msg_timestamp";
	
	private static final String SQL_ORDER_BY_SURVEY_USER_TIMESTAMP = 
		" ORDER BY sr.survey_id, pr.repeatable_set_id, pr.repeatable_set_iteration, " +
	    "pr.prompt_id, u.username, sr.msg_timestamp";
	
	private static final String SQL_ORDER_BY_SURVEY_TIMESTAMP_USER =
		" ORDER BY sr.survey_id, pr.repeatable_set_id, pr.repeatable_set_iteration, " +
	    "pr.prompt_id, sr.msg_timestamp, u.username";
	
	private static final String SQL_ORDER_BY_TIMESTAMP_USER_SURVEY =
		" ORDER BY sr.msg_timestamp, u.username, sr.survey_id, " +
	    "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id";
	
	private static final String SQL_ORDER_BY_TIMESTAMP_SURVEY_USER =
		" ORDER BY sr.msg_timestamp, sr.survey_id, pr.repeatable_set_id, " +
        "pr.repeatable_set_iteration, pr.prompt_id, u.username";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	private SurveyResponseReadDao(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Runs a SELECT statement to return survey responses using dynamically 
	 * generated SQL. Assumes all parameters have been previously validated.
	 * 
	 * @param request  The request to fail should an IO problem occur.
	 * @param userList  A possibly empty list of usernames or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param campaignID  A campaign URN indicating which campaign the responses
	 * must belong to. 
	 * @param promptIdList A possibly empty list of prompt ids or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param surveyIdList A possibly empty list of survey ids or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param startDate A possibly null start date.
	 * @param endDate A possibly null end date. Both start date and end date must be
	 * present if either is non-null
	 * @param sortOrder A valid sort order that is used to choose the correct 
	 * ORDER BY clause to use.
	 * @param configuration  A campaign configuration that is used to merge survey
	 * responses with static configuration data that is used during output 
	 * generation.
	 * @return A possibly empty list of survey responses.
	 * @throws DataAccessException if an IO problem occurs
	 * @throws IllegalArgumentException if any of the required parameters are null.
	 * The required parameters are request, userList, campaignID, promptIdList, 
	 * surveyIdList, sortOrder, and configuration.
	 */
	public static List<SurveyResponseReadResult> retrieveSurveyResponses(Request request, 
			List<String> userList, String campaignID,
			List<String> promptIdList, List<String> surveyIdList, 
			Date startDate, Date endDate, String sortOrder,
			final Configuration configuration) throws DataAccessException {
		
		// check for logical errors -- missing required params
		if(request == null || userList == null || StringUtils.isEmptyOrWhitespaceOnly(campaignID)
			|| (promptIdList == null && surveyIdList == null) || configuration == null) {
			
			throw new IllegalArgumentException("request, userList, campaignID, promptIdList or surveyIdList, " +
				" and configuration must all be non-null");
		}
		
		LOGGER.info("Generating SQL for survey response read");
		
		String sql = generateSql(userList, promptIdList, surveyIdList, startDate, endDate, sortOrder);
		
		final List<Object> paramList = new ArrayList<Object>();
		
		paramList.add(campaignID);
		
		if(! userList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
			paramList.addAll(userList);
		}
		if(surveyIdList.isEmpty()) {
			if(! promptIdList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
				paramList.addAll(promptIdList);
			}
		} 
		else {
			if(! surveyIdList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
				paramList.addAll(surveyIdList);
			}
		}
		if(startDate != null && endDate != null) {
			paramList.add(new java.sql.Date(startDate.getTime()).toString());
			paramList.add(new java.sql.Date(endDate.getTime()).toString());
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("SQL params: " + paramList);
		}
		
		try {
			return instance.getJdbcTemplate().query(
				sql, paramList.toArray(), new RowMapper<SurveyResponseReadResult>() {
					public SurveyResponseReadResult mapRow(ResultSet rs, int rowNum) throws SQLException {
						SurveyResponseReadResult result = new SurveyResponseReadResult();
						result.setPromptId(rs.getString(1));
						result.setPromptType(rs.getString(2));
						result.setResponse(rs.getObject(3));
						
						Object o = rs.getObject(4);
						if(null == o) {
							result.setRepeatableSetIteration(null);	
						} else {
							result.setRepeatableSetIteration(rs.getInt(4));
						}
						
						o = rs.getObject(5);
						if(null == o) {
							result.setRepeatableSetId(null);
						} else {
							result.setRepeatableSetId(rs.getString(5));
						}
						
						result.setTimestamp(StringUtils.stripMillisFromJdbcTimestampString(rs.getString(6))); 
						result.setTimezone(rs.getString(7));
						result.setLocationStatus(rs.getString(8));
						result.setLocation(rs.getString(9));
						result.setSurveyId(rs.getString(10));
						result.setUsername(rs.getString(11));
						result.setClient(rs.getString(12));
						result.setLaunchContext(rs.getString(13));
						result.setSurveyPrimaryKeyId(rs.getInt(14));
						result.setPrivacyState(SurveyResponsePrivacyStateCache.PrivacyState.getValue(rs.getString(15)));
						result.setEpochMillis(rs.getLong(16));
						
						ConfigurationValueMerger.merge(result, configuration);
						
						return result;
					}
				}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Dynamically generates the SQL statement based on the state of the
	 * provided parameters. Assumes all parameters have been previously
	 * validated.
	 * 
	 * @param userList  A possibly empty list of usernames or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param promptIdList A possibly empty list of prompt ids or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param surveyIdList A possibly empty list of survey ids or the special
	 * value {@link org.ohmage.request.SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST}
	 * @param startDate A possibly null start date.
	 * @param endDate A possibly null end date. Both start date and end date must be
	 * present if either is non-null
	 * @param sortOrder A valid sort order that is used to choose the correct 
	 * ORDER BY clause to use.
	 * @return  A dynamically generated SQL statement that contains parameter lists
	 * and an ORDER BY clause based on the contents of the provided parameters.
	 */
	private static String generateSql(List<String> userList, List<String> promptIdList, 
				List<String> surveyIdList, Date startDate, Date endDate, String sortOrder) {
		
		StringBuilder builder = new StringBuilder(); 
		
		builder.append(SQL_SELECT);
		
		if(! userList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
			
			builder.append(SQL_AND_USERS);
			builder.append(StringUtils.generateStatementPList(userList.size()));
		} 
		
		if(! promptIdList.isEmpty()) {
			
			if(! promptIdList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
				
				builder.append(SQL_AND_PROMPT_IDS);
				builder.append(StringUtils.generateStatementPList(promptIdList.size()));
			}
			
		} 
		else { // surveys
			
			if(! surveyIdList.equals(SurveyResponseReadRequest.URN_SPECIAL_ALL_LIST)) {
				
				builder.append(SQL_AND_SURVEY_IDS);
				builder.append(StringUtils.generateStatementPList(surveyIdList.size()));
			}
		}
		
		if(null != startDate && null != endDate) {
			builder.append(SQL_AND_DATES_BETWEEN);
		}
		
		// this is super-lame; it should be using an enum
		
		if(sortOrder != null) {
		
			if("user,timestamp,survey".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_USER_TIMESTAMP_SURVEY);
				
			} else if("user,survey,timestamp".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_USER_SURVEY_TIMESTAMP);
				
			} else if("survey,user,timestamp".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_SURVEY_USER_TIMESTAMP);
				
			} else if("survey,timestamp,user".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_SURVEY_TIMESTAMP_USER);
				
			} else if("timestamp,survey,user".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_TIMESTAMP_SURVEY_USER);
				
			} else if("timestamp,user,survey".equals(sortOrder)) {
				
				builder.append(SQL_ORDER_BY_TIMESTAMP_USER_SURVEY);
			} 
			// FIXME - what's the default if there is no sort order? Ask Hongsuda.
		}
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Generated SQL: " + builder);
		}
		
		return builder.toString();
	}
}
