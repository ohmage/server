package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Check that the user is an admin or privileged in each of the classes in the
 * list.
 * 
 * @author John Jenkins
 */
public class UserIsAdminOrPrivilegedInClassesInListDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserIsAdminOrPrivilegedInClassesInListDao.class);
	
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
	 * Default constructor that sets up the DataSource that queries against
	 * the database will use.
	 * 
	 * @param dataSource The DataSource used in queries against the database.
	 */
	public UserIsAdminOrPrivilegedInClassesInListDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks that the user is an admin or is privileged in each of the classes
	 * in the list. 
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
		
		// Get the class' ID from the request.
		String classIdList;
		try {
			classIdList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing validated class URN in toProcess map.", e);
			throw new DataAccessException(e);
		}
		
		// If the class list is empty, return;
		if("".equals(classIdList)) {
			return;
		}
		
		String[] classIdArray = classIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
		
		// Check if the user is privileged in any of the classes.
		for(int i = 0; i < classIdArray.length; i++) {
			try {
				if(! (Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_IS_PRIVILEGED_IN_CLASS, new Object[] { username, classIdArray[i] }, Boolean.class)) {
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL_GET_USER_IS_PRIVILEGED_IN_CLASS '" + SQL_GET_USER_IS_PRIVILEGED_IN_CLASS + "' with parameters: " + 
						username + ", " + classIdArray[i], e);
				throw new DataAccessException(e);
			}
		}
	}
}