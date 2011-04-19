package edu.ucla.cens.awserver.dao;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;

import javax.sql.DataSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Adds the campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationDao.class);
	
	private static final String SQL_GET_NEW_CAMPAIGN_ID = "SELECT id " +
														  "FROM campaign " +
														  "WHERE urn=?";

	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn=?";
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id=?";
	
	private static final String SQL_GET_ROLE_ID = "SELECT id " +
												  "FROM user_role " +
												  "WHERE role='author'";
	
	private static final String SQL_INSERT_CAMPAIGN = "INSERT INTO campaign(description, xml, running_state, privacy_state, name, urn, creation_timestamp) " +
											 		  "VALUES (?,?,?,?,?,?,?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS = "INSERT INTO campaign_class(campaign_id, class_id) " +
															"VALUES (?,?)";
	
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN = "INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
																"VALUES (?,?,?)";
	
	/**
	 * Creates a basic DAO.
	 * 
	 * @param dataSource The DataSource that we will run our queries against.
	 */
	public CampaignCreationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Adds the campaign to the database, connects the classes to the
	 * campaign, and sets the currently logged in user as the creator.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Inserting campaign into the database.");
		
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_XML);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException(e);
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(campaignXml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unable to read XML.", e);
			throw new DataAccessException("XML was unreadable.");
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Invalid XML.", e);
			throw new DataAccessException("XML was invalid.");
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unparcelable XML.", e);
			throw new DataAccessException("XML was unparcelable.");
		}
		
		Element root = document.getRootElement();
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue();
		String campaignName = root.query("/campaign/campaignName").get(0).getValue();
		
		Calendar now = Calendar.getInstance();
		String nowFormatted = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " +
							  now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
	
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Campaign creation and user/class hookups.");
		
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
			getJdbcTemplate().update(SQL_INSERT_CAMPAIGN, 
									 new Object[] { ((awRequest.existsInToProcess(CampaignCreationAwRequest.KEY_DESCRIPTION)) ? awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_DESCRIPTION) : "" ), 
									 				awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_XML), 
									 				awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_RUNNING_STATE), 
									 				awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_PRIVACY_STATE), 
									 				campaignName,
									 				campaignUrn,
									 				nowFormatted});
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN + "' with parameters: " +
						  ((awRequest.existsInToProcess(CampaignCreationAwRequest.KEY_DESCRIPTION)) ? awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_DESCRIPTION) : "" ) + ", " + 
						  awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_XML) + ", " +
						  awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_RUNNING_STATE) + ", " +
						  awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_PRIVACY_STATE) + ", " +
						  campaignName + ", " +
						  campaignUrn + ", " +
						  nowFormatted, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		catch(IllegalArgumentException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Missing parameter in the toProcess map.", e);
		}
		
		// Get campaign ID.
		int campaignId;
		try {
			campaignId = getJdbcTemplate().queryForInt(SQL_GET_NEW_CAMPAIGN_ID, new Object[] { campaignUrn });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_NEW_CAMPAIGN_ID + "' with parameter: " + campaignUrn, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Get the Author role ID.
		int authorRoleId;
		try {
			authorRoleId = getJdbcTemplate().queryForInt(SQL_GET_ROLE_ID);
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_ROLE_ID + "'", dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Get the currently logged in user's ID.
		int userId;
		try {
			userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { awRequest.getUser().getUserName() });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Make the current user the creator.
		try {
			getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, authorRoleId });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + authorRoleId, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Hookup to classes and users.
		String[] classes = ((String) awRequest.getToProcessValue(CampaignCreationAwRequest.KEY_LIST_OF_CLASSES_AS_STRING)).split(",");
		for(int i = 0; i < classes.length; i++) {
			// Get the current class' ID.
			int classId;
			try {
				classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { classes[i] });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + classes[i], dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
			}
			
			// Hookup the current class with the campaign.
			try {
				getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + campaignId + ", " + classId, dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
			}
			
			// TODO: Hookup all users in the current class with this campaign and
			// their respective roles.
			// If the role is author and the current user is the creator, then
			// don't attempt to insert them again as that will cause a 
			// DataAccessException.
		}
		
		try {
			transactionManager.commit(status);
		}
		catch(TransactionException e) {
			_logger.error("Error while attempting to rollback transaction.");
			transactionManager.rollback(status);
			throw new DataAccessException(e);
		}
	}
}