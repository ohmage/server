package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import jbcrypt.BCrypt;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Changes the user's password.
 * 
 * @author John Jenkins
 */
public class PasswordChangeDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PasswordChangeDao.class);
	
	private static final String SQL = "UPDATE user " + 
									  "SET password = ? " +
									  "WHERE login_id = ?";
	
	/**
	 * Sets the DataSource for this DAO to use when running its update.
	 * 
	 * @param dataSource The DataSource to use when updating this service.
	 */
	public PasswordChangeDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Changes the user's password.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String newPassword;
		try {
			newPassword = (String) awRequest.getToProcessValue(InputKeys.NEW_PASSWORD);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing new password in toProcess map.");
			throw new DataAccessException("Missing password in toProcess map.", e);
		}
		String newPasswordHashed = BCrypt.hashpw(newPassword, BCrypt.gensalt(13));
		
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("User's password update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(SQL, new Object[] { newPasswordHashed, awRequest.getUser().getUserName() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL + "' with parameters: " + newPasswordHashed + ", " + awRequest.getUser().getUserName(), e);
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			transactionManager.commit(status);
		}
		catch(TransactionException e) {
			_logger.error("Error while rolling back the transaction.", e);
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		}
	}

}
