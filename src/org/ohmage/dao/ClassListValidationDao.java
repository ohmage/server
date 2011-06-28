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
 * DAO for checking the list of classes associated with a campaign creation
 * request. Checks to ensure that the user has the ability to create campaigns
 * associated with the list of classes and that the classes exist.
 * 
 * @author John Jenkins
 */
public class ClassListValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassListValidationDao.class);
	
	private static final String SQL_GET_USER_IS_ADMIN =
		"SELECT admin " +
		"FROM user " +
		"WHERE username = ?";
	
	private static final String SQL_CLASS_COUNT = "SELECT EXISTS(" +
												  	"SELECT * " +
												  	"FROM class " +
												  	"WHERE urn = ?" +
												  ")";
	
	private static final String SQL_USER_IN_CLASS = "SELECT EXISTS(" +
														"SELECT * " +
														"FROM user u, class c, user_class uc " +
														"WHERE c.urn = ? " +
														"AND u.username = ? " +
														"AND c.id = uc.class_id " +
														"AND u.id = uc.user_id" +
													")";
	
	private boolean _required;
	
	/**
	 * Sets up the data source for this DAO.
	 * 
	 * @param dataSource The data source that will be used to query the
	 * 					 database for information.
	 */
	public ClassListValidationDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Checks to ensure that this user has the appropriate permissions to
	 * create a class associated with the list of classes and that those
	 * classes exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of classes.
		String classesAsString;
		try {
			classesAsString = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("Missing required class URN list in toProcess map.");
				throw new DataAccessException(e);
			}
			else {
				_logger.info("No class list in the toProcess map, so skipping service validation.");
				return;
			}
		}
		
		// Check if the user is an admin.
		final boolean admin;
		String username = awRequest.getUser().getUserName();
		try {
			// If the user is an admin, return.
			admin = (Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_IS_ADMIN, new Object[] { username }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_IS_ADMIN + "' with parameter: " + username, e);
			throw new DataAccessException(e);
		}
		
		// For each class in the list,
		String[] classes = classesAsString.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		for(int i = 0; i < classes.length; i++) {
			// Ensure that the class exists.
			try {
				if(getJdbcTemplate().queryForInt(SQL_CLASS_COUNT, new Object[] { classes[i] }) == 0) {
					_logger.info("Class does not exist: " + classes[i]);
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_CLASS_COUNT + "' with parameter: " + classes[i], dae);
				throw new DataAccessException(dae);
			}
			
			// If they aren't an admin, ensure that they are in the class.
			if(! admin) {
				try {
					if(getJdbcTemplate().queryForInt(SQL_USER_IN_CLASS, new Object [] { classes[i], username }) == 0) {
						_logger.error("User does not belong to this class: " + classes[i]);
						awRequest.setFailedRequest(true);
						return;
					}
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_USER_IN_CLASS + "' with parameters: " + classes[i] + ", " + username, dae);
					throw new DataAccessException(dae);
				}
			}
		}
	}
}
