package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.UserPersonal;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
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
public class UserDaos extends Dao {
	// Returns a boolean representing whether or not a user exists
	private static final String SQL_EXISTS_USER = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Returns a boolean representing whether a user is an admin or not. If the
	// user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_IS_ADMIN = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND admin = true" +
		")";
	
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
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static UserDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
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
	public static void createUser(String username, String hashedPassword, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege) 
		throws DataAccessException {
		
		Boolean tNewAccount = newAccount;
		if(tNewAccount == null) {
			tNewAccount = Boolean.TRUE;
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
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				instance.getJdbcTemplate().update(SQL_INSERT_USER, new Object[] { username, hashedPassword, admin, enabled, tNewAccount, tCampaignCreationPrivilege });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_USER + "' with parameters: " +
						username + ", " + hashedPassword + ", " + admin + ", " + enabled + ", " + tNewAccount + ", " + tCampaignCreationPrivilege, e);
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
	public static Boolean userExists(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
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
	public static Boolean userIsAdmin(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_ADMIN, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ADMIN + "' with parameter: " + username, e);
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
	public static Boolean userCanCreateCampaigns(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS + "' with parameter: " + username, e);
		}
	}
	
	private static final Logger LOGGER = Logger.getLogger(UserDaos.class);
	
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
	public static Boolean userHasPersonalInfo(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
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
	public static UserPersonal getPersonalInfoForUser(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
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
	public static void updateUser(String username, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege, UserPersonal personalInfo) 
		throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's privileges and information.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the admin value if it's not null.
			if(admin != null) {
				try {
					instance.getJdbcTemplate().update(SQL_UPDATE_ADMIN, admin, username);
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
					instance.getJdbcTemplate().update(SQL_UPDATE_ENABLED, enabled, username);
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
					instance.getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, newAccount, username);
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
					instance.getJdbcTemplate().update(SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege, username);
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
							instance.getJdbcTemplate().update(SQL_UPDATE_FIRST_NAME, firstName, username);
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
							instance.getJdbcTemplate().update(SQL_UPDATE_LAST_NAME, lastName, username);
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
							instance.getJdbcTemplate().update(SQL_UPDATE_ORGANIZATION, organization, username);
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
							instance.getJdbcTemplate().update(SQL_UPDATE_PERSONAL_ID, personalId, username);
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
						instance.getJdbcTemplate().update(
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
						instance.getJdbcTemplate().update(SQL_UPDATE_EMAIL_ADDRESS, emailAddress, username);
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
						instance.getJdbcTemplate().update(SQL_UPDATE_JSON_DATA, jsonData.toString(), username);
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
	public static void updateUserPassword(String username, String hashedPassword) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's password.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the password.
			try {
				instance.getJdbcTemplate().update(SQL_UPDATE_PASSWORD, hashedPassword, username);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_PASSWORD + "' with parameters: " + 
						hashedPassword + ", " + username, e);
			}
			
			// Ensure that this user is no longer a new user.
			try {
				instance.getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, false, username);
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
	public static void deleteUsers(Collection<String> usernames) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Delete the users.
			for(String username : usernames) {
				try {
					instance.getJdbcTemplate().update(SQL_DELETE_USER, username);
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