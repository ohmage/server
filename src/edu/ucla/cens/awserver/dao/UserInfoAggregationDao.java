package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.domain.UserInfoQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.request.UserInfoQueryAwRequest;

/**
 * Aggregates all the information about a series of users in the request.
 * 
 * @author John Jenkins
 */
public class UserInfoAggregationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserInfoAggregationDao.class);
	
	private static final String SQL_GET_PRIVILEGED_CLASSES = "SELECT c.urn " +
											  			     "FROM class c, user u, user_class uc, user_class_role ucr " +
											  			     "WHERE u.login_id = ? " +
											  			     "AND u.id = uc.user_id " +
											  			     "AND uc.class_id = c.id " +
											  			     "AND uc.user_class_role_id = ucr.id " +
											  			     "AND ucr.role = 'privileged'";
	
	private static final String SQL_GET_USER_LOGINS_FOR_CLASS = "SELECT u.login_id " +
	      														"FROM class c, user u, user_class uc " +
	      														"WHERE c.urn = ? " +
	      														"AND c.id = uc.class_id " +
	      														"AND uc.user_id = u.id";
	
	private static final String SQL_GET_USER_CREATION_PRIVILEGE = "SELECT campaign_creation_privilege " +
															   	  "FROM user u " +
															   	  "WHERE u.login_id = ?";
	
	private static final String SQL_GET_USER_CLASSES = "SELECT c.urn, c.name " +
	  												   "FROM class c, user u, user_class uc " +
	  												   "WHERE u.login_id = ? " +
	  												   "AND u.id = uc.user_id " +
	  												   "AND uc.class_id = c.id";
	
	private static final String SQL_GET_CLASS_ROLES = "SELECT distinct(ucr.role) " +
												      "FROM user u, user_class uc, user_class_role ucr " +
												      "WHERE u.login_id = ? " +
												      "AND u.id = uc.user_id " +
												      "AND uc.user_class_role_id = ucr.id";
	
	private final static String SQL_GET_CAMPAIGN_ROLES = "SELECT distinct(ur.role) " +
													     "FROM user u, user_role ur, user_role_campaign urc " +
													     "WHERE u.login_id = ? " +
													     "AND u.id = urc.user_id " +
													     "AND urc.user_role_id = ur.id";
	
	/**
	 * Used when retrieving the user's class and the URN for that class.
	 * 
	 * @author John Jenkins
	 */
	private class ClassAndUrn {
		public String _name;
		public String _urn;
		
		/**
		 * Basic constructor for inlining.
		 * 
		 * @param name The name of the class.
		 * 
		 * @param urn The URN of the class.
		 */
		public ClassAndUrn(String urn, String name) {
			_name = name;
			_urn = urn;
		}
	}
	
	/**
	 * Default constructor that sets this DAO's DataSource.
	 * 
	 * @param dataSource The DataSource that this object will use when running
	 * 					 its queries.
	 */
	public UserInfoAggregationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Executes the query by aggregating all the applicable information for
	 * each of the users in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of usernames from the request.
		String usernames;
		try {
			usernames = (String) awRequest.getToProcessValue(InputKeys.USER_LIST);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing user in list in request.");
			throw new DataAccessException(e);
		}
		
		// Get all the classes that this user is privileged in.
		List<?> privilegedClasses;
		try {
			// Get all the classes for which the logged in user is a supervisor.
			privilegedClasses = getJdbcTemplate().query(SQL_GET_PRIVILEGED_CLASSES, 
														new Object[] { awRequest.getUser().getUserName() },
														new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_PRIVILEGED_CLASSES + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		// Get a union of all the users in all of the privileged classes.
		List<String> usersList = new LinkedList<String>();
		ListIterator<?> privilegedClassesIter = privilegedClasses.listIterator();
		while(privilegedClassesIter.hasNext()) {
			String currClassUrn = (String) privilegedClassesIter.next();
			
			List<?> classUsers;
			try {
				classUsers = getJdbcTemplate().query(SQL_GET_USER_LOGINS_FOR_CLASS, 
													 new Object[] { currClassUrn },
													 new SingleColumnRowMapper());			
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_USER_LOGINS_FOR_CLASS + "' with parameter: " + currClassUrn, dae);
				throw new DataAccessException(dae);
			}
			
			ListIterator<?> classUsersIter = classUsers.listIterator();
			while(classUsersIter.hasNext()) {
				String currClassUser = (String) classUsersIter.next();
				
				if(! usersList.contains(currClassUser)) {
					usersList.add(currClassUser);
				}
			}
		}
		
		// Validate that the currently logged in user has the correct
		// permissions to get information about each of the users in the
		// query.
		String[] usersBeingQueried = usernames.split(",");
		for(int i = 0; i < usersBeingQueried.length; i++) {
			if(! usersBeingQueried[i].equals(awRequest.getUser().getUserName())) {
				boolean userFound = false;
				
				ListIterator<?> usersListIter = usersList.listIterator();
				while(usersListIter.hasNext()) {
					String applicableUser = (String) usersListIter.next();
					if(applicableUser.equals(usersBeingQueried[i])) {
						userFound = true;
						break;
					}
				}
				
				if(!userFound) {
					awRequest.setFailedRequest(true);
					return;
				}
			}
		}
					
		// Get information about each of the users.
		UserInfoQueryResult queryResult = new UserInfoQueryResult();
		for(int i = 0; i < usersBeingQueried.length; i++) {
			// Get permissions.
			JSONObject permissionsJson = new JSONObject();
			try {
				int canCreate = getJdbcTemplate().queryForInt(SQL_GET_USER_CREATION_PRIVILEGE, new Object[] { usersBeingQueried[i] });
				permissionsJson.put("cancreate", canCreate == 1);	
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_USER_CREATION_PRIVILEGE + "' with parameter: " + usersBeingQueried[i], dae);
				throw new DataAccessException(dae);
			}
			catch(JSONException e) {
				_logger.error("Problem creating 'permissions' JSONObject.", e);
				throw new DataAccessException(e);
			}
			
			// Get classes.
			JSONObject classesJson = new JSONObject();
			try {
				List<?> classesList = getJdbcTemplate().query(SQL_GET_USER_CLASSES, 
															  new Object[] { usersBeingQueried[i] },
															  new RowMapper() {
															  		@Override
															   		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
															   			return new ClassAndUrn(rs.getString("urn"), rs.getString("name"));
															   		}
															  });
				ListIterator<?> classesListIter = classesList.listIterator();
				while(classesListIter.hasNext()) {
					ClassAndUrn cau = (ClassAndUrn) classesListIter.next();
					
					try {
						classesJson.put(cau._urn, cau._name);
					}
					catch(JSONException e) {
						_logger.error("Problem creating 'classes' JSONObject.", e);
						throw new DataAccessException(e);
					}
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_USER_CLASSES + "' with parameter: " + usersBeingQueried[i], dae);
				throw new DataAccessException(dae);
			}
			
			// Get class roles.
			JSONArray classRolesJson = new JSONArray();
			try {
				ListIterator<?> classRolesListIter = getJdbcTemplate().query(SQL_GET_CLASS_ROLES, 
																 			 new Object[] { usersBeingQueried[i] }, 
																 			 new SingleColumnRowMapper()).listIterator();
				while(classRolesListIter.hasNext()) {
					classRolesJson.put((String) classRolesListIter.next()); 
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_CLASS_ROLES + "' with parameter: " + usersBeingQueried[i], dae);
				throw new DataAccessException(dae);
			}
			
			// Get campaign roles.
			JSONArray campaignRolesJson = new JSONArray();
			try {
				ListIterator<?> campaignRolesListIter = getJdbcTemplate().query(SQL_GET_CAMPAIGN_ROLES, 
																				new Object[] { usersBeingQueried[i] }, 
																				new SingleColumnRowMapper()).listIterator();
				while(campaignRolesListIter.hasNext()) {
					campaignRolesJson.put((String) campaignRolesListIter.next());
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_CAMPAIGN_ROLES + "' with parameter: " + usersBeingQueried[i], dae);
				throw new DataAccessException(dae);
			}
			
			// Add user.
			queryResult.addUser(usersBeingQueried[i], permissionsJson, classesJson, classRolesJson, campaignRolesJson);
		}
		
		awRequest.addToProcess(UserInfoQueryAwRequest.RESULT, queryResult, true);
	}
}
