package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.domain.UserPercentage;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for counting the number of successful location updates for the previous 24 hours (based on server-time) for a particular
 * user or a group of users. 
 * 
 * A successful location update is defined by non-null latitude and longitude values in the prompt_response and 
 * mobility_mode_only_entry tables.
 * 
 * @author selsky
 */
public class SingleUserSuccessfulLocationUpdatesQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SingleUserSuccessfulLocationUpdatesQueryDao.class);
	
	private String _mobilitySuccessSql = "SELECT COUNT(*)" +
			                              " FROM mobility_mode_only_entry" +
			                              " WHERE user_id = ?" +
			                              " AND DATE(time_stamp) BETWEEN DATE((now() - 1)) and DATE(now())" +
			                              " AND latitude is not NULL" +
			                              " AND longitude is not NULL";      
	
	private String _promptResponseSuccessSql = "SELECT COUNT(*)" +
								               " FROM prompt_response, prompt, campaign_prompt_group" +
									           " WHERE user_id = ?" +
									           " AND campaign_id = ?" +
									           " AND prompt_response.prompt_id = prompt.id" +
									           " AND prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
									           " AND date(time_stamp) BETWEEN DATE(now() - 1) and DATE(now())" +
									           " AND latitude is not NULL" +
									           " AND longitude is not NULL";      
	
	private String _mobilityTotalSql = "SELECT COUNT(*)" +
				                       " FROM mobility_mode_only_entry" +
							           " WHERE user_id = ? " +
							           " AND DATE(time_stamp) BETWEEN DATE(now() - 1) and DATE(now())";

	private String _promptResponseTotalSql = "SELECT COUNT(*)" +
                                             " FROM prompt_response, prompt, campaign_prompt_group" +
                                             " WHERE user_id = ? " +
                                             " AND campaign_id = ?" +
									         " AND prompt_response.prompt_id = prompt.id" +
									         " AND prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
                                             " AND DATE(time_stamp) BETWEEN DATE(now() - 1) and DATE(now())";
	
	/**
	 * Points this DAO at the provided DataSource by passing into the super class.
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
		User user = awRequest.getUser();
		outputList.add(executeSqlForUser(Integer.parseInt(user.getCurrentCampaignId()), user.getId(), user.getUserName()));
		awRequest.setResultList(outputList);
	}
	
	/**
	 * Finds the percentage of successful location updates for the user described by the provided parameters.
	 * 
	 * TODO the in-line percentage calculation should be moved into a service so this class contains db logic only 
	 */
	protected UserPercentage executeSqlForUser(int campaignId, int userId, String userName) {
		String currentSql = null;
		UserPercentage userPercentage = null;
		
		try {
			double totalSuccess = 0d;
			double total = 0d;
			
			// PreparedStatement params
			Object[] mobilityParamArray = {userId}; 
			Object[] promptParamArray = {userId, campaignId};
			
			currentSql = _mobilityTotalSql;
			total += getJdbcTemplate().queryForInt(_mobilityTotalSql, mobilityParamArray);
			
			currentSql = _promptResponseTotalSql;
			total += getJdbcTemplate().queryForInt(_promptResponseTotalSql, promptParamArray); 
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("total: " + total);
			}
			
			if(0 == total) {
				
				userPercentage = new UserPercentage(userName, 0d);
				
			} else  {
			
				currentSql = _mobilitySuccessSql; 
				totalSuccess += getJdbcTemplate().queryForInt(_mobilitySuccessSql, mobilityParamArray);
				
				currentSql = _promptResponseSuccessSql;
				totalSuccess += getJdbcTemplate().queryForInt(_promptResponseSuccessSql, promptParamArray);
				
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
