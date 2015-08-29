/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.Clazz.Role;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserClassQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-class relationships. While it may read 
 * information pertaining to other entities, the information it takes and
 * provides should pertain to user-class relationships only.
 * 
 * @author John Jenkins
 * @author Hongsuda T.
 */
public final class UserClassQueries extends Query implements IUserClassQueries {
	private static final Logger LOGGER = Logger.getLogger(UserClassQueries.class);
	
	// Returns a boolean representing whether or not a user is associated with
	// a class in any capacity.
	private static final String SQL_EXISTS_USER_CLASS = 
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM user u, class c, user_class uc " +
			"WHERE u.username = ? " +
			"AND c.urn = ? " +
			"AND u.id = uc.user_id " +
			"AND c.id = uc.class_id" +
		")";
	
	// Returns all of the users in a class.
	private static final String SQL_GET_USER_CLASS = 
		"SELECT u.username " +
		"FROM user u, class c, user_class uc " +
		"WHERE c.urn = ? " +
		"AND c.id = uc.class_id " +
		"AND u.id = uc.user_id";
	
	// Returns the user's role in a class.
	private static final String SQL_GET_USER_ROLE = 
		"SELECT ucr.role " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE u.username = ? " +
		"AND c.urn = ? " +
		"AND u.id = uc.user_id " +
		"AND c.id = uc.class_id " +
		"AND uc.user_class_role_id = ucr.id";
	
	// Retrieves the class roles for some user in a list of classes. The user
	// of this SQL, but append a comma-separated parenthetical with the number
	// of supplied classes.
	private static final String SQL_GET_DISTINCT_USER_ROLES =
		"SELECT DISTINCT(ucr.role) " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND ucr.id = uc.user_class_role_id " +
		"AND c.id = uc.class_id " +
		"AND c.urn in ";
	
	// Retrieves the ID and name of all of the classes to which a user belongs.
	private static final String SQL_GET_CLASS_ID_AND_NAMES_FOR_USER =
		"SELECT c.urn, c.name " +
		"FROM user u, class c, user_class uc " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND c.id = uc.class_id";
	
	// Retrieves the ID and name of all of the classes to which a user belongs.
	private static final String SQL_GET_CLASS_IDS_FOR_USER_WITH_ROLE =
		"SELECT c.urn " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND c.id = uc.class_id " +
		"AND uc.user_class_role_id = ucr.id " +
		"AND ucr.role = ?";
	
	// Retrieves all of the classes and their respective role for a user. Each 
	// rows is a unique class-role combination.
	private static final String SQL_GET_CLASSES_AND_ROLE_FOR_USER =
			"SELECT c.urn, ucr.role " +
			"FROM user u, class c, user_class uc, user_class_role ucr " +
			"WHERE u.username = ? " +
			"AND u.id = uc.user_id " +
			"AND uc.class_id = c.id " +
			"AND uc.user_class_role_id = ucr.id";
	
	// Inserts a single user into a single class with a given role.
	private static final String SQL_INSERT_USER_CLASS =
			"INSERT INTO user_class(user_id, class_id, user_class_role_id) " +
			"VALUES (" +
				"(" +
					"SELECT id " +
					"FROM user " +
					"WHERE username = ?" +
				")," +
				"(" +
					"SELECT id " +
					"FROM class " +
					"WHERE urn = ?" +
				")," +
				"(" +
					"SELECT id " +
					"FROM user_class_role " +
					"WHERE role = ?" +
				")" +
			")";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserClassQueries(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Queries the database to see if a user belongs to a class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @param username The username for the user.
	 * 
	 * @return Whether or not the user belongs to the class.
	 */
	public boolean userBelongsToClass(String classId, String username) throws DataAccessException {
		try {
			return (Boolean) getJdbcTemplate().queryForObject(SQL_EXISTS_USER_CLASS, new Object[] { username, classId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_USER_CLASS + "' with parameters: " + username + ", " + classId, e);
		}
	}
	
	/**
	 * Retrieves all of the users in a class.
	 * 
	 * @param classId The unique identifier for the class.
	 * 
	 * @return Returns a List of usernames of all of the users in a class.
	 */
	public List<String> getUsersInClass(String classId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(SQL_GET_USER_CLASS, new Object[] { classId }, new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_CLASS + "' with parameters: " + classId, e);
		}
	}
	
	/**
	 * Queries the database to get the role of a user in a class. If a user 
	 * doesn't have a role in a class, null is returned.
	 * 
	 * @param classId A class' unique identifier.
	 * 
	 * @param username A the username of the user whose role is being checked.
	 * 
	 * @return Returns the user's role in the class unless they have no role in
	 * 		   the class in which case null is returned.
	 */
	public Clazz.Role getUserClassRole(String classId, String username) throws DataAccessException {
		try {
			return Clazz.Role.getValue(getJdbcTemplate().queryForObject(SQL_GET_USER_ROLE, new Object[] { username, classId }, String.class));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				LOGGER.error("A user has more than one role in a class.", e);
				throw new DataAccessException(e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_ROLE + "' with parameters: " + username + ", " + classId, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserClassQueries#getUserClassRoles(java.lang.String, java.util.Set)
	 */
	public Set<Clazz.Role> getUserClassRoles(
			final String username, 
			final Set<String> classIds) 
			throws DataAccessException {
		
		if((classIds == null) || (classIds.size() == 0)) {
			return Collections.emptySet();
		}
		
		String sql = 
				SQL_GET_DISTINCT_USER_ROLES + 
				StringUtils.generateStatementPList(classIds.size());
		
		List<Object> parameters = 
				new ArrayList<Object>(classIds.size() + 1);
		parameters.add(username);
		parameters.addAll(classIds);
		
		try {
			return new HashSet<Clazz.Role>(
					getJdbcTemplate().query(
						sql, 
						parameters.toArray(), 
						new RowMapper<Clazz.Role>() {
							/**
							 * Converts the class role string to a class Role 
							 * object.
							 */
							@Override
							public Clazz.Role mapRow(ResultSet rs, int rowNum)
									throws SQLException {
								
								try {
									return Clazz.Role.getValue(
											rs.getString("role"));
								}
								catch(IllegalArgumentException e) {
									throw new SQLException(
											"Unknown role in the database.", 
											e);
								}
							}
					
						}
					)
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							sql + 
						"' with parameters: " + 
							parameters.toString(),
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserClassQueries#getClassAndRoleForUser(java.lang.String)
	 */
	@Override
	public Map<String, Clazz.Role> getClassesAndRolesForUser(
			final String username)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_CLASSES_AND_ROLE_FOR_USER,
					new Object[] { username },
					new ResultSetExtractor<Map<String, Clazz.Role>>() {
						/**
						 * Extracts the data creating the class ID to class 
						 * role map.
						 */
						@Override
						public Map<String, Role> extractData(
								final ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							Map<String, Clazz.Role> result =
									new HashMap<String, Clazz.Role>();
							
							while(rs.next()) {
								Clazz.Role role;
								try {
									role = Clazz.Role.getValue(
											rs.getString("role"));
								}
								catch(IllegalArgumentException e) {
									throw new SQLException(
											"The role is not a valid role.",
											e);
								}
								
								result.put(rs.getString("urn"), role);
							}
							
							return result;
						}
					}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_CLASSES_AND_ROLE_FOR_USER
						+ "' with parameter: " + 
						username, 
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserClassQueries#getClassAndRoleForUserSet(java.lang.String)
	 */
	@Override
	public Map<String, Map<String, Clazz.Role>> getClassesAndRolesForUsers(
			final String userSubSelectStmt,
			final Collection<Object> userSubSelectParameters)
			throws DataAccessException {
		
		StringBuilder sql = 
				new StringBuilder(
						"SELECT u.username, c.urn, ucr.role " +
						"FROM user u join user_class uc on (u.id = uc.user_id) " +
						" JOIN class c on (c.id = uc.class_id) " + 
						" JOIN user_class_role ucr on (ucr.id = uc.user_class_role_id) " +
						"WHERE " +
						  "u.username in ");
		sql.append(" ( " + userSubSelectStmt + " )");
		
		try {
			final Map<String, Map<String, Clazz.Role>> userClassRoleMap = 
					new HashMap<String, Map<String, Clazz.Role>>();
			
			getJdbcTemplate().query(
					sql.toString(), 
					userSubSelectParameters.toArray(), 
					new RowMapper<Object>() {
						@Override
						public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							try {
								
								String username = rs.getString("username");
								String urn = rs.getString("urn");
								String role = rs.getString("role");
								
								// create Class.role
								Map<String, Clazz.Role> classRoles = userClassRoleMap.get(username);
								if (classRoles == null) {
									classRoles = new HashMap<String, Clazz.Role>();
									userClassRoleMap.put(username, classRoles);
								} 
								
								try { 
									Clazz.Role crole = Clazz.Role.getValue(role);
									classRoles.put(urn, crole);								
								} catch (IllegalArgumentException e) {
									throw new SQLException("The role is invalid:" + role, e);
								}
								
								return null;
							} 
							catch (Exception e) {
								throw new SQLException("Can't create a role with parameter: " + 
										rs.getString("username") + "," + rs.getString("urn") + "," + rs.getString("role"), e);
							}
						}
					}
				);
			return userClassRoleMap;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + 
					"' with parameters: " + userSubSelectStmt, e);
		}
		
	}
	
	/**
	 * Returns the set of all class IDs and their names with which a user is
	 * associated.
	 * 
	 * @param username The user's username.
	 * 
	 * @return A, possibly empty but never null, map of class unique 
	 * 		   identifiers to their name for all of the classes to which a user
	 * 		   is associated.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Map<String, String> getClassIdsAndNameForUser(String username) throws DataAccessException {
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			getJdbcTemplate().query(
					SQL_GET_CLASS_ID_AND_NAMES_FOR_USER, 
					new Object[] { username }, 
					new RowMapper<Object> () {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							result.put(rs.getString("urn"), rs.getString("name"));
							return null;
						}
					}
				);
			
			return result;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CLASS_ID_AND_NAMES_FOR_USER + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Retrieves the list of class identifiers for a user with a given role in
	 * that class.
	 * 
	 * @param username The user's username.
	 *  
	 * @param role The user's class role.
	 * 
	 * @return The list of class identifiers.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public List<String> getClassIdsForUserWithRole(
			final String username, final Clazz.Role role) 
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_CLASS_IDS_FOR_USER_WITH_ROLE, 
					new Object[] { username, role.toString() },
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_CLASS_IDS_FOR_USER_WITH_ROLE + 
						"' with parameters: " + 
						username + 
						", " + 
						role.toString(), 
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserClassQueries#addUserToClassWithRole(java.lang.String, java.lang.String, org.ohmage.domain.Clazz.Role)
	 */
	public void addUserToClassWithRole(
			final String username,
			final String classId,
			final Clazz.Role classRole)
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Adding a user to a class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
					new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				getJdbcTemplate().update(
						SQL_INSERT_USER_CLASS, 
						new Object[] { 
								username, 
								classId, 
								classRole.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + 
							SQL_INSERT_USER_CLASS + 
							"' with parameters: " +
							username + ", " + 
							classId + ", " + 
							classRole.toString(), 
						e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while committing the transaction.", 
						e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException(
					"Error while attempting to rollback the transaction.", 
					e);
		}
	}
}