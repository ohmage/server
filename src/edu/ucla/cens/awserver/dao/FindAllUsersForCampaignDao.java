package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.SimpleUser;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * 
 * @author selsky
 */
public class FindAllUsersForCampaignDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllUsersForCampaignDao.class);
	
	private String findAllUsersForCampaignSql = "select distinct user_id, login_id" + // distinct is used here in order to avoid 
												" from user_role_campaign, user" +    // multiple rows returned for users with 
 												" where campaign_id = ? " +           // multiple roles
												" and user_id = user.id";
	
	public FindAllUsersForCampaignDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Returns all of the distinct user ids for the current campaign id of the user in the AwRequest.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
		
			awRequest.setResultList(
				getJdbcTemplate().query(
					findAllUsersForCampaignSql,
					new Object[] {awRequest.getUser().getCurrentCampaignId()},
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							SimpleUser su = new SimpleUser();
							su.setId(rs.getInt(1));
							su.setUserName(rs.getString(2));
							return su;
						}
					}
				)
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("an error occurred running the following SQL '" + findAllUsersForCampaignSql + "' with the parameter " + 
				awRequest.getUser().getCurrentCampaignId() + ": " + dae.getMessage());
			
			throw new DataAccessException(dae);
			
		}
	}
}
