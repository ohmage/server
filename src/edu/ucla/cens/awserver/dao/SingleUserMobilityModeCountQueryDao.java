package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MobilityModeCountQueryResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for finding the aggregate number of mobility modes per day for a particular user.
 * 
 * @author selsky
 */
public class SingleUserMobilityModeCountQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SingleUserMobilityModeCountQueryDao.class);
	
	private String _sql = "SELECT mode, DATE(time_stamp), COUNT(*)" +
 					      " FROM mobility_mode_only_entry, user" +
						  " WHERE user_id = ?" +
					      " AND user.id = user_id" +
					      " AND DATE(time_stamp) >= ? " +
					      " AND DATE(time_stamp) < ?" +
					      " GROUP BY DATE(time_stamp), mode" +
					      " ORDER BY DATE(time_stamp)";
	
	public SingleUserMobilityModeCountQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * For the user found in the provided AwRequest, dispatch to executeSqlForUser().
	 */
	@Override
	public void execute(AwRequest awRequest) {
		User user = awRequest.getUser();
		
		List<MobilityModeCountQueryResult> results 
			= executeSqlForUser(user.getId(),
					            user.getUserName(), 
					            awRequest.getStartDate(), 
					            awRequest.getEndDate());
		
		awRequest.setResultList(results);
	}
	
	/**
	 * Runs SQL to find the mobility mode counts based on the provided parameters.
	 */
	protected List<MobilityModeCountQueryResult> executeSqlForUser(int userId, final String userName, String startDate, String endDate) { 
		
		try {
			
			@SuppressWarnings("unchecked")
			List<MobilityModeCountQueryResult> results = getJdbcTemplate().query(_sql, new Object[] {userId, startDate, endDate}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						MobilityModeCountQueryResult result = new MobilityModeCountQueryResult();
						
						result.setUserName(userName);
						result.setMode(rs.getString(1));
						result.setDate(rs.getDate(2).toString());
						result.setCount(rs.getInt(3));
						
						if(_logger.isDebugEnabled()) {
							_logger.debug(result);
						}
						
						return result;
					}
				}
			);
			
			if(results.size() == 0) {
				
				MobilityModeCountQueryResult result = new MobilityModeCountQueryResult();
				result.setUserName(userName);
				result.setEmpty(true);
				results.add(result);
			} 
			
			return results;
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + userId + ", " + startDate + ", " + endDate);
			throw new DataAccessException(dae.getMessage());
			
		}
		
	}
}
