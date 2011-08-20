package org.ohmage.dao;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class is responsible for creating, reading, updating, and deleting
 * survey responses.
 * 
 * @author John Jenkins
 */
public class SurveyResponseDaos extends Dao {
	// Deletes a survey response and subsequently all prompt response 
	// references.
	private static final String SQL_DELETE_SURVEY_RESPONSE =
		"DELETE FROM survey_response " +
		"WHERE id = ?";
	
	private static SurveyResponseDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private SurveyResponseDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Deletes a survey response.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static void deleteSurveyResponse(Long surveyResponseId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new campaign.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(instance.getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				instance.getJdbcTemplate().update(
						SQL_DELETE_SURVEY_RESPONSE, 
						new Object[] { surveyResponseId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing SQL '" + SQL_DELETE_SURVEY_RESPONSE + "' with parameter: " + surveyResponseId, e);
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
