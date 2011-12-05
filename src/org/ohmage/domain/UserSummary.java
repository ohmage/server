package org.ohmage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.campaign.Campaign;

/**
 * This class is responsible for collecting and displaying the information 
 * about a user. This includes their campaign creation privilege, the campaigns
 * and classes to which they belong, and the union of their roles in those
 * campaigns and classes. 
 * 
 * @author John Jenkins
 */
public class UserSummary {
	private static final Logger LOGGER = Logger.getLogger(UserSummary.class);
	
	private static final String JSON_KEY_PERMISSIONS = "permissions";
	private static final String JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION = "can_create_campaigns";
	
	private static final String JSON_KEY_CAMPAIGNS = "campaigns";
	private static final String JSON_KEY_CAMPAIGN_ROLES = "campaign_roles";
	
	private static final String JSON_KEY_CLASSES = "classes";
	private static final String JSON_KEY_CLASS_ROLES = "class_roles";
	
	private final boolean campaignCreationPrivilege;
	
	private final Map<String, String> campaigns;
	private final Set<Campaign.Role> campaignRoles;
	
	private final Map<String, String> classes;
	private final Set<Clazz.Role> classRoles;
	
	/**
	 * Creates a new user information object with a default campaign creation
	 * privilege, no campaigns or campaign roles, and no classes or class 
	 * roles.
	 * 
	 * @param campaignCreationPrivilege Whether or not the user is allowed to
	 * 									create new campaigns.
	 */
	public UserSummary(boolean campaignCreationPrivilege) {
		this.campaignCreationPrivilege = campaignCreationPrivilege;
		
		campaigns = new HashMap<String, String>();
		campaignRoles = new HashSet<Campaign.Role>();
		
		classes = new HashMap<String, String>();
		classRoles = new HashSet<Clazz.Role>();
	}
	
	/**
	 * Creates a UserSummary object from the information.
	 * 
	 * @param information The information used to create this object.
	 * 
	 * @throws IllegalArgumentException Thrown if information object is null or
	 * 									malformed.
	 */
	public UserSummary(final JSONObject information) {
		if(information == null) {
			throw new IllegalArgumentException("The information is null.");
		}
		
		JSONObject permissions;
		try {
			permissions = information.getJSONObject(JSON_KEY_PERMISSIONS);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The permissions JSON is missing.", e);
		}
		
		try {
			campaignCreationPrivilege = permissions.getBoolean(JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION); 
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The campaign creation permission is missing from the list of permissions.", e);
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
			throw new IllegalArgumentException("The campaigns JSON is missing or malformed.", e);
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
			throw new IllegalArgumentException("The campaign roles JSON is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The campaign roles JSON contains an unknown role.", e);
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
			throw new IllegalArgumentException("The classes JSON is missing or malformed.", e);
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
			throw new IllegalArgumentException("The class roles JSON is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The class roles JSON contains an unknown role.", e);
		}
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
	 * Adds a new campaign to the list of campaigns.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param campaignName The campaign's name.
	 */
	public void addCampaign(String campaignId, String campaignName) {
		if(campaignId == null) {
			throw new NullPointerException("The campaign ID is null.");
		}
		else if(campaignName == null) {
			throw new NullPointerException("The campaign name is null.");
		}
		
		campaigns.put(campaignId, campaignName);
	}
	
	/**
	 * Adds a Map of campaign ID to campaign names associations to the current
	 * Map.
	 * 
	 * @param campaigns A Map of campaign IDs to campaign names to be added to
	 * 					the current Map.
	 */
	public void addCampaigns(Map<String, String> campaigns) {
		if(campaigns == null) {
			throw new NullPointerException("The campaign ID to name map is null.");
		}
		
		this.campaigns.putAll(campaigns);
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
	 * Adds a new campaign role.
	 * 
	 * @param campaignRole A campaign role to be added to be added to the list.
	 */
	public void addCampaignRole(Campaign.Role campaignRole) {
		if(campaignRole == null) {
			throw new NullPointerException("The campaign role is null.");
		}
		
		campaignRoles.add(campaignRole);
	}
	
	/**
	 * Adds a Collection of campaign roles to the current list of campaign 
	 * roles.
	 * 
	 * @param campaignRoles A Collection of campaign roles to be added to the
	 * 						list.
	 */
	public void addCampaignRoles(Collection<Campaign.Role> campaignRoles) {
		if(campaignRoles == null) {
			throw new NullPointerException("The collection of campaign roles is null.");
		}
		
		this.campaignRoles.addAll(campaignRoles);
	}
	
	/**
	 * Adds a class ID and a class name association to the current Map.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param className The class' name.
	 */
	public void addClass(String classId, String className) {
		if(classId == null) {
			throw new NullPointerException("The class ID is null.");
		}
		else if(className == null) {
			throw new NullPointerException("The class name is null.");
		}

		classes.put(classId, className);
	}
	
	/**
	 * Adds a Map of class ID to class name associations to the current Map.
	 * 
	 * @param classes A Collection of class roles to be added to the list.
	 */
	public void addClasses(Map<String, String> classes) {
		if(classes == null) {
			throw new NullPointerException("The class ID to name map is null.");
		}

		this.classes.putAll(classes);
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
	 * Adds a new class role to the list of class roles.
	 * 
	 * @param classRole The class role to be added.
	 */
	public void addClassRole(Clazz.Role classRole) {
		if(classRole == null) {
			throw new NullPointerException("The class role is null.");
		}
		
		classRoles.add(classRole);
	}
	
	/**
	 * Adds a Collection of class roles to the current list of class roles.
	 * 
	 * @param classRoles A Collection of class roles to add to the current list
	 * 					 of class roles.
	 */
	public void addClassRoles(Collection<Clazz.Role> classRoles) {
		if(classRoles == null) {
			throw new NullPointerException("The collection of class roles is null.");
		}

		this.classRoles.addAll(classRoles);
	}
	
	/**
	 * Creates a JSONObject representation of this instance of this class.
	 * 
	 * @return A JSONObject representation of this object.
	 */
	public JSONObject toJsonObject() {
		try {
			JSONObject result = new JSONObject();
			
			JSONObject permissions = new JSONObject();
			permissions.put(JSON_KEY_PERMISSIONS_CAMPAIGN_CREATION, campaignCreationPrivilege);
			result.put(JSON_KEY_PERMISSIONS, permissions);
			
			result.put(JSON_KEY_CAMPAIGNS, campaigns);
			result.put(JSON_KEY_CAMPAIGN_ROLES, campaignRoles);
			
			result.put(JSON_KEY_CLASSES, classes);
			result.put(JSON_KEY_CLASS_ROLES, classRoles);
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the JSONObject.", e);
			return null;
		}
	}
}