package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for counting the number of successful location updates for the previous day for a particular user. A successful location
 * update is defined by non-null latitude and longitude values in the prompt_response and mobility_mode_only_entry tables.
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
	 */
	public SuccessfulLocationUpdatesQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Calculates the percentage of successful location updates for the user id found in the provided AwRequest.
	 * 
	 * TODO the percentage calculation should be moved into a service so this class contains db logic only
	 */
	@Override
	public void execute(AwRequest awRequest) {
		List<Double> percentList = new ArrayList<Double>();
		String currentSql = null;
		int userId = awRequest.getUser().getId();
		
		try {
			
			double totalSuccess = 0d;
			double total = 0d;
			Object[] paramArray = {userId}; 
			
			currentSql = singleUserMobilityTotalSql;
			total += getJdbcTemplate().queryForInt(singleUserMobilityTotalSql, paramArray);
			
			currentSql = singleUserPromptResponseTotalSql;
			total += getJdbcTemplate().queryForInt(singleUserPromptResponseTotalSql, paramArray); 
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("total: " + total);
			}
			
			if(0 == total) {
				
				percentList.add(0d);
				
			} else  {
			
				currentSql = singleUserMobilitySuccessSql; 
				totalSuccess += getJdbcTemplate().queryForInt(singleUserMobilitySuccessSql, paramArray);
				
				currentSql = singleUserPromptResponseSuccessSql;
				totalSuccess += getJdbcTemplate().queryForInt(singleUserPromptResponseSuccessSql, paramArray);
				
				if(_logger.isDebugEnabled()) {
					_logger.debug("totalSucess: " + totalSuccess);
				}
				
				percentList.add(totalSuccess / total);
			}
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("percentage: " + percentList.get(0));
			}
			
			awRequest.setResultList(percentList);
			
		} catch (IncorrectResultSizeDataAccessException irsdae) { // thrown if queryForInt returns more than one row
			
			_logger.error("an incorrect number of rows was returned by '" + currentSql + "' with parameter " + userId);
			throw new DataAccessException(irsdae);
			
		} catch (org.springframework.dao.DataAccessException dae) { // thrown for general SQL errors
			
			_logger.error("an error was encountered when executing the following SQL: " + currentSql + " with paramter " + userId);
			throw new DataAccessException(dae);
			
		}
	}
}
