package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserRoleCampaignResult;
import edu.ucla.cens.awserver.domain.UserRoleImpl;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author joshua selsky
 */
public class UserRoleCampaignPopulationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleCampaignPopulationDao.class);
	
	private static final String _selectSql = "SELECT campaign.urn, user_role_campaign.user_role_id, user_role.role" 
        									 + " FROM campaign, user, user_role_campaign, user_role"
        									 + " WHERE user.login_id = ?"
        									 +   " AND user.id = user_role_campaign.user_id"
        									 +   " AND campaign.id = user_role_campaign.campaign_id"
        									 +   " AND user_role.id = user_role_campaign.user_role_id";

	public UserRoleCampaignPopulationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Populating user-role-campaign connections in user object in awRequest.");
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_selectSql, 
					new String[] {
					    awRequest.getUser().getUserName()
					}, 
					new RowMapper() {
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleCampaignResult result = new UserRoleCampaignResult();
							
							result.setCampaignUrn(rs.getString(1));
							result.setUserRole(new UserRoleImpl(rs.getInt(2), rs.getString(3)));
							
							return result;
						}
					}
				)
			);
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" + _selectSql + "' with the following parameters: " + 
					awRequest.getUser().getUserName() + " (password omitted)");
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}

}
