package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MostRecentSurveyActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class SingleUserMostRecentSurveyQueryDao extends AbstractDao {

private static Logger _logger = Logger.getLogger(SingleUserMostRecentSurveyQueryDao.class);
	
	private String _sql = "select login_id, max(time_stamp), phone_timezone" +
		                  " from prompt_response, prompt, campaign_prompt_group, user" +
		                  " where prompt_response.prompt_id = prompt.id" +
		                  " and campaign_id = ?" +
		                  " and user_id = ?" +
		                  " and prompt_response.user_id = user.id" +
		                  " group by user_id";
	
	public SingleUserMostRecentSurveyQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		int c = -1, u = -1;
		
		try {
			
			c = Integer.parseInt(awRequest.getUser().getCurrentCampaignId());
			u = awRequest.getUser().getId();

			awRequest.setResultList(
				getJdbcTemplate().query(_sql, new Object[] {c, u}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MostRecentSurveyActivityQueryResult result = new MostRecentSurveyActivityQueryResult(); 
						result.setUserName(rs.getString(1));
						result.setTimestamp(rs.getTimestamp(2));
						result.setTimezone(rs.getString(3));
						return result;
						
					}
				})
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + c + ", " + u);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
