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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignSurveyResponseQueries;
import org.springframework.jdbc.core.RowMapper;

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
	public long getNumberOfPromptResponsesForCampaign(String campaignId) throws DataAccessException {
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

	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignSurveyResponseQueries#getSurveyResponseCountInfo(java.lang.String, java.lang.String, java.lang.Collection)
	 */
	public Map<String, Map<SurveyResponse.PrivacyState, Integer>> getSurveyResponseCountInfoForCampaigns(
		final String requestUser,
		final String campaignSqlStmt, 
		final Collection<Object> campaignSqlParameters)
			throws DataAccessException {
	
	    Collection<Object> parameters = new LinkedList<Object>();
	    
	    StringBuilder sql = new StringBuilder(
		    "SELECT c.urn, srps.privacy_state, COUNT(DISTINCT sr.id) as count " + 
		    "FROM survey_response sr " +
		    	"JOIN campaign c ON (sr.campaign_id = c.id) " +
		    	"JOIN user u ON (sr.user_id = u.id) " + 
		    	"JOIN survey_response_privacy_state srps ON (sr.privacy_state_id = srps.id) " + 
		    	"JOIN campaign_privacy_state cps ON (c.privacy_state_id = cps.id) " +
		    	"CROSS JOIN user ru " +
		    	"LEFT JOIN user_role_campaign urc on (urc.user_id = ru.id AND urc.campaign_id = c.id) " +
		    	"LEFT JOIN user_role ur on (urc.user_role_id = ur.id) " +
		    "WHERE "  +
		      "( " +
		    	"ru.admin = TRUE " + 
		        "OR ru.id = sr.user_id " +
		        "OR ur.role = 'supervisor' " +
		        "OR (ur.role = 'author' AND srps.privacy_state = 'shared') " +
		        "OR (ur.role = 'analyst' AND srps.privacy_state = 'shared' AND cps.privacy_state = 'shared') " +
		      ") " +
		      "AND ru.username = ? " +
		      "AND c.id IN ");
	    sql.append(	   "(" + campaignSqlStmt  + ")");
	    sql.append(" GROUP BY c.urn, srps.id ");
	    parameters.add(requestUser);
	    parameters.addAll(campaignSqlParameters);
	    
	    final Map<String, Map<SurveyResponse.PrivacyState, Integer>> responseCountInfoMap = 
		    new HashMap<String, Map<SurveyResponse.PrivacyState, Integer>>();

	    try {

		getJdbcTemplate().query(
			sql.toString(), 
			parameters.toArray(), 
			new RowMapper<Object>() {
			    @Override
			    public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
				try {

				    String urn = rs.getString("urn");						    	
				    Integer count = rs.getInt("count");
				    SurveyResponse.PrivacyState privacy_state = SurveyResponse.PrivacyState.getValue(rs.getString("privacy_state"));
				    
				    Map<SurveyResponse.PrivacyState, Integer> responseCountInfo = responseCountInfoMap.get(urn);
				    if (responseCountInfo == null) {
					responseCountInfo = new HashMap<SurveyResponse.PrivacyState, Integer>();
					responseCountInfoMap.put(urn, responseCountInfo);
				    } 
				    responseCountInfo.put(privacy_state, count);								
				    
				    return null;
				} 
				catch (IllegalArgumentException e) {
					throw new SQLException("Can't resolve privacy state: " + rs.getString("privacy_state"), e); 
				}
				catch (Exception e) {
				    throw new SQLException("Can't create a role with parameters: " + 
					    rs.getString("username") + "," + rs.getString("urn") + "," + rs.getString("roles"), e);
				}
			    }
			}
			);
		return responseCountInfoMap;
	    }
	    catch(org.springframework.dao.DataAccessException e) {
		throw new DataAccessException("Error executing SQL '" + sql + 
			"' with parameters: " + campaignSqlStmt , 
			e);
	    }
	    
	    
	}

}
