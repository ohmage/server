package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.exception.DataAccessException;
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
public final class UserClassDaos extends Dao {
	private static final Logger LOGGER = Logger.getLogger(UserClassDaos.class);
	
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
	
	// Retrieves the ID and name of all of the classes to which a user belongs.
	private static final String SQL_GET_CLASS_ID_AND_NAMES_FOR_USER =
		"SELECT c.urn, c.name " +
		"FROM user u, class c, user_class uc " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND c.id = uc.class_id";
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static UserClassDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserClassDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
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
	public static Boolean userBelongsToClass(String classId, String username) throws DataAccessException {
		try {
			return (Boolean) instance.getJdbcTemplate().queryForObject(SQL_EXISTS_USER_CLASS, new Object[] { username, classId }, Boolean.class);
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
	public static List<String> getUsersInClass(String classId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_USER_CLASS, new Object[] { classId }, new SingleColumnRowMapper<String>());
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
	public static ClassRoleCache.Role getUserClassRole(String classId, String username) throws DataAccessException {
		try {
			return ClassRoleCache.Role.getValue(instance.getJdbcTemplate().queryForObject(SQL_GET_USER_ROLE, new Object[] { username, classId }, String.class));
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
	public static Map<String, String> getClassIdsAndNameForUser(String username) throws DataAccessException {
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			instance.getJdbcTemplate().query(
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
}