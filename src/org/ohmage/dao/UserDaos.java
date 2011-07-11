package org.ohmage.dao;

import javax.sql.DataSource;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting users. While it may read information pertaining to
 * other entities, the information it takes and provides should pertain to 
 * users only.
 * 
 * @author John Jenkins
 */
public class UserDaos extends Dao {
	// Returns a boolean representing whether or not a user exists
	private static final String SQL_EXISTS_USER = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Returns a boolean representing whether a user is an admin or not. If the
	// user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_IS_ADMIN = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND admin = true" +
		")";
	
	// Returns a boolean representing whether a user can create campaigns or 
	// not. If the user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND campaign_creation_privilege = true" +
		")";
	
	// The single instance of this class as the constructor should only ever be
	// called once by Spring.
	private static UserDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Returns whether or not a user exists.
	 * 
	 * @param username The username for which to check.
	 * 
	 * @return Returns true if the user exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static Boolean userExists(String username) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForObject(
					SQL_EXISTS_USER, 
					new Object[] { username }, 
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Gets whether or not the user is an admin.
	 * 
	 * @param username The username to check.
	 * 
	 * @return Whether or not they are an admin.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	public static Boolean userIsAdmin(String username) throws DataAccessException {
		try {
			return instance.jdbcTemplate.queryForObject(
					SQL_EXISTS_USER_IS_ADMIN, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ADMIN + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Gets whether or not the user is allowed to create campaigns.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @return Whether or not the user can create campaigns.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	public static Boolean userCanCreateCampaigns(String username) throws DataAccessException {
		try {
			return (Boolean) instance.jdbcTemplate.queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS + "' with parameter: " + username, e);
		}
	}
}