package org.ohmage.domain;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;

/**
 * This class encapsulates a user's information.
 * 
 * @author John Jenkins
 */
public class UserPersonal {
	private static final Logger LOGGER = Logger.getLogger(UserPersonal.class);
	
	private static final String KEY_FIRST_NAME = "first_name";
	private static final String KEY_LAST_NAME = "last_name";
	private static final String KEY_ORGANIZATION = "organization";
	private static final String KEY_PERSONAL_ID = "personal_id";
	private static final String KEY_EMAIL_ADDRESS = "email_address";
	private static final String KEY_JSON_DATA = "json_data";
	
	private final String firstName;
	private final String lastName;
	private final String organization;
	private final String personalId;
	private final String emailAddress;
	private final JSONObject jsonData;
	
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
	 * @param emailAddress The email address for the user.
	 * 
	 * @param jsonData Additional information about the user in a JSON format.
	 */
	public UserPersonal(String firstName, String lastName, String organization, String personalId, String emailAddress, String jsonData) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.organization = organization;
		this.personalId = personalId;
		this.emailAddress = emailAddress;
		
		JSONObject tJsonData = new JSONObject();
		if(! StringUtils.isEmptyOrWhitespaceOnly(jsonData)) {
			try {
				tJsonData = new JSONObject(jsonData);
			}
			catch(JSONException e) {
				LOGGER.error("Error converting a String that is supposed to represent a JSONObject into a JSONObject.", e);
			}
		}
		this.jsonData = tJsonData;
	}
	
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
	 * @param emailAddress The email address for the user.
	 * 
	 * @param jsonData Additional information about the user as a JSONObject.
	 */
	public UserPersonal(String firstName, String lastName, String organization, String personalId, String emailAddress, JSONObject jsonData) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.organization = organization;
		this.personalId = personalId;
		this.emailAddress = emailAddress;
		this.jsonData = jsonData;
	}
	
	/**
	 * Creates an UserPeronal object from the information in the information
	 * JSONObject.
	 * 
	 * @param information The JSONObject with the user's personal information.
	 * 
	 * @throws IllegalArgumentException Thrown if the information JSONObject
	 * 									object is invalid.
	 */
	public UserPersonal(final JSONObject information) {
		if(information == null) {
			throw new IllegalArgumentException("The information is null.");
		}
		
		try {
			firstName = information.getString(KEY_FIRST_NAME);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The first name is missing: " + KEY_FIRST_NAME);
		}
		
		try {
			lastName = information.getString(KEY_LAST_NAME);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The last name is missing: " + KEY_LAST_NAME);
		}
		
		try {
			organization = information.getString(KEY_ORGANIZATION);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The organization is missing: " + KEY_ORGANIZATION);
		}
		
		try {
			personalId = information.getString(KEY_PERSONAL_ID);
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The personal ID is missing: " + KEY_PERSONAL_ID);
		}
		
		String tEmailAddress = null;
		try {
			tEmailAddress = information.getString(KEY_EMAIL_ADDRESS);
		}
		catch(JSONException e) {
			// The email address may be empty.
		}
		emailAddress = tEmailAddress;
		
		JSONObject tJsonData = null;
		try {
			tJsonData = information.getJSONObject(KEY_JSON_DATA);
		}
		catch(JSONException e) {
			// The JSON data may be empty.
		}
		jsonData = tJsonData;
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
	 * Returns the user's email address or null if one doesn't exist.
	 * 
	 * @return the emailAddress
	 */
	public final String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * Returns the additional information about the user as a JSONObject or 
	 * null if one doesn't exist.
	 * 
	 * @return the jsonData
	 */
	public final JSONObject getJsonData() {
		return jsonData;
	}
	
	/**
	 * Checks if there is any meaningful data in this object.
	 * 
	 * @return Returns true if all fields in this object are null; otherwise, 
	 * 		   it returns false.
	 */
	public boolean isEmpty() {
		return((firstName == null) &&
			   (lastName == null) &&
			   (organization == null) &&
			   (personalId == null) &&
			   (emailAddress == null) &&
			   (jsonData == null));
	}

	/**
	 * Creates a JSONObject that represents this user. Any fields that were set
	 * to null will be missing an entry in this object.
	 * 
	 * @return A JSONObject that represents this object.
	 */
	public JSONObject toJsonObject() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(KEY_FIRST_NAME, firstName);
			result.put(KEY_LAST_NAME, lastName);
			result.put(KEY_ORGANIZATION, organization);
			result.put(KEY_PERSONAL_ID, personalId);
			result.put(KEY_EMAIL_ADDRESS, emailAddress);
			result.put(KEY_JSON_DATA, jsonData);
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("There was a problem building the JSONObject.", e);
			return null;
		}
	}
}