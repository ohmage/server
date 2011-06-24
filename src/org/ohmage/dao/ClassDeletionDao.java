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
 * Deletes the class in the request.
 * 
 * @author John Jenkins
 */
public class ClassDeletionDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassDeletionDao.class);
	
	private static final String SQL_DELETE_CLASS = 
		"DELETE FROM class " +
		"WHERE urn = ?";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public ClassDeletionDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Deletes the class in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		final String classId;
		try {
			classId = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key: " + InputKeys.CLASS_URN, e);
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the class.
			try {
				getJdbcTemplate().update(SQL_DELETE_CLASS, new Object[] { classId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_DELETE_CLASS + "' with parameters: " +
						classId, e);
				transactionManager.rollback(status);
				throw new DataAccessException(e);
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