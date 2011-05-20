package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignUrnUserRole;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class FindAllCampaignRolesForUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllCampaignRolesForUserDao.class);
	private boolean _useLoggedInUser;
	
	private String _sql = "SELECT ur.role, c.urn " +
			              "FROM user_role_campaign urc, user u, user_role ur, campaign c " +
			              "WHERE urc.campaign_id = c.id " +
			                "AND urc.user_id = u.id " +
			                "AND ur.id = urc.user_role_id " +
			                "AND u.login_id = ?";
	
	/**
	 * @param useLoggedInUser if true, the logged in user's user name will be used in the query. if false, the user name request
	 * parameter will be used (e.g., the data point API). 
	 */
	public FindAllCampaignRolesForUserDao(DataSource dataSource, boolean useLoggedInUser) {
		super(dataSource);
		_useLoggedInUser = useLoggedInUser;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("lookup up campaign roles for " + 
			(_useLoggedInUser ? awRequest.getUser().getUserName() : awRequest.getUserNameRequestParam()));
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					_useLoggedInUser ? new Object[] { awRequest.getUser().getUserName() } 
									 : new Object[] { awRequest.getUserNameRequestParam() },
					new RowMapper() { 
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							CampaignUrnUserRole cuur = new CampaignUrnUserRole();
							cuur.setRole(rs.getString(1));
							cuur.setUrn(rs.getString(2));
							//_logger.info(cuur);
							return cuur;
						}
					}
				)
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter"
				+ (_useLoggedInUser ? awRequest.getUser().getUserName() : awRequest.getUserNameRequestParam()), dae);
			throw new DataAccessException(dae);
		}
	}
}
