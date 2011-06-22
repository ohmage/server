package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.UserBin;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Deletes the user's in the user list in the request.
 * 
 * @author John Jenkins
 */
public class UserDeletionDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserDeletionDao.class);
	
	private static final String SQL_DELETE_USER = 
		"DELETE FROM user " +
		"WHERE username = ?";
	
	private final String _key;
	private final UserBin _userBin;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param key The key to use to get the user list from the request.
	 */
	public UserDeletionDao(DataSource dataSource, String key, UserBin userBin) {
		super(dataSource);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		if(userBin == null) {
			throw new IllegalArgumentException("The user bin cannot be null.");
		}
		
		_key = key;
		_userBin = userBin;
	}

	/**
	 * Deletes all the users in the list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String userListString;
		try {
			userListString = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + _key);
			throw new DataAccessException(e);
		}
		
		if("".equals(userListString)) {
			return;
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
		
			boolean includesRequester = false;
			String requestersUsername = awRequest.getUser().getUserName();
			String[] userList = userListString.split(InputKeys.LIST_ITEM_SEPARATOR);
			for(int i = 0; i < userList.length; i++) {
				try {
					getJdbcTemplate().update(SQL_DELETE_USER, new Object[] { userList[i] });
					
					if(requestersUsername.equals(userList[i])) {
						includesRequester = true;
					}
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_DELETE_USER + "' with parameter: " + userList[i], e);
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
			}
			
			// If the user was in the list of users, remove their token from
			// the bin.
			if(includesRequester) {
				_userBin.removeUser(awRequest.getUserToken());
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
