package edu.ucla.cens.awserver.dao;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.UserInfoQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserInfoQueryAwRequest;

public class UserInfoAggregationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserInfoAggregationDao.class);
	
	private static final String _sqlGetSupervisorClasses = "SELECT distinct(c.name) " +
											  "FROM class c, user u, user_class uc " +
											  "WHERE u.login_id = ? " +
											  "AND u.id = uc.user_id " +
											  "AND uc.class_id = c.id " +
											  "AND uc.class_role in ('supervisor')";
	
	private static final String _sqlGetUserCreationPrivilege = "SELECT campaign_creation_privilege " +
															   "FROM user u " +
															   "WHERE u.login_id = ?";
	
	private static final String _sqlGetUserClasses = "SELECT distinct(c.name) " +
	  												 "FROM class c, user u, user_class uc " +
	  												 "WHERE u.login_id = ? " +
	  												 "AND u.id = uc.user_id " +
	  												 "AND uc.class_id = c.id";
	
	private static final String _sqlGetUserRoles = "SELECT distinct(uc.class_role) " +
												   "FROM user u, user_class uc " +
												   "WHERE u.login_id = ? " +
												   "AND u.id = uc.user_id ";
	
	// This is acceptable to build on the fly, because we are getting input
	// from the database and not from a user.
	private String _sqlGetAllUsersForClassesTemplate = "SELECT distinct(u.login_id) " +
											   		   "FROM class c, user u, user_class uc " +
											   		   "WHERE c.id = uc.class_id " +
											   		   "AND uc.user_id = u.id " +
											   		   "AND c.name in (";
	
	public UserInfoAggregationDao(DataSource dataSource) {
		super(dataSource);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		if(! (awRequest instanceof UserInfoQueryAwRequest)) {
			throw new DataAccessException("UserInfoAggregationDao's only accept UserInfoQueryAwRequest objects.");
		}
		UserInfoQueryAwRequest request = (UserInfoQueryAwRequest) awRequest;
		
		List<LinkedHashMap<String, String>> supervisorClasses;
		try {
			// Get all the classes for which the logged in user is a supervisor.
			supervisorClasses = (List<LinkedHashMap<String, String>>) getJdbcTemplate().queryForList(_sqlGetSupervisorClasses, new Object[] { request.getUser().getUserName() });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			throw new DataAccessException("Problem calling SQL '" + _sqlGetSupervisorClasses + "' with parameter: " + request.getUser().getUserName(), dae);
		}
				
		String classes = null;
		if(supervisorClasses.size() == 0) {
			classes = "''";
		}
		else {
			ListIterator<LinkedHashMap<String, String>> supervisorClassesIter = supervisorClasses.listIterator();
			while(supervisorClassesIter.hasNext()) {
				if(classes == null) {
					classes = "";
				}
				else {
					classes += ", ";
				}
				classes += "'" + supervisorClassesIter.next().get("name") + "'";
			}
		}
		classes += ")";
		
		// Check that all the users he is requesting information about are in one of those classes.
		String sqlGetAllUsersForClasses = _sqlGetAllUsersForClassesTemplate + classes;
		List<LinkedHashMap<String, String>> applicableUsers;
		try {
			applicableUsers = (List<LinkedHashMap<String, String>>) getJdbcTemplate().queryForList(sqlGetAllUsersForClasses);
		}
		catch(org.springframework.dao.DataAccessException dae) {
			throw new DataAccessException("Problem calling SQL '" + sqlGetAllUsersForClasses + "'", dae);
		}
				
		String[] usersBeingQueried = request.getUsernames();
		for(int i = 0; i < usersBeingQueried.length; i++) {
			if(! usersBeingQueried[i].equals(request.getUser().getUserName())) {
				boolean userFound = false;
				
				for(int j = 0; j < applicableUsers.size(); j++) {
					if(applicableUsers.get(j).get("login_id").equals(usersBeingQueried[i])) {
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
				int canCreate = getJdbcTemplate().queryForInt(_sqlGetUserCreationPrivilege, new Object[] { usersBeingQueried[i] });
				permissionsJson.put("cancreate", canCreate == 1);	
			}
			catch(org.springframework.dao.DataAccessException dae) {
				throw new DataAccessException("Problem calling SQL '" + _sqlGetUserCreationPrivilege + "' with parameter: " + usersBeingQueried[i], dae);
			}
			catch(JSONException e) {
				throw new DataAccessException("Problem creating 'permissions' JSONObject.", e);
			}
			
			// Get classes.
			JSONArray classesJson = new JSONArray();
			try {
				List<LinkedHashMap<String, String>> classesList = (List<LinkedHashMap<String, String>>) getJdbcTemplate().queryForList(_sqlGetUserClasses, new Object[] { usersBeingQueried[i] });
				for(int j = 0; j < classesList.size(); j++) {
					classesJson.put(classesList.get(j).get("name"));
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				throw new DataAccessException("Problem calling SQL '" + _sqlGetUserClasses + "' with parameter: " + usersBeingQueried[i], dae);
			}
			
			// Get roles.
			JSONArray rolesJson = new JSONArray();
			try {
				List<LinkedHashMap<String, String>> rolesList = (List<LinkedHashMap<String, String>>) getJdbcTemplate().queryForList(_sqlGetUserRoles, new Object[] { usersBeingQueried[i] });
				for(int j = 0; j < rolesList.size(); j++) {
					rolesJson.put(rolesList.get(j).get("class_role"));
				}
			}
			catch(org.springframework.dao.DataAccessException dae) {
				throw new DataAccessException("Problem calling SQL '" + _sqlGetUserRoles + "' with parameter: " + usersBeingQueried[i], dae);
			}
			
			// Add user.
			queryResult.addUser(usersBeingQueried[i], permissionsJson, classesJson, rolesJson);
		}
	}
}
