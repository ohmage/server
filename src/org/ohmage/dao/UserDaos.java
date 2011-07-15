package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jbcrypt.BCrypt;

import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.UserPersonal;
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
	
	// Retrieves the personal information about a user.
	private static final String SQL_GET_USER_PERSONAL =
		"SELECT up.first_name, up.last_name, up.organization, up.personal_id, up.email_address, up.json_data " +
		"FROM user u, user_personal up " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	// Inserts a new user.
	private static final String SQL_INSERT_USER = 
		"INSERT INTO user(username, password, admin, enabled, new_account, campaign_creation_privilege) " +
		"VALUES(?,?,?,?,?,?)";
	
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
	 * @param password The password for the user new user.
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
	public static void createUser(String username, String password, Boolean admin, Boolean enabled, Boolean newAccount, Boolean campaignCreationPrivilege) {
		String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(13));
		
		if(newAccount == null) {
			newAccount = new Boolean("true");
		}
		
		if(campaignCreationPrivilege == null) {
			try {
				campaignCreationPrivilege = PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE).equals("true");
			}
			catch(CacheMissException cacheMissException) {
				throw new DataAccessException("Cache doesn't know about 'known' value: " + PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE);
			}
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.dataSource);
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				instance.jdbcTemplate.update(SQL_INSERT_USER, new Object[] { username, hashedPassword, admin, enabled, newAccount, campaignCreationPrivilege });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_USER + "' with parameters: " +
						username + ", " + hashedPassword + ", " + admin + ", " + enabled + ", " + newAccount + ", " + campaignCreationPrivilege, e);
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
			throw new DataAccessException("Error while attempting to rollback the transaction.");
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
			return instance.jdbcTemplate.queryForObject(
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
			return instance.jdbcTemplate.queryForObject(
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
			return (Boolean) instance.jdbcTemplate.queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS + "' with parameter: " + username, e);
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
			return instance.jdbcTemplate.queryForObject(
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
				throw new DataAccessException("There are multiple users with the same username.");
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_GET_USER_PERSONAL + "' with parameter: " + username, e);
		}
	}
}