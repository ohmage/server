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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DomainException;

/**
 * All of the information about a user in the system.
 * 
 * @author John Jenkins
 */
public class UserInformation {
	/**
	 * The keys for a user.
	 * 
	 * @author John Jenkins
	 */
	public static enum UserColumnKey implements ColumnKey {
		USERNAME ("username"),
		EMAIL_ADDRESS ("email_address"),
		PERMISSIONS ("permissions"),
		CAMPAIGNS ("campaigns"),
		CLASSES ("classes"),
		PERSONAL ("personal");
		
		private final String key;
		
		/**
		 * Initializes the key enum with its key value.
		 * 
		 * @param key The key's value.
		 */
		private UserColumnKey(final String key) {
			this.key = key;
		}
		
		/**
		 * Returns the key's value.
		 * 
		 * @return The key's value.
		 */
		@Override
		public String toString() {
			return key;
		}
	}
	
	/**
	 * The permissions key for a user.
	 * 
	 * @author John Jenkins
	 */
	public static enum UserPermissionColumnKey implements ColumnKey {
		ADMIN ("admin"),
		ENABLED ("enabled"),
		NEW_ACCOUNT ("new_account"),
		CAN_CREATE_CAMPAIGNS ("can_create_campaigns"),
		CAN_CREATE_CLASSES ("can_create_classes"),
		CAN_SETUP_USERS ("can_setup_users");
		
		private final String key;
		
		/**
		 * Initializes the key enum with its key value.
		 * 
		 * @param key The key's value.
		 */
		private UserPermissionColumnKey(final String key) {
			this.key = key;
		}
		
		/**
		 * Returns the key's value.
		 * 
		 * @return The key's value.
		 */
		@Override 
		public String toString() {
			return key;
		}
	}
	
	/**
	 * This class encapsulates a user's information.
	 * 
	 * @author John Jenkins
	 */
	public static class UserPersonal {
		/**
		 * The personal keys for a user.
		 * 
		 * @author John Jenkins
		 */
		private static enum UserPersonalColumnKey implements ColumnKey {
			FIRST_NAME ("first_name"),
			LAST_NAME ("last_name"),
			ORGANIZATION ("organization"),
			PERSONAL_ID ("personal_id");
			
			private final String key;
			
			/**
			 * Initializes the key enum with its key value.
			 * 
			 * @param key The key's value.
			 */
			private UserPersonalColumnKey(final String key) {
				this.key = key;
			}
			
			/**
			 * Returns the key's value.
			 * 
			 * @return The key's value.
			 */
			@Override 
			public String toString() {
				return key;
			}
		}
		
		private final String firstName;
		private final String lastName;
		private final String organization;
		private final String personalId;
		
		/**
		 * Creates a new object with the specified personal information about the 
		 * user. Any data can be null.
		 * 
		 * @param firstName The first name of the user.
		 * 
		 * @param lastName The last name of the user.
		 * 
		 * @param organization The organization for the user.
		 * 
		 * @param personalId The personal identifier for the user.
		 * 
		 * @throws DomainException One of the values is null or only 
		 * 						   whitespace.
		 */
		public UserPersonal(
				final String firstName, 
				final String lastName, 
				final String organization, 
				final String personalId) 
				throws DomainException {
			
			if(firstName == null) {
				throw new DomainException("The first name is null.");
			}
			if(lastName == null) {
				throw new DomainException("The last name is null.");
			}
			if(organization == null) {
				throw new DomainException("The organization is null.");
			}
			if(personalId == null) {
				throw new DomainException("The personal ID is null.");
			}
			
			this.firstName = firstName;
			this.lastName = lastName;
			this.organization = organization;
			this.personalId = personalId;
		}
		
		/**
		 * Creates an UserPersonal object. 
		 * 
		 * @param information The JSONObject with the user's personal 
		 * 					  information.
		 * 
		 * @throws DomainException The information JSONObject object is 
		 * 						   invalid.
		 */
		public UserPersonal(
				final JSONObject information) 
				throws DomainException {
			
			if(information == null) {
				throw new DomainException("The information is null.");
			}
			
			try {
				firstName = 
						information.getString(
								UserPersonalColumnKey.FIRST_NAME.toString());
			}
			catch(JSONException e) {
				throw new DomainException(
						"The first name is missing: " + 
							UserPersonalColumnKey.FIRST_NAME.toString());
			}
			
			try {
				lastName = 
						information.getString(
								UserPersonalColumnKey.LAST_NAME.toString());
			}
			catch(JSONException e) {
				throw new DomainException(
						"The last name is missing: " + 
							UserPersonalColumnKey.LAST_NAME.toString());
			}
			
			try {
				organization = 
						information.getString(
								UserPersonalColumnKey.ORGANIZATION.toString());
			}
			catch(JSONException e) {
				throw new DomainException(
						"The organization is missing: " + 
							UserPersonalColumnKey.ORGANIZATION.toString());
			}
			
			try {
				personalId = 
						information.getString(
								UserPersonalColumnKey.PERSONAL_ID.toString());
			}
			catch(JSONException e) {
				throw new DomainException(
						"The personal ID is missing: " + 
							UserPersonalColumnKey.PERSONAL_ID.toString());
			}
		}
		
		/**
		 * Returns the user's first name or null if one doesn't exist.
		 * 
		 * @return the firstName
		 */
		public final String getFirstName() {
			return firstName;
		}

		/**
		 * Returns the user's last name or null if one doesn't exist.
		 * 
		 * @return the lastName
		 */
		public final String getLastName() {
			return lastName;
		}

		/**
		 * Returns the user's organization or null if one doesn't exist.
		 * 
		 * @return the organization
		 */
		public final String getOrganization() {
			return organization;
		}

		/**
		 * Returns the user's personal ID or null if one doesn't exist.
		 * 
		 * @return the personalId
		 */
		public final String getPersonalId() {
			return personalId;
		}

		/**
		 * Creates a JSONObject that represents this user. Any fields that were set
		 * to null will be missing an entry in this object.
		 * 
		 * @return A JSONObject that represents this object.
		 * 
		 * @throws JSONException There was an error creating the JSONObject.
		 */
		public JSONObject toJsonObject() throws JSONException {
			JSONObject result = new JSONObject();
			
			result.put(UserPersonalColumnKey.FIRST_NAME.toString(), firstName);
			result.put(UserPersonalColumnKey.LAST_NAME.toString(), lastName);
			result.put(
					UserPersonalColumnKey.ORGANIZATION.toString(), 
					organization);
			result.put(
					UserPersonalColumnKey.PERSONAL_ID.toString(), 
					personalId);
			
			return result;
		}
	}
	private final UserPersonal personalInfo;
	
	private final String username;
	private final String emailAddress;
	
	private final boolean isAdmin;
	private final boolean isEnabled;
	private final boolean isNewAccount;
	private final boolean campaignCreationPrivilege;
	private final boolean classCreationPrivilege;
	private final boolean userSetupPrivilege;
	
	private Map<String, Set<Campaign.Role>> campaigns;
	private Map<String, Clazz.Role> classes;
	
	/**
	 * Creates a new information object for this user.
	 * 
	 * @param username
	 *        The user's username.
	 * 
	 * @param emailAddress
	 *        The user's email address.
	 * 
	 * @param isAdmin
	 *        Whether or not the user is an admin.
	 * 
	 * @param isEnabled
	 *        Whether or not the user's account is enabled.
	 * 
	 * @param isNewAccount
	 *        Whether or not the account is new.
	 * 
	 * @param campaignCreationPrivilege
	 *        Whether or not the user is allowed to create new campaigns.
	 * 
	 * @param classCreationPrivilege
	 *        Whether or not the user is allowed to create new classes.
	 * 
	 * @param userSetupPrivilege
	 *        Whether or not the user can setup other users.
	 * 
	 * @param campaigns
	 *        The map of campaign IDs to a set of the user's roles in those
	 *        campaigns.
	 * 
	 * @param classes
	 *        The map of class IDs to the user's role in that class.
	 * 
	 * @param personalInfo
	 *        The personal information about the user or null if the user
	 *        doesn't have a personal information record.
	 * 
	 * @throws DomainException
	 *         The campaign and/or class parameter is null.
	 */
	public UserInformation(
			final String username,
			final String emailAddress,
			final boolean isAdmin,
			final boolean isEnabled,
			final boolean isNewAccount,
			final boolean campaignCreationPrivilege,
			final boolean classCreationPrivilege,
			final boolean userSetupPrivilege,
			final Map<String, Set<Campaign.Role>> campaigns,
			final Map<String, Clazz.Role> classes,
			final UserPersonal personalInfo) 
			throws DomainException {
		
		if(username == null) {
			throw new DomainException(
					"The username is null");
		}
		
		this.username = username;
		this.emailAddress = emailAddress;
		this.isAdmin = isAdmin;
		this.isEnabled = isEnabled;
		this.isNewAccount = isNewAccount;
		this.campaignCreationPrivilege = campaignCreationPrivilege;
		this.classCreationPrivilege = classCreationPrivilege;
		this.userSetupPrivilege = userSetupPrivilege;
		
		if(campaigns == null) {
			this.campaigns = null;
		}
		else {
			this.campaigns = 
					new HashMap<String, Set<Campaign.Role>>(campaigns);
		}
		if(classes == null) {
			this.classes = null;
		}
		else {
			this.classes = new HashMap<String, Clazz.Role>(classes);
		}
		
		this.personalInfo = personalInfo;
	}
	
	/**
	 * Adds a campaign, if it doesn't already exist, and a set of roles for the
	 * user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param roles The user's roles in that campaign. This cannot be empty.
	 * 
	 * @throws DomainException The campaign ID or list of roles was null or the
	 * 						   list of roles was empty.
	 */
	public void addCampaign(
			final String campaignId, 
			final Set<Campaign.Role> roles) 
			throws DomainException {
		
		if(campaignId == null) {
			throw new DomainException("The campaign ID is null.");
		}
		
		if(roles == null) {
			throw new DomainException("The list of roles is null.");
		}
		else if(roles.size() == 0) {
			throw new DomainException("The list of roles is empty.");
		}
		
		if(campaigns == null) {
			campaigns = new HashMap<String, Set<Campaign.Role>>();
		}
		
		Set<Campaign.Role> oldRoles = campaigns.get(campaignId);
		
		if(oldRoles == null) {
			campaigns.put(campaignId, roles);
		}
		else {
			oldRoles.addAll(roles);
		}
	}
	
	/**
	 * Adds a set of campaigns and the user's respective roles. The map cannot 
	 * be null, but it may be empty. None of the sets of roles may be null or 
	 * empty.
	 * 
	 * @param campaigns The map of campaign IDs to their roles for this user.
	 * 
	 * @throws DomainException The map was null or one of the lists of roles 
	 * 						   was null or empty.
	 */
	public void addCampaigns(
			final Map<String, Set<Campaign.Role>> campaigns)
			throws DomainException {
		
		if(campaigns == null) {
			throw new DomainException("The campaigns map is null.");
		}
		
		if(this.campaigns == null) {
			this.campaigns = 
					new HashMap<String, Set<Campaign.Role>>(campaigns);
		}
		else {
			campaigns.putAll(campaigns);
		}
	}
	
	/**
	 * Adds a class, if it doesn't already exist, and a role for the user. If a
	 * role already existed for this user in this class, it is overwritten.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param role The users role in the class.
	 * 
	 * @throws DomainException The class ID or role were null.
	 */
	public void addClass(
			final String classId, 
			final Clazz.Role role)
			throws DomainException {
		
		if(classId == null) {
			throw new DomainException("The class ID is null.");
		}
		
		if(role == null) {
			throw new DomainException("The role is null.");
		}
		
		if(classes == null) {
			classes = new HashMap<String, Clazz.Role>();
		}
		
		classes.put(classId, role);
	}
	
	/**
	 * Adds a set of classes and the user's respective role. The map cannot be
	 * null but may be empty. The role associated with each class cannot be 
	 * null.
	 * 
	 * @param classes The map of class IDs to the user's respective role.
	 * 
	 * @throws DomainException The map was null or one of the roles was null.
	 */
	public void addClasses(
			final Map<String, Clazz.Role> classes)
			throws DomainException {
		
		if(classes == null) {
			throw new DomainException("The classes map is null.");
		}
		
		if(this.classes == null) {
			this.classes = new HashMap<String, Clazz.Role>(classes);
		}
		else {
			this.classes.putAll(classes);
		}
	}
	
	/**
	 * Returns the user's username.
	 * 
	 * @return The user's username.
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Returns the user's email address or null if they don't have one.
	 * 
	 * @return The user's email address or null if they don't have one.
	 */
	public String getEmailAddress() {
		return emailAddress;
	}
	
	/**
	 * Returns the personal information about the user.
	 * 
	 * @return The personal information about the user.
	 */
	public UserPersonal getPersonalInfo() {
		return personalInfo;
	}
	
	/**
	 * Serializes the user's information object.
	 * 
	 * @return A JSONObject representing this user information.
	 * 
	 * @throws JSONException Thrown if there is a problem building the JSON.
	 */
	public JSONObject toJson(
			final boolean withUsername,
			final boolean withPersonal) 
			throws JSONException {
		
		JSONObject result = new JSONObject();

		if(withUsername) {
			result.put(UserColumnKey.USERNAME.toString(), username);
		}

		result.put(UserColumnKey.EMAIL_ADDRESS.toString(), emailAddress);

		JSONObject permissionsJson = new JSONObject();
		permissionsJson.put(UserPermissionColumnKey.ADMIN.toString(), isAdmin);
		permissionsJson.put(
			UserPermissionColumnKey.ENABLED.toString(),
			isEnabled);
		permissionsJson.put(
			UserPermissionColumnKey.NEW_ACCOUNT.toString(),
			isNewAccount);
		permissionsJson.put(
			UserPermissionColumnKey.CAN_CREATE_CAMPAIGNS.toString(),
			campaignCreationPrivilege);
		permissionsJson.put(
			UserPermissionColumnKey.CAN_CREATE_CLASSES.toString(),
			classCreationPrivilege);
		permissionsJson.put(
			UserPermissionColumnKey.CAN_SETUP_USERS.toString(),
			userSetupPrivilege);
		result.put(UserColumnKey.PERMISSIONS.toString(), permissionsJson);

		if(campaigns != null) {
			result.put(UserColumnKey.CAMPAIGNS.toString(), campaigns);
		}
		if(classes != null) {
			result.put(UserColumnKey.CLASSES.toString(), classes);
		}

		if((personalInfo != null) && withPersonal) {
			result.put(
				UserColumnKey.PERSONAL.toString(),
				personalInfo.toJsonObject());
		}

		return result;
	}
}
