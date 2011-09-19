/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.ClassRoleCache;

/**
 * Information about a class. The object must contain a a URN and name of the
 * class. The description, as specified by the database, is not required.
 * 
 * @author John Jenkins
 */
public class ClassInformation {
	/**
	 * Private class for aggregating specific information about a user.
	 * 
	 * @author John Jenkins
	 */
	private final class UserAndRole {
		private final String username;
		private final ClassRoleCache.Role role;
		
		/**
		 * Convenience constructor.
		 * 
		 * @param username The username of the user.
		 * 
		 * @param role The class role of the user.
		 */
		private UserAndRole(String username, ClassRoleCache.Role role) {
			this.username = username;
			this.role = role;
		}
	}
	
	private final String urn;
	private final String name;
	private final String description;
	
	private final List<UserAndRole> users;
	
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
	public ClassInformation(String urn, String name, String description) {
		if(urn == null) {
			throw new IllegalArgumentException("Class URN cannot be null.");
		}
		if(name == null) {
			throw new IllegalArgumentException("Class name cannot be null.");
		}
		
		this.urn = urn;
		this.name = name;
		this.description = description;
		
		users = new LinkedList<UserAndRole>();
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
	public void addUser(String username, ClassRoleCache.Role role) {
		if(username == null) {
			throw new IllegalArgumentException("User's username cannot be null.");
		}
		
		users.add(new UserAndRole(username, role));
	}
	
	/**
	 * Gets the number of users that have been associated with this class
	 * object. There is no guarantee that this also agrees with the database.
	 * 
	 * @return The number of users associated with this class object.
	 */
	public int getNumUsers() {
		return users.size();
	}
	
	/**
	 * Gets the URN of this class.
	 * 
	 * @return The URN of this class.
	 */
	public String getUrn() {
		return urn;
	}
	
	/**
	 * Returns this class object as a JSONObject with the URN as an optional
	 * inclusion.
	 * 
	 * @param withId Whether or not to include the URN in the output.
	 * 
	 * @return Returns a JSONObject with the classes as a JSONObject where the
	 * 		   keys are the users and their values are their class roles.
	 * 
	 * @throws JSONException Thrown if generating the object caused an error.
	 */
	public JSONObject toJson(boolean withId) throws JSONException {
		JSONObject result = new JSONObject();
		
		if(withId) {
			result.put("urn", urn);
		}
		result.put("name", name);
		result.put("description", ((description == null) ? "" : description));
		
		JSONObject users = new JSONObject();
		ListIterator<UserAndRole> usersIter = this.users.listIterator();
		while(usersIter.hasNext()) {
			UserAndRole currUser = usersIter.next();
			
			users.put(currUser.username, (currUser.role == null) ? "" : currUser.role);
		}
		result.put("users", users);
		
		return result;
	}
}