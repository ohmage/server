package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignPromptGroupItemCount;

/**
 * @author selsky
 */
public class CampaignPromptGroupItemCountDao implements ParameterLessDao {
	private static Logger _logger = Logger.getLogger(CampaignPromptGroupItemCountDao.class);
	private JdbcTemplate _jdbcTemplate;
	private String _sql = "select campaign_id, campaign_prompt_group.id, count(*)" +
			              " from prompt, campaign_prompt_group" +
			              " where prompt.campaign_prompt_group_id = campaign_prompt_group.id" +
			              " group by campaign_prompt_group_id;";
	
	/**
	 * @throws IllegalArgumentException if the provided DataSource is null
	 */
	public CampaignPromptGroupItemCountDao(DataSource dataSource) {
		_jdbcTemplate = new JdbcTemplate(dataSource);
	}
	
	/**
	 * Returns the prompt counts in each campaign_prompt_group  
	 */
	@Override
	public List<?> execute() {
		try {
		
			return _jdbcTemplate.query(_sql, new RowMapper() {
				public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
					int campaignId = rs.getInt(1);
					int groupId = rs.getInt(2);
					int count = rs.getInt(3);
					return new CampaignPromptGroupItemCount(campaignId, groupId, count);
				}
			});
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("an exception occurred running the sql '" + _sql + "' " + dae.getMessage());
			throw new DataAccessException(dae);
			
		}
	}
}
