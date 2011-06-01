/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
 * Performs the delete operation on a survey response. When a survey_response row is deleted, the delete cascades to all of the 
 * prompt_responses linked to that survey_reponse.
 *  
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(SurveyResponseDeleteDao.class);
	
	private String _deleteSql = "DELETE FROM survey_response WHERE id = ?";
	
	public SurveyResponseDeleteDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Deleting a survey response (and its associated prompt responses).");
		
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Survey response delete.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				
				int numberOfRowsDeleted = getJdbcTemplate().update(
					_deleteSql, 
					new Object[] { Integer.parseInt((String) awRequest.getToValidate().get(InputKeys.SURVEY_KEY)) }
				);
				
				if(numberOfRowsDeleted == 0) {
					_logger.error("Expected to delete at least one row, but none were deleted.");
					awRequest.setFailedRequest(true);
					transactionManager.rollback(status);
					throw new DataAccessException("Delete operation didn't affect any rows");
				}
				
				_logger.info("Deleted " + numberOfRowsDeleted + " row(s).");
				
				// Commit transaction.
				transactionManager.commit(status);
			}	
			
			catch (org.springframework.dao.DataAccessException dae) {
				
				_logger.error("a DataAccessException occurred when running the following sql '" + _deleteSql + "' with the parameters "
					 + awRequest.getToValidate().get(InputKeys.SURVEY_KEY), dae);
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
