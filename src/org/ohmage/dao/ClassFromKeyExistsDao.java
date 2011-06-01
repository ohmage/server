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


/**
 * Checks if a class exists given a URN.
 * 
 * @author John Jenkins
 */
public class ClassFromKeyExistsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassFromKeyExistsDao.class);
	
	public static final String KEY_CLASS_EXISTS_DAO = "key_class_exists_dao";
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM class " +
									  "WHERE urn = ?";

	private boolean _required;
	
	/**
	 * Sets up this DAO with a DataSource to use for querying and a key from
	 * which to get the URN.
	 * 
	 * @param dataSource The data source that will be queried when queries are
	 * 					 run.
	 * 
	 * @param key The key to use to get the URN from the request.
	 */
	public ClassFromKeyExistsDao(DataSource dataSource, boolean required) {
		super(dataSource);

		_required = required;
	}

	/**
	 * Checks to make sure that the campaign in question exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the URN if it exists and error out of it doesn't but is
		// required.
		String urn;
		try {
			urn = (String) awRequest.getToProcessValue(KEY_CLASS_EXISTS_DAO);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new DataAccessException("The required key '" + KEY_CLASS_EXISTS_DAO + "' is missing.");
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the requested class exists: " + urn);
		
		// Check if it exists and, if not sets it as a failed request.
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { urn }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL + "' with parameter: " + urn, dae);
			throw new DataAccessException(dae);
		}
	}
}
