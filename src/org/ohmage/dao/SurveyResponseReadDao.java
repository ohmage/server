/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.Configuration;
import org.ohmage.domain.ConfigurationValueMerger;
import org.ohmage.domain.SurveyResponseReadResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;


/**
 * @author Joshua Selsky
 */
public class SurveyResponseReadDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadDao.class);
	private ConfigurationValueMerger _configurationValueMerger;
	
	private String _sql = "SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
			           + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id, u.username," +
			           		" sr.client, sr.launch_context, sr.id, srps.privacy_state"
	                   + " FROM prompt_response pr, survey_response sr, user u, campaign c, survey_response_privacy_state srps"
	                   + " WHERE pr.survey_response_id = sr.id"
                       + " AND c.urn = ?"
                       + " AND c.id = sr.campaign_id"
                       + " AND sr.user_id = u.id"
                       + " AND sr.privacy_state_id = srps.id"; 
	
	private String _andDatesBetween = " AND sr.msg_timestamp BETWEEN ? AND ?";
	
	private String _andUsers = " AND u.username IN "; 
	
	private String _andPromptIds = " AND pr.prompt_id IN ";  
	
	private String _andSurveyIds = " AND sr.survey_id IN ";

	private String _orderByUserTimestampSurvey = " ORDER BY u.username, sr.msg_timestamp, sr.survey_id, " +
			                                     "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id";
	
	private String _orderByUserSurveyTimestamp = " ORDER BY u.username, sr.survey_id, " +
                                                 "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id, sr.msg_timestamp";

	private String _orderBySurveyUserTimestamp = " ORDER BY sr.survey_id, pr.repeatable_set_id, pr.repeatable_set_iteration, " +
			                                     "pr.prompt_id, u.username, sr.msg_timestamp";
	
	private String _orderBySurveyTimestampUser = " ORDER BY sr.survey_id, pr.repeatable_set_id, pr.repeatable_set_iteration, " +
			                                     "pr.prompt_id, sr.msg_timestamp, u.username";
	
	private String _orderByTimestampUserSurvey = " ORDER BY sr.msg_timestamp, u.username, sr.survey_id, " +
                                                 "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id";

	private String _orderByTimestampSurveyUser = " ORDER BY sr.msg_timestamp, sr.survey_id, pr.repeatable_set_id, " +
			                                     "pr.repeatable_set_iteration, pr.prompt_id, u.username";	
	
	public SurveyResponseReadDao(DataSource dataSource, ConfigurationValueMerger configurationValueMerger) {
		super(dataSource);
		if(null == configurationValueMerger) {
			throw new IllegalArgumentException("a ConfigurationValueMerger is required");
		}
		_configurationValueMerger = configurationValueMerger;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Generating SQL for a survey_response query");
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		String sql = generateSql(req);
		
		final Configuration configuration = req.getConfiguration();
		final List<Object> paramObjects = new ArrayList<Object>();
		paramObjects.add(req.getCampaignUrn());
		
		if(! "urn:ohmage:special:all".equals(req.getUserListString())) {
			paramObjects.addAll(req.getUserList());
		}
		
		// FIXME: Is this correct, or should it be:
		if((null != req.getPromptIdList()) && (req.getPromptIdList().size() != 0)) {
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				paramObjects.addAll(req.getPromptIdList());
			}
		} 
		else {
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				paramObjects.addAll(req.getSurveyIdList());
			}
		}
		
		// FIXME: Do these two look correct? Is this where they should be?
		if(null != req.getStartDate()) {
			paramObjects.add(req.getStartDate());
		}
		
		if(null != req.getEndDate()) {
			paramObjects.add(req.getEndDate());
		}
		
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("SQL params: " + paramObjects);
		}
		
		try {
			
			_logger.info("Running SQL for a survey_response query");
			
			List<?> results = getJdbcTemplate()
				.query(sql, paramObjects.toArray(), new RowMapper() { 
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
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
						result.setPrivacyState(rs.getString(15));
						
						_configurationValueMerger.merge(result, configuration);
						
						return result;
					}
				}
			);
			
			_logger.info("Found " + results.size() + " query results");
			req.setResultList(results);
			
		} catch(org.springframework.dao.DataAccessException dae) {
			
			_logger.error("Caught DataAccessException when running the following SQL '" + sql + "' with the parameters: " +
				paramObjects, dae);
			
			throw new DataAccessException(dae);
		}
	}
	
	private String generateSql(SurveyResponseReadAwRequest req) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(_sql);
		
		if(! "urn:ohmage:special:all".equals(req.getUserListString())) {
			
			builder.append(_andUsers);
			builder.append(generateParams(req.getUserList().size()));
		} 
		
		if((null != req.getPromptIdList()) && (req.getPromptIdList().size() != 0)) {
			
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				
				builder.append(_andPromptIds);
				builder.append(generateParams(req.getPromptIdList().size()));
			}
			
		} 
		else { // surveys
			
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				
				builder.append(_andSurveyIds);
				builder.append(generateParams(req.getSurveyIdList().size()));
			}
		}
		
		if(null != req.getStartDate() && null != req.getEndDate()) {
			builder.append(_andDatesBetween);
		}
		
		// this is super-lame; it should be using an enum
		
		if("user,timestamp,survey".equals(req.getSortOrder())) {
			
			builder.append(_orderByUserTimestampSurvey);
			
		} else if("user,survey,timestamp".equals(req.getSortOrder())) {
			
			builder.append(_orderByUserSurveyTimestamp);
			
		} else if("survey,user,timestamp".equals(req.getSortOrder())) {
			
			builder.append(_orderBySurveyUserTimestamp);
			
		} else if("survey,timestamp,user".equals(req.getSortOrder())) {
			
			builder.append(_orderBySurveyTimestampUser);
			
		} else if("timestamp,survey,user".equals(req.getSortOrder())) {
			
			builder.append(_orderByTimestampSurveyUser);
			
		} else if("timestamp,user,survey".equals(req.getSortOrder())) {
			
			builder.append(_orderByTimestampUserSurvey);
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("Generated SQL: " + builder);
		}
		
		return builder.toString();
	}
	
	private String generateParams(int numberOfParams) {
		StringBuilder builder = new StringBuilder("(");
		
		for(int i = 0; i < numberOfParams; i++) {
			builder.append("?");
			if(i != numberOfParams - 1) {
				builder.append(",");
			}
		}
		
		builder.append(")");
		return builder.toString();
	}
}
