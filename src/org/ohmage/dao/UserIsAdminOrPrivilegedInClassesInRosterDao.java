package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks that either the user is an admin or privileged in each of the classes
 * in the parameterized list.
 * 
 * @author John Jenkins
 */
public class UserIsAdminOrPrivilegedInClassesInRosterDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserIsAdminOrPrivilegedInClassesInRosterDao.class);
	
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
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public UserIsAdminOrPrivilegedInClassesInRosterDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks whether this user is an admin and, if not, if they are privileged
	 * in each of the classes in the list.
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
		
		// Get the roster.
		String roster;
		try {
			roster = (String) awRequest.getToProcessValue(InputKeys.ROSTER);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing validated class URN in toProcess map.", e);
			throw new DataAccessException(e);
		}
		
		// If the roster is empty, then all is well.
		if("".equals(roster)) {
			return;
		}
		
		// Split the roster into each of the lines and evaluate the class ID at
		// that line.
		String[] rosterLines = roster.split("\n");
		for(int i = 0; i < rosterLines.length; i++) {
			String[] rosterLine = rosterLines[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			try {
				if(! (Boolean) getJdbcTemplate().queryForObject(
						SQL_GET_USER_IS_PRIVILEGED_IN_CLASS, 
						new Object[] { username, rosterLine[0] }, 
						Boolean.class)) {
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL_GET_USER_IS_PRIVILEGED_IN_CLASS '" + SQL_GET_USER_IS_PRIVILEGED_IN_CLASS + "' with parameters: " + 
						username + ", " + rosterLine[0], e);
				throw new DataAccessException(e);
			}
		}
	}
}