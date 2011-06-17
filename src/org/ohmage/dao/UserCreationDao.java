package org.ohmage.dao;

import javax.sql.DataSource;

import jbcrypt.BCrypt;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class UserCreationDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserCreationDao.class);
	
	private static final String SQL_INSERT_USER = 
		"INSERT INTO user(username, password, admin, enabled, new_account, campaign_creation_privilege) " +
		"VALUES(?,?,?,?,?,?)";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public UserCreationDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Creating a new user.");
		
		// Get the new user's username.
		String username;
		try {
			username = (String) awRequest.getToProcessValue(InputKeys.NEW_USERNAME);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.NEW_USERNAME);
			throw new DataAccessException(e);
		}
		
		// Get the new user's password.
		String plainTextPassword;
		try {
			plainTextPassword = (String) awRequest.getToProcessValue(InputKeys.NEW_PASSWORD);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.NEW_PASSWORD);
			throw new DataAccessException(e);
		}
		String password = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(13));
		
		// Get whether or not this user should be an admin.
		String adminString;
		try {
			adminString = (String) awRequest.getToProcessValue(InputKeys.USER_ADMIN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.USER_ADMIN);
			throw new DataAccessException(e);
		}
		boolean admin = "true".equals(adminString);
		
		// Get whether or not this user should be enabled.
		String enabledString;
		try {
			enabledString = (String) awRequest.getToProcessValue(InputKeys.USER_ENABLED);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.USER_ENABLED);
			throw new DataAccessException(e);
		}
		boolean enabled = "true".equals(enabledString);
		
		// Get whether or not this account should be considered new or not.
		String newAccountString;
		try {
			newAccountString = (String) awRequest.getToProcessValue(InputKeys.NEW_ACCOUNT);
		}
		catch(IllegalArgumentException e) {
			// This is an optional parameter, so we will just set the default.
			newAccountString = "true";
		}
		boolean newAccount = !"false".equals(newAccountString);
		
		// Get whether or not this user should have campaign creation 
		// privileges.
		String campaignCreationPrivilegeString;
		try {
			campaignCreationPrivilegeString = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
		}
		catch(IllegalArgumentException illegalArgumentException) {
			// This an optional parameter, so we will just get the default.
			try {
				campaignCreationPrivilegeString = PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE);
			}
			catch(CacheMissException cacheMissException) {
				_logger.error("Cache doesn't know about 'known' value: " + PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE);
				throw new DataAccessException(cacheMissException);
			}
		}
		boolean campaignCreationPrivilege = "true".equals(campaignCreationPrivilegeString);
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				getJdbcTemplate().update(SQL_INSERT_USER, new Object[] { username, password, admin, enabled, newAccount, campaignCreationPrivilege });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_INSERT_USER + "' with parameters: " +
						username + ", " + password + ", " + admin + ", " + enabled + ", " + newAccount + ", " + campaignCreationPrivilege, e);
				transactionManager.rollback(status);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				_logger.error("Error while committing the transaction.", e);
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
		}
		catch(TransactionException e) {
			_logger.error("Error while attempting to rollback the transaction.");
			throw new DataAccessException(e);
		}
	}
}