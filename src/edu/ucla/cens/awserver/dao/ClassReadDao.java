package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.ClassInfo;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Reads the information about each of the classes in the list and sets the
 * appropriate values in the request.
 * 
 * FIXME: This does no checking on the ACL, and needs to be updated to do so:
 * 	- For each class in the list,
 * 		- If the user is privileged and the user_personal=true flag is set, 
 * 			return all of the users' class role, first name, last name,
 * 			organization, personal ID, and email address.
 * 		- If the user is restricted and the user_personal=true flag is set,
 * 			return all of the users' class role, first name, last name,
 * 			organization, and email address. 
 * 
 * @author John Jenkins
 */
public class ClassReadDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassReadDao.class);
	
	private static final String SQL_GET_CLASS_INFO = "SELECT name, description " +
													 "FROM class " +
													 "WHERE urn = ?";
	
	private static final String SQL_GET_USERS_AND_CLASS_ROLES = "SELECT u.login_id, ucr.role " +
																"FROM user u, user_class uc, user_class_role ucr, class c " +
																"WHERE u.id = uc.user_id " +
																"AND c.id = uc.class_id " + 
																"AND ucr.id = uc.user_class_role_id " +
																"AND c.urn = ?";
	
	/**
	 * Inner class for getting the information from the database abotu a
	 * class.
	 * 
	 * @author John Jenkins
	 */
	private class ClassInformation {
		public String _name;
		public String _description;
		
		public ClassInformation(String name, String description) {
			_name = name;
			_description = description;
		}
	}
	
	private class UserInformation {
		public String _loginId;
		public String _classRole;
		
		public UserInformation(String loginId, String classRole) {
			_loginId = loginId;
			_classRole = classRole;
		}
	}
	
	/**
	 * Creates the DAO with the provided DataSource.
	 * 
	 * @param dataSource The DataSource to use when making this request.
	 */
	public ClassReadDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets then basic information about each class, then gets the users in
	 * the class and their respective roles and populates the toReturn map
	 * with all this information. Each entry in the toReturn map is the URN
	 * of the class as the key and a UserInfo object as the value. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the class list as a String.
		String classUrnList;
		try {
			classUrnList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required class URN list in the request. This should have been caught earlier.");
			throw new DataAccessException(e);
		}
		
		// Parse the class list into an array and traverse the array.
		String[] classUrnArray = classUrnList.split(",");
		for(int i = 0; i < classUrnArray.length; i++) {
			// Get the class' information.
			ClassInformation classInformation;
			try {
				classInformation = (ClassInformation) getJdbcTemplate().queryForObject(SQL_GET_CLASS_INFO, 
												new Object[] { classUrnArray[i] }, 
												new RowMapper() {
													@Override
													public Object mapRow(ResultSet rs, int row) throws SQLException {
														return new ClassInformation(rs.getString("name"), rs.getString("description"));
													}
												});
			}
			catch(org.springframework.dao.DataAccessException e){
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_INFO + "' with parameter: " + classUrnArray[i]);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			// Create a new ClassInfo object that will be put into the
			// toReturn map.
			ClassInfo classInfo = new ClassInfo(classUrnArray[i], classInformation._name, classInformation._description);
			
			// Get all the users in this class and their class role.
			List<?> userAndRole;
			try {
				userAndRole = getJdbcTemplate().query(SQL_GET_USERS_AND_CLASS_ROLES, 
														new Object[] { classUrnArray[i] }, 
														new RowMapper() {
															@Override
															public Object mapRow(ResultSet rs, int row) throws SQLException {
																return new UserInformation(rs.getString("login_id"), rs.getString("role"));
															}
														});
			}
			catch(org.springframework.dao.DataAccessException e){
				_logger.error("Error executing SQL '" + SQL_GET_USERS_AND_CLASS_ROLES + "' with parameter: " + classUrnArray[i]);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			// For each of the users add them to the current ClassInfo object.
			ListIterator<?> userAndRoleIter = userAndRole.listIterator();
			while(userAndRoleIter.hasNext()) {
				UserInformation userInformation = (UserInformation) userAndRoleIter.next();
				
				classInfo.addUser(userInformation._loginId, userInformation._classRole);
			}
			
			// Add it to the toReturn map to be returned to the user.
			awRequest.addToReturn(classUrnArray[i], classInfo, true);
		}
	}
}