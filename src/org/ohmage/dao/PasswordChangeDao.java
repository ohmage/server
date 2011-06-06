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

import jbcrypt.BCrypt;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Changes the user's password.
 * 
 * @author John Jenkins
 */
public class PasswordChangeDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(PasswordChangeDao.class);
	
	private static final String SQL = "UPDATE user " + 
									  "SET password = ? " +
									  "WHERE username = ?";
	
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
