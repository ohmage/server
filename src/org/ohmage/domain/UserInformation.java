package org.ohmage.domain;

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
 * All of the information about a user in the system.
 * 
 * @author John Jenkins
 */
public class UserInformation {
	private static final String JSON_KEY_PERMISSIONS = "permissions";
	private static final String JSON_KEY_PERMISSIONS_ADMIN = "admin";
	private static final String JSON_KEY_PERMISSIONS_ENABLED = "enabled";
	private static final String JSON_KEY_PERMISSIONS_NEW_ACCOUNT = "new_account";
	private static final String JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION = "can_create_campaigns";
	
	private static final String JSON_KEY_CAMPAIGNS = "campaigns";
	private static final String JSON_KEY_CLASSES = "classes";
	
	private static final String JSON_KEY_PERSONAL_INFO = "personal";
	
	private final boolean isAdmin;
	private final boolean isEnabled;
	private final boolean isNewAccount;
	private final boolean campaignCreationPrivilege;
	
	private final Map<String, Set<Campaign.Role>> campaigns;
	private final Map<String, Clazz.Role> classes;
	
	private final UserPersonal personalInfo;
	
	/**
	 * Creates a new information object for this user.
	 * 
	 * @param isAdmin Whether or not the user is an admin.
	 * 
	 * @param isEnabled Whether or not the user's account is enabled.
	 * 
	 * @param isNewAccount Whether or not the account is new.
	 * 
	 * @param campaignCreationPrivilege Whether or not the user is allowed to
	 * 									create new campaigns.
	 * 
	 * @param campaigns The map of campaign IDs to a set of the user's roles in
	 * 					those campaigns.
	 * 
	 * @param classes The map of class IDs to the user's role in that class.
	 * 
	 * @param personalInfo The personal information about the user or null if
	 * 					   the user doesn't have a personal information record.
	 * 
	 * @throws DomainException The campaign and/or class parameter is null.
	 */
	public UserInformation(
			final boolean isAdmin,
			final boolean isEnabled,
			final boolean isNewAccount,
			final boolean campaignCreationPrivilege,
			final Map<String, Set<Campaign.Role>> campaigns,
			final Map<String, Clazz.Role> classes,
			final UserPersonal personalInfo) 
			throws DomainException {
		
		if(campaigns == null) {
			throw new DomainException(
					"The campaign ID-role map is null.");
		}
		else if(classes == null) {
			throw new DomainException(
					"The class ID-role map is null.");
		}
		
		this.isAdmin = isAdmin;
		this.isEnabled = isEnabled;
		this.isNewAccount = isNewAccount;
		this.campaignCreationPrivilege = campaignCreationPrivilege;
		
		this.campaigns = new HashMap<String, Set<Campaign.Role>>(campaigns);
		this.classes = new HashMap<String, Clazz.Role>(classes);
		
		this.personalInfo = personalInfo;
	}
	
	/**
	 * Deserializes a JSONObject that is information about a user.
	 * 
	 * @param userInfo The user's information as a JSONObject.
	 * 
	 * @throws DomainException Thrown if a required field is missing.
	 */
	public UserInformation(final JSONObject userInfo) throws DomainException {
		JSONObject permissionsJson;
		try {
			permissionsJson = userInfo.getJSONObject(JSON_KEY_PERMISSIONS);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The JSON information is missing the permissions key.",
					e);
		}
		
		try {
			isAdmin = permissionsJson.getBoolean(JSON_KEY_PERMISSIONS_ADMIN);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The permissions are missing the admin value.",
					e);
		}
		
		try {
			isEnabled = 
				permissionsJson.getBoolean(JSON_KEY_PERMISSIONS_ENABLED);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The permissions are missing the enabled value.",
					e);
		}
		
		try {
			isNewAccount = 
				permissionsJson.getBoolean(JSON_KEY_PERMISSIONS_NEW_ACCOUNT);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The permissions are missing the new account value.",
					e);
		}
		
		try {
			campaignCreationPrivilege = 
				permissionsJson.getBoolean(
						JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The permissions are missing the campaign creation value.",
					e);
		}
		
		JSONObject campaignsJson;
		try {
			campaignsJson = userInfo.getJSONObject(JSON_KEY_CAMPAIGNS);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The campaigns list is missing.",
					e);
		}
		
		campaigns = 
			new HashMap<String, Set<Campaign.Role>>(campaignsJson.length());
		Iterator<?> campaignIds = campaignsJson.keys();
		while(campaignIds.hasNext()) {
			String campaignId = (String) campaignIds.next();
			
			JSONArray rolesJson;
			try {
				rolesJson = campaignsJson.getJSONArray(campaignId);
			}
			catch(JSONException e) {
				throw new DomainException(
						"The campaign list has changed while being read.",
						e);
			}
			
			int numRoles = rolesJson.length();
			Set<Campaign.Role> roles = new HashSet<Campaign.Role>(numRoles);
			for(int i = 0; i < numRoles; i++) {
				try {
					roles.add(
							Campaign.Role.getValue(
									rolesJson.getString(i)));
				}
				catch(JSONException e) {
					throw new DomainException(
							"The campaign list has changed while being read.",
							e);
				}
				catch(IllegalArgumentException e) {
					throw new DomainException(
							"The campaign role is unknown.",
							e);
				}
			}
			
			campaigns.put(campaignId, roles);
		}
		
		JSONObject classesJson;
		try {
			classesJson = userInfo.getJSONObject(JSON_KEY_CLASSES);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The class list is missing.",
					e);
		}
		
		classes = new HashMap<String, Clazz.Role>(classesJson.length());
		Iterator<?> classIds = classesJson.keys();
		while(classIds.hasNext()) {
			String classId = (String) classIds.next();
			
			Clazz.Role role;
			try {
				role = Clazz.Role.getValue(classesJson.getString(classId));
			}
			catch(JSONException e) {
				throw new DomainException(
						"The class list has changed while being read.",
						e);
			}
			catch(IllegalArgumentException e) {
				throw new DomainException(
						"The class role is unknown.",
						e);
			}
			
			classes.put(classId, role);
		}
		
		UserPersonal tPersonalInfo;
		try {
			tPersonalInfo = 
				new UserPersonal(
						userInfo.getJSONObject(
								JSON_KEY_PERSONAL_INFO));
		}
		catch(JSONException e) {
			tPersonalInfo = null;
		}
		personalInfo = tPersonalInfo;
	}
	
	/**
	 * Serializes the user's information object.
	 * 
	 * @return A JSONObject representing this user information.
	 * 
	 * @throws JSONException Thrown if there is a problem building the JSON.
	 */
	public JSONObject toJson() throws JSONException {
		JSONObject result = new JSONObject();
		
		JSONObject permissionsJson = new JSONObject();
		permissionsJson.put(
				JSON_KEY_PERMISSIONS_ADMIN, 
				isAdmin);
		permissionsJson.put(
				JSON_KEY_PERMISSIONS_ENABLED, 
				isEnabled);
		permissionsJson.put(
				JSON_KEY_PERMISSIONS_NEW_ACCOUNT, 
				isNewAccount);
		permissionsJson.put(
				JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION, 
				campaignCreationPrivilege);
		result.put(JSON_KEY_PERMISSIONS, permissionsJson);
		
		result.put(JSON_KEY_CAMPAIGNS, campaigns);
		result.put(JSON_KEY_CLASSES, classes);
		
		if(personalInfo != null) {
			result.put(JSON_KEY_PERSONAL_INFO, personalInfo.toJsonObject());
		}
		
		return result;
	}
}