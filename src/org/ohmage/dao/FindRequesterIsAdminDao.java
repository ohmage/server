package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;

/**
 * Gets whether or not the requesting user is an admin.
 * 
 * @author John Jenkins
 */
public class FindRequesterIsAdminDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(FindRequesterIsAdminDao.class);
	
	private static final String SQL_GET_ADMIN = 
		"SELECT admin " +
		"FROM user " +
		"WHERE username = ?";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public FindRequesterIsAdminDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets whether or not the requester is an admin and returns it in the 
	 * result list with a lenght of one.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			// Get the admin privilege.
			Boolean isAdmin = (Boolean) getJdbcTemplate().queryForObject(
					SQL_GET_ADMIN, 
					new Object[] { awRequest.getUser().getUserName() },
					Boolean.class);
			
			// Put it in a List.
			List<Boolean> resultList = new LinkedList<Boolean>();
			resultList.add(isAdmin);
			
			// Return the List.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.DataIntegrityViolationException e) {
			_logger.error("More than one user has the same username.", e);
			throw new DataAccessException(e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_ADMIN + "' with parameter: " + awRequest.getUser().getUserName(), e);
			throw new DataAccessException(e);
		}
	}
}