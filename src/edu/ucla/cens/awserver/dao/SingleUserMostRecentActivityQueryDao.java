package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.MostRecentActivityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for looking up the most recent prompt response and mobility uploads for a particular user.
 * 
 * @author selsky
 */
public class SingleUserMostRecentActivityQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MultiUserMostRecentActivityQueryDao.class);
	
	private String _sql = "select login_id, max(prompt_response.time_stamp), prompt_response.phone_timezone," +
			              " max(mobility_mode_only_entry.time_stamp), mobility_mode_only_entry.phone_timezone" +
			              " from prompt_response, prompt, campaign_prompt_group, user, mobility_mode_only_entry" +
			              " where prompt_response.prompt_id = prompt.id" +
			              " and campaign_id = ?" +
			              " and user_id = ?" +
			              " and prompt_response.user_id = user.id" +
			              " and mobility_mode_only_entry.user_id = user.id" +
			              " group by prompt_response.user_id" +
			              " order by prompt_response.user_id";
	
	public SingleUserMostRecentActivityQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		int u = -1, c = -1;
		
		try {
			
			c = Integer.parseInt(awRequest.getUser().getCurrentCampaignId());
			u = awRequest.getUser().getId();

			awRequest.setResultList(
				getJdbcTemplate().query(_sql, new Object[] {c, u}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						
						MostRecentActivityQueryResult result = new MostRecentActivityQueryResult(); 
						result.setUserName(rs.getString(1));
						result.setPromptResponseTimestamp(rs.getTimestamp(2));
						result.setPromptTimezone(rs.getString(3));
						result.setMobilityTimestamp(rs.getTimestamp(4));
						result.setMobilityTimezone(rs.getString(5));
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
