package org.ohmage.query;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.ohmage.domain.Clazz;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Survey;
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
public final class CampaignQueries extends Query {
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
		"SELECT c.name, c.description, c.icon_url, c.authored_by, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp " +
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
	
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static CampaignQueries instance;

	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignQueries(DataSource dataSource) {
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
			Campaign.RunningState runningState, 
			Campaign.PrivacyState privacyState, 
			Collection<String> classIds, String creatorUsername)
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
				instance.getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, creatorUsername, campaignId, Campaign.Role.AUTHOR.toString());
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
						creatorUsername + ", " + campaignId + ", " + Campaign.Role.AUTHOR, e);
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
	public static Campaign findCampaignConfiguration(final String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_INFORMATION, 
					new Object[] { campaignId }, 
					new RowMapper<Campaign>() {
						@Override
						public Campaign mapRow(ResultSet rs, int rowNum) 
								throws SQLException {
							
						return new Campaign(
								rs.getString("description"),
								Campaign.RunningState.getValue(
										rs.getString("running_state")),
								Campaign.PrivacyState.getValue(
										rs.getString("privacy_state")),
								rs.getTimestamp("creation_timestamp"),
								rs.getString("xml")
							);
						}
					}
				);
		}
		catch(IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() == 0) {
				return null;
			}
			
			throw new DataAccessException("Multiple campaigns have the same ID: " + campaignId, e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("General error executing SQL '" + SQL_GET_CAMPAIGN_INFORMATION + "' with parameter: " + campaignId, e);
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
	public static Campaign.PrivacyState getCampaignPrivacyState(String campaignId) throws DataAccessException {
		try {
			return Campaign.PrivacyState.getValue(instance.getJdbcTemplate().queryForObject(SQL_GET_PRIVACY_STATE, new Object[] { campaignId }, String.class));
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
	public static Campaign.RunningState getCampaignRunningState(String campaignId) throws DataAccessException {
		try {
			return Campaign.RunningState.getValue(instance.getJdbcTemplate().queryForObject(SQL_GET_RUNNING_STATE, new Object[] { campaignId }, String.class));
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
	public static Campaign getCampaignInformation(final String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_INFORMATION,
					new Object[] { campaignId },
					new RowMapper<Campaign>() {
						@Override
						public Campaign mapRow(ResultSet rs, int rowNum) throws SQLException {
							URL iconUrl = null;
							String iconString = rs.getString("icon_url");
							if(iconString != null) {
								try {
									iconUrl = new URL(iconString);
								}
								catch(MalformedURLException e) {
									// This parameter is still experimental, so
									// we will leave this alone for now.
								}
							}
							
							return new Campaign(
									campaignId,
									rs.getString("name"),
									rs.getString("description"),
									null,
									iconUrl,
									rs.getString("authored_by"),
									Campaign.RunningState.valueOf(rs.getString("running_state").toUpperCase()),
									Campaign.PrivacyState.valueOf(rs.getString("privacy_state").toUpperCase()),
									rs.getTimestamp("creation_timestamp"),
									new HashMap<String, Survey>(0),
									rs.getString("xml"));
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
	public static List<String> getCampaignsWithPrivacyState(Campaign.PrivacyState privacyState) throws DataAccessException {
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
	public static List<String> getCampaignsWithRunningState(Campaign.RunningState runningState) throws DataAccessException {
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
	 * @param classesToAdd The collection of classes to associate with the
	 * 					   campaign.
	 * 
	 * @param classesToRemove The collection of classes to disassociate from
	 * 						  the campaign.
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
			Campaign.RunningState runningState, 
			Campaign.PrivacyState privacyState, 
			Collection<String> classesToAdd,
			Collection<String> classesToRemove,
			Map<String, Set<Campaign.Role>> usersAndRolesToAdd, 
			Map<String, Set<Campaign.Role>> usersAndRolesToRemove)
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
					for(Campaign.Role role : usersAndRolesToAdd.get(username)) {
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
					for(Campaign.Role role : usersAndRolesToRemove.get(username)) {
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
			
			if(classesToRemove != null) {
				// For all of the classes that are associated with the campaign
				// but are not in the classIds list,
				for(String classId : classesToRemove) {
					// For each of the users in the class, if they are only 
					// associated with the campaign through this class then 
					// remove them.
					List<String> usernames;
					try {
						usernames = UserClassQueries.getUsersInClass(classId);
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
							numClasses = UserCampaignClassQueries.getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(username, campaignId); 
						}
						catch(DataAccessException e) {
							transactionManager.rollback(status);
							throw e;
						}
						if(numClasses == 1) {
							// Retrieve the default roles that the user was 
							// given when they joined the class.
							List<Campaign.Role> roles;
							try {
								roles = instance.getJdbcTemplate().query(
										SQL_GET_USER_DEFAULT_ROLES, 
										new Object[] { username, campaignId, classId }, 
										new RowMapper<Campaign.Role> () {
											@Override
											public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
												return Campaign.Role.getValue("role");
											}
										});
							}
							catch(org.springframework.dao.DataAccessException e) {
								transactionManager.rollback(status);
								throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
										username + ", " + campaignId + ", " + classId, e);
							}
							
							for(Campaign.Role role : roles) {
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
			}
			
			if(classesToAdd != null) {
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
							Clazz.Role.PRIVILEGED.toString(), 
							Campaign.Role.SUPERVISOR.toString() }
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.PRIVILEGED + ", " + Campaign.Role.SUPERVISOR, e);
		}
		try {
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId, 
							classId, 
							Clazz.Role.PRIVILEGED.toString(), 
							Campaign.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.PRIVILEGED + ", " + Campaign.Role.PARTICIPANT, e);
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
							Clazz.Role.RESTRICTED.toString(), 
							Campaign.Role.ANALYST.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.RESTRICTED + ", " + Campaign.Role.ANALYST, e);
		}
		try {
			instance.getJdbcTemplate().update(
					SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, 
					new Object[] { 
							campaignId,
							classId,
							Clazz.Role.RESTRICTED.toString(), 
							Campaign.Role.PARTICIPANT.toString() }
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + 
					campaignId + ", " + classId + ", " + Clazz.Role.RESTRICTED + ", " + Campaign.Role.PARTICIPANT, e);
		}
		
		// Get the list of users in the class.
		List<String> usernames;
		try {
			usernames = UserClassQueries.getUsersInClass(classId);
		}
		catch(DataAccessException e) {
			transactionManager.rollback(status);
			throw e;
		}
		
		// For each of the users in the class, assign them their default roles
		// in the campaign.
		for(String username : usernames) {
			List<Campaign.Role> roles;
			try {
				roles = instance.getJdbcTemplate().query(
						SQL_GET_USER_DEFAULT_ROLES, 
						new Object[] { username, campaignId, classId }, 
						new RowMapper<Campaign.Role>() {
							@Override
							public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
								return Campaign.Role.getValue(rs.getString("role"));
							}
						});
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_DEFAULT_ROLES + "' with parameters: " + 
						username + ", " + campaignId + ", " + classId, e);
			}
			
			for(Campaign.Role role : roles) {
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