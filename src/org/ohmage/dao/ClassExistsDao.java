package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;

/**
 * Checks if a class exists.
 * 
 * @author John Jenkins
 */
public class ClassExistsDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassExistsDao.class);
	
	private static final String SQL_GET_CLASS_EXISTS =
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public ClassExistsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the class' ID from the request, queries the database to see if it 
	 * exists, and returns a result list in the request of length 1 with a
	 * Boolean value representing whether not it exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the class' ID from the request..
		String classId;
		try {
			classId = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required key: " + InputKeys.CLASS_URN, e);
			throw new DataAccessException(e);
		}
		
		try {
			// Get whether or not it exists.
			Boolean result = (Boolean) getJdbcTemplate().queryForObject(SQL_GET_CLASS_EXISTS, new Object[] { classId }, Boolean.class);
			
			// Create the result list and add the boolean to it.
			List<Boolean> resultList = new LinkedList<Boolean>();
			resultList.add(result);
			
			// Set the result list in the request.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CLASS_EXISTS + "' with parameter: " + classId, e);
			throw new DataAccessException(e);
		}
	}

}
