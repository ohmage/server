package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks that the user in the request exists.
 * 
 * @author John Jenkins
 */
public class UserExistsDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(UserExistsDao.class);
	
	private static final String SQL_GET_USER = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public UserExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks if the user exists in the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String user;
		try {
			user = (String) awRequest.getToProcessValue(InputKeys.USER);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("User is missing from the request.");
		}
		
		try {
			if(getJdbcTemplate().queryForInt(SQL_GET_USER, new Object[] { user }) == 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER + "' with parameter: " + user, e);
			throw new DataAccessException(e);
		}
	}
}