package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import jbcrypt.BCrypt;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * DAO for performing user authentication.
 * 
 * @author selsky
 */
public class AuthenticationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(AuthenticationDao.class);
	private String _salt;
	private boolean _performHash;
	
	private static final String _selectSql = "select user.id, user.enabled, user.new_account, campaign.id, campaign.name,"
		                                     +     " user_role_campaign.user_role_id" 
		                                     + " from campaign, user, user_role_campaign"
		                                     + " where user.login_id = ?"
		                                     +   " and user.password = ?"
		                                     +   " and user.id = user_role_campaign.user_id"
		                                     +   " and campaign.id = user_role_campaign.campaign_id";
	
	/**
	 * @param dataSource the data source used to connect to the MySQL db
	 * @param salt the salt to use for password hashing with bcrypt
	 * @param performHash specifies whether hashing should be performed on the inbound password. Passwords sent from the phone
	 * are already hashed. 
	 * 
	 * @throws IllegalArgumentException if the provided salt is empty, null, or all whitespace and performHash is true
	 */
	public AuthenticationDao(DataSource dataSource, String salt, boolean performHash) {
		super(dataSource);
		
		if(performHash) {
			if(StringUtils.isEmptyOrWhitespaceOnly(salt)) {
				throw new IllegalArgumentException("a salt value is required");
			}
		}
		
		_salt = salt;
		_performHash = performHash;
	}
	
	/**
	 * Checks the db for the existence of a user represented by a user name and a subdomain found in the AwRequest. Places the 
	 * query results into LoginResult objects.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("attempting login for user " + awRequest.getUser());
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					_selectSql, 
					new String[] {
					    awRequest.getUser().getUserName(), 
					    _performHash ? BCrypt.hashpw(awRequest.getUser().getPassword(), _salt) : awRequest.getUser().getPassword(), 
					}, 
					new AuthenticationResultRowMapper()
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
