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


/**
 * Checks that the document given by an ID in the request exists.
 * 
 * @author John Jenkins
 */
public class DocumentExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentExistsDao.class);
	
	private static final String SQL_EXISTS_DOCUMENT_ID = "SELECT EXISTS( " +
														 " SELECT * " + 
														 " FROM document " +
														 " WHERE uuid = ?)";
	
	private boolean _required;
	
	/**
	 * Creates the DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 * 
	 * @param required Whether or not this check is required.
	 */
	public DocumentExistsDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Ensures that the key exists if required, and then checks that it exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		} 
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new DataAccessException("Missing required key '" + InputKeys.DOCUMENT_ID + "'");
			}
			else {
				return;
			}
		}
		
		try {
			if(getJdbcTemplate().queryForInt(SQL_EXISTS_DOCUMENT_ID, new Object[] { documentId }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_EXISTS_DOCUMENT_ID + "' with parameter: " + documentId, e);
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		}
	}
}