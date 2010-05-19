package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.domain.UserPercentage;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for counting the number of successful location updates for the previous day for a particular user or a group of users if the 
 * current application user is a researcher or admin. 
 * 
 * A successful location update is defined by non-null latitude and longitude values in the prompt_response and 
 * mobility_mode_only_entry tables.
 * 
 * @author selsky
 */
public class SingleUserSuccessfulLocationUpdatesQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SingleUserSuccessfulLocationUpdatesQueryDao.class);
	
	private String _mobilitySuccessSql = "select count(*)" +
			                              " from mobility_mode_only_entry" +
			                              " where user_id = ?" +
			                              " and date(time_stamp) between date((now() - 1)) and date(now())" +
			                              " and latitude is not NULL" +
			                              " and longitude is not NULL";      
	
	// FIXME - both the prompt SQL statements have bugs: they need to use the campaign id in order to not 
	// select prompt responses across multiple campaigns for one user
	
	private String _promptResponseSuccessSql = "select count(*)" +
								               " from prompt_response" +
									           " where user_id = ?" +
									           " and date(time_stamp) between date(now() - 1) and date(now())" +
									           " and latitude is not NULL" +
									           " and longitude is not NULL";      
	
	private String _mobilityTotalSql = "select count(*)" +
				                       " from mobility_mode_only_entry" +
							           " where user_id = ? " +
							           " and date(time_stamp) between date(now() - 1) and date(now())";

	private String _promptResponseTotalSql = "select count(*)" +
                                             " from prompt_response" +
                                             " where user_id = ? " +
                                             " and date(time_stamp) between date(now() - 1) and date(now())";
	
	/**
	 * Points this DAO at the provided DataSource.
	 * 
	 * @throws IllegalArgumentException if the provided DataSource is null
	 */
	public SingleUserSuccessfulLocationUpdatesQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Calculates the percentage of successful location updates for the user id found in the provided AwRequest.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		List<UserPercentage> outputList = new ArrayList<UserPercentage>();
		outputList.add(executeSqlForUser(awRequest.getUser().getId(), awRequest.getUser().getUserName()));
		awRequest.setResultList(outputList);
	}
	
	/**
	 * TODO the in-line percentage calculation should be moved into a service so this class contains db logic only 
	 */
	protected UserPercentage executeSqlForUser(int userId, String userName) {
		String currentSql = null;
		UserPercentage userPercentage = null;
		
		try {
			double totalSuccess = 0d;
			double total = 0d;
			Object[] paramArray = {userId}; // JdbcTemplate.queryForInt requires an Object array for filling in the underlying
			                                // PreparedStatement
			
			currentSql = _mobilityTotalSql;
			total += getJdbcTemplate().queryForInt(_mobilityTotalSql, paramArray);
			
			currentSql = _promptResponseTotalSql;
			total += getJdbcTemplate().queryForInt(_promptResponseTotalSql, paramArray); 
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("total: " + total);
			}
			
			if(0 == total) {
				
				userPercentage = new UserPercentage(userName, 0d);
				
			} else  {
			
				currentSql = _mobilitySuccessSql; 
				totalSuccess += getJdbcTemplate().queryForInt(_mobilitySuccessSql, paramArray);
				
				currentSql = _promptResponseSuccessSql;
				totalSuccess += getJdbcTemplate().queryForInt(_promptResponseSuccessSql, paramArray);
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("totalSucess: " + totalSuccess);
				}
				
				userPercentage = new UserPercentage(userName, (totalSuccess / total));
			}
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("user-percentage: " + userPercentage);
			}
			
			return userPercentage;
			
		} catch (IncorrectResultSizeDataAccessException irsdae) { // thrown if queryForInt returns more than one row which means 
			                                                      // there is a logical error in the SQL being run
			
			_logger.error("an incorrect number of rows was returned by '" + currentSql + "' with parameter " + userId);
			throw new DataAccessException(irsdae);
			
		} catch (org.springframework.dao.DataAccessException dae) { // thrown for general SQL errors
			
			_logger.error("an error was encountered when executing the following SQL: " + currentSql + " with paramter " + userId);
			throw new DataAccessException(dae);
			
		}
	}
}
