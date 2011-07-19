package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.configuration.SurveyMapFromXmlBuilder;
import org.ohmage.exception.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting campaigns. While it may read information pertaining 
 * to other entities, the information it takes and provides should pertain to 
 * campaigns only with the exception of linking other entities to campaigns  
 * such as campaign creation which needs to be able to associate users in a 
 * in a class with this campaign in a single transaction.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class CampaignDaos extends Dao {
	// Returns a boolean value of whether or not the campaign exists.
	private static final String SQL_EXISTS_CAMPAIGN = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";
	
	// Inserts a new campaign.
	private static final String SQL_INSERT_CAMPAIGN = 
		"INSERT INTO campaign(urn, name, xml, description, creation_timestamp, running_state_id, privacy_state_id) " +
		"VALUES (?, ?, ?, ?, now(), (" +
				"SELECT id " +
				"FROM campaign_running_state " +
				"WHERE running_state = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign_privacy_state " +
				"WHERE privacy_state = ?" +
			")" +
		")";
	
	// Inserts a campagin-class association.
	private static final String SQL_INSERT_CAMPAIGN_CLASS =
		"INSERT INTO campaign_class(campaign_id, class_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			")" +
		")";
	
	// Inserts an entry into the campaign_class_default_role table.
	private static final String SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE = 
		"INSERT INTO campaign_class_default_role(campaign_class_id, user_class_role_id, user_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT cc.id " +
				"FROM class cl, campaign ca, campaign_class cc " +
				"WHERE ca.urn = ? " +
				"AND ca.id = cc.campaign_id " +
				"AND cl.urn = ? " +
				"AND cl.id = cc.class_id" +
			"), (" +
				"SELECT id " +
				"FROM user_class_role " +
				"WHERE role = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Associates a user with a campaign and a given campaign role.
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN =
		"INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	/**
	 * Inserts the users into the user_role_campaign table.
	 * 
	 * Note: Use this with great care. It contains a MySQL-specific "IGNORE"
	 * attribute. This means that if you attempt to add a row that would break
	 * any constraint that it will still succeed. This was originally being 
	 * done because, when creating a new campaign, this will be called multiple
	 * times in sequence for each class to which the campaign is being 
	 * associated. If a user was in multiple of those classes and had the same
	 * role in any of those classes or the default campaign roles based on the
	 * class roles overlapped, then the subsequent calls to this would return
	 * a duplicate entry issue. The "IGNORE" will ignore this and just not add
	 * the row. However, any other constraints that were violated would 
	 * silently break as well. The only other constraint on the table is that
	 * NULL values are not allowed, but the way the SELECT is formed no such
	 * NULL value can ever be returned. Therefore, given the current rules we
	 * are safe, but this may not always be the case.
	 */
	private static final String SQL_INSERT_BATCH_USER_ROLE_CAMPAIGN = 
		"INSERT IGNORE INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
		"(" +
			"SELECT uc.user_id, ca.id, ccdr.user_role_id " +
			"FROM campaign ca, class cl, campaign_class cc, user_class uc, campaign_class_default_role ccdr " +
			"WHERE ca.urn = ? " +
			"AND cl.urn = ? " +
			"AND ca.id = cc.campaign_id " +
			"AND cl.id = cc.class_id " +
			"AND cc.id = ccdr.campaign_class_id " +
			"AND cl.id = uc.class_id " +
			"AND uc.user_class_role_id = ccdr.user_class_role_id" +
		")";
	
	// Finds the running state for a particular campaign
	private static final String SQL_SELECT_CAMPAIGN_RUNNING_STATE = 
		"SELECT crs.running_state " +
		"FROM campaign c, campaign_running_state crs " +
		"WHERE c.urn = ? " +
		"AND c.running_state_id = crs.id";
	
	private static final String SQL_SELECT_CAMPAIGN_CONFIGURATION = 
		"SELECT c.name, c.description, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp" +
        " FROM campaign c, campaign_running_state crs, campaign_privacy_state cps" +
        " WHERE urn = ?" +
        " AND c.running_state_id = crs.id" +
        " AND c.privacy_state_id = cps.id";
	
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static CampaignDaos instance;

	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Creates a new campaign.
	 * 
	 * @param campaignId The new campaign's unique identifier.
	 * 
	 * @param name The new campaign's name.
	 * 
	 * @param xml The XML defining the new campaign.
	 * 
	 * @param description An optional description for the campaign.
	 * 
	 * @param runningState The initial running state for the campaign.
	 * 
	 * @param privacyState The initial privacy state for the campaign.
	 * 
	 * @param classIds A List of classes with which this campaign should be 
	 * 				   associated.
	 * 
	 * @param creatorUsername The username of the creator of this campaign.
	 */
	public static void createCampaign(String campaignId, String name, String xml, String description, 
			String runningState, String privacyState, List<String> classIds, String creatorUsername) 
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Create the campaign.
			try {
				instance.jdbcTemplate.update(
						SQL_INSERT_CAMPAIGN, 
						new Object[] { campaignId, name, xml, description, runningState, privacyState });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN + "' with parameters: " +
						campaignId + ", " + name + ", " + xml + ", " + description + ", " + runningState + ", " + privacyState, e);
			}
			
			// Add each of the classes to the campaign.
			for(String classId : classIds) {
				// Associate this class to the campaign.
				try {
					instance.jdbcTemplate.update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + 
							campaignId + ", " + classId, e);
				}
				
				// Insert the default campaign_class_default_role
				// relationships for privileged users.
				// TODO: This should be a parameter in the API.
				try {
					instance.jdbcTemplate.update(
							SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
							new Object[] { 
									campaignId, 
									classId, 
									ClassRoleCache.ROLE_PRIVILEGED, 
									CampaignRoleCache.ROLE_SUPERVISOR }
							);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
							campaignId + ", " + classId + ", " + ClassRoleCache.ROLE_PRIVILEGED + ", " + CampaignRoleCache.ROLE_SUPERVISOR, e);
				}
				try {
					instance.jdbcTemplate.update(
							SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
							new Object[] { 
									campaignId, 
									classId, 
									ClassRoleCache.ROLE_PRIVILEGED, 
									CampaignRoleCache.ROLE_PARTICIPANT }
							);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
							campaignId + ", " + classId + ", " + ClassRoleCache.ROLE_PRIVILEGED + ", " + CampaignRoleCache.ROLE_PARTICIPANT, e);
				}
				
				// Insert the default campaign_class_default_role
				// relationships for restricted users.
				// TODO: This should be a parameter in the API.
				try {
					instance.jdbcTemplate.update(
							SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
							new Object[] { 
									campaignId, 
									classId, 
									ClassRoleCache.ROLE_RESTRICTED, 
									CampaignRoleCache.ROLE_ANALYST}
							);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
							campaignId + ", " + classId + ", " + ClassRoleCache.ROLE_RESTRICTED + ", " + CampaignRoleCache.ROLE_ANALYST, e);
				}
				try {
					instance.jdbcTemplate.update(
							SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
							new Object[] { 
									campaignId,
									classId,
									ClassRoleCache.ROLE_RESTRICTED, 
									CampaignRoleCache.ROLE_PARTICIPANT}
							);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
							campaignId + ", " + classId + ", " + ClassRoleCache.ROLE_RESTRICTED + ", " + CampaignRoleCache.ROLE_PARTICIPANT, e);
				}
				
				// For each of the users in this class, assign them their
				// default role.
				try {
					instance.jdbcTemplate.update(
							SQL_INSERT_BATCH_USER_ROLE_CAMPAIGN, 
							new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_BATCH_USER_ROLE_CAMPAIGN + "' with parameters: " + 
							campaignId + ", " + classId, e);
				}
			}
			
			// Add the requesting user as the author. This may have already 
			// happened above.
			try {
				instance.jdbcTemplate.update(SQL_INSERT_USER_ROLE_CAMPAIGN, creatorUsername, campaignId, CampaignRoleCache.ROLE_AUTHOR);
			}
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				// The user was already an author of this campaign implying 
				// that it's one of the default campaign roles based on a class
				// role that the 'creatorUsername' has.
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
						creatorUsername + ", " + campaignId + ", " + CampaignRoleCache.ROLE_AUTHOR, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Returns whether or not a campaign with the unique campaign identifier
	 * 'campaignId' exists.
	 * 
	 * @param campaignId The unique identifier for the campaign whose existence
	 * 					 is in question.
	 * 
	 * @return Returns true if the campaign exists; false, otherwise.
	 */
	public static Boolean getCampaignExists(String campaignId) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForObject(
					SQL_EXISTS_CAMPAIGN, 
					new Object[] { campaignId }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_CAMPAIGN + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Finds the running state for the provided campaign id.
	 * 
	 * @param campaignId The unique identifier for the campaign running
	 *                   state in question.
	 * @return A String identifying the campaign's running state.
	 * @throws DataAccessException If an error occurs running the SQL.
	 */
	public static String getRunningStateForCampaignId(String campaignId) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForObject(
					SQL_SELECT_CAMPAIGN_RUNNING_STATE, 
					new Object[] { campaignId }, 
					String.class
			);
		} 
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_SELECT_CAMPAIGN_RUNNING_STATE + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Finds the configuration for the provided campaign id.
	 * 
	 * @param campaignId The unique identifier for the campaign..
	 * @throws DataAccessException If an error occurs running the SQL.
	 */
	public static Configuration findCampaignConfiguration(final String campaignId) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForObject(
					SQL_SELECT_CAMPAIGN_CONFIGURATION, 
					new Object[] { campaignId }, 
					new RowMapper<Configuration>() { public Configuration mapRow(ResultSet rs, int rowNum) throws SQLException {
						String name = rs.getString(1);
						String description = rs.getString(2);
						String xml = rs.getString(3);
						String runningState = rs.getString(4);
						String privacyState = rs.getString(5);
						String timestamp = rs.getTimestamp(6).toString();
						return new Configuration(campaignId, name, description, 
								runningState, privacyState, timestamp, SurveyMapFromXmlBuilder.buildFrom(xml), xml);
					}}
			);
		}
		catch(IncorrectResultSizeDataAccessException e) {
			throw new DataAccessException("Found an incorrect number of results executing SQL '" + SQL_SELECT_CAMPAIGN_CONFIGURATION + "' with parameter: " + campaignId, e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("General error executing SQL '" + SQL_SELECT_CAMPAIGN_CONFIGURATION + "' with parameter: " + campaignId, e);
		}
	}
}