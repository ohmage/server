package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.cache.CacheMissException;
import edu.ucla.cens.awserver.cache.SurveyResponsePrivacyStateCache;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Performs the update operation on a specific survey response. The class is named generically, but all an end user can change
 * is the privacy_state (for now).
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseUpdateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyResponseUpdateDao.class);
	
	private String _updateSql = "UPDATE survey_response "
		                        + " SET privacy_state_id = ? "
		                        + " WHERE id = ?";
	
	public SurveyResponseUpdateDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Survey response update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				
				int numberOfRowsUpdated = getJdbcTemplate().update(
					_updateSql, 
					new Object[] {
						SurveyResponsePrivacyStateCache.instance().lookup((String) awRequest.getToValidate().get(InputKeys.PRIVACY_STATE)),
						awRequest.getToValidate().get(InputKeys.SURVEY_KEY)}
				);
				
				if(numberOfRowsUpdated != 1) {
					_logger.error("expected to update only one row, but " + numberOfRowsUpdated + " were updated");
					awRequest.setFailedRequest(true);
					transactionManager.rollback(status);
					throw new DataAccessException("attempt to update an incorrect number of rows");
				}
				
				// Commit transaction.
				transactionManager.commit(status);
			
			}	
			catch (CacheMissException ce) {
				_logger.error("Error while reading from the cache.", ce);
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(ce);
			}
			
			catch (org.springframework.dao.DataAccessException dae) {
				
				_logger.error("a DataAccessException occurred when running the following sql '" + _updateSql + "' with the parameters "
					+ awRequest.getToValidate().get(InputKeys.PRIVACY_STATE) + ", " + awRequest.getToValidate().get(InputKeys.SURVEY_KEY), dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
				
			}
		}
	
		catch(TransactionException e) {
			
			_logger.error("Error while rolling back the transaction.", e);
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		
		}
	}
}
