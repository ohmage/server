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
import org.ohmage.util.StringUtils;


/**
 * Checks that all users in the request based on the key given when this
 * object was built exist in the database.
 * 
 * @author John Jenkins
 */
public class UsersInListExistDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UsersInListExistDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM user " +
									  "WHERE username = ?";
	
	private String _key;
	private boolean _required;
	
	/**
	 * Sets up this DAO with the DataSource to use for querying and the key
	 * to use to get the list from the request object.
	 * 
	 * @param dataSource The DataSource to run our queries against.
	 * 
	 * @param key The key to use to find the list in the request.
	 */
	public UsersInListExistDao(DataSource dataSource, String key, boolean required) {
		super(dataSource);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("A key must be given.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Gets the list of users from the request with the locally stored key and
	 * checks that all the users in the list exist in the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String usersList;
		try {
			usersList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("Missing users list in request using key: " + _key);
				throw new DataAccessException(e);
			}
			else {
				return;
			}
		}
		
		String[] users = usersList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < users.length; i++) {
			try {
				if(getJdbcTemplate().queryForInt(SQL, new Object[] { users[i] }) == 0) {
					_logger.info("No such user: " + users[i]);
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL + "' with parameter: " + users[i]);
				throw new DataAccessException(e);
			}
		}
	}
}
