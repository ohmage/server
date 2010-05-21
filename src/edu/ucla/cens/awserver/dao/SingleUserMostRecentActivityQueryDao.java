package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MobilityActivityQueryResult;
import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.domain.PromptActivityQueryResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for looking up the most recent prompt response and mobility uploads for a particular user.
 * 
 * @author selsky
 */
public class SingleUserMostRecentActivityQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SingleUserMostRecentActivityQueryDao.class);
	
	private String _promptResponseSql = "SELECT DISTINCT time_stamp, prompt_response.phone_timezone" +
									    " FROM prompt_response, prompt, campaign_prompt_group" +
									    " WHERE prompt_response.prompt_id = prompt.id" +
									    " AND prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
									    " AND campaign_id = ?" +
									    " AND prompt_response.user_id = ?" +
									    " AND prompt_response.time_stamp = " +
									    "  (SELECT MAX(time_stamp) FROM prompt_response WHERE user_id = ?)";
	
	private String _mobilityUploadSql = "SELECT DISTINCT time_stamp, phone_timezone" +
							            " FROM mobility_mode_only_entry" +
							            " WHERE user_id = ?" +
							            " AND time_stamp = " +
							            "  (SELECT MAX(time_stamp) FROM mobility_mode_only_entry WHERE user_id = ?)";
	
	public SingleUserMostRecentActivityQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Finds the currently logged-in user and dispatches to executeSqlForSingleUser().
	 */
	@Override
	public void execute(AwRequest awRequest) {
		List<MostRecentActivityQueryResult> results = new ArrayList<MostRecentActivityQueryResult>();
		User user = awRequest.getUser();
		results.add(executeSqlForSingleUser(Integer.parseInt(user.getCurrentCampaignId()), user.getId(), user.getUserName()));
		awRequest.setResultList(results);
	}
	
	/**
	 * Runs queries for find the most recent prompt and mobility activities for the user described by the provided parameters. 
	 */
	protected MostRecentActivityQueryResult executeSqlForSingleUser(int campaignId, int userId, final String userName) {
		String currentSql = null; // used for logging messages
		
		try {
			
			MostRecentActivityQueryResult result = new MostRecentActivityQueryResult();
			currentSql = _promptResponseSql;
			
			List<?> results = 
				getJdbcTemplate().query(_promptResponseSql, new Object[] {campaignId, userId, userId}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						PromptActivityQueryResult result = new PromptActivityQueryResult(); 
						result.setUserName(userName);
						result.setPromptTimestamp(rs.getTimestamp(1));
						result.setPromptTimezone(rs.getString(2));
						return result;
						
					}
				}
			);
			
			if(results.size() != 0) {
				
				result.setPromptActivityQueryResult((PromptActivityQueryResult) results.get(0));
			}
			
			
			currentSql = _mobilityUploadSql;
			
			results = getJdbcTemplate().query(_mobilityUploadSql, new Object[] {userId, userId}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MobilityActivityQueryResult result = new MobilityActivityQueryResult(); 
						result.setUserName(userName);
						result.setMobilityTimestamp(rs.getTimestamp(1));
						result.setMobilityTimezone(rs.getString(2));
						return result;
						
					}
				}
			);
			
			if(results.size() != 0) {
				
				result.setMobilityActivityQueryResult((MobilityActivityQueryResult) results.get(0));
			}
			
			if(null == result.getMobilityActivityQueryResult() && null == result.getPromptActivityQueryResult()) {
				result.setUserName(userName);
			}
			
			return result;
			
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + currentSql + "' Params: " + campaignId + ", " + userId);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
