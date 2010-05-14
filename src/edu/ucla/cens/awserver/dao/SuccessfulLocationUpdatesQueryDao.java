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
public class SuccessfulLocationUpdatesQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SuccessfulLocationUpdatesQueryDao.class);
	
	private String singleUserMobilitySuccessSql = "select count(*)" +
					                              " from mobility_mode_only_entry" +
					                              " where user_id = ?" +
					                              " and date(time_stamp) between date((now() - 1)) and date(now())" +
					                              " and latitude is not NULL" +
					                              " and longitude is not NULL";      
	
	private String singleUserPromptResponseSuccessSql = "select count(*)" +
   											            " from prompt_response" +
												        " where user_id = ?" +
												        " and date(time_stamp) between date(now() - 1) and date(now())" +
												        " and latitude is not NULL" +
												        " and longitude is not NULL";      
	
	private String singleUserMobilityTotalSql = "select count(*)" +
   							                    " from mobility_mode_only_entry" +
										        " where user_id = ? " +
										        " and date(time_stamp) between date(now() - 1) and date(now())";
	
	private String singleUserPromptResponseTotalSql = "select count(*)" +
                                                      " from prompt_response" +
			                                          " where user_id = ? " +
			                                          " and date(time_stamp) between date(now() - 1) and date(now())";
	
	/**
	 * Points this DAO at the provided DataSource.
	 * 
	 * @throws IllegalArgumentException if the provided DataSource is null
	 */
	public SuccessfulLocationUpdatesQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Calculates the percentage of successful location updates for the user id found in the provided AwRequest.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		List<UserPercentage> percentList = new ArrayList<UserPercentage>();
		executeSqlForUser(awRequest.getUser().getId(), awRequest.getUser().getUserName(), percentList);
		awRequest.setResultList(percentList);
	}
	
	/**
	 * TODO the in-line percentage calculation should be moved into a service so this class contains db logic only 
	 */
	protected void executeSqlForUser(int userId, String userName, List<UserPercentage> outputList) {
		String currentSql = null;
		
		try {
			double totalSuccess = 0d;
			double total = 0d;
			Object[] paramArray = {userId}; // JdbcTemplate.queryForInt requires an Object array for filling in the underlying
			                                // PreparedStatement
			
			currentSql = singleUserMobilityTotalSql;
			total += getJdbcTemplate().queryForInt(singleUserMobilityTotalSql, paramArray);
			
			currentSql = singleUserPromptResponseTotalSql;
			total += getJdbcTemplate().queryForInt(singleUserPromptResponseTotalSql, paramArray); 
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("total: " + total);
			}
			
			if(0 == total) {
				
				outputList.add(new UserPercentage(userName, 0d));
				
			} else  {
			
				currentSql = singleUserMobilitySuccessSql; 
				totalSuccess += getJdbcTemplate().queryForInt(singleUserMobilitySuccessSql, paramArray);
				
				currentSql = singleUserPromptResponseSuccessSql;
				totalSuccess += getJdbcTemplate().queryForInt(singleUserPromptResponseSuccessSql, paramArray);
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("totalSucess: " + totalSuccess);
				}
				
				outputList.add(new UserPercentage(userName, (totalSuccess / total)));
			}
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("percentage: " + outputList.get(0));
			}
			
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
