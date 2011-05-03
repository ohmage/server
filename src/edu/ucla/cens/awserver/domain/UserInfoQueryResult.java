package edu.ucla.cens.awserver.domain;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Container class for the results of a query about a list of users.
 * 
 * @author John Jenkins
 */
public class UserInfoQueryResult {
	private static Logger _logger = Logger.getLogger(UserInfoQueryResult.class);
	
	/**
	 * Class for holding the specific information about a user that needs to
	 * be returned by this query. For now, the User class didn't have all the
	 * information needed and contained some information that I didn't want to
	 * leak. This is a better form of encapsulation.
	 * 
	 * @author John Jenkins
	 */
	private class UserInfo {
		public String _username;
		public JSONObject _permissions;
		public JSONObject _classes;
		public JSONArray _classRoles;
		public JSONArray _campaignRoles;
	};
	List<UserInfo> _users;
	
	/**
	 * Initializes the local variables.
	 */
	public UserInfoQueryResult() {
		_users = new LinkedList<UserInfo>();
	}
	
	/**
	 * Adds all the information for the new user to the local store of users
	 * and information.
	 */
	public void addUser(String username, JSONObject permissions, JSONObject classes, JSONArray classRoles, JSONArray campaignRoles) {
		UserInfo newUser = new UserInfo();
		newUser._username = username;
		newUser._permissions = permissions;
		newUser._classes = classes;
		newUser._classRoles = classRoles;
		newUser._campaignRoles = campaignRoles;

		_users.add(newUser);
	}
	
	/**
	 * Returns a JSONArray object where each key is a different user and the
	 * value associated with the key is the information about that user.
	 * 
	 * @return A JSONArray where the keyset is the list of users and the value
	 * 		   of a key is the information about that user.
	 */
	public JSONObject getUsersInfo() {
		return usersInfoIntoJsonObject();
	}
	
	/**
	 * Dumps everything we know about the users that have been added thus far.
	 */
	@Override
	public String toString() {
		return("UserInfoQueryResult [_users=" + (usersInfoIntoJsonObject()).toString() + "]");
	}
	
	/**
	 * Puts the items in the _users List intoa JSONObject of the following
	 * form:
	 * 
	 * 		{ _username: {
	 * 			"permissions":_permissions,
	 * 			"classes":_classes,
	 * 			"roles":_roles
	 * 			}
	 * 		}
	 * 
	 * This means that the keyset of the resulting JSONObject is the list of
	 * users and that by "get()"ing a username from the resulting object you
	 * will get all the information about that object.
	 * 
	 * @return A JSONObject where the keys are the users and the value of a
	 * 		   key is the info about that user.
	 */
	private JSONObject usersInfoIntoJsonObject() {
		JSONObject result = new JSONObject();
		
		for(UserInfo info : _users) {
			try {
				JSONObject userInfo = new JSONObject();
				userInfo.put("permissions", info._permissions);
				userInfo.put("classes", info._classes);
				userInfo.put("class_roles", info._classRoles);
				userInfo.put("campaign_roles", info._campaignRoles);
				
				result.put(info._username, userInfo);
			}
			catch(JSONException e) {
				_logger.error("Error while trying to add some information about a user in response to a query.", e);
			}
		}
		
		return result;
	}
}
