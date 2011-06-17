package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks whether the new username in the request already exists.
 * 
 * @author John Jenkins
 */
public class NewUsernameExistsDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(NewUsernameExistsDao.class);
	
	private static final String SQL_GET_USERNAME =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username=?" +
		")";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public NewUsernameExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Returns whether or not this username already exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the username.
		String newUsername;
		try {
			newUsername = (String) awRequest.getToProcessValue(InputKeys.NEW_USERNAME);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required key: " + InputKeys.NEW_USERNAME);
			throw new DataAccessException(e);
		}
		
		try {
			// Get whether or not the username exists.
			Boolean usernameExists = (Boolean) getJdbcTemplate().queryForObject(
					SQL_GET_USERNAME, 
					new Object[] { newUsername }, 
					Boolean.class);
			
			// Create the result list.
			List<Boolean> resultList = new LinkedList<Boolean>();
			resultList.add(usernameExists);
			
			// Set the result list in the request.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.DataIntegrityViolationException e) {
			_logger.error("More than one user have the same username.", e);
			throw new DataAccessException(e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL'" + SQL_GET_USERNAME + "' with parameter: " + newUsername, e);
			throw new DataAccessException(e);
		}
	}
}