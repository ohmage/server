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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.util.StringUtils;

/**
 * An internal representation of a user. The username and password must be set,
 * but the map of campaigns to which the user belongs and their roles in those 
 * campaigns and the list of maps to which the user belong and their roles in 
 * those classes may not be set. Those must be explicitly built in the 
 * workflow.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class User {
	private final String username;
	private String password;
	private boolean hashPassword;
	
	private String token;
	
	private boolean loggedIn;
	
	private final Map<String, List<String>> campaignRoleMap; // A user can have multiple roles in the same campaign.
	private final Map<String, String> classRoleMap;
	
	/**
	 * Creates a new User object.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param password The password, plaintext or hashed, of the user.
	 * 
	 * @param hashPassword Whether or not the password should be hashed before
	 * 					   being used.
	 * 
	 * @throws IllegalArgumentException Thrown if the username or password are
	 * 									null or whitespace only.
	 */
	public User(String username, String password, boolean hashPassword) {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(password)) {
			throw new IllegalArgumentException("The password cannot be null or whitespace only.");
		}
		
		this.username = username;
		this.password = password;
		this.hashPassword = hashPassword;
		
		token = null;
		
		loggedIn = false;
		
		campaignRoleMap = new HashMap<String, List<String>>();
		classRoleMap = new HashMap<String, String>();
	}
	
	/**
	 * Copy constructor. Performs a deep copy in the sense that Strings are
	 * immutable, so, while this new object will point to the same String  
	 * objects as the old User object did, they cannot change.
	 */
	public User(User user) {
		if(null == user) {
			throw new IllegalArgumentException("Cannot copy a null object.");
		}
		
		username = user.username;
		password = user.password;
		hashPassword = user.hashPassword;
		
		token = user.token;
		
		loggedIn = user.loggedIn;
		
		// Create a new campaign-role map and copy everything over.
		campaignRoleMap = new HashMap<String, List<String>>();
		for(String campaignId : user.campaignRoleMap.keySet()) {
			List<String> originalCampaignRoles = user.campaignRoleMap.get(campaignId);
			List<String> campaignRoles = new ArrayList<String>(originalCampaignRoles.size());
			for(String campaignRole : originalCampaignRoles) {
				campaignRoles.add(campaignRole);
			}
			
			campaignRoleMap.put(campaignId, campaignRoles);
		}
		
		// Create a new class-role map and copy everything over.
		classRoleMap = new HashMap<String, String>();
		for(String classId : user.classRoleMap.keySet()) {
			classRoleMap.put(classId, user.classRoleMap.get(classId));
		}
	}
	
	/**
	 * Returns the token which is null if it hasn't yet been set.
	 * 
	 * @return The token which is null if it hasn't yet been set.
	 */
	public String getToken() {
		return token;
	}
	
	/**
	 * Sets the token.
	 * 
	 * @param token The token for this user.
	 */
	public void setToken(String token) {
		this.token = token;
	}
	
	/**
	 * Associates a campaign and role in that campaign to this user.
	 * 
	 * @param campaignId A unique identifier for the campaign.
	 * 
	 * @param role The user's role in that campaign.
	 * 
	 * @throws IllegalArgumentException Thrown if the campaign ID or the user's
	 * 									role are obviously invalid.
	 */
	public void addCampaignRole(String campaignId, String role) {
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new IllegalArgumentException("The campaign ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			throw new IllegalArgumentException("The campaign role cannot be null or whitespace only.");
		}
		
		// Get the current list of roles for this user in this campaign. If no
		// such association has been made, create a new List and associate it.
		List<String> roles = campaignRoleMap.get(campaignId);
		if(roles == null) {
			roles = new LinkedList<String>();
			campaignRoleMap.put(campaignId, roles);
		}
		
		// Add the role to the list if it doesn't already exist.
		if(! roles.contains(role)) {
			roles.add(role);
		}
	}
	
	/**
	 * Gets the list of campaigns with which the user is associated and the  
	 * user's roles in each campaign.
	 * 
	 * @return A Map of the campaign IDs to a List of all the user's roles in
	 * 		   the campaign. This may be null which may not be accurate if it
	 * 		   has not yet been explicitly populated.
	 */
	public Map<String, List<String>> getCampaignsAndRoles() {
		return campaignRoleMap;
	}
	
	/**
	 * Associates a class and role in that class to this user.
	 * 
	 * @param classId A unique identifier fo the class. 
	 * 
	 * @param role The user's role in that class.
	 * 
	 * @throws IllegalArgumentException Thrown if the class Id or the user's 
	 * 									role are obviously invalid.
	 */
	public void getClassRole(String classId, String role) {
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			throw new IllegalArgumentException("The class ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			throw new IllegalArgumentException("The class role cannot be null or whitespace only.");
		}
		
		classRoleMap.put(classId, role);
	}
	
	/**
	 * Gets the list of classes with which the user is associated and the 
	 * user's roles in each campaign.
	 *  
	 * @return A Map of the class IDs to user's role in the class. This may be 
	 * 		   null which may not be accurate if it has not been explicitly
	 * 		   populated.
	 */
	public Map<String, String> getClassesAndRole() {
		return classRoleMap;
	}
	
	/**
	 * Returns the username of this user.
	 * 
	 * @return The username of this user.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns the password of this user.
	 * 
	 * @return The password of this user.
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Updates the user's password with a hashed version of the password.
	 * 
	 * @param hashedPassword The hashed version of the user's password.
	 */
	public void setHashedPassword(String hashedPassword) {
		hashPassword = false;
		password = hashedPassword;
	}
	
	/**
	 * Returns whether or not to hash this password.
	 * 
	 * @return Whether or not to hash this password.
	 */
	public boolean hashPassword() {
		return hashPassword;
	}
	
	/**
	 * Returns whether or not this user is logged in.
	 * 
	 * @return Whether or not this user is logged in.
	 */
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	/**
	 * Sets whether or not this user is logged in.
	 * 
	 * @param loggedIn Whether or not this user is logged in.
	 */
	public void isLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}
	
	/**
	 * Given a campaign's ID, return whether or not this user has the role of
	 * supervisor in that campaign.
	 * 
	 * This requires that the campaign-roles have been populated.
	 * 
	 * @param campaignId A unique ID for a campaign.
	 * 
	 * @return Whether or not the user is a supervisor in the campaign 
	 * 		   specified by the 'campaignId'.
	 */
	public boolean isSupervisorInCampaign(String campaignId) {
		List<String> roles = campaignRoleMap.get(campaignId);
		
		if(roles == null) {
			return false;
		}
		
		return roles.contains(CampaignRoleCache.ROLE_SUPERVISOR);
	}
	
	/**
	 * Given a campaign's ID, return whether or not this user has the role of
	 * author in that campaign.
	 * 
	 * This requires that the campaign-roles have been populated.
	 * 
	 * @param campaignId A unique ID for a campaign.
	 * 
	 * @return Whether or not the user is an author in the campaign 
	 * 		   specified by the 'campaignId'.
	 */
	public boolean isAuthorInCampaign(String campaignId) {
		List<String> roles = campaignRoleMap.get(campaignId);
		
		if(roles == null) {
			return false;
		}
		
		return roles.contains(CampaignRoleCache.ROLE_AUTHOR);
	}
	
	/**
	 * Given a campaign's ID, return whether or not this user has the role of
	 * analyst in that campaign.
	 * 
	 * This requires that the campaign-roles have been populated.
	 * 
	 * @param campaignId A unique ID for a campaign.
	 * 
	 * @return Whether or not the user is an analyst in the campaign 
	 * 		   specified by the 'campaignId'.
	 */
	public boolean isAnalystInCampaign(String campaignId) {
		List<String> roles = campaignRoleMap.get(campaignId);
		
		if(roles == null) {
			return false;
		}
		
		return roles.contains(CampaignRoleCache.ROLE_ANALYST);
	}
	
	/**
	 * Given a campaign's ID, return whether or not this user has the role of
	 * participant in that campaign.
	 * 
	 * This requires that the campaign-roles have been populated.
	 * 
	 * @param campaignId A unique ID for a campaign.
	 * 
	 * @return Whether or not the user is a participant in the campaign 
	 * 		   specified by the 'campaignId'.
	 */
	public boolean isParticipantInCampaign(String campaignId) {
		List<String> roles = campaignRoleMap.get(campaignId);
		
		if(roles == null) {
			return false;
		}
		
		return roles.contains(CampaignRoleCache.ROLE_PARTICIPANT);
	}

	/**
	 * Given a campaign's ID, return whether or not this user has the given 
	 * role in that campaign.
	 * 
	 * This requires that the campaign-roles have been populated.
	 * 
	 * @param campaignId A unique ID for a campaign.
	 * 
	 * @param role The role to check if the user has for the given campaign.
	 * 
	 * @return Whether or not the user is a specified 'role' in the campaign 
	 * 		   specified by the 'campaignId'.
	 */
	public boolean hasRoleInCampaign(String campaignId, String role) {
		List<String> roles = campaignRoleMap.get(campaignId);
		
		if(roles == null) {
			return false;
		}
		
		return roles.contains(role);
	}
	
	/**
	 * Given a class' ID, return whether or not this user has the role of
	 * privileged in that class.
	 * 
	 * This requires that the class-roles have been populated.
	 * 
	 * @param classId A unique ID for a class.
	 * 
	 * @return Whether or not the user is privileged in the class specified by 
	 * 		   the 'classId'.
	 */
	public boolean isPrivilegedInClass(String classId) {
		String role = classRoleMap.get(classId);
		
		if(role == null) {
			return false;
		}
		
		return role.equals(ClassRoleCache.ROLE_PRIVILEGED);
	}
	
	/**
	 * Given a class' ID, return whether or not this user has the role of
	 * restricted in that class.
	 * 
	 * This requires that the class-roles have been populated.
	 * 
	 * @param classId A unique ID for a class.
	 * 
	 * @return Whether or not the user is restricted in the class specified by 
	 * 		   the 'classId'.
	 */
	public boolean isRestrictedInClass(String classId) {
		String role = classRoleMap.get(classId);
		
		if(role == null) {
			return false;
		}
		
		return role.equals(ClassRoleCache.ROLE_RESTRICTED);
	}
	
	/**
	 * Given a class' ID, return whether or not this user has the given role in
	 * that class.
	 * 
	 * This requires that the class-roles have been populated.
	 * 
	 * @param classId A unique ID for a class.
	 * 
	 * @return Whether or not the user has the 'role' in the class specified by 
	 * 		   the 'classId'.
	 */
	public boolean hasRoleInClass(String classId, String classRole) {
		String role = classRoleMap.get(classId);
		
		if(role == null) {
			return false;
		}
		
		return role.equals(classRole);
	}
	
	/**
	 * Returns a String dump of this user.
	 */
	@Override
	public String toString() {
		return "User [username=" + username + ", _password=omitted"
				+ ", loggedIn=" + loggedIn + ", campaignRoleMap="
				+ campaignRoleMap + ", classRoleMap=" + classRoleMap + "]";
	}

	/**
	 * Generates a hash code of this instance of this class.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		final int booleanTruePrime = 1231;
		final int booleanFalsePrime = 1237;
		int result = 1;
		result = prime
				* result
				+ ((campaignRoleMap == null) ? 0 : campaignRoleMap.hashCode());
		result = prime * result
				+ ((classRoleMap == null) ? 0 : classRoleMap.hashCode());
		result = prime * result + (loggedIn ? booleanTruePrime : booleanFalsePrime);
		result = prime * result
				+ ((password == null) ? 0 : password.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	/**
	 * Compares this object against another Object to determine if their 
	 * contents are identical.
	 */
	@Override
	public boolean equals(Object object) {
		// If they point to the same object, they are the same.
		if (this == object) {
			return true;
		}
		
		// If the other object is null, they can't be the same.
		if (object == null) {
			return false;
		}
		
		// If the other object isn't a User, they can't be the same.
		if (getClass() != object.getClass()) {
			return false;
		}
		
		User other = (User) object;
		
		// Ensure that the campaign-role map is equal.
		if (campaignRoleMap == null) {
			if (other.campaignRoleMap != null) {
				return false;
			}
		} else if (!campaignRoleMap.equals(other.campaignRoleMap)) {
			return false;
		}
		
		// Ensure that the class-role map is equal.
		if (classRoleMap == null) {
			if (other.classRoleMap != null) {
				return false;
			}
		} else if (!classRoleMap.equals(other.classRoleMap)) {
			return false;
		}
		
		// Ensure that the logged in statuses are equal.
		if (loggedIn != other.loggedIn) {
			return false;
		}
		
		// Ensure that the passwords are equal.
		if (password == null) {
			if (other.password != null) {
				return false;
			}
		} else if (!password.equals(other.password)) {
			return false;
		}
		
		// Ensure that the usernames are equal.
		if (username == null) {
			if (other.username != null) {
				return false;
			}
		} else if (!username.equals(other.username)) {
			return false;
		}
		
		return true;
	}
}