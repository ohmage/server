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

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DocumentReadContentsAwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Gets the URL of the document being requested and stores it in the toReturn
 * map.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentReadContentsDao.class);
	
	private static final String SQL_GET_DOCUMENT_URL = "SELECT url " +
													   "FROM document " +
													   "WHERE uuid = ?";
	
	/**
	 * Creates this service.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public DocumentReadContentsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the URL from the database and puts it in the toReturn map.
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
		
		List<?> urlList;
		try {
			urlList = getJdbcTemplate().query(SQL_GET_DOCUMENT_URL, 
											  new Object[] { documentId }, 
											  new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_DOCUMENT_URL + "' with parameter: " + documentId, e);
			throw new DataAccessException(e);
		}
		
		if(urlList.size() == 0) {
			_logger.error("Document doesn't exist, but this should have been caught sooner.");
			awRequest.setFailedRequest(true);
		}
		else if(urlList.size() > 1) {
			_logger.error("Data integrity error. More than one document has the same UUID.");
			awRequest.setFailedRequest(true);
		}
		else {
			awRequest.addToReturn(DocumentReadContentsAwRequest.KEY_DOCUMENT_FILE, urlList.listIterator().next(), true);
		}
	}

}
