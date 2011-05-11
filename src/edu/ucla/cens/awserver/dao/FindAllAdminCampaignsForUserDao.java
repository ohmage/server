package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.cache.CampaignRoleCache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class FindAllAdminCampaignsForUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllCampaignsForUserDao.class);
	private boolean _useLoggedInUser;
	
	private String _sql = "SELECT c.urn " +
			              "FROM campaign c, user_role_campaign urc, user u, user_role ur " +
			              "WHERE urc.campaign_id = c.id " +
			              "AND urc.user_id = u.id AND u.login_id = ? " +
			              "AND urc.user_role_id =  ur.id " +
			              "AND ur.role in ('" + CampaignRoleCache.ROLE_SUPERVISOR + "')"; // for now, assume supervisor.
	
	/**
	 * @param useLoggedInUser if true, the logged in user's user name will be used in the query. if false, the user name request
	 * parameter will be used (e.g., the data point API). 
	 */
	public FindAllAdminCampaignsForUserDao(DataSource dataSource, boolean useLoggedInUser) {
		super(dataSource);
		_useLoggedInUser = useLoggedInUser;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_sql, 
					_useLoggedInUser ? new Object[] { awRequest.getUser().getUserName() } 
									 : new Object[] { awRequest.getUserNameRequestParam() },
					new SingleColumnRowMapper())
			);
		}	
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + _sql + "' with the parameter"
				+ (_useLoggedInUser ? awRequest.getUser().getUserName() : awRequest.getUserNameRequestParam()), dae);
			throw new DataAccessException(dae);
		}
	}
}
