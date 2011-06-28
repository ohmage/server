package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.request.ClassRosterReadRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.RowMapper;

/**
 * Gets the class roster information for each of the classes in the list.
 * 
 * @author John Jenkins
 */
public class ClassRosterReadDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(ClassRosterReadDao.class);
	
	private static final String SQL_GET_USERS_AND_ROLE_FOR_CLASS =
		"SELECT u.username, ucr.role " +
		"FROM user u, class c, user_class uc, user_class_role ucr " +
		"WHERE c.urn = ? " +
		"AND uc.class_id = c.id " +
		"AND uc.user_id = u.id " +
		"AND uc.user_class_role_id = ucr.id";
	
	private final class UserAndRole {
		public final String user;
		public final String role;
		
		public UserAndRole(String user, String role) {
			this.user = user;
			this.role = role;
		}
	}
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public ClassRosterReadDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the list of classes from the request. Then, gets the users and 
	 * their role for each of the classes in the list. Finally, it stores all
	 * the information in a JSONObject in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Building the class roster.");
		
		String classIdList;
		try {
			classIdList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key: " + InputKeys.CLASS_URN_LIST);
		}
		
		JSONObject result = new JSONObject();
		String[] classIdArray = classIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < classIdArray.length; i++) {
			// Get the information about the class.
			List<?> userAndRoleList;
			try {
				userAndRoleList = getJdbcTemplate().query(
						SQL_GET_USERS_AND_ROLE_FOR_CLASS, 
						new Object[] { classIdArray[i] },
						new RowMapper() {
							@Override
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								return new UserAndRole(rs.getString("username"), rs.getString("role"));
							}
						}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USERS_AND_ROLE_FOR_CLASS + "' with parameter: " + classIdArray[i], e);
				throw new DataAccessException(e);
			}
			
			// Create a JSONObject.
			JSONObject currClass = new JSONObject();
			
			// Add each of the users to the current class JSONObject.
			ListIterator<?> userAndRoleListIter = userAndRoleList.listIterator();
			while(userAndRoleListIter.hasNext()) {
				UserAndRole userAndRole = (UserAndRole) userAndRoleListIter.next();
				
				try {
					currClass.put(userAndRole.user, userAndRole.role);
				}
				catch(JSONException e) {
					_logger.error("There was an error adding a user and their role to the resulting JSONObject.", e);
					throw new DataAccessException(e);
				}
			}
			
			// Add the current JSONObjec to the result JSONObject.
			try {
				result.put(classIdArray[i], currClass);
			}
			catch(JSONException e) {
				_logger.error("There was an error adding a class' roster to the result list.");
				throw new DataAccessException(e);
			}
		}
		
		// Store the result in the request.
		awRequest.addToReturn(ClassRosterReadRequest.KEY_RESULT, result, true);
	}
}