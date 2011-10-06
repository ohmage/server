package org.ohmage.dao;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.domain.CampaignInformation;
import org.ohmage.domain.CampaignInformation.PrivacyState;
import org.ohmage.domain.CampaignInformation.RunningState;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.configuration.SurveyMapFromXmlBuilder;
import org.ohmage.exception.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
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
public final class CampaignDaos extends Dao {
	// Returns a boolean value of whether or not the campaign exists.
	private static final String SQL_EXISTS_CAMPAIGN = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM campaign " +
			"WHERE urn = ?" +
		")";

	// Returns the name of a campaign.
	private static final String SQL_GET_NAME =
		"SELECT name " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the description of a campaign.
	private static final String SQL_GET_DESCRIPTION = 
		"SELECT description " +
		"FROM campaign " +
		"WHERE urn = ?";

	// Returns the XML for a campaign.
	private static final String SQL_GET_XML = 
		"SELECT xml " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the icon URL for a campaign.
	private static final String SQL_GET_ICON_URL = 
		"SELECT icon_url " +
		"FROM campaign " +
		"WHERE urn = ?";

	// Returns the running state String of a campaign.
	private static final String SQL_GET_RUNNING_STATE =
		"SELECT crs.running_state " +
		"FROM campaign c, campaign_running_state crs " +
		"WHERE c.urn = ? " +
		"AND c.running_state_id = crs.id";

	// Returns the privacy state String of a campaign.
	private static final String SQL_GET_PRIVACY_STATE = 
		"SELECT cps.privacy_state " +
		"FROM campaign c, campaign_privacy_state cps " +
		"WHERE c.urn = ?" +
		"AND c.privacy_state_id = cps.id";

	// Returns the campaign's creation timestamp.
	private static final String SQL_GET_CREATION_TIMESTAMP =
		"SELECT creation_timestamp " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	// Returns the information pertaining directly to a campaign.
	private static final String SQL_GET_CAMPAIGN_INFORMATION =
		"SELECT c.name, c.description, c.icon_url, c.authored_by, crs.running_state, cps.privacy_state, c.creation_timestamp " +
		"FROM campaign c, campaign_running_state crs, campaign_privacy_state cps " +
		"WHERE c.urn = ? " +
		"AND c.running_state_id = crs.id " +
		"AND c.privacy_state_id = cps.id";
	
	// Returns all of the IDs for all of the campaigns whose creation timestamp
	// was on or after some date.
	private static final String SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE creation_timestamp >= ?";
	
	// Returns all of the IDs for all of the campaigns whose creation timestamp
	// was on or before some date.
	private static final String SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE =
		"SELECT urn " +
		"FROM campaign " +
		"WHERE creation_timestamp <= ?";
	
	// Returns all of the IDs for all of the campaigns whose privacy state is
	// some value.
	private static final String SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE privacy_state_id = (" +
			"SELECT id " +
			"FROM campaign_privacy_state " +
			"WHERE privacy_state = ?" +
		")";
	
	// Returns all of the IDs for all of the campaigns whose running state is
	// some value.
	private static final String SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE = 
		"SELECT urn " +
		"FROM campaign " +
		"WHERE running_state_id = (" +
			"SELECT id " +
			"FROM campaign_running_state " +
			"WHERE running_state = ?" +
		")";
	
	// Retrieves the campaign roles for a user based on the default roles for
	// a campaign-class association.
	public static final String SQL_GET_USER_DEFAULT_ROLES =
		"SELECT ur.role " +
		"FROM user u, campaign ca, class cl, campaign_class cc, user_role ur, user_class uc, campaign_class_default_role ccdr " +
		"WHERE u.username = ? " +
		"AND ca.urn = ? " +
		"AND cl.urn = ? " +
		"AND ca.id = cc.campaign_id " +
		"AND cl.id = cc.class_id " +
		"AND cc.id = ccdr.campaign_class_id " +
		"AND u.id = uc.user_id " +
		"AND cl.id = uc.class_id " +
		"AND uc.user_class_role_id = ccdr.user_class_role_id " +
		"AND ccdr.user_role_id = ur.id";

	// Inserts a new campaign.
	private static final String SQL_INSERT_CAMPAIGN = 
		"INSERT INTO campaign(urn, name, xml, description, icon_url, authored_by, creation_timestamp, running_state_id, privacy_state_id) " +
		"VALUES (?, ?, ?, ?, ?, ?, now(), (" +
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
	
	// Updates the campaign's XML.
	private static final String SQL_UPDATE_XML =
		"UPDATE campaign " +
		"SET xml = ? " +
		"WHERE urn = ?";
	
	// Updates a campaign's description.
	private static final String SQL_UPDATE_DESCRIPTION = 
		"UPDATE campaign " +
		"SET description = ? " +
		"WHERE urn = ?";
	
	// Updates a campaign's privacy state.
	private static final String SQL_UPDATE_PRIVACY_STATE =
		"UPDATE campaign " +
		"SET privacy_state_id = (" +
			"SELECT id " +
			"FROM campaign_privacy_state " +
			"WHERE privacy_state = ?" +
		") " +
		"WHERE urn = ?";
	
	// Updates a campaign's running state.
	private static final String SQL_UPDATE_RUNNING_STATE =
		"UPDATE campaign " +
		"SET running_state_id = (" +
			"SELECT id " +
			"FROM campaign_running_state " +
			"WHERE running_state = ?" +
		") " +
		"WHERE urn = ?";
		
	// Deletes a campaign.
	private static final String SQL_DELETE_CAMPAIGN = 
		"DELETE FROM campaign " +
		"WHERE urn = ?";
	
	// Deletes a campaign, class association.
	private static final String SQL_DELETE_CAMPAIGN_CLASS =
		"DELETE FROM campaign_class " +
		"WHERE campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Removes a role from a user in a campaign.
	private static final String SQL_DELETE_USER_ROLE_CAMPAIGN =
		"DELETE FROM user_role_campaign " +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		") " +
		"AND campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		") " +
		"AND user_role_id = (" +
			"SELECT id " +
			"FROM user_role " +
			"WHERE role = ?" +
		")";
	
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
			String iconUrl, String authoredBy, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			List<String> classIds, String creatorUsername)
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Create the campaign.
			try {
				instance.getJdbcTemplate().update(
						SQL_INSERT_CAMPAIGN, 
						new Object[] { campaignId, name, xml, description, iconUrl, authoredBy, runningState.toString(), privacyState.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN + "' with parameters: " +
						campaignId + ", " + name + ", " + xml + ", " + description + ", " + iconUrl + ", " + authoredBy + ", " + runningState + ", " + privacyState, e);
			}
			
			// Add each of the classes to the campaign.
			for(String classId : classIds) {
				associateCampaignAndClass(transactionManager, status, campaignId, classId);
			}
			
			// Add the requesting user as the author. This may have already 
			// happened above.
			try {
				instance.getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, creatorUsername, campaignId, CampaignRoleCache.Role.AUTHOR.toString());
			}
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				// The user was already an author of this campaign implying 
				// that it's one of the default campaign roles based on a class
				// role that the 'creatorUsername' has.
				e.printStackTrace();
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
						creatorUsername + ", " + campaignId + ", " + CampaignRoleCache.Role.AUTHOR, e);
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
			return instance.getJdbcTemplate().queryForObject(
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
	 * Retrieves a campaign's name.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its name is returned. Otherwise, null is
	 * 		   returned.
	 */
	public static String getName(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_NAME, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_NAME + "' with parameter: " + campaignId, e);
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
			return instance.getJdbcTemplate().queryForObject(
					SQL_SELECT_CAMPAIGN_CONFIGURATION, 
					new Object[] { campaignId }, 
					new RowMapper<Configuration>() { public Configuration mapRow(ResultSet rs, int rowNum) throws SQLException {
						String name = rs.getString(1);
						String description = rs.getString(2);
						String xml = rs.getString(3);
						CampaignRunningStateCache.RunningState runningState = CampaignRunningStateCache.RunningState.getValue(rs.getString(4));
						CampaignPrivacyStateCache.PrivacyState privacyState = CampaignPrivacyStateCache.PrivacyState.getValue(rs.getString(5));
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
    
	/**
	 * Retrieves a campaign's description.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its description is returned. Otherwise, 
	 * 		   null is returned.
	 */
	public static String getDescription(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_DESCRIPTION, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DESCRIPTION + "' with parameter: " + campaignId, e);
		}
	}

	/**
	 * Retrieves the campaign's privacy state.
	 * 
	 * @param campaignId A campaign's unique identifier.
	 * 
	 * @return If the campaign exists, its PrivacyState enum is returned;
	 * 		   otherwise, null is returned.
	 */
	public static CampaignPrivacyStateCache.PrivacyState getCampaignPrivacyState(String campaignId) throws DataAccessException {
		try {
			return CampaignPrivacyStateCache.PrivacyState.getValue(instance.getJdbcTemplate().queryForObject(SQL_GET_PRIVACY_STATE, new Object[] { campaignId }, String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_PRIVACY_STATE + "' with parameter: " + campaignId, e);
		}
	}

	/**
	 * Retrieves the campaign's running state.
	 * 
	 * @param campaignId A campaign's unique identifier.
	 * 
	 * @return If the campaign exists, its running state String is returned;
	 * 		   otherwise, null is returned.
	 */
	public static CampaignRunningStateCache.RunningState getCampaignRunningState(String campaignId) throws DataAccessException {
		try {
			return CampaignRunningStateCache.RunningState.getValue(instance.getJdbcTemplate().queryForObject(SQL_GET_RUNNING_STATE, new Object[] { campaignId }, String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_PRIVACY_STATE + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Retrieves a campaign's XML.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its XML is returned. Otherwise, null is
	 * 		   returned.
	 */
	public static String getXml(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_XML, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_XML + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Retrieves a campaign's icon's URL.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its icon URL is returned. Otherwise, 
	 * 		   null is returned.
	 */
	public static String getIconUrl(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_ICON_URL, new Object[] { campaignId }, String.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_ICON_URL + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Retrieves a campaign's creation timestamp.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its timestamp is returned; otherwise, 
	 * 		   null is returned.
	 */
	public static Timestamp getCreationTimestamp(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_GET_CREATION_TIMESTAMP, new Object[] { campaignId }, Timestamp.class);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Multiple campaigns have the same unique identifier.", e);
			}

			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CREATION_TIMESTAMP + "' with parameter: " + campaignId, e);
		}
	}
	
	/**
	 * Creates a new CampaignInformation object based on the information about
	 * some campaign.
	 *  
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A CampaignInformation object with the required information about
	 * 		   a campaign or null if no such campaign exists.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static CampaignInformation getCampaignInformation(final String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_INFORMATION,
					new Object[] { campaignId },
					new RowMapper<CampaignInformation>() {
						@Override
						public CampaignInformation mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new CampaignInformation(
									campaignId,
									rs.getString("name"),
									rs.getString("description"),
									rs.getString("icon_url"),
									rs.getString("authored_by"),
									RunningState.valueOf(rs.getString("running_state").toUpperCase()),
									PrivacyState.valueOf(rs.getString("privacy_state").toUpperCase()),
									rs.getTimestamp("creation_timestamp"));
						}
					});
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("Mutiple campaigns have the same ID: " + campaignId, e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + SQL_GET_CAMPAIGN_INFORMATION +
					"' with parameter: " +
						campaignId,
					e);
		}
	}
	
	/**
	 * Retrieves the IDs for all campaigns whose creation timestamp was on or
	 * after some date.
	 * 
	 * @param date The date as a Calendar.
	 * 
	 * @return A List of campaign IDs. This will never be null.
	 */
	public static List<String> getCampaignsOnOrAfterDate(Calendar date) throws DataAccessException {
		Timestamp dateAsTimestamp = new Timestamp(date.getTimeInMillis());
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE,
					new Object[] { dateAsTimestamp },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ON_OR_AFTER_DATE + "' with parameter: " + dateAsTimestamp, e);
		}
	}
	
	/**
	 * Retrieves the IDs for all campaigns whose creation timestamp was on or
	 * before some date.
	 * 
	 * @param date The date as a Calendar.
	 * 
	 * @return A List of campaign IDs. This will never be null.
	 */
	public static List<String> getCampaignsOnOrBeforeDate(Calendar date) throws DataAccessException {
		Timestamp dateAsTimestamp = new Timestamp(date.getTimeInMillis());
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE,
					new Object[] { dateAsTimestamp },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_ON_OR_BEFORE_DATE + "' with parameter: " + dateAsTimestamp, e);
		}
	}
	
	/**
	 * Returns a list of campaign IDs for all of the campaigns with a specified
	 * privacy state.
	 * 
	 * @param privacyState The privacy state in question.
	 * 
	 * @return Returns a list of campaign IDs whose is privacy state is 
	 * 		   'privacyState'.
	 */
	public static List<String> getCampaignsWithPrivacyState(CampaignPrivacyStateCache.PrivacyState privacyState) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE,
					new Object[] { privacyState.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_WITH_PRIVACY_STATE + "' with parameter: " + privacyState, e);
		}
	}
	
	/**
	 * Returns a list of campaign IDs for all of the campaigns with a specified
	 * running state.
	 * 
	 * @param runningState The running state in question.
	 * 
	 * @return Returns a list of campaign IDs whose is running state is 
	 * 		   'runningState'.
	 */
	public static List<String> getCampaignsWithRunningState(CampaignRunningStateCache.RunningState runningState) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE,
					new Object[] { runningState.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGNS_WITH_RUNNING_STATE + "' with parameter: " + runningState, e);
		}
	}
	
	/**
	 * Updates a campaign. The 'request' and 'campaignId' are required; 
	 * however, the remaining parameters may be null indicating that they 
	 * should not be updated.
	 * 
	 * @param request The Request that is performing this service.
	 *  
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param xml The new XML for the campaign or null if the XML should not be
	 * 			  updated.
	 * 
	 * @param description The new description for the campaign or null if the
	 * 					  description should not be updated.
	 * 
	 * @param runningState The new running state for the campaign or null if 
	 * 					   the running state should not be updated.
	 * 
	 * @param privacyState The new privacy state for the campaign or null if 
	 * 					   the privacy state should not be updated.
	 * 
	 * @param classIds FIXME: A collection of class IDs where any classes in
	 * 				   the list will have their users added to the class based
	 * 				   on the default class-campaign default roles. Any classes
	 * 				   that are not in the list will be disassociated and all
	 * 				   of their users will be disassociated unless they are
	 * 				   associated in another class.
	 * 
	 * @param usersAndRolesToAdd A map of usernames to a list of roles that the
	 * 							 users should be granted in the campaign or 
	 * 							 null if no users should be granted any new 
	 * 							 roles.
	 * 
	 * @param usersAndRolesToRemove A map of usernames to a list of roles that
	 * 								should be revoked from the user in the
	 * 								campaign or null if no users should have 
	 * 								any of their roles revoked.
	 */
	public static void updateCampaign(String campaignId, String xml, String description, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			Collection<String> classIds, 
			Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToAdd, 
			Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToRemove)
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the XML if it is present.
			if(xml != null) {
				try {
					instance.getJdbcTemplate().update(SQL_UPDATE_XML, new Object[] { xml, campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_XML + "' with parameters: " + xml + ", " + campaignId, e);
				}
			}
			
			// Update the description if it is present.
			if(description != null) {
				try {
					instance.getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { description, campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_DESCRIPTION + "' with parameters: " + description + ", " + campaignId, e);
				}
			}
			
			// Update the running state if it is present.
			if(runningState != null) {
				try {
					instance.getJdbcTemplate().update(SQL_UPDATE_RUNNING_STATE, new Object[] { runningState.toString(), campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_RUNNING_STATE + "' with parameters: " + runningState + ", " + campaignId, e);
				}
			}
			
			// Update the privacy state if it is present.
			if(privacyState != null) {
				try {
					instance.getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { privacyState.toString(), campaignId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " + privacyState + ", " + campaignId, e);
				}
			}
			
			// Add the specific users with specific roles.
			if(usersAndRolesToAdd != null) {
				for(String username : usersAndRolesToAdd.keySet()) {
					for(CampaignRoleCache.Role role : usersAndRolesToAdd.get(username)) {
						try {
							instance.getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { username, campaignId, role.toString() });
						}
						catch(org.springframework.dao.DuplicateKeyException e) {
							// This means that the user already had the role in
							// the campaign. We can ignore this.
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + 
									"' with parameters: " + username + ", " + campaignId + ", " + role, e);
						}
					}
				}
			}
			
			// Remove the specific users and their roles.
			if(usersAndRolesToRemove != null) {
				for(String username : usersAndRolesToRemove.keySet()) {
					for(CampaignRoleCache.Role role : usersAndRolesToRemove.get(username)) {
						try {
							instance.getJdbcTemplate().update(SQL_DELETE_USER_ROLE_CAMPAIGN, new Object[] { username, campaignId, role.toString() });
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + 
									"' with parameters: " + username + ", " + campaignId + ", " + role, e);
						}
					}
				}
			}
			
			// Update the classes
			if(classIds != null) {
				// Retrieve all of the classes that are currently associated with the campaign.
				List<String> classesToRemove;
				try {
					classesToRemove = CampaignClassDaos.getClassesAssociatedWithCampaign(campaignId);
				}
				catch(DataAccessException e) {
					transactionManager.rollback(status);
					throw e;
				}
				
				// Create the list of classes to add by taking the list of 
				// classes from the user and removing all those that were 
				// already associated with campaign.
				List<String> classesToAdd = new ArrayList<String>(classIds);
				classesToAdd.removeAll(classesToRemove);

				// Create the list of classes to remove by taking those that 
				// were already associated with the campaign and remove all 
				// that should still be associated with the campaign.
				classesToRemove.removeAll(classIds);
					
				// For all of the classes that are associated with the campaign but are not in the classIds list,
				for(String classId : classesToRemove) {
					// For each of the users in the class, if they are only 
					// associated with the campaign through this class then 
					// remove them.
					List<String> usernames;
					try {
						usernames = UserClassDaos.getUsersInClass(classId);
					}
					catch(DataAccessException e) {
						transactionManager.rollback(status);
						throw e;
					}
					
					for(String username : usernames) {
						// If the user is not associated with the campaign 
						// through any other class, they are removed from the
						// campaign.
						int numClasses;
						try {
							numClasses = UserCampaignClassDaos.getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(username, campaignId); 
						}
						catch(DataAccessException e) {
							transactionManager.rollback(status);
							throw e;
						}
						if(numClasses == 1) {
							// Retrieve the default roles that the user was 
							// given when they joined the class.
							List<CampaignRoleCache.Role> roles;
							try {
								roles = instance.getJdbcTemplate().query(
										SQL_GET_USER_DEFAULT_ROLES, 
										new Object[] { username, campaignId, classId }, 
										new RowMapper<CampaignRoleCache.Role> () {
											@Override
											public CampaignRoleCache.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
												return CampaignRoleCache.Role.getValue("role");
											}
										});
							}
							catch(org.springframework.dao.DataAccessException e) {
								transactionManager.rollback(status);
								throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
										username + ", " + campaignId + ", " + classId, e);
							}
							
							for(CampaignRoleCache.Role role : roles) {
								try {
									instance.getJdbcTemplate().update(
											SQL_DELETE_USER_ROLE_CAMPAIGN, 
											new Object[] { username, campaignId, role.toString() });
								}
								catch(org.springframework.dao.DataAccessException e) {
									transactionManager.rollback(status);
									throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + "' with parameters: " + 
											username + ", " + campaignId + ", " + role, e);
								}
							}
						}
					}

					// Remove the campaign, class association.
					try {
						instance.getJdbcTemplate().update(SQL_DELETE_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing SQL '" + SQL_DELETE_CAMPAIGN_CLASS + 
								"' with parameters: " + campaignId + ", " + classId, e);
					}
				}
				
				// For all of the classes that are in the classIds list but not
				// associated with the campaign,
				for(String classId : classesToAdd) {
					associateCampaignAndClass(transactionManager, status, campaignId, classId);
				}
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
	 * Deletes a campaign.
	 * 
	 * @param campaignId The unique identifier of the campaign to be deleted.
	 */
	public static void deleteCampaign(String campaignId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				instance.getJdbcTemplate().update(SQL_DELETE_CAMPAIGN, campaignId);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_DELETE_CAMPAIGN + "' with parameter: " + campaignId, e);
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
	 * Creates the association between a class and a campaign in the database.
	 * It then creates a set of default roles for all users of the classes and
	 * adds all of the users in the class to the campaign with the default 
	 * roles.
	 * 
	 * @param transactionManager The PlatformTransactionManager that is 
	 * 							 managing the transaction from which this was
	 * 							 called.
	 * 
	 * @param status The current status of the transaction.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @param classId The unique identifier for the class.
	 */
	private static void associateCampaignAndClass(PlatformTransactionManager transactionManager, TransactionStatus status, String campaignId, String classId) 
		throws DataAccessException {
		
		// Associate this class to the campaign.
		try {
			instance.getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
		}
		catch(org.springframework.dao.DuplicateKeyException e) {
			// If the campaign was already associated with the class, ignore
			// this call.
			return;
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
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							ClassRoleCache.Role.PRIVILEGED.toString(), 
							CampaignRoleCache.Role.SUPERVISOR.toString() }
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + ClassRoleCache.Role.PRIVILEGED + ", " + CampaignRoleCache.Role.SUPERVISOR, e);
		}
		try {
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							ClassRoleCache.Role.PRIVILEGED.toString(), 
							CampaignRoleCache.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + ClassRoleCache.Role.PRIVILEGED + ", " + CampaignRoleCache.Role.PARTICIPANT, e);
		}
		
		// Insert the default campaign_class_default_role
		// relationships for restricted users.
		// TODO: This should be a parameter in the API.
		try {
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							ClassRoleCache.Role.RESTRICTED.toString(), 
							CampaignRoleCache.Role.ANALYST.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + ClassRoleCache.Role.RESTRICTED + ", " + CampaignRoleCache.Role.ANALYST, e);
		}
		try {
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId,
							classId,
							ClassRoleCache.Role.RESTRICTED.toString(), 
							CampaignRoleCache.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + ClassRoleCache.Role.RESTRICTED + ", " + CampaignRoleCache.Role.PARTICIPANT, e);
		}
		
		// Get the list of users in the class.
		List<String> usernames;
		try {
			usernames = UserClassDaos.getUsersInClass(classId);
		}
		catch(DataAccessException e) {
			transactionManager.rollback(status);
			throw e;
		}
		
		// For each of the users in the class, assign them their default roles
		// in the campaign.
		for(String username : usernames) {
			List<CampaignRoleCache.Role> roles;
			try {
				roles = instance.getJdbcTemplate().query(
						SQL_GET_USER_DEFAULT_ROLES, 
						new Object[] { username, campaignId, classId }, 
						new RowMapper<CampaignRoleCache.Role>() {
							@Override
							public CampaignRoleCache.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
								return CampaignRoleCache.Role.getValue(rs.getString("role"));
							}
						});
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
						username + ", " + campaignId + ", " + classId, e);
			}
			
			for(CampaignRoleCache.Role role : roles) {
				try {
					instance.getJdbcTemplate().update(
							SQL_INSERT_USER_ROLE_CAMPAIGN, 
							new Object[] { username, campaignId, role.toString() });
				}
				catch(org.springframework.dao.DuplicateKeyException e) {
					// If the user already has the role in the campaign then
					// ignore it.
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
							username + ", " + campaignId + ", " + role, e);
				}
			}
		}
	}
}