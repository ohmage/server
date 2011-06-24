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
 * Creates a new class.
 * 
 * @author John Jenkins
 */
public class ClassCreationDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassCreationDao.class);
	
	private static final String SQL_INSERT_CLASS =
		"INSERT INTO class(urn, name, description) " +
		"VALUES (?,?,?)";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public ClassCreationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Inserts a new class object into the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the required class ID.
		String classId;
		try {
			classId = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required paramter: " + InputKeys.CLASS_URN, e);
			throw new DataAccessException(e);
		}
		
		// Get the required class name.
		String className;
		try {
			className = (String) awRequest.getToProcessValue(InputKeys.CLASS_NAME);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.CLASS_NAME, e);
			throw new DataAccessException(e);
		}
		
		// Get the optional description.
		String description = null;
		try {
			description = (String) awRequest.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch(IllegalArgumentException e) {
			// This is an optional parameter.
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the class.
			try {
				getJdbcTemplate().update(SQL_INSERT_CLASS, new Object[] { classId, className, description });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_INSERT_CLASS + "' with parameters: " +
						classId + ", " + className + ", " + description, e);
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