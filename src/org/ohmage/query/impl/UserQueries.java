package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserQueries;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting users. While it may read information pertaining to
 * other entities, the information it takes and provides should pertain to 
 * users only.
 * 
 * @author John Jenkins
 */
public class UserQueries extends Query implements IUserQueries {
	// Returns a boolean representing whether or not a user exists
	private static final String SQL_EXISTS_USER = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user is an admin or not.
	private static final String SQL_EXISTS_USER_IS_ADMIN = 
		"SELECT admin " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user's account is enabled.
	private static final String SQL_EXISTS_USER_IS_ENABLED = 
		"SELECT enabled " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user's account is new.
	private static final String SQL_EXISTS_USER_IS_NEW_ACCOUNT = 
		"SELECT new_account " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a boolean representing whether a user can create campaigns or 
	// not. If the user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND campaign_creation_privilege = true" +
		")";
	
	// Returns a boolean representing whether or not a user has a personal
	// information entry.
	private static final String SQL_EXISTS_USER_PERSONAL =
		"SELECT EXISTS(" +
			"SELECT user_id " +
			"FROM user_personal " +
			"WHERE user_id = (" +
				"SELECT Id " +
				"FROM user " +
				"WHERE username = ?" +
			")" +
		")";
	
	private static final String SQL_GET_ALL_USERNAMES =
		"SELECT username " +
		"FROM user";
	
	private static final String SQL_GET_USERNAMES_LIKE_USERNAME =
		"SELECT username " +
		"FROM user " +
		"WHERE username LIKE ?";
	
	private static final String SQL_GET_USERNAMES_WITH_ADMIN_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE admin = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_ENABLED_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE enabled = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE new_account = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE =
		"SELECT username " +
		"FROM user " +
		"WHERE campaign_creation_privilege = ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_FIRST_NAME =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND first_name LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_LAST_NAME =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND last_name LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_ORGANIZATION =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND organization LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_PERSONAL_ID =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND personal_id LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND email_address LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_JSON_DATA =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND json_data LIKE ?";
	
	// Retrieves the personal information about a user.
	private static final String SQL_GET_USER_PERSONAL =
		"SELECT up.first_name, up.last_name, up.organization, up.personal_id, up.email_address, up.json_data " +
		"FROM user u, user_personal up " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	// Inserts a new user.
	private static final String SQL_INSERT_USER = 
		"INSERT INTO user(username, password, admin, enabled, new_account, campaign_creation_privilege) " +
		"VALUES (?,?,?,?,?,?)";
	
	// Inserts a new personal information record for a user. Note: this doesn't
	// insert the email address or JSON data; to add these, update the record
	// immediately after using this to insert it.
	private static final String SQL_INSERT_USER_PERSONAL =
		"INSERT INTO user_personal(user_id, first_name, last_name, organization, personal_id) " +
		"VALUES ((" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		"),?,?,?,?)";
	
	// Updates the user's password.
	private static final String SQL_UPDATE_PASSWORD = 
		"UPDATE user " +
		"SET password = ? " +
		"WHERE username = ?";
	
	// Updates a user's admin value.
	private static final String SQL_UPDATE_ADMIN =
		"UPDATE user " +
		"SET admin = ? " +
		"WHERE username = ?";
	
	// Updates a user's enabled value.
	private static final String SQL_UPDATE_ENABLED =
		"UPDATE user " +
		"SET enabled = ? " +
		"WHERE username = ?";
	
	// Updates a user's new account value.
	private static final String SQL_UPDATE_NEW_ACCOUNT =
		"UPDATE user " +
		"SET new_account = ? " +
		"WHERE username = ?";
	
	// Updates a user's campaign creation privilege.
	private static final String SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE =
		"UPDATE user " +
		"SET campaign_creation_privilege = ? " +
		"WHERE username = ?";
	
	// Updates a user's first name in their personal information record.
	private static final String SQL_UPDATE_FIRST_NAME = 
		"UPDATE user_personal " +
		"SET first_name = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's last name in their personal information record.
	private static final String SQL_UPDATE_LAST_NAME = 
		"UPDATE user_personal " +
		"SET last_name = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's organization in their personal information record.
	private static final String SQL_UPDATE_ORGANIZATION = 
		"UPDATE user_personal " +
		"SET organization = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's personal ID in their personal information record.
	private static final String SQL_UPDATE_PERSONAL_ID = 
		"UPDATE user_personal " +
		"SET personal_id = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's email address in their personal information record.
	private static final String SQL_UPDATE_EMAIL_ADDRESS = 
		"UPDATE user_personal " +
		"SET email_address = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's json_data in their personal information record.
	private static final String SQL_UPDATE_JSON_DATA = 
		"UPDATE user_personal " +
		"SET json_data = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Deletes the user.
	private static final String SQL_DELETE_USER = 
		"DELETE FROM user " +
		"WHERE username = ?";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Creates a new user.
	 * 
	 * @param username The username for the new user.
	 * 
	 * @param password The hashed password for the new user.
	 * 
	 * @param admin Whether or not the user should initially be an admin.
	 * 
	 * @param enabled Whether or not the user should initially be enabled.
	 * 
	 * @param newAccount Whether or not the new user must change their password
	 * 					 before using any other APIs.
	 * 
	 * @param campaignCreationPrivilege Whether or not the new user is allowed
	 * 									to create campaigns.
	 */
	public void createUser(String username, String hashedPassword, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege) 
		throws DataAccessException {
		
		Boolean tAdmin = admin;
		if(tAdmin == null) {
			tAdmin = false;
		}
		
		Boolean tEnabled = enabled;
		if(tEnabled == null) {
			tEnabled = false;
		}
		
		Boolean tNewAccount = newAccount;
		if(tNewAccount == null) {
			tNewAccount = true;
		}
		
		Boolean tCampaignCreationPrivilege = campaignCreationPrivilege;
		if(tCampaignCreationPrivilege == null) {
			try {
				tCampaignCreationPrivilege = PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE).equals("true");
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Cache doesn't know about 'known' value: " + PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE, e);
			}
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				getJdbcTemplate().update(SQL_INSERT_USER, new Object[] { username, hashedPassword, tAdmin, tEnabled, tNewAccount, tCampaignCreationPrivilege });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_USER + "' with parameters: " +
						username + ", " + hashedPassword + ", " + tAdmin + ", " + tEnabled + ", " + tNewAccount + ", " + tCampaignCreationPrivilege, e);
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
	 * Returns whether or not a user exists.
	 * 
	 * @param username The username for which to check.
	 * 
	 * @return Returns true if the user exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Boolean userExists(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER, 
					new Object[] { username }, 
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Gets whether or not the user is an admin.
	 * 
	 * @param username The username to check.
	 * 
	 * @return Whether or not they are an admin.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	public Boolean userIsAdmin(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_ADMIN, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ADMIN + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userIsEnabled(java.lang.String)
	 */
	@Override
	public Boolean userIsEnabled(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_ENABLED, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ENABLED + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userHasNewAccount(java.lang.String)
	 */
	@Override
	public Boolean userHasNewAccount(String username)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_NEW_ACCOUNT, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_NEW_ACCOUNT + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Gets whether or not the user is allowed to create campaigns.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @return Whether or not the user can create campaigns.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	public Boolean userCanCreateCampaigns(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS + "' with parameter: " + username, e);
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(UserQueries.class);
	
	/**
	 * Checks if a user has a personal information entry in the database.
	 *  
	 * @param username The username of the user.
	 * 
	 * @return Returns true if the user has a personal information entry; 
	 * 		   returns false otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Boolean userHasPersonalInfo(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_PERSONAL,
					new Object[] { username },
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			LOGGER.error("Error executing the following SQL '" + SQL_EXISTS_USER_PERSONAL + "' with parameter: " + username, e);
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_PERSONAL + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getAllUsernames()
	 */
	@Override
	public List<String> getAllUsernames() throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_ALL_USERNAMES,
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_ALL_USERNAMES,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialUsername(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialUsername(String username)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_USERNAME, 
					new Object[] { "%" + username + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_USERNAME +
						"' with parameter: " +
						"%" + username + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithAdminValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithAdminValue(Boolean admin)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_ADMIN_VALUE, 
					new Object[] { admin }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_ADMIN_VALUE +
						"' with parameter: " +
						admin,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithEnabledValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithEnabledValue(Boolean enabled)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_ENABLED_VALUE, 
					new Object[] { enabled }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_ENABLED_VALUE +
						"' with parameter: " +
						enabled,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithNewAccountValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithNewAccountValue(Boolean newAccount)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE, 
					new Object[] { newAccount }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE +
						"' with parameter: " +
						newAccount,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithCampaignCreationPrivilege(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithCampaignCreationPrivilege(
			Boolean campaignCreationPrivilege) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE, 
					new Object[] { campaignCreationPrivilege }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE +
						"' with parameter: " +
						campaignCreationPrivilege,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialFirstName(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialFirstName(String partialFirstName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_FIRST_NAME, 
					new Object[] { "%" + partialFirstName + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_FIRST_NAME +
						"' with parameter: " +
						"%" + partialFirstName + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialLastName(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialLastName(String partialLastName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_LAST_NAME, 
					new Object[] { "%" + partialLastName + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_LAST_NAME +
						"' with parameter: " +
						"%" + partialLastName + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialOrganization(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialOrganization(
			String partialOrganization) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_ORGANIZATION, 
					new Object[] { "%" + partialOrganization + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_ORGANIZATION +
						"' with parameter: " +
						"%" + partialOrganization + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialPersonalId(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialPersonalId(
			String partialPersonalId) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_PERSONAL_ID, 
					new Object[] { "%" + partialPersonalId + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_PERSONAL_ID +
						"' with parameter: " +
						"%" + partialPersonalId + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialEmailAddress(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialEmailAddress(
			String partialEmailAddress) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS, 
					new Object[] { "%" + partialEmailAddress + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS +
						"' with parameter: " +
						"%" + partialEmailAddress + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialJsonData(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialJsonData(String partialJsonData)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_JSON_DATA, 
					new Object[] { "%" + partialJsonData + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_JSON_DATA +
						"' with parameter: " +
						"%" + partialJsonData + "%",
					e);
		}
	}
	
	/**
	 * Retrieves the personal information for a user or null if the user 
	 * doesn't have any personal information.
	 *
	 * @param username The username of the user whose information is being
	 * 				   retrieved.
	 * 
	 * @return If the user has a personal entry in the database, a UserPersonal
	 * 		   object with that information is returned; otherwise, null is
	 * 		   returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public UserPersonal getPersonalInfoForUser(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_USER_PERSONAL, 
					new Object[] { username }, 
					new RowMapper<UserPersonal>() {
						@Override
						public UserPersonal mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new UserPersonal(
									rs.getString("first_name"),
									rs.getString("last_name"),
									rs.getString("organization"),
									rs.getString("personal_id"),
									rs.getString("email_address"),
									rs.getString("json_data"));
						}
					});
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("There are multiple users with the same username.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_GET_USER_PERSONAL + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Updates a user's account information.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param username The username of the user whose information is to be
	 * 				   updated.
	 * 
	 * @param admin Whether or not the user should be an admin. A null value
	 * 			    indicates that this field should not be updated.
	 * 
	 * @param enabled Whether or not the user's account should be enabled. A
	 * 				  null value indicates that this field should not be
	 * 				  updated.
	 * 
	 * @param newAccount Whether or not the user should be required to change
	 * 					 their password. A null value indicates that this field
	 * 					 should not be updated.
	 * 
	 * @param campaignCreationPrivilege Whether or not the user should be 
	 * 									allowed to create campaigns. A null
	 * 									value indicates that this field should
	 * 									not be updated.
	 * 
	 * @param personalInfo Personal information about a user. If this is null,
	 * 					   none of the user's personal information will be
	 * 					   updated. If it is not null, all non-null values 
	 * 					   inside this object will be used to update the user's
	 * 					   personal information database record; all null 
	 * 					   values will be ignored.
	 */
	public void updateUser(String username, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege, UserPersonal personalInfo) 
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's privileges and information.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the admin value if it's not null.
			if(admin != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_ADMIN, admin, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_ADMIN + "' with parameters: " + 
							admin + ", " + username, e);
				}
			}
			
			// Update the enabled value if it's not null.
			if(enabled != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_ENABLED, enabled, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_ENABLED + "' with parameters: " + 
							enabled + ", " + username, e);
				}
			}
			
			// Update the new account value if it's not null.
			if(newAccount != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, newAccount, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_NEW_ACCOUNT + "' with parameters: " + 
							newAccount + ", " + username, e);
				}
			}
			
			// Update the campaign creation privilege value if it's not null.
			if(campaignCreationPrivilege != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE + "' with parameters: " + 
							campaignCreationPrivilege + ", " + username, e);
				}
			}
			
			// Update the personal information if it's not null.
			if((personalInfo != null) && (! personalInfo.isEmpty())) {
				// Figure out if the user already has a personal information
				// entry.
				Boolean userHasPersonalInfo = false;
				try {
					userHasPersonalInfo = userHasPersonalInfo(username);
				}
				catch(DataAccessException e) {
					transactionManager.rollback(status);
					throw e;
				}
				
				// If the user already has a personal information entry,
				// update it.
				if(userHasPersonalInfo) {
					// Update the first name if it's not null.
					String firstName = personalInfo.getFirstName();
					if(firstName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_FIRST_NAME, firstName, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_FIRST_NAME + "' with parameters: " +
									firstName + ", " + username, e);
						}
					}
					
					// Update the last name if it's not null.
					String lastName = personalInfo.getLastName();
					if(lastName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_LAST_NAME, lastName, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_LAST_NAME + "' with parameters: " +
									lastName + ", " + username, e);
						}
					}
					
					// Update the organization if it's not null.
					String organization = personalInfo.getOrganization();
					if(organization != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_ORGANIZATION, organization, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_ORGANIZATION + "' with parameters: " +
									organization + ", " + username, e);
						}
					}
					
					// Update the personal ID if it's not null.
					String personalId = personalInfo.getPersonalId();
					if(personalId != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_PERSONAL_ID, personalId, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_PERSONAL_ID + "' with parameters: " +
									personalId + ", " + username, e);
						}
					}
				}
				// If the user does not have a personal information entry,
				// create a new one.
				else {
					try {
						getJdbcTemplate().update(
								SQL_INSERT_USER_PERSONAL, 
								username, 
								personalInfo.getFirstName(), 
								personalInfo.getLastName(), 
								personalInfo.getOrganization(), 
								personalInfo.getPersonalId());
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
								"Error executing SQL '" + SQL_INSERT_USER_PERSONAL + "' with parameters: " +
									username + ", " + 
									personalInfo.getFirstName() + ", " + 
									personalInfo.getLastName() + ", " + 
									personalInfo.getOrganization() + ", " + 
									personalInfo.getPersonalId(), 
								e);
					}
				}
				
				// Update the user's email address if it's not null.
				String emailAddress = personalInfo.getEmailAddress();
				if(emailAddress != null) {
					try {
						getJdbcTemplate().update(SQL_UPDATE_EMAIL_ADDRESS, emailAddress, username);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_EMAIL_ADDRESS + "' with parameters: " + 
								emailAddress + ", " + username, e);
					}
				}
				
				// Update the user's JSON data if it's not null.
				JSONObject jsonData = personalInfo.getJsonData();
				if(jsonData != null) {
					try {
						getJdbcTemplate().update(SQL_UPDATE_JSON_DATA, jsonData.toString(), username);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_JSON_DATA + "' with parameters: " + 
								jsonData.toString() + ", " + username, e);
					}
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
	 * Updates a user's password.
	 * 
	 * @param username The username of the user to be updated.
	 * 
	 * @param hashedPassword The new, hashed password for the user.
	 */
	public void updateUserPassword(String username, String hashedPassword) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's password.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the password.
			try {
				getJdbcTemplate().update(SQL_UPDATE_PASSWORD, hashedPassword, username);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_PASSWORD + "' with parameters: " + 
						hashedPassword + ", " + username, e);
			}
			
			// Ensure that this user is no longer a new user.
			try {
				getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, false, username);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_NEW_ACCOUNT + "' with parameters: " + 
						false + ", " + username, e);
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
	 * Deletes all of the users in a Collection.
	 * 
	 * @param usernames A Collection of usernames for the users to delete.
	 */
	public void deleteUsers(Collection<String> usernames) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Delete the users.
			for(String username : usernames) {
				try {
					getJdbcTemplate().update(SQL_DELETE_USER, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_PASSWORD + "' with parameters: " + 
							username, e);
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
}