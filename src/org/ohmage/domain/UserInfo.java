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

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;


/**
 * A class for holding personal information for a user.
 * 
 * @author John Jenkins
 */
public class UserInfo {
	private String _username;
	private String _firstName;
	private String _lastName;
	private String _organization;
	private String _personalId;
	private String _emailAddress;
	private JSONObject _jsonData;
	
	/**
	 * Constructs information about this user.
	 * 
	 * @param username The username for this user.
	 * 
	 * @param firstName The first name of this user.
	 * 
	 * @param lastName The last name of this user.
	 * 
	 * @param organization The organization to which this user belongs.
	 * 
	 * @param personalId The personal identification number for this user.
	 * 
	 * @param emailAddress The email address of this user which may be null or
	 * 					   empty.
	 * 
	 * @param jsonData JSON data associated with this user which may be null or
	 * 				   empty.
	 * 
	 * @throws IllegalArgumentException Thrown if the username, first name, 
	 * 									last name, organization or the personal
	 * 									identifier for this user are null or 
	 * 									whitespace only.
	 */
	public UserInfo(String username, String firstName, String lastName, String organization, String personalId, String emailAddress, String jsonData) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(username)) {
			throw new IllegalArgumentException("The username cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(firstName)) {
			throw new IllegalArgumentException("The first name cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(lastName)) {
			throw new IllegalArgumentException("The last name cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(organization)) {
			throw new IllegalArgumentException("The organization cannot be null or whitespace only.");
		}
		
		_username = username;
		_firstName = firstName;
		_lastName = lastName;
		_organization = organization;
		_personalId = personalId;
		_emailAddress = emailAddress;
		
		try {
			_jsonData = new JSONObject(jsonData);
		}
		catch(JSONException e) {
			_jsonData = null;
		}
	}
	
	/**
	 * Returns the username for this user.
	 * 
	 * @return The username for this user.
	 */
	public String getUsername() {
		return _username;
	}
	
	/**
	 * Returns the first name of this user.
	 * 
	 * @return The first name of this user.
	 */
	public String getFirstName() {
		return _firstName;
	}
	
	/**
	 * Returns the last name of this user.
	 * 
	 * @return The last name of this user.
	 */
	public String getLastName() {
		return _lastName;
	}
	
	/**
	 * Returns the organization to which this user belongs.
	 * 
	 * @return The organization to which this user belongs.
	 */
	public String getOrganization() {
		return _organization;
	}
	
	/**
	 * Returns the personal identification number for this user.
	 * 
	 * @return The personal identification number for this user.
	 */
	public String getPersonalId() {
		return _personalId;
	}
	
	/**
	 * Returns the email address of this user, which may be null or empty if
	 * no such data was found.
	 * 
	 * @return The email address of this user, which may be null or empty if
	 * 		   no such data was found.
	 */
	public String getEmailAddress() {
		return _emailAddress;
	}
	
	/**
	 * Returns the JSON data that was associated with this user, which may be
	 * null or empty if no such data was found.
	 * 
	 * @return The JSON data that was associated with this user, which may be
	 * 		   null or empty if no such data was found.
	 */
	public JSONObject getJsonData() {
		return _jsonData;
	}
	
	/**
	 * Returns a JSONObject representation of the user's information.
	 * 
	 * @param withUsername Whether or not he username should be included in the
	 * 					   return value.
	 * 
	 * @return Returns a JSONObject representing this user.
	 * 
	 * @throws JSONException Thrown if there is an error with constructing the
	 * 						 JSONObject. This is done as opposed in favor of
	 * 						 swallowing the exception myself and returning 
	 * 						 null.
	 */
	public JSONObject toJsonObject(boolean withUsername) throws JSONException {
		JSONObject result = new JSONObject();
		
		if(withUsername) {
			result.put("username", _username);
		}
		result.put("first_name", _firstName);
		result.put("last_name", _lastName);
		result.put("organization", _organization);
		result.put("personal_id", _personalId);
		result.put("email_address", ((_emailAddress == null) ? "" : _emailAddress));
		result.put("json_data", ((_jsonData == null) ? new JSONObject() : _jsonData));
		
		return result;
	}
}
