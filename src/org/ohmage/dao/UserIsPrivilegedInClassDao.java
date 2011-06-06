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
public class UserIsPrivilegedInClassDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserIsPrivilegedInClassDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM user u, class c, user_class uc, user_class_role ucr " +
									  "WHERE u.username = ? " +
									  "AND uc.user_id = u.id " +
									  "AND ucr.id = uc.user_class_role_id " +
									  "AND ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "' " +
									  "AND c.id = uc.class_id " +
									  "AND c.urn = ?";
	
	/**
	 * Default constructor that sets up the DataSource that queries against
	 * the database will use.
	 * 
	 * @param dataSource The DataSource used in queries against the database.
	 */
	public UserIsPrivilegedInClassDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks that the user is privileged in the class in the request. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String classUrn;
		try {
			classUrn = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing validated class URN in toProcess map.");
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		}
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { awRequest.getUser().getUserName(), classUrn }) == 0) {
				awRequest.setFailedRequest(true);
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL + "' with parameters: " + awRequest.getUser().getUserName() + ", " + classUrn, e);
			throw new DataAccessException(e);
		}
	}

}
