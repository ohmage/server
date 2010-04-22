package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
 
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for executing EMA (prompt/survey response) query for visualization.
 * 
 * @author selsky
 */
public class EmaQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(EmaQueryDao.class);
	
	// The visualizations use a js 'config' file for interpreting each prompt response's data 
	// type. The type is identified by prompt.prompt_config_id (the phone's prompt id) and 
	// campaign_prompt_group.group_id (the phone's group id).
	private String _selectSql = "select prompt_response.json_data, prompt_response.phone_timezone," +
			                    " prompt_response.utc_time_stamp, prompt.prompt_config_id, " +
			                    " campaign_prompt_group.group_id" +
			                    " from prompt_response, prompt, campaign_prompt_group, campaign" +
			                    " where prompt_response.utc_time_stamp >= timestamp(?)" +
			                    " and prompt_response.utc_time_stamp <= timestamp(?)" +
			                    " and prompt_response.user_id = ?" +
			                    " and prompt_response.prompt_id = prompt.id" +
			                    " and prompt.campaign_prompt_group_id = campaign_prompt_group.id " +
			                    " and campaign_prompt_group.campaign_id = ?" +
			                    " order by prompt_response.utc_time_stamp";
	
	/**
	 * Creates an instance of this class using the provided DataSource as the method of data access.
	 */
	public EmaQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Executes the visualization query and places the list of results into the AwRequest with the key emaQueryResults.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("executing ema viz query");
		String s = null, e = null;
		int u = -1, c = -1;
		
		try {
			s = awRequest.getStartDate();
			e = awRequest.getEndDate();
			u = awRequest.getUser().getId();
			c = Integer.parseInt(awRequest.getUser().getCurrentCampaignId());
			
			List<?> l = getJdbcTemplate().query(_selectSql, new Object[]{s, e, u, c}, new EmaQueryRowMapper());
			
			awRequest.setResultList(l);
			
			_logger.info("found " + l.size() + " query results");
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" + _selectSql + "' with the following parameters: " + 
					s + ", " + e + ", " + u + ", " + c);
			
			throw new DataAccessException(dae); // wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception
		}
	}
	
	public class EmaQueryRowMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
		                                                       	             // in a Spring DataAccessException
			EmaQueryResult result = new EmaQueryResult();
			result.setJsonData(rs.getString(1));
			result.setTimezone(rs.getString(2));
			result.setTimestamp(rs.getTimestamp(3).toString());
			result.setPromptConfigId(rs.getInt(4));
			result.setPromptGroupId(rs.getInt(5));
			return result;
		}
	}
	
	public class EmaQueryResult {
		private String _jsonData;
		private String _timezone;
		private String _timestamp;
		private int _promptConfigId;
		private int _promptGroupId;
		
		public int getPromptConfigId() {
			return _promptConfigId;
		}
		public void setPromptConfigId(int promptConfigId) {
			_promptConfigId = promptConfigId;
		}
		public int getPromptGroupId() {
			return _promptGroupId;
		}
		public void setPromptGroupId(int promptGroupId) {
			_promptGroupId = promptGroupId;
		}
		public String getJsonData() {
			return _jsonData;
		}
		public void setJsonData(String jsonData) {
			_jsonData = jsonData;
		}
		public String getTimezone() {
			return _timezone;
		}
		public void setTimezone(String timezone) {
			_timezone = timezone;
		}
		public String getTimestamp() {
			return _timestamp;
		}
		public void setTimestamp(String timestamp) {
			_timestamp = timestamp;
		}
	}
}
