package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.PromptGroupCountQueryResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for finding the number of prompt groups uploaded by a particular user within a time frame.
 * 
 * @author selsky
 */
public class SingleUserPromptGroupCountQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SingleUserPromptGroupCountQueryDao.class);
	
	private String _sql = "SELECT campaign_prompt_group_id, DATE(time_stamp), COUNT(*)" +
					      " FROM prompt_response, prompt, campaign_prompt_group, user" +
						  " WHERE prompt_response.prompt_id = prompt.id" +
						  " AND campaign_id = ?" +
						  " AND prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
					      " AND user_id = ?" +
					      " AND user.id = user_id" +
					      " AND DATE(time_stamp) >= ? " +
					      " AND DATE(time_stamp) < ?" +
					      " GROUP BY campaign_prompt_group_id, DATE(time_stamp)" +
					      " ORDER BY login_id, DATE(time_stamp), campaign_prompt_group_id";
	
	public SingleUserPromptGroupCountQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * For the user found in the provided AwRequest, dispatch to executeSqlForUser().
	 */
	@Override
	public void execute(AwRequest awRequest) {
		User user = awRequest.getUser();
		
		List<PromptGroupCountQueryResult> results 
			= executeSqlForUser(Integer.parseInt(user.getCurrentCampaignId()),
								user.getId(),
					            user.getUserName(), 
					            awRequest.getStartDate(), 
					            awRequest.getEndDate());
		
		awRequest.setResultList(results);
	}
	
	/**
	 * Runs SQL to find the prompt group counts based on the provided parameters.
	 */
	protected List<PromptGroupCountQueryResult> executeSqlForUser(int campaignId, int userId, final String userName, String startDate, String endDate) { 
		
		try {
			
			@SuppressWarnings("unchecked")
			List<PromptGroupCountQueryResult> results = getJdbcTemplate().query(_sql, new Object[] {campaignId, userId, startDate, endDate}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						PromptGroupCountQueryResult result = new PromptGroupCountQueryResult();
						result.setUser(userName);
						result.setCampaignPromptGroupId(rs.getInt(1));
						result.setDate(rs.getDate(2).toString());
						result.setCount(rs.getInt(3));
						return result;
					}
				}
			);
			
			if(results.size() == 0) {
				
				PromptGroupCountQueryResult result = new PromptGroupCountQueryResult();
				result.setUser(userName);
				result.setEmpty(true);
				results.add(result);
			} 
			
			return results;
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + campaignId + ", " +  userId + ", " + startDate + ", " + endDate);
			throw new DataAccessException(dae.getMessage());
			
		}
		
	}
}
