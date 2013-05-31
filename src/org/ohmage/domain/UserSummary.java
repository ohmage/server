/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DomainException;

/**
 * This class is responsible for collecting and displaying the information 
 * about a user. This includes their campaign creation privilege, the campaigns
 * and classes to which they belong, and the union of their roles in those
 * campaigns and classes. 
 * 
 * @author John Jenkins
 */
public class UserSummary {
	private static final String JSON_KEY_EMAIL_ADDRESS = "email_address";
	
	private static final String JSON_KEY_PERMISSIONS = "permissions";
	private static final String JSON_KEY_PERMISSIONS_ADMIN = "is_admin";
	private static final String JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION =
		"can_create_campaigns";
	private static final String JSON_KEY_PERMISSIONS_CLASS_CREATION =
		"can_create_classes";
	private static final String JSON_KEY_PERMISSIONS_USER_SETUP =
		"can_setup_users";
	
	private static final String JSON_KEY_CAMPAIGNS = "campaigns";
	private static final String JSON_KEY_CAMPAIGN_ROLES = "campaign_roles";
	
	private static final String JSON_KEY_CLASSES = "classes";
	private static final String JSON_KEY_CLASS_ROLES = "class_roles";
	
	private final String emailAddress;
	
	private final boolean isAdmin;
	private final boolean campaignCreationPrivilege;
	private final boolean classCreationPrivilege;
	private final boolean userSetupPrivilege;
	
	private final Map<String, String> campaigns;
	private final Set<Campaign.Role> campaignRoles;
	
	private final Map<String, String> classes;
	private final Set<Clazz.Role> classRoles;
	
	/**
	 * Creates a new user information object with a default campaign creation
	 * privilege, no campaigns or campaign roles, and no classes or class
	 * roles.
	 * 
	 * @param emailAddress
	 *        The user's email address if one exists, which it may not.
	 * 
	 * @param isAdmin
	 *        Whether or not the user is an admin.
	 * 
	 * @param campaignCreationPrivilege
	 *        Whether or not the user is allowed to create new campaigns.
	 * 
	 * @param classCreationPrivilege
	 *        Whether or not the user is allowed to create new classes.
	 * 
	 * @param userSetupPrivilege
	 *        Whether or not the user is allowed to setup users.
	 * 
	 * @param campaigns
	 *        The map of all campaigns to which the user is associated where
	 *        the key is the campaign's ID and its value is the campaign's
	 *        name.
	 * 
	 * @param campaignRoles
	 *        A set of all of the campaign roles for the user across all of
	 *        their campaigns.
	 * 
	 * @param classes
	 *        The map of all classes to which the user is associated where the
	 *        key is the class' ID and its value is the class' name.
	 * 
	 * @param classRoles
	 *        A set of all of the class roles for the user across all of their
	 *        classes.
	 * 
	 * @throws DomainException
	 *         The campaigns, classes, or role lists is empty.
	 */
	public UserSummary(
			String emailAddress,
			boolean isAdmin,
			boolean campaignCreationPrivilege,
			boolean classCreationPrivilege,
			boolean userSetupPrivilege,
			final Map<String, String> campaigns,
			final Set<Campaign.Role> campaignRoles,
			final Map<String, String> classes,
			final Set<Clazz.Role> classRoles) 
			throws DomainException {
		
		if(campaigns == null) {
			throw new DomainException(
					"The map of campaigns to their names cannot be null.");
		}
		if(campaignRoles == null) {
			throw new DomainException(
					"The set of campaign roles for a user cannot be empty.");
		}
		if(classes == null) {
			throw new DomainException(
					"The map of classes to their names cannot be null.");
		}
		if(classRoles == null) {
			throw new DomainException(
					"The set of class roles for a user cannot be empty.");
		}
		
		this.emailAddress = emailAddress;
		
		this.isAdmin = isAdmin;
		this.campaignCreationPrivilege = campaignCreationPrivilege;
		this.classCreationPrivilege = classCreationPrivilege;
		this.userSetupPrivilege = userSetupPrivilege;
		
		this.campaigns = new HashMap<String, String>(campaigns);
		this.campaignRoles = new HashSet<Campaign.Role>(campaignRoles);
		
		this.classes = new HashMap<String, String>(classes);
		this.classRoles = new HashSet<Clazz.Role>(classRoles);
	}
	
	/**
	 * Creates a UserSummary object from the information.
	 * 
	 * @param information The information used to create this object.
	 * 
	 * @throws DomainException Thrown if information object is null or 
	 * 						   malformed.
	 */
	public UserSummary(final JSONObject information) throws DomainException {
		if(information == null) {
			throw new DomainException("The information is null.");
		}
		
		try {
			emailAddress = information.getString(JSON_KEY_EMAIL_ADDRESS);
		}
		catch(JSONException e) {
			throw new DomainException("The email address is missing.", e);
		}
		
		JSONObject permissions;
		try {
			permissions = information.getJSONObject(JSON_KEY_PERMISSIONS);
		}
		catch(JSONException e) {
			throw new DomainException("The permissions JSON is missing.", e);
		}
		
		try {
			isAdmin = permissions.getBoolean(JSON_KEY_PERMISSIONS_ADMIN);
		}
		catch(JSONException e) {
			throw new DomainException("The 'is admin' permission is missing from the list of permissions.", e);
		}
		
		try {
			campaignCreationPrivilege = permissions.getBoolean(JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION); 
		}
		catch(JSONException e) {
			throw new DomainException("The campaign creation permission is missing from the list of permissions.", e);
		}
		
		try {
			classCreationPrivilege = permissions.getBoolean(JSON_KEY_PERMISSIONS_CLASS_CREATION); 
		}
		catch(JSONException e) {
			throw new DomainException("The class creation permission is missing from the list of permissions.", e);
		}
		
		try {
			userSetupPrivilege = permissions.getBoolean(JSON_KEY_PERMISSIONS_USER_SETUP); 
		}
		catch(JSONException e) {
			throw new DomainException("The class creation permission is missing from the list of permissions.", e);
		}
		
		try {
			JSONObject campaignsJson = information.getJSONObject(JSON_KEY_CAMPAIGNS);
			
			campaigns = new HashMap<String, String>(campaignsJson.length());
			
			Iterator<?> keys = campaignsJson.keys();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				
				campaigns.put(key, campaignsJson.getString(key));
			}	
		}
		catch(JSONException e) {
			throw new DomainException("The campaigns JSON is missing or malformed.", e);
		}
		
		try {
			JSONArray campaignRolesJson = information.getJSONArray(JSON_KEY_CAMPAIGN_ROLES);
			int numRoles = campaignRolesJson.length();

			campaignRoles = new HashSet<Campaign.Role>(numRoles);
			
			for(int i = 0; i < numRoles; i++) {
				campaignRoles.add(Campaign.Role.getValue(campaignRolesJson.getString(i)));
			}
		}
		catch(JSONException e) {
			throw new DomainException("The campaign roles JSON is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The campaign roles JSON contains an unknown role.", e);
		}
		
		try {
			JSONObject classesJson = information.getJSONObject(JSON_KEY_CLASSES);
			
			classes = new HashMap<String, String>(classesJson.length());
			
			Iterator<?> keys = classesJson.keys();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				
				classes.put(key, classesJson.getString(key));
			}
		}
		catch(JSONException e) {
			throw new DomainException("The classes JSON is missing or malformed.", e);
		}
		
		try {
			JSONArray classRolesJson = information.getJSONArray(JSON_KEY_CLASS_ROLES);
			int numRoles = classRolesJson.length();
			
			classRoles = new HashSet<Clazz.Role>(numRoles);
			
			for(int i = 0; i < numRoles; i++) {
				classRoles.add(Clazz.Role.getValue(classRolesJson.getString(i)));
			}
		}
		catch(JSONException e) {
			throw new DomainException("The class roles JSON is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The class roles JSON contains an unknown role.", e);
		}
	}
	
	/**
	 * Returns whether or not the user is an admin.
	 * 
	 * @return Whether or not the user is an admin.
	 */
	public boolean getAdminPrivilege() {
		return isAdmin;
	}
	
	/**
	 * Returns whether or not the user is allowed to create campaigns.
	 * 
	 * @return Whether or not the user is allowed to create campaigns.
	 */
	public boolean getCampaignCreationPrivilege() {
		return campaignCreationPrivilege;
	}
	
	/**
	 * Returns whether or not the user is allowed to create classes.
	 * 
	 * @return Whether or not the user is allowed to create classes.
	 */
	public boolean getClassCreationPrivilege() {
		return classCreationPrivilege;
	}
	
	/**
	 * Returns whether or not the user is allowed to setup users.
	 * 
	 * @return Whether or not the user is allowed to setup users.
	 */
	public boolean getUserSetupPrivilege() {
		return userSetupPrivilege;
	}
	
	/**
	 * Returns an unmodifiable map of all of the campaigns associated with the 
	 * user.
	 * 
	 * @return An unmodifiable map of all of the campaigns associated with a
	 * 		   user.
	 */
	public Map<String, String> getCampaigns() {
		return Collections.unmodifiableMap(campaigns);
	}
	
	/**
	 * Returns an unmodifiable set of all of the campaign roles associated with
	 * the user across all of their campaigns.
	 * 
	 * @return An unmodifiable set of all of the campaign roles associated with
	 * 		   the user across all of their campaigns.
	 */
	public Set<Campaign.Role> getCampaignRoles() {
		return Collections.unmodifiableSet(campaignRoles);
	}
	
	/**
	 * Returns an unmodifiable map of the classes associated with the user.
	 * 
	 * @return An unmodifiable map of the classes associated with the user.
	 */
	public Map<String, String> getClasses() {
		return Collections.unmodifiableMap(classes);
	}
	
	/**
	 * Returns an unmodifiable set of the class roles associated with the user
	 * across all of their classes.
	 * 
	 * @return An unmodifiable set of the class roles associated with the user
	 * 		   across all of their classes.
	 */
	public Set<Clazz.Role> getClassRoles() {
		return Collections.unmodifiableSet(classRoles);
	}
	
	/**
	 * Creates a JSONObject representation of this instance of this class.
	 * 
	 * @return A JSONObject representation of this object.
	 */
	public JSONObject toJsonObject() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_EMAIL_ADDRESS, emailAddress);
		
		JSONObject permissions = new JSONObject();
		permissions.put(JSON_KEY_PERMISSIONS_ADMIN, isAdmin);
		permissions.put(JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION, campaignCreationPrivilege);
		permissions.put(JSON_KEY_PERMISSIONS_CLASS_CREATION, classCreationPrivilege);
		permissions.put(JSON_KEY_PERMISSIONS_USER_SETUP, userSetupPrivilege);
		result.put(JSON_KEY_PERMISSIONS, permissions);
		
		result.put(JSON_KEY_CAMPAIGNS, campaigns);
		result.put(JSON_KEY_CAMPAIGN_ROLES, campaignRoles);
		
		result.put(JSON_KEY_CLASSES, classes);
		result.put(JSON_KEY_CLASS_ROLES, classRoles);
		
		return result;
	}
}
