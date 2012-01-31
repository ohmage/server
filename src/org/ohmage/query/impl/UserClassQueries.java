package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserClassQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-class relationships. While it may read 
 * information pertaining to other entities, the information it takes and
 * provides should pertain to user-class relationships only.
 * 
 * @author John Jenkins
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
		"AND c.id in ";
	
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
	public Boolean userBelongsToClass(String classId, String username) throws DataAccessException {
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
}