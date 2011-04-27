package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import edu.ucla.cens.awserver.request.UserInfoQueryAwRequest;

/**
 * Aggregates all the information about a series of users in the request.
 * 
 * @author John Jenkins
 */
public class UserInfoAggregationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserInfoAggregationDao.class);
	
	private static final String SQL_GET_PRIVILEGED_CLASSES = "SELECT distinct(c.name) " +
											  			     "FROM class c, user u, user_class uc, user_class_role ucr " +
											  			     "WHERE u.login_id = ? " +
											  			     "AND u.id = uc.user_id " +
											  			     "AND uc.class_id = c.id " +
											  			     "AND ucr.role = 'privileged'";
	
	private static final String SQL_GET_USER_CREATION_PRIVILEGE = "SELECT campaign_creation_privilege " +
															   	  "FROM user u " +
															   	  "WHERE u.login_id = ?";
	
	private static final String SQL_GET_USER_CLASSES = "SELECT distinct(c.urn), c.name " +
	  												   "FROM class c, user u, user_class uc " +
	  												   "WHERE u.login_id = ? " +
	  												   "AND u.id = uc.user_id " +
	  												   "AND uc.class_id = c.id";
	
	private static final String SQL_GET_USER_ROLES = "SELECT distinct(ucr.role) " +
												     "FROM user u, user_class uc, user_class_role ucr " +
												     "WHERE u.login_id = ? " +
												     "AND u.id = uc.user_id " +
												     "AND uc.user_class_role_id = ucr.id";
	
	// This is acceptable to build on the fly, because we are getting input
	// from the database and not from a user.
	private String SQL_GET_ALL_USERS_FOR_CLASS_TEMPLATE = "SELECT distinct(u.login_id) " +
											   		      "FROM class c, user u, user_class uc " +
											   		      "WHERE c.id = uc.class_id " +
											   		      "AND uc.user_id = u.id " +
											   		      "AND c.name in (";
	
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
		public ClassAndUrn(String name, String urn) {
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
		// TODO: Make this use the toValidate map.
		if(! (awRequest instanceof UserInfoQueryAwRequest)) {
			throw new DataAccessException("UserInfoAggregationDao's only accept UserInfoQueryAwRequest objects.");
		}
		UserInfoQueryAwRequest request = (UserInfoQueryAwRequest) awRequest;
		
		List<?> privilegedClasses;
		try {
			// Get all the classes for which the logged in user is a supervisor.
			privilegedClasses = getJdbcTemplate().query(SQL_GET_PRIVILEGED_CLASSES, 
														new Object[] { request.getUser().getUserName() },
														new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_PRIVILEGED_CLASSES + "' with parameter: " + request.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
				
		String classes = null;
		if(privilegedClasses.size() == 0) {
			classes = "''";
		}
		else {
			ListIterator<?> privilegedClassesIter = privilegedClasses.listIterator();
			while(privilegedClassesIter.hasNext()) {
				if(classes == null) {
					classes = "";
				}
				else {
					classes += ", ";
				}
				classes += "'" + ((String) privilegedClassesIter.next()) + "'";
			}
		}
		classes += ")";
		
		// Check that all the users he is requesting information about are in one of those classes.
		String sqlGetAllUsersForClasses = SQL_GET_ALL_USERS_FOR_CLASS_TEMPLATE + classes;
		List<?> applicableUsers;
		try {
			applicableUsers = getJdbcTemplate().query(sqlGetAllUsersForClasses, new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + sqlGetAllUsersForClasses + "'", dae);
			throw new DataAccessException(dae);
		}
		
		String[] usersBeingQueried = request.getUsernames();
		for(int i = 0; i < usersBeingQueried.length; i++) {
			if(! usersBeingQueried[i].equals(request.getUser().getUserName())) {
				boolean userFound = false;
				
				ListIterator<?> applicableUsersIter = applicableUsers.listIterator();
				while(applicableUsersIter.hasNext()) {
					String applicableUser = (String) applicableUsersIter.next();
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
		UserInfoQueryResult queryResult = request.getUserInfoQueryResult();
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
			
			// Get roles.
			JSONArray rolesJson = new JSONArray();
			try {
				List<?> rolesList = getJdbcTemplate().query(SQL_GET_USER_ROLES, new Object[] { usersBeingQueried[i] }, new SingleColumnRowMapper());
				ListIterator<?> rolesListIter = rolesList.listIterator();
				while(rolesListIter.hasNext()) {
					rolesJson.put((String) rolesListIter.next()); 
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error while calling SQL '" + SQL_GET_USER_ROLES + "' with parameter: " + usersBeingQueried[i], dae);
				throw new DataAccessException(dae);
			}
			
			// Add user.
			queryResult.addUser(usersBeingQueried[i], permissionsJson, classesJson, rolesJson);
		}
	}
}
