package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class MultiUserMostRecentSurveyQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MultiUserMostRecentSurveyQueryDao.class);
	
	private String _sql = "select login_id, max(time_stamp), phone_timezone" +
		                  " from prompt_response, prompt, campaign_prompt_group, user" +
		                  " where prompt_response.prompt_id = prompt.id" +
		                  " and campaign_id = ? " +
		                  " and prompt_response.user_id = user.id " +
		                  " group by user_id " +
		                  " order by user_id";
	
	public MultiUserMostRecentSurveyQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		int c = -1;
		
		try {
			
			c = Integer.parseInt(awRequest.getUser().getCurrentCampaignId());

			awRequest.setResultList(
				getJdbcTemplate().query(_sql, new Object[] {c}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MostRecentActivityQueryResult result = new MostRecentActivityQueryResult(); 
						result.setUserName(rs.getString(1));
						result.setTimestamp(rs.getTimestamp(2));
						result.setTimezone(rs.getString(3));
						return result;
						
					}
				})
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Param: " + c);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
