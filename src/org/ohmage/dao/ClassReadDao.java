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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.domain.ClassInfo;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.RowMapper;


/**
 * Reads the information about each of the classes in the list and sets the
 * appropriate values in the request.
 * 
 * @author John Jenkins
 */
public class ClassReadDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassReadDao.class);
	
	private static final String SQL_GET_CLASS_INFO_AND_USER_ROLE = "SELECT c.name, c.description, ucr.role " +
													 			   "FROM user u, class c, user_class uc, user_class_role ucr " +
													 			   "WHERE c.urn = ? " +
													 			   "AND c.id = uc.class_id " +
													 			   "AND uc.user_id = u.id " +
													 			   "AND u.username = ? " +
													 			   "AND uc.user_class_role_id = ucr.id ";
	
	private static final String SQL_GET_USERS_AND_CLASS_ROLES = "SELECT u.username, ucr.role " +
																"FROM user u, user_class uc, user_class_role ucr, class c " +
																"WHERE u.id = uc.user_id " +
																"AND c.id = uc.class_id " + 
																"AND ucr.id = uc.user_class_role_id " +
																"AND c.urn = ?";
	
	/**
	 * Inner class for getting the information from the database abotu a
	 * class.
	 * 
	 * @author John Jenkins
	 */
	private class ClassInformation {
		public String _name;
		public String _description;
		public String _userRole;
		
		public ClassInformation(String name, String description, String role) {
			_name = name;
			_description = description;
			_userRole = role;
		}
	}
	
	private class UserInformation {
		public String _loginId;
		public String _classRole;
		
		public UserInformation(String loginId, String classRole) {
			_loginId = loginId;
			_classRole = classRole;
		}
	}
	
	/**
	 * Creates the DAO with the provided DataSource.
	 * 
	 * @param dataSource The DataSource to use when making this request.
	 */
	public ClassReadDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets then basic information about each class, then gets the users in
	 * the class and their respective roles and populates the toReturn map
	 * with all this information. Each entry in the toReturn map is the URN
	 * of the class as the key and a UserInfo object as the value. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the class list as a String.
		String classUrnList;
		try {
			classUrnList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required class URN list in the request. This should have been caught earlier.");
			throw new DataAccessException(e);
		}
		
		// Parse the class list into an array and traverse the array.
		String[] classUrnArray = classUrnList.split(InputKeys.LIST_ITEM_SEPARATOR);
		List<ClassInfo> classInfoList = new LinkedList<ClassInfo>();
		for(int i = 0; i < classUrnArray.length; i++) {
			// Get the class' information.
			ClassInformation classInformation;
			try {
				classInformation = (ClassInformation) getJdbcTemplate().queryForObject(SQL_GET_CLASS_INFO_AND_USER_ROLE, 
						new Object[] { classUrnArray[i], awRequest.getUser().getUserName() }, 
						new RowMapper() {
							@Override
							public Object mapRow(ResultSet rs, int row) throws SQLException {
								return new ClassInformation(rs.getString("name"),
															rs.getString("description"),
															rs.getString("role"));
							}
						}
				);
			}
			catch(org.springframework.dao.DataAccessException e){
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_INFO_AND_USER_ROLE + "' with parameters: " + 
						classUrnArray[i] + ", " + awRequest.getUser().getUserName(), e);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			// Create a new ClassInfo object that will be put into the
			// toReturn map.
			ClassInfo classInfo = new ClassInfo(classUrnArray[i], classInformation._name, classInformation._description);
			boolean includeUserRoles = classInformation._userRole.equals(ClassRoleCache.ROLE_PRIVILEGED);
			
			// Get all the users in this class and their class role.
			List<?> userAndRole;
			try {
				userAndRole = getJdbcTemplate().query(SQL_GET_USERS_AND_CLASS_ROLES, 
														new Object[] { classUrnArray[i] }, 
														new RowMapper() {
															@Override
															public Object mapRow(ResultSet rs, int row) throws SQLException {
																return new UserInformation(rs.getString("username"), rs.getString("role"));
															}
														});
			}
			catch(org.springframework.dao.DataAccessException e){
				_logger.error("Error executing SQL '" + SQL_GET_USERS_AND_CLASS_ROLES + "' with parameter: " + classUrnArray[i]);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			// For each of the users add them to the current ClassInfo object.
			ListIterator<?> userAndRoleIter = userAndRole.listIterator();
			while(userAndRoleIter.hasNext()) {
				UserInformation userInformation = (UserInformation) userAndRoleIter.next();
				
				classInfo.addUser(userInformation._loginId, ((includeUserRoles) ? userInformation._classRole : ""));
			}
			
			// Add it to the list of classes to be returned.
			classInfoList.add(classInfo);
		}
		
		// Store the results as the result list.
		awRequest.setResultList(classInfoList);
	}
}
