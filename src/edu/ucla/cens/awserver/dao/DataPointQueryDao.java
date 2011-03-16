package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointQueryAwRequest;

/**
 * @author selsky
 */
public class DataPointQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DataPointQueryDao.class);
	
	private String _select = "SELECT pr.prompt_id, pr.prompt_type, pr.response, pr.repeatable_set_iteration, pr.repeatable_set_id,"
			            + " sr.msg_timestamp, sr.phone_timezone, sr.location_status, sr.location, sr.survey_id"
	                    + " FROM prompt_response pr, survey_response sr, user u, campaign_configuration cc, campaign c"
	                    + " WHERE pr.survey_response_id = sr.id"
	                    + " AND u.login_id = ?"
	                    + " AND sr.user_id = u.id"
	                    + " AND c.name = ?"
	                    + " AND cc.campaign_id = c.id"
	                    + " AND cc.version = ?"
	                    + " AND cc.id = sr.campaign_configuration_id"
	                    + " AND sr.msg_timestamp BETWEEN ? AND ?"
	                    + " AND prompt_id in ";
	
	public DataPointQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		DataPointQueryAwRequest req = (DataPointQueryAwRequest) awRequest;
		List<String> metadataPromptIds = req.getMetadataPromptIds();
		String[] promptIds = req.getDataPointIds();
		
		int numberOfMetadataPoints = metadataPromptIds.size();
		int numberOfPromptIds = promptIds.length;
		int totalNumberOfParams = numberOfMetadataPoints + numberOfPromptIds;
		
		StringBuilder builder = new StringBuilder("(");
		for(int i = 0; i < totalNumberOfParams; i++) {
			builder.append("?");
			if(i < totalNumberOfParams - 1) {
				builder.append(",");
			}
		}
		builder.append(")");
		
		final String sql = _select + builder.toString();
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(sql);
		}
		
		final List<Object> paramObjects = new ArrayList<Object>();
		paramObjects.add(req.getUserNameRequestParam());
		paramObjects.add(req.getCampaignName());
		paramObjects.add(req.getCampaignVersion());
		paramObjects.add(req.getStartDate());
		paramObjects.add(req.getEndDate());
		
		for(int i = 0; i < numberOfPromptIds; i++) {
			paramObjects.add(promptIds[i]);
		}
		
		for(int i = 0; i < numberOfMetadataPoints; i++) {
			paramObjects.add(metadataPromptIds.get(i));
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
}
