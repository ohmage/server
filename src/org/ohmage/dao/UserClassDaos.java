package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting user-class relationships. While it may read 
 * information pertaining to other entities, the information it takes and
 * provides should pertain to user-class relationships only.
 * 
 * @author John Jenkins
 */
public class UserClassDaos extends Dao {
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
	
	// Returns the user's role in a class.
	private static final String SQL_GET_USER_ROLE = 
		"SELECT ucr.role " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE u.username = ? " +
		"AND c.urn = ? " +
		"AND u.id = uc.user_id " +
		"AND c.id = uc.class_id " +
		"AND uc.user_class_role_id = ucr.id";
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static UserClassDaos instance;
	
	/**
	 * Sets up this DAO with a shared DataSource to use. This is called from
	 * Spring and is an error to call within application code.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	public UserClassDaos(DataSource dataSource) {
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
	public static Boolean userBelongsToClass(String classId, String username) {
		try {
			return (Boolean) instance.jdbcTemplate.queryForObject(SQL_EXISTS_USER_CLASS, new Object[] { username, classId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_USER_CLASS + "' with parameters: " + username + ", " + classId, e);
		}
	}
	
	/**
	 * Querys the database to get the role of a user in a class. If a user 
	 * doesn't have a role in a class, null is returned.
	 * 
	 * @param classId A class' unique identifier.
	 * 
	 * @param username A the username of the user whose role is being checked.
	 * 
	 * @return Returns the user's role in the class unless they have no role in
	 * 		   the class in which case null is returned.
	 */
	public static String userClassRole(String classId, String username) {
		try {
			return (String) instance.jdbcTemplate.queryForObject(SQL_GET_USER_ROLE, new Object[] { username, classId }, String.class);
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
}