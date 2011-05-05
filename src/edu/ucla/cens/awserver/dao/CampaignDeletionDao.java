package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.cache.CampaignRoleCache;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * DAO to delete a campaign and all references to it. It will first check
 * the permissions on the user to ensure that they are allowed to delete it.
 * 
 * This is done in one DAO as opposed to the segregated fashion of most DAOs
 * to mitigate concurrency issues. One example would be if an author attempted
 * to delete their own campaign. This is acceptable as long as no one has
 * uploaded the data yet. Therefore, we would like to delete the campaign as
 * soon as possible after we have assured that there is no data. 
 * 
 * @author John Jenkins
 */
public class CampaignDeletionDao extends AbstractDao {
	private Logger _logger = Logger.getLogger(CampaignDeletionDao.class);
	
	private static final String SQL_GET_IS_AUTHOR = "SELECT count(*) " +
													"FROM user u, user_role ur, user_role_campaign urc, campaign c " +
													"WHERE u.login_id = ? " +
													"AND ur.role = '" + CampaignRoleCache.ROLE_AUTHOR + "' " +
													"AND c.urn = ? " +
													"AND u.id = urc.user_id " +
													"AND ur.id = urc.user_role_id " +
													"AND c.id = urc.campaign_id";
	
	private static final String SQL_GET_IS_SUPERVISOR = "SELECT count(*) " +
														"FROM user u, user_role ur, user_role_campaign urc, campaign c " +
														"WHERE u.login_id = ? " +
														"AND ur.role = " + CampaignRoleCache.ROLE_SUPERVISOR + "' " +
														"AND c.urn = ? " +
														"AND u.id = urc.user_id " +
														"AND ur.id = urc.user_role_id " +
														"AND c.id = urc.campaign_id";
	
	private static final String SQL_GET_RESPONSES_COUNT = "SELECT count(*) " +
														  "FROM campaign c, survey_response sr " +
														  "WHERE c.urn = ? " +
														  "AND c.id = sr.campaign_id";
	
	private static final String SQL_DELETE_CAMPAIGN = "DELETE FROM campaign " +
													  "WHERE urn = ?";
	
	/**
	 * Basic constructor.
	 * 
	 * @param dataSource The data source to which queries will be run against.
	 */
	public CampaignDeletionDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Checks to ensure that the logged in user has sufficient permissions to
	 * delete this campaign and then deletes it. The database should cascade
	 * the deletes. The validation and deletion are done in one step to
	 * minimize the possibility of a concurrency issue.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		boolean doIt = false;
		String campaignUrn = awRequest.getCampaignUrn();
		
		// If they are a supervisor then do it.
		try {
			doIt = (getJdbcTemplate().queryForInt(SQL_GET_IS_SUPERVISOR, new Object[] { awRequest.getUser().getUserName(), campaignUrn }) != 0);
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_IS_SUPERVISOR + "' with parameters: " + awRequest.getUser().getUserName() + ", " + campaignUrn, dae);
			throw new DataAccessException(dae);
		}
		
		// If they are an author and there are no responses.
		if(! doIt) {
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_IS_AUTHOR, new Object[] { awRequest.getUser().getUserName(), campaignUrn }) != 0) {
					try {
						doIt = (getJdbcTemplate().queryForInt(SQL_GET_RESPONSES_COUNT, new Object[] { campaignUrn }) == 0);
					}
					catch(org.springframework.dao.DataAccessException dae) {
						_logger.error("Error executing SQL '" + SQL_GET_RESPONSES_COUNT + "' with parameter: " + campaignUrn, dae);
						throw new DataAccessException(dae);
					}
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_GET_IS_AUTHOR + "' with parameters: " + awRequest.getUser().getUserName() + ", " + campaignUrn, dae);
				throw new DataAccessException(dae);
			}
		}
		
		// Delete the campaign which will cause a cascade of deletes.
		if(doIt) {
			// Begin transaction
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("Campaign deletion.");
			
			try {
				PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
				TransactionStatus status = transactionManager.getTransaction(def);
				
				try {
					_logger.info("Deleting campaign " + campaignUrn + ".");
					getJdbcTemplate().update(SQL_DELETE_CAMPAIGN, new Object[] { campaignUrn });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_DELETE_CAMPAIGN + "' with parameterr: " + campaignUrn, dae);
					transactionManager.rollback(status);
					throw new DataAccessException(dae);
				}
				
				// Commit transaction.
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				_logger.error("Error while rolling back the transaction.", e);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
		}
		else {
			awRequest.setFailedRequest(true);
		}
	}
}
