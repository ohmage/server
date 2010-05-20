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
 * @author selsky
 */
public class SingleUserPromptGroupCountQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MultiUserPromptGroupCountQueryDao.class);
	
	private String _sql = "select campaign_prompt_group_id, date(time_stamp), count(*)" +
					      " from prompt_response, prompt, campaign_prompt_group, user" +
						  " where prompt_response.prompt_id = prompt.id" +
						  " and campaign_id = ?" +
						  " and prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
					      " and user_id = ?" +
					      " and user.id = user_id" +
					      " and date(time_stamp) between ? and ?" +
					      " group by campaign_prompt_group_id, date(time_stamp)" +
					      " order by login_id, date(time_stamp), campaign_prompt_group_id";
	
	public SingleUserPromptGroupCountQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * 
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
