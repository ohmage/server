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
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks if a user is privileged in the single class in the request.
 * 
 * @author John Jenkins
 */
public class UserIsAdminOrPrivilegedInClassDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserIsAdminOrPrivilegedInClassDao.class);
	
	private static final String SQL_GET_USER_IS_ADMIN =
		"SELECT admin " +
		"FROM user " +
		"WHERE username = ?";
	
	private static final String SQL_GET_USER_IS_PRIVILEGED_IN_CLASS = 
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM user u, class c, user_class uc, user_class_role ucr " +
			"WHERE u.username = ? " +
			"AND uc.user_id = u.id " +
			"AND ucr.id = uc.user_class_role_id " +
			"AND ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "' " +
			"AND c.id = uc.class_id " +
			"AND c.urn = ?" +
		")";
	
	/**
	 * Default constructor that sets up the DataSource that queries against
	 * the database will use.
	 * 
	 * @param dataSource The DataSource used in queries against the database.
	 */
	public UserIsAdminOrPrivilegedInClassDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks that the user is privileged in the class in the request. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the requester's username.
		String username = awRequest.getUser().getUserName();
		
		// Check if the user is an admin.
		try {
			// If the user is an admin, return.
			if((Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_IS_ADMIN, new Object[] { username }, Boolean.class)) {
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_IS_ADMIN + "' with parameter: " + username, e);
			throw new DataAccessException(e);
		}
		
		// Get the class' ID from the request.
		String classUrn;
		try {
			classUrn = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing validated class URN in toProcess map.", e);
			throw new DataAccessException(e);
		}
		
		// Check if the user is privileged in the class.
		try {
			if(! (Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_IS_PRIVILEGED_IN_CLASS, new Object[] { username, classUrn }, Boolean.class)) {
				awRequest.setFailedRequest(true);
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL_GET_USER_IS_PRIVILEGED_IN_CLASS '" + SQL_GET_USER_IS_PRIVILEGED_IN_CLASS + "' with parameters: " + 
					username + ", " + classUrn, e);
			throw new DataAccessException(e);
		}
	}
}