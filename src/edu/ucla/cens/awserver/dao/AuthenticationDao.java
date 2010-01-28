package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Performs a lookup against the database to check user existence within a particular campaign.
 * 
 * @author selsky
 */
public class AuthenticationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(AuthenticationDao.class);
	
	// Note that the user role is not checked - future feature
	// Also, in the future, we may have users that belong to multiple campaigns in which case
	// the authentication flow will have to be rethought a bit.
	private static final String _selectSql = "select user.id, user.enabled, campaign.id from user, user_role_campaign, campaign " +
			                                 "where user.login_id = ? " +
			                                     "and user.id = user_role_campaign.user_id " +
			                                     "and user_role_campaign.id = campaign.id " +
			                                     "and campaign.subdomain = ?";
	/**
	 * Creates an instance of this class that will use the supplied DataSource for data retrieval.
	 */
	public AuthenticationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Checks the db for the existence of a user represented by a user name and a subdomain found in the AwRequest. Places the 
	 * query results into LoginResult objects.
	 */
	public void execute(AwRequest request) {
		_logger.info("attempting login for user " + request.getUser().getUserName());
		
		try {
			
			request.setAttribute("results", getJdbcTemplate().query(_selectSql, 
					             new Object[]{request.getUser().getUserName(), request.getAttribute("subdomain")}, 
					             new QueryRowMapper()));
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}

	/**
	 * Maps each row from a query ResultSet to a LoginResult. Used by JdbcTemplate in call-back fashion. 
	 * 
	 * @author selsky
	 */
	public class QueryRowMapper implements RowMapper {
		
		public Object mapRow(ResultSet rs, int rowNum) throws SQLException { // The Spring classes will wrap this exception
			                                                                 // in a Spring DataAccessException
			LoginResult lr = new LoginResult();
			lr.setUserId(rs.getInt(1));
			lr.setEnabled(rs.getBoolean(2));
			lr.setCampaignId(rs.getInt(3));
			return lr;
		}
	}
	
	/**
	 * Container used for query results.
	 * 
	 * @author selsky
	 */
	public class LoginResult {
		private int _campaignId;
		private int _userId;
		private boolean _enabled;
		
		public int getCampaignId() {
			return _campaignId;
		}
		public void setCampaignId(int campaignId) {
			_campaignId = campaignId;
		}
		public int getUserId() {
			return _userId;
		}
		public void setUserId(int userId) {
			_userId = userId;
		}
		public boolean isEnabled() {
			return _enabled;
		}
		public void setEnabled(boolean enabled) {
			_enabled = enabled;
		}
	}
}
