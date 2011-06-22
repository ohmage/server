package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Gets whether or not the user has personal information.
 * 
 * @author John Jenkins
 */
public class UserPersonalExistsDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserPersonalExistsDao.class);
	
	public static final String SQL_GET_USER_PERSONAL =
		"SELECT EXISTS(" +
			"SELECT up.first_name " +
			"FROM user u, user_personal up " +
			"WHERE u.username = ? " +
			"AND u.id = up.user_id" +
		")";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public UserPersonalExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets whether or not the user has personal information.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the user from the request parameters.
		String user;
		try {
			user = (String) awRequest.getToProcessValue(InputKeys.USER);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required key: " + InputKeys.USER);
			throw new DataAccessException(e);
		}
		
		try {
			// Get whether or not the user has any personal information.
			Boolean result = (Boolean) getJdbcTemplate().queryForObject(
					SQL_GET_USER_PERSONAL, 
					new Object[] { user }, 
					Boolean.class);
			
			// Create a list for the result.
			List<Boolean> resultList = new LinkedList<Boolean>();
			resultList.add(result);
			
			// Set the result list in the request.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_PERSONAL + "' with parameter: " + user);
			throw new DataAccessException(e);
		}
	}
}