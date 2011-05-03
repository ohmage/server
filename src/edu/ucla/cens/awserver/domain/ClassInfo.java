package edu.ucla.cens.awserver.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Information about a class. The object must contain a a URN and name of the
 * class. The description, as specified by the database, is not required.
 * 
 * @author John Jenkins
 */
public class ClassInfo {
	/**
	 * Private class for aggregating specific information about a user.
	 * 
	 * @author John Jenkins
	 */
	private class UserAndRole {
		public String _username;
		public String _role;
		
		public UserAndRole(String username, String role) {
			_username = username;
			_role = role;
		}
	}
	
	private String _urn;
	private String _name;
	private String _description;
	List<UserAndRole> _users;
	
	/**
	 * Creates a new class information object that contains the class' name
	 * and description and no users associated with it.
	 * 
	 * @param name The name of this class.
	 * 
	 * @param description The description of this class.
	 * 
	 * @throws IllegalArgumentException Thrown if 'name' and/or 'urn' is null
	 * 									as classes must always have names and
	 * 									URNs.
	 */
	public ClassInfo(String urn, String name, String description) {
		if(urn == null) {
			throw new IllegalArgumentException("Class URN cannot be null.");
		}
		if(name == null) {
			throw new IllegalArgumentException("Class name cannot be null.");
		}
		
		_urn = urn;
		_name = name;
		_description = description;
		
		_users = new LinkedList<UserAndRole>();
	}
	
	/**
	 * Adds a user to this class information object.
	 * 
	 * @param username The username of the user being added.
	 * 
	 * @param role The role of this user in this class.
	 * 
	 * @throws IllegalArgumentException Thrown if the username and/or the role
	 * 									is null.
	 */
	public void addUser(String username, String role) {
		if(username == null) {
			throw new IllegalArgumentException("User's username cannot be null.");
		}
		else if(role == null) {
			throw new IllegalArgumentException("User's role cannot be null.");
		}
		
		_users.add(new UserAndRole(username, role));
	}
	
	/**
	 * Gets the number of users that have been associated with this class
	 * object. There is no guarantee that this also agrees with the database.
	 * 
	 * @return The number of users associated with this class object.
	 */
	public int getNumUsers() {
		return _users.size();
	}
	
	/**
	 * Gets the URN of this class.
	 * 
	 * @return The URN of this class.
	 */
	public String getUrn() {
		return _urn;
	}
	
	/**
	 * Returns this class object as a JSONObject with the URN as an optional
	 * inclusion.
	 * 
	 * @param withUrn Whether or not to include the URN in the output.
	 * 
	 * @return Returns a JSONObject with the classes as a JSONObject where the
	 * 		   keys are the users and their values are their class roles.
	 * 
	 * @throws JSONException Thrown if generating the object caused an error.
	 */
	public JSONObject getJsonRepresentation(boolean withUrn) throws JSONException {
		JSONObject result = new JSONObject();
		
		if(withUrn) {
			result.put("urn", _urn);
		}
		result.put("name", _name);
		result.put("description", _description);
		
		JSONObject users = new JSONObject();
		ListIterator<UserAndRole> usersIter = _users.listIterator();
		while(usersIter.hasNext()) {
			UserAndRole currUser = usersIter.next();
			
			users.put(currUser._username, currUser._role);
		}
		result.put("users", users);
		
		return result;
	}
}