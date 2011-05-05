package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.cache.ClassRoleCache;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Checks if a user is privileged in the single class in the request.
 * 
 * @author John Jenkins
 */
public class UserIsPrivilegedInClassDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserIsPrivilegedInClassDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM user u, class c, user_class uc, user_class_role ucr " +
									  "WHERE u.login_id = ? " +
									  "AND uc.user_id = u.id " +
									  "AND ucr.id = uc.user_class_role_id " +
									  "AND ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "' " +
									  "AND c.id = uc.class_id " +
									  "AND c.urn = ?";
	
	/**
	 * Default constructor that sets up the DataSource that queries against
	 * the database will use.
	 * 
	 * @param dataSource The DataSource used in queries against the database.
	 */
	public UserIsPrivilegedInClassDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks that the user is privileged in the class in the request. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String classUrn;
		try {
			classUrn = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing validated class URN in toProcess map.");
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		}
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { awRequest.getUser().getUserName(), classUrn }) == 0) {
				awRequest.setFailedRequest(true);
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL + "' with parameters: " + awRequest.getUser().getUserName() + ", " + classUrn, e);
			throw new DataAccessException(e);
		}
	}

}
