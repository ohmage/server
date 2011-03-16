package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public class NewDataPointQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryDao.class);
	
	// TODO - later on the columns that are selected can be optimized to whatever columns are present in the query
	// need a mapping from URN to column name?
	private String _sql = "SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
			           + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id, u.login_id," +
			           		" sr.client, sr.launch_context"
	                   + " FROM prompt_response pr, survey_response sr, user u, campaign_configuration cc, campaign c"
	                   + " WHERE pr.survey_response_id = sr.id"
                       + " AND c.name = ?"
                       + " AND cc.campaign_id = c.id"
                       + " AND cc.version = ?"
                       + " AND cc.id = sr.campaign_configuration_id"
                       + " AND sr.msg_timestamp BETWEEN ? AND ?"
                       + " AND sr.user_id = u.id"; 
	
	private String _andUsers = " AND u.login_id IN "; 
	
	private String _andPromptIds = " AND pr.prompt_id IN ";  
	
	private String _andSurveyIds = " AND sr.survey_id IN ";
	
	private String _orderBy = " ORDER BY u.login_id, sr.msg_timestamp, sr.survey_id, pr.prompt_id";
	
	public NewDataPointQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		String sql = generateSql(req);
		
		final List<Object> paramObjects = new ArrayList<Object>();
		paramObjects.add(req.getCampaignName());
		paramObjects.add(req.getCampaignVersion());
		paramObjects.add(req.getStartDate());
		paramObjects.add(req.getEndDate());
		
		if(! "urn:sys:special:all".equals(req.getUserListString())) {
			paramObjects.addAll(req.getUserList());
		} 
		
		if(null != req.getPromptIdList()) {
			if(! "urn:sys:special:all".equals(req.getPromptIdListString())) {
				paramObjects.addAll(req.getPromptIdList());
			}
		} else {
			if(! "urn:sys:special:all".equals(req.getSurveyIdListString())) {
				paramObjects.addAll(req.getSurveyIdList());
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug("sql params: " + paramObjects);
		}
		
		try {
			
			List<?> results = getJdbcTemplate().query(sql, paramObjects.toArray(), new DataPointQueryRowMapper());
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
		
		if(! "urn:sys:special:all".equals(req.getUserListString())) {
			
			builder.append(_andUsers);
			builder.append(generateParams(req.getUserList().size()));
		} 
		
		if(null != req.getPromptIdList()) {
			
			if(! "urn:sys:special:all".equals(req.getPromptIdListString())) {
				
				builder.append(_andPromptIds);
				builder.append(generateParams(req.getPromptIdList().size()));
			}
			
		} else { // surveys
			
			if(! "urn:sys:special:all".equals(req.getSurveyIdListString())) {
				
				builder.append(_andSurveyIds);
				builder.append(generateParams(req.getSurveyIdList().size()));
			}
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
