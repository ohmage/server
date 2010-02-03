package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * DAO for executing EMA (prompt/survey response) query for visualization.
 * 
 * @author selsky
 */
public class EmaQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(EmaQueryDao.class);
	
	// if the User object was placed into the session, a join could be eliminated here
	private String _selectSql = "select prompt_response.json_data, prompt_response.phone_timezone, prompt_response.utc_time_stamp" +
			                    " from prompt_response, user" +
			                    " where prompt_response.utc_time_stamp >= timestamp(?)" +
			                    " and prompt_response.utc_time_stamp <= timestamp(?)" +
			                    " and prompt_response.user_id = user.id" +
			                    " and user.login_id = ?" +
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
		
		try {
			String s = (String) awRequest.getAttribute("startDate");
			String e = (String) awRequest.getAttribute("endDate");
			String u = awRequest.getUser().getUserName();
			
			awRequest.setAttribute("emaQueryResults", 
				getJdbcTemplate().query(
					_selectSql, 
					new Object[]{s, e, u},  	
				    new EmaQueryRowMapper())
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
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
			return result;
		}
	}
	
	public class EmaQueryResult {
		private String _jsonData;
		private String _timezone;
		private String _timestamp;
		
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
