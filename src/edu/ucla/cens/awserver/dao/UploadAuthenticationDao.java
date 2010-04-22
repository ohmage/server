package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO for performing authentication checks for the data upload feature.
 * 
 * @author selsky
 */
public class UploadAuthenticationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UploadAuthenticationDao.class);
	
	private static final String _selectSql = "select user.id, user.enabled, user.new_account, campaign.id, " +
			                                 "user_role_campaign.user_role_id " +
											 "from user, campaign, user_role_campaign " +
	                                         "where user.login_id = ? " +
	                                         "and user.password = ? " +
	                                         "and campaign.id = ? " +
	                                         "and user_role_campaign.campaign_id = campaign.id " +
	                                         "and user_role_campaign.user_id = user.id";
	
	/**
	 * @param dataSource the data source used to connect to the MySQL db
	 */
	public UploadAuthenticationDao(DataSource dataSource/*, String salt, boolean performHash*/) {
		super(dataSource);
	}
	
	/**
	 * Checks the db for the existence of a user represented by a user name and a subdomain found in the AwRequest. Places the 
	 * query results into LoginResult objects.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("attempting login for user " + awRequest.getUser().getUserName());
		
		try {
			
			awRequest.setResultList(getJdbcTemplate().query(
				_selectSql, 
				new Object [] {
				    awRequest.getUser().getUserName(), 
				    awRequest.getUser().getPassword(),
				    awRequest.getUser().getCurrentCampaignId()
				},
				new AuthenticationResultRowMapper()
			));
			
		} catch (org.springframework.dao.DataAccessException dae) {
			
			_logger.error("caught DataAccessException when running SQL '" + _selectSql + "' with the following parameters: " + 
					awRequest.getUser().getUserName() + " (password omitted)");
			
			throw new DataAccessException(dae); // Wrap the Spring exception and re-throw in order to avoid outside dependencies
			                                    // on the Spring Exception (in case Spring JDBC is replaced with another lib in 
			                                    // the future).
		}
	}
}
