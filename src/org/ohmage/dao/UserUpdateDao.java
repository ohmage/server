package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Updates the user's privileges, then, if any user personal information 
 * exists, it creates a new personal entry if necessary then updates the fields
 * appropriately.
 * 
 * @author John Jenkins
 */
public class UserUpdateDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserUpdateDao.class);
	
	private static final String SQL_GET_USER_ID = 
		"SELECT id " +
		"FROM user " +
		"WHERE username = ?";
	
	private static final String SQL_GET_PERSONAL_EXISTS = 
		"SELECT EXISTS(" +
			"SELECT up.id " +
			"FROM user u, user_personal up " +
			"WHERE u.username = ? " +
			"AND u.id = up.user_id" +
		")";
	
	private static final String SQL_INSERT_PERSONAL = 
		"INSERT INTO user_personal(user_id, first_name, last_name, organization, personal_id) " +
		"VALUES (?, ?, ?, ?, ?)";
	
	private static final String SQL_UPDATE_ADMIN = 
		"UPDATE user " +
		"SET admin = ? " +
		"WHERE username = ?";
	
	private static final String SQL_UPDATE_ENABLED = 
		"UPDATE user " +
		"SET enabled = ? " +
		"WHERE username = ?";
	
	private static final String SQL_UPDATE_NEW_ACCOUNT = 
		"UPDATE user " +
		"SET new_account = ? " +
		"WHERE username = ?";
	
	private static final String SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE = 
		"UPDATE user " +
		"SET campaign_creation_privilege = ? " +
		"WHERE username = ?";
	
	private static final String SQL_UPDATE_FIRST_NAME =
		"UPDATE user u, user_personal up " +
		"SET up.first_name = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	private static final String SQL_UPDATE_LAST_NAME =
		"UPDATE user u, user_personal up " +
		"SET up.last_name = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	private static final String SQL_UPDATE_ORGANIZATION =
		"UPDATE user u, user_personal up " +
		"SET up.organization = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	private static final String SQL_UPDATE_PERSONAL_ID =
		"UPDATE user u, user_personal up " +
		"SET up.personal_id = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	private static final String SQL_UPDATE_EMAIL_ADDRESS =
		"UPDATE user u, user_personal up " +
		"SET up.email_address = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	private static final String SQL_UPDATE_JSON_DATA =
		"UPDATE user u, user_personal up " +
		"SET up.json_data = ? " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public UserUpdateDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets each of the fields from the request then updates the permission
	 * fields first. Then, it creates a new personal entry if necessary or 
	 * updates the existing one.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the user for the request.
		String user;
		try {
			user = (String) awRequest.getToProcessValue(InputKeys.USER);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.USER);
			throw new DataAccessException(e);
		}
		
		// Get all the permissions values.
		String admin = null;
		try {
			admin = (String) awRequest.getToProcessValue(InputKeys.USER_ADMIN);
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String enabled = null;
		try {
			enabled = (String) awRequest.getToProcessValue(InputKeys.USER_ENABLED);
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String newAccount = null;
		try {
			newAccount = (String) awRequest.getToProcessValue(InputKeys.NEW_ACCOUNT);
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String campaignCreationPrivilege = null;
		try {
			campaignCreationPrivilege = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_CREATION_PRIVILEGE);
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		boolean anyPersonalParamExists = false;
		
		// Get all of the personal values.
		String firstName = null;
		try {
			firstName = (String) awRequest.getToProcessValue(InputKeys.FIRST_NAME);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String lastName = null;
		try {
			lastName = (String) awRequest.getToProcessValue(InputKeys.LAST_NAME);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String organization = null;
		try {
			organization = (String) awRequest.getToProcessValue(InputKeys.ORGANIZATION);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String personalId = null;
		try {
			personalId = (String) awRequest.getToProcessValue(InputKeys.PERSONAL_ID);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String emailAddress = null;
		try {
			emailAddress = (String) awRequest.getToProcessValue(InputKeys.EMAIL_ADDRESS);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		String jsonData = null;
		try {
			jsonData = (String) awRequest.getToProcessValue(InputKeys.USER_JSON_DATA);
			anyPersonalParamExists = true;
		}
		catch(IllegalArgumentException e) {
			// Do nothing.
		}
		
		// Begin the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new document entry.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
		
			// Update the necessary permission fields.
			if(admin != null) {
				int adminInt = ("true".equals(admin)) ? 1 : 0;
				try {
					getJdbcTemplate().update(SQL_UPDATE_ADMIN, new Object[] { adminInt, user });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_UPDATE_ADMIN + "' with parameters: " + adminInt + ", " + user, e);
					throw new DataAccessException(e);
				}
			}
			
			if(enabled != null) {
				int enabledInt = ("true".equals(enabled)) ? 1 : 0;
				try {
					getJdbcTemplate().update(SQL_UPDATE_ENABLED, new Object[] { enabledInt, user });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_UPDATE_ENABLED + "' with parameters: " + enabledInt + ", " + user, e);
					throw new DataAccessException(e);
				}
			}
			
			if(newAccount != null) {
				int newAccountInt = ("true".equals(newAccount)) ? 1 : 0;
				try {
					getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, new Object[] { newAccountInt, user });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_UPDATE_NEW_ACCOUNT + "' with parameters: " + newAccountInt + ", " + user, e);
					throw new DataAccessException(e);
				}
			}
			
			if(campaignCreationPrivilege != null) {
				int campaignCreationPrivilegeInt = ("true".equals(campaignCreationPrivilege)) ? 1 : 0;
				try {
					getJdbcTemplate().update(SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE, new Object[] { campaignCreationPrivilegeInt, user });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE + "' with parameters: " + campaignCreationPrivilegeInt + ", " + user, e);
					throw new DataAccessException(e);
				}
			}
			
			// Figure out if a personal entry exists for the user.
			if(anyPersonalParamExists) {
				Boolean userPersonalExists;
				try {
					userPersonalExists = (Boolean) getJdbcTemplate().queryForObject(SQL_GET_PERSONAL_EXISTS, new Object[] { user }, Boolean.class);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_PERSONAL_EXISTS + "' with parameter: " + user, e);
					throw new DataAccessException(e);
				}
				
				if(userPersonalExists) {
					// If so, update the existing one's required fields.
					if(firstName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_FIRST_NAME, new Object[] { firstName, user });
						}
						catch(org.springframework.dao.DataAccessException e) {
							_logger.error("Error executing SQL '" + SQL_UPDATE_FIRST_NAME + "' with parameters: " + firstName + ", " + user, e);
							throw new DataAccessException(e);
						}
					}
					
					if(lastName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_LAST_NAME, new Object[] { lastName, user });
						}
						catch(org.springframework.dao.DataAccessException e) {
							_logger.error("Error executing SQL '" + SQL_UPDATE_LAST_NAME + "' with parameters: " + lastName + ", " + user, e);
							throw new DataAccessException(e);
						}
					}
					
					if(organization != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_ORGANIZATION, new Object[] { organization, user });
						}
						catch(org.springframework.dao.DataAccessException e) {
							_logger.error("Error executing SQL '" + SQL_UPDATE_ORGANIZATION + "' with parameters: " + organization + ", " + user, e);
							throw new DataAccessException(e);
						}
					}
					
					if(personalId != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_PERSONAL_ID, new Object[] { personalId, user });
						}
						catch(org.springframework.dao.DataAccessException e) {
							_logger.error("Error executing SQL '" + SQL_UPDATE_PERSONAL_ID + "' with parameters: " + personalId + ", " + user, e);
							throw new DataAccessException(e);
						}
					}
				}
				else {
					// If not, create a new one with the specified values.
					long userId;
					try {
						userId = getJdbcTemplate().queryForLong(SQL_GET_USER_ID, new Object[] { user });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameters: " + user, e);
						throw new DataAccessException(e);
					}
					
					try {
						getJdbcTemplate().update(SQL_INSERT_PERSONAL, new Object[] { userId, firstName, lastName, organization, personalId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_PERSONAL + "' with parameters: " + 
								userId + ", " + firstName + ", " + lastName + ", " + organization + ", " + personalId, e);
						throw new DataAccessException(e);
					}
				}
				
				
				// Either, we just created a new one or one already existed, and 
				// a previous validator guaranteed that either it already existed 
				// or that enough parameters existed to create a new one. So, we 
				// can safely update the personal information.
				if(emailAddress != null) {
					try {
						getJdbcTemplate().update(SQL_UPDATE_EMAIL_ADDRESS, new Object[] { emailAddress, user });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_UPDATE_EMAIL_ADDRESS + "' with parameters: " + emailAddress + ", " + user, e);
						throw new DataAccessException(e);
					}
				}
				
				if(jsonData != null) {
					try {
						getJdbcTemplate().update(SQL_UPDATE_JSON_DATA, new Object[] { jsonData, user });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_UPDATE_JSON_DATA + "' with parameters: " + jsonData + ", " + user, e);
						throw new DataAccessException(e);
					}
				}
			}
			
			// Commit it all.
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