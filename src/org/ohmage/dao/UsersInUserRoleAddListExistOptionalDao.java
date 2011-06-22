package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks the user-role add list to ensure that all of the users in the list
 * exist.
 * 
 * @author John Jenkins
 */
public class UsersInUserRoleAddListExistOptionalDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UsersInUserRoleAddListExistOptionalDao.class);
	
	private static final String SQL_GET_USER_EXISTS = 
		"SELECT EXISTS(" +
			"SELECT login_id " +
			"FROM user " +
			"WHERE login_id = ?" +
		")";
	
	private final boolean _required;
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource that is used to query the database.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public UsersInUserRoleAddListExistOptionalDao(DataSource dataSource, boolean required) {
		super(dataSource);
		
		_required = required;
	}

	/**
	 * Gets the list if it exists and ensures that each of the users in the 
	 * list exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list as a String.
		String userRoleListString;
		try {
			userRoleListString = (String) awRequest.getToProcessValue(InputKeys.USER_ROLE_LIST_ADD);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("The required key is missing from the request: " + InputKeys.USER_ROLE_LIST_ADD);
				throw new DataAccessException(e);
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the users in the user-role add list all exist.");
		
		// For each of the users in the list, ensure that they exist in the
		// database.
		String[] userRoleListArray = userRoleListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < userRoleListArray.length; i++) {
			String[] userRoleArray = userRoleListArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_USER_EXISTS, new Object[] { userRoleArray[0] }) == 0) {
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USER_EXISTS + "' with parameter: " + userRoleArray[0], e);
				throw new DataAccessException(e);
			}
		}
	}
}