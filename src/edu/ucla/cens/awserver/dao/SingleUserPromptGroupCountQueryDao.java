package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.SingleUserPromptGroupCountQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class SingleUserPromptGroupCountQueryDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(MultiUserPromptGroupCountQueryDao.class);
	
	private String _sql = "select count(*), campaign_prompt_group_id, date(time_stamp)" +
					      " from prompt_response, prompt, campaign_prompt_group" +
						  " where prompt_response.prompt_id = prompt.id" +
						  " and campaign_id = ?" +
						  " and prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
					      " and user_id = ?" +
					      " and date(time_stamp) between ? and ?" +
					      " group by user_id, campaign_prompt_group_id, date(time_stamp)";
	
	public SingleUserPromptGroupCountQueryDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String s = null, e = null;
		int u = -1, c = -1;
		
		try {
			s = awRequest.getStartDate();
			e = awRequest.getEndDate();
			u = awRequest.getUser().getId();
			c = Integer.parseInt(awRequest.getUser().getCurrentCampaignId());

			awRequest.setResultList(
				getJdbcTemplate().query(_sql, new Object[] {c, u, s, e}, new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						SingleUserPromptGroupCountQueryResult result = new SingleUserPromptGroupCountQueryResult();
						result.setCount(rs.getInt(1));
						result.setCampaignPromptGroupId(rs.getInt(2));
						result.setDate(rs.getDate(3).toString());
						return result;
					}
				})
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error(dae.getMessage() + " SQL: '" + _sql + "' Params: " + c + ", " +  u + ", " + s + ", " + e);
			throw new DataAccessException(dae.getMessage());
			
		}
	}
}
