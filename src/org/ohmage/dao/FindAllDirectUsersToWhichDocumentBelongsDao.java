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
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Gets the list of users to which this document is directly associated and 
 * returns it as the result list in the request.
 * 
 * @author John Jenkins
 */
public class FindAllDirectUsersToWhichDocumentBelongsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDirectUsersToWhichDocumentBelongsDao.class);
	
	private static final String SQL_GET_DIRECT_USERS_FOR_DOCUMENT = "SELECT u.login_id " +
																	"FROM user u, document d, document_user_role dur " +
																	"WHERE d.uuid = ? " +
																	"AND d.id = dur.document_id " +
																	"AND u.id = dur.user_id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public FindAllDirectUsersToWhichDocumentBelongsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the list of users that are directly associated with this document
	 * and stores the list in the result list of the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the document's ID.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID);
			throw new DataAccessException(e);
		}
		
		// Set the result list with all the campaigns with which this document
		// is associated.
		try {
			awRequest.setResultList(getJdbcTemplate().query(SQL_GET_DIRECT_USERS_FOR_DOCUMENT, new Object[] { documentId }, new SingleColumnRowMapper()));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_DIRECT_USERS_FOR_DOCUMENT + "' with parameter: " + documentId, e);
			throw new DataAccessException(e);
		}
	}
}
