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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;

/**
 * Information about a class.
 * 
 * @author John Jenkins
 */
public class Clazz {
	private static final String JSON_KEY_ID = "id";
	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_DESCRIPTION = "description";
	private static final String JSON_KEY_USERS = "users";
	
	private final String id;
	private final String name;
	private final String description;
	
	/**
	 * Class roles.
	 * 
	 * @author John Jenkins
	 */
	public static enum Role {
		PRIVILEGED,
		RESTRICTED;
		
		/**
		 * Converts a String value into a Role or throws an exception if there
		 * is no comparable role.
		 * 
		 * @param role The role to be converted into a Role enum.
		 * 
		 * @return A comparable Role enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									Role enum.
		 */
		public static Role getValue(String role) {
			return valueOf(role.toUpperCase());
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final Map<String, Role> userRole;
	
	/**
	 * Creates a new class information object that contains the class' name
	 * and description and no users associated with it.
	 * 
	 * @param id The class' unique identifier.
	 * 
	 * @param name The name of this class.
	 * 
	 * @param description The description of this class. This may be null.
	 * 
	 * @throws IllegalArgumentException Thrown if id or name are null or 
	 * 									whitespace only.
	 */
	public Clazz(String id, String name, String description) {
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("Class ID cannot be null or whitespace only.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("Class name cannot be null or whitespace only.");
		}
		
		this.id = id;
		this.name = name;
		this.description = description;
		
		userRole = new HashMap<String, Role>();
	}
	
	/**
	 * Creates a Clazz object based on the information.
	 * 
	 * @param id The class' unique identifier.
	 * 
	 * @param information A JSONObject containing the information about the
	 * 					  Clazz.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are
	 * 									invalid.
	 */
	public Clazz(final String id, final JSONObject information) {
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("The ID cannot be null or whitespace only.");
		}
		else if(information == null) {
			throw new IllegalArgumentException("The information is null.");
		}
		
		this.id = id;
		
		try {
			name = information.getString(JSON_KEY_NAME);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The information is missing the class' name.", e);
		}
		
		String tDescription = null;
		try {
			tDescription = information.getString(JSON_KEY_DESCRIPTION);
		}
		catch(JSONException e) {
			// The description is optional.
		}
		description = tDescription;
		
		try {
			JSONObject userRolesJson = information.getJSONObject(JSON_KEY_USERS);
			userRole = new HashMap<String, Role>(userRolesJson.length());
			
			Iterator<?> keys = userRolesJson.keys();
			while(keys.hasNext()) {
				String username = (String) keys.next();
				
				Role role = null;
				String roleString = userRolesJson.getString(username);
				if(! StringUtils.isEmptyOrWhitespaceOnly(roleString)) {
					role = Role.getValue(roleString);
				}
				
				userRole.put(username, role);
			}
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The information is missing the class' name.", e);
		}
	}
	
	/**
	 * Returns the unique identifier of this class.
	 * 
	 * @return The unique identifier of this class.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the name of this class.
	 * 
	 * @return The name of this class.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the description of this class.
	 * 
	 * @return The description of this class. This may be null.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Adds a user with a role or updates the user's existing role.
	 * 
	 * @param username The user's username.
	 * 
	 * @param role The user's class role.
	 * 
	 * @throws IllegalArgumentException Thrown if the username or role are 
	 * 									null.
	 */
	public void addUser(final String username, final Role role) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username is null or whitespace only.");
		}
		
		userRole.put(username, role);
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
	 * @throws IllegalStateException Thrown if generating the object caused an 
	 * 								 error.
	 */
	public JSONObject toJson(boolean withId) {
		try {
			JSONObject result = new JSONObject();
			
			if(withId) {
				result.put(JSON_KEY_ID, id);
			}
			result.put(JSON_KEY_NAME, name);
			
			//result.put(JSON_KEY_DESCRIPTION, ((description == null) ? "" : description));
			result.put(JSON_KEY_DESCRIPTION, description);
			
			JSONObject users = new JSONObject();
			for(String username : userRole.keySet()) {
				Role role = (Role) userRole.get(username);
				
				users.put(username, ((role == null) ? "" : role));
			}
			result.put(JSON_KEY_USERS, users);
			
			return result;
		}
		catch(JSONException e) {
			throw new IllegalStateException("There was an error building the JSON.", e);
		}
	}
}