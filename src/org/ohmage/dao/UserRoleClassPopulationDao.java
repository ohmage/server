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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.UserRoleClassResult;
import org.ohmage.request.AwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * Gets the list of classes that the currently logged in user belongs to and
 * sets its result list to that list.
 * 
 * @author John Jenkins
 */
public class UserRoleClassPopulationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleClassPopulationDao.class);
	
	private static final String SQL_GET_CLASSES_AND_ROLE = "SELECT c.urn, c.name, c.description, ucr.role " +
														   "FROM user u, class c, user_class uc, user_class_role ucr " +
														   "WHERE u.username = ? " +
														   "AND u.id = uc.user_id " +
														   "AND c.id = uc.class_id " +
														   "AND ucr.id = uc.user_class_role_id";
	
	/**
	 * Sets up this DAO with the DataSource to use when running queries.
	 * 
	 * @param dataSource The DataSource to use to access the database.
	 */
	public UserRoleClassPopulationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Queries the database for all the classes the user belongs and their role
	 * in that class.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Querying database for class information and the currently logged in user's role in that class.");
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					SQL_GET_CLASSES_AND_ROLE,
					new Object[] { awRequest.getUser().getUserName() },
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleClassResult result = new UserRoleClassResult(rs.getString("urn"),
																				 rs.getString("name"),
																				 rs.getString("description"),
																				 rs.getString("role"));
							return result;
						}
					}
				)
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("There was an error executing SQL '" + SQL_GET_CLASSES_AND_ROLE + "' with parameter: " + awRequest.getUser().getUserName());
			throw new DataAccessException(e);
		}
	}

}
