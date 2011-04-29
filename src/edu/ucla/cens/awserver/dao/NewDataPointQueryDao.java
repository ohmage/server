package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.domain.ConfigurationValueMerger;
import edu.ucla.cens.awserver.domain.NewDataPointQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public class NewDataPointQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryDao.class);
	private ConfigurationValueMerger _configurationValueMerger;
	
	// TODO - later on the columns that are selected can be optimized to whatever columns are present in the query
	// need a mapping from URN to column name?
	private String _sql = "SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
			           + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id, u.login_id," +
			           		" sr.client, sr.launch_context, sr.id"
	                   + " FROM prompt_response pr, survey_response sr, user u, campaign c"
	                   + " WHERE pr.survey_response_id = sr.id"
                       + " AND c.urn = ?"
                       + " AND c.id = sr.campaign_id"
                       + " AND sr.user_id = u.id"; 
	
	private String _andDatesBetween = " AND sr.msg_timestamp BETWEEN ? AND ?";
	
	private String _andUsers = " AND u.login_id IN "; 
	
	private String _andPromptIds = " AND pr.prompt_id IN ";  
	
	private String _andSurveyIds = " AND sr.survey_id IN ";
	
	private String _orderBy = " ORDER BY u.login_id, sr.msg_timestamp, sr.survey_id, " +
			                  "pr.repeatable_set_id, pr.repeatable_set_iteration, pr.prompt_id";
	
	public NewDataPointQueryDao(DataSource dataSource, ConfigurationValueMerger configurationValueMerger) {
		super(dataSource);
		if(null == configurationValueMerger) {
			throw new IllegalArgumentException("a ConfigurationValueMerger is required");
		}
		_configurationValueMerger = configurationValueMerger;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("about to construct and run a \"new\" data point query");
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		String sql = generateSql(req);
		
		final Configuration configuration = req.getConfiguration();
		final List<Object> paramObjects = new ArrayList<Object>();
		paramObjects.add(req.getCampaignUrn());
		
		if(! "urn:ohmage:special:all".equals(req.getUserListString())) {
			paramObjects.addAll(req.getUserList());
		}
		
		if(null != req.getStartDate()) {
			paramObjects.add(req.getStartDate());
		}
		
		if(null != req.getEndDate()) {
			paramObjects.add(req.getEndDate());
		}
		
		if(null != req.getPromptIdList()) {
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				paramObjects.addAll(req.getPromptIdList());
			}
		} else {
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				paramObjects.addAll(req.getSurveyIdList());
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("sql params: " + paramObjects);
		}
		
		try {
			
			List<?> results = getJdbcTemplate()
				.query(sql, paramObjects.toArray(), new RowMapper() { 
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						NewDataPointQueryResult result = new NewDataPointQueryResult();
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
						
						result.setTimestamp(rs.getString(6));
						result.setTimezone(rs.getString(7));
						result.setLocationStatus(rs.getString(8));
						result.setLocation(rs.getString(9));
						result.setSurveyId(rs.getString(10));
						result.setLoginId(rs.getString(11));
						result.setClient(rs.getString(12));
						result.setLaunchContext(rs.getString(13));
						result.setSurveyPrimaryKeyId(rs.getInt(14));
						
						_configurationValueMerger.merge(result, configuration);
						
						return result;
					}
				}
			);
			
			_logger.info("found " + results.size() + " query results");
			req.setResultList(results);
			
		} catch(org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running the following SQL '" + sql + "' with the parameters: " +
				paramObjects, dae);
			
			throw new DataAccessException(dae);
		}
	}
	
	private String generateSql(NewDataPointQueryAwRequest req) {
		StringBuilder builder = new StringBuilder();
		
		builder.append(_sql);
		
		if(! "urn:ohmage:special:all".equals(req.getUserListString())) {
			
			builder.append(_andUsers);
			builder.append(generateParams(req.getUserList().size()));
		} 
		
		if(null != req.getPromptIdList()) {
			
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				
				builder.append(_andPromptIds);
				builder.append(generateParams(req.getPromptIdList().size()));
			}
			
		} else { // surveys
			
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				
				builder.append(_andSurveyIds);
				builder.append(generateParams(req.getSurveyIdList().size()));
			}
		}
		
		if(null != req.getStartDate() && null != req.getEndDate()) {
			builder.append(_andDatesBetween);
		}
		
		builder.append(_orderBy);
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("generated sql: " + builder);
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
