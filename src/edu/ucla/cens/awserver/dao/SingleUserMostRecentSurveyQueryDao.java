package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MostRecentSurveyActivityQueryResult;
import edu.ucla.cens.awserver.domain.User;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class SingleUserMostRecentSurveyQueryDao extends AbstractDao {

private static Logger _logger = Logger.getLogger(SingleUserMostRecentSurveyQueryDao.class);
	
	private String _sql = "select max(time_stamp), phone_timezone" +
		                  " from prompt_response, prompt, campaign_prompt_group" +
		                  " where prompt_response.prompt_id = prompt.id" +
		                  " and prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
		                  " and campaign_id = ?" +
		                  " and user_id = ?" +
		                  " group by user_id";
	
	public SingleUserMostRecentSurveyQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<MostRecentSurveyActivityQueryResult> results = new ArrayList<MostRecentSurveyActivityQueryResult> ();
		User user = awRequest.getUser();
		results.add(executeSqlForUser(Integer.parseInt(user.getCurrentCampaignId()), user.getId(), user.getUserName()));
		awRequest.setResultList(results);
	}	
	
	protected MostRecentSurveyActivityQueryResult executeSqlForUser(int campaignId, int userId, final String userName) {
		try {
				
			List<?> results = 
				getJdbcTemplate().query(_sql, new Object[] {campaignId, userId}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MostRecentSurveyActivityQueryResult result = new MostRecentSurveyActivityQueryResult(); 
						result.setUserName(userName);
						result.setTimestamp(rs.getTimestamp(1));
						result.setTimezone(rs.getString(2));
						return result;
					}
			});
			
			if(results.size() == 0) {
				
				MostRecentSurveyActivityQueryResult result = new MostRecentSurveyActivityQueryResult();
				result.setUserName(userName);
				result.setTimestamp(null);
				result.setTimezone(null);
				return result;
				
			} else {
				
				return (MostRecentSurveyActivityQueryResult) results.get(0);
			}
			
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + campaignId + ", " + userId);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
