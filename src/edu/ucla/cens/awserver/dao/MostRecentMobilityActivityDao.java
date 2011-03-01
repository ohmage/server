package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserStatsQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserStatsQueryAwRequest;

/**
 * @author selsky
 */
public class MostRecentMobilityActivityDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MostRecentMobilityActivityDao.class);
	
	private String _sql = "SELECT msg_timestamp, phone_timezone"
					    + " FROM mobility_mode_only_entry"
					    + " WHERE upload_timestamp ="
						+  " (SELECT MAX(upload_timestamp)"
						+     " FROM mobility_mode_only_entry m, user u"
						+  	  " WHERE m.user_id = u.id AND u.login_id = ?)";
	
	public MostRecentMobilityActivityDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the most recent survey activity time for awRequest.getUserNameRequestParam().
	 */
	@Override
	public void execute(AwRequest awRequest) {
		UserStatsQueryAwRequest req = (UserStatsQueryAwRequest) awRequest;
		UserStatsQueryResult userStatsQueryResult = null;
		
		final String userId = req.getUserNameRequestParam();
		
		if(null == req.getUserStatsQueryResult()) {
			userStatsQueryResult = new UserStatsQueryResult();
			req.setUserStatsQueryResult(userStatsQueryResult);
		} else {
			userStatsQueryResult = req.getUserStatsQueryResult();
		}
		
		try {
			
			List<?> results = getJdbcTemplate().query(_sql, new Object[] {userId}, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getTimestamp(1).getTime();
				}
			});
			
			if(0 != results.size()) { 
				userStatsQueryResult.setMostRecentMobilityUploadTime((Long) results.get(0));
			}
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Param: " + userId);
			throw new DataAccessException(dae.getMessage());
		}
	}
}
