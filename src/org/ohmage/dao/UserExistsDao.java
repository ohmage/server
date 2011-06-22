package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Gets whether or not the user exists in the database.
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
	 * Gets whether or not the user exists and returns the result as the only
	 * element in the result list in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			int intResult = getJdbcTemplate().queryForInt(SQL_GET_USER, new Object[] { awRequest.getToProcessValue(InputKeys.USER_ID) });
			
			List<Boolean> result = new LinkedList<Boolean>();
			result.add(intResult != 0);
			
			awRequest.setResultList(result);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER + "' with parameter: " + awRequest.getToProcessValue(InputKeys.USER_ID), e);
			throw new DataAccessException(e);
		}
	}
}