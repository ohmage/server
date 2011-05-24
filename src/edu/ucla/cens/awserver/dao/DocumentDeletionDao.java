package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Deletes the document from the database.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentDeletionDao.class);
	
	private static final String SQL_DELETE_DOCUMENT = "DELETE FROM document " +
													  "WHERE uuid = ?";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource that is used when manipulating the
	 * 					 database.
	 */
	public DocumentDeletionDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Deletes the document from the database which will delete all the entries
	 * in the adjoining tables through database updates.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key '" + InputKeys.DOCUMENT_ID + "'.");
		}
		
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Document delete.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(SQL_DELETE_DOCUMENT, new Object[] { documentId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_DELETE_DOCUMENT + "' with parameter: " + documentId, e);
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
			
			// Commit transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				_logger.error("Error while committing the transaction.");
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
		}
		catch(TransactionException e) {
			_logger.error("Error while rolling back the transaction.", e);
			throw new DataAccessException(e);
		}
	}
}