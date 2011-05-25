package edu.ucla.cens.awserver.dao;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Gets the URNs for the classes that the user has the parameterized roles in.
 * 
 * @author John Jenkins
 */
public class FindAllClassesForCurrentUserWithRolesDao extends AbstractDao {
private static Logger _logger = Logger.getLogger(FindAllClassesForCurrentUserWithRolesDao.class);
	
	public static final String KEY_CLASSES_FOR_CURRENT_USER_WITH_ROLES = "key_classes_for_current_user_with_roles";
	
	private static final String SQL_GET_CLASSES_WITH_ROLES = "SELECT c.urn " +
    														 "FROM class c, user u, user_class uc, user_class_role ucr " +
    														 "WHERE uc.class_id = c.id " +
    														 "AND uc.user_id = u.id " +
    														 "AND u.login_id = ? " +
    														 "AND uc.user_class_role_id = ucr.id " +
    														 "AND ucr.role = ?";
	
	private List<String> _roles;

	/**
	 * Sets up this DAO with a DataSource to use for the query and a list of
	 * roles to find for the currently logged in user.
	 * 
	 * @param dataSource The DataSource to use to run the query.
	 * 
	 * @param roles The roles to search for for this user.
	 */
	public FindAllClassesForCurrentUserWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The list of roles must be non-null and non-empty.");
		}
		
		_roles = roles;
	}
	
	/**
	 * Creates a Set of type String with the URNs of the classes to which the
	 * user belongs with the set of roles that were set in the constructor.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		Set<String> result = new HashSet<String>();
		
		for(String role : _roles) {
			List<?> classes;
			try {
				classes = getJdbcTemplate().query(SQL_GET_CLASSES_WITH_ROLES, 
												  new Object[] { awRequest.getUser().getUserName(), role },
												  new SingleColumnRowMapper());
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASSES_WITH_ROLES + "' with parameters: " +
						awRequest.getUser().getUserName() + ", " + role, e);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			ListIterator<?> classesIter = classes.listIterator();
			while(classesIter.hasNext()) {
				result.add((String) classesIter.next());
			}
		}
		
		awRequest.addToProcess(KEY_CLASSES_FOR_CURRENT_USER_WITH_ROLES, result, true);
	}
}