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
 * DAO for finding all of the user roles and campaign metadata for a particular user.
 * 
 * @author Joshua Selsky
 */
public class UserRoleCampaignPopulationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleCampaignPopulationDao.class);
	
	private static final String _selectSql = "SELECT campaign.urn, campaign.name, campaign.description, campaign.running_state,"
			                                 + " campaign.privacy_state, campaign.creation_timestamp, user_role_campaign.user_role_id,"
			                                 + " user_role.role"
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
							result.setCampaignName(rs.getString(2));
							result.setCampaignDescription(rs.getString(3));
							result.setCampaignRunningState(rs.getString(4));
							result.setCampaignPrivacyState(rs.getString(5));
							
							
							String ts = rs.getString(6); // this will return the timestamp in JDBC escape format (ending with nanoseconds)
                                                         // and the nanoseconds value is not needed, so shave it off
                                                         // it is weird to be formatting the data inside the DAO here, but the nanoseconds
                                                         // aren't even *stored* in the db, they are appended to the string during
                                                         // whatever conversion the MySQL JDBC connector does when it converts the db's
  	                                                     // timestamp to a Java String.
							if(ts.contains(".")) {
								
								int indexOfDot = ts.indexOf(".");
								result.setCampaignCreationTimestamp(ts.substring(0, indexOfDot));
								
							} else {
								
								result.setCampaignCreationTimestamp(ts);
							}
							
							result.setUserRole(new UserRoleImpl(rs.getInt(7), rs.getString(8)));
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
