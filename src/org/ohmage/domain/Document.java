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
import java.util.Iterator;
import java.util.Map;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;
import org.ohmage.util.DateTimeUtils;

/**
 * A class to represent documents in the database. 
 * 
 * @author John Jenkins
 */
public class Document {	
	private static final String JSON_KEY_NAME = "name";
	private static final String JSON_KEY_DESCRIPTION = "description";
	private static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	private static final String JSON_KEY_LAST_MODIFIED = "last_modified";
	private static final String JSON_KEY_CREATION_DATE = "creation_date";
	private static final String JSON_KEY_SIZE = "size";
	private static final String JSON_KEY_CREATOR = "creator";
	private static final String JSON_KEY_USER_ROLE = "user_role";
	private static final String JSON_KEY_CAMPAIGN_ROLE = "campaign_role";
	private static final String JSON_KEY_CLASS_ROLE = "class_role";
	private static final String JSON_KEY_MAX_ROLE = "user_max_role";
	
	private final String documentId;
	private final String name;
	private final String description;
	private final DateTime lastModified;
	private final DateTime creationDate;
	private final int size;
	private final String creator;
	
	/**
	 * Known document privacy states.
	 * 
	 * @author John Jenkins
	 */
	public static enum PrivacyState {
		PRIVATE,
		SHARED;
		
		/**
		 * Converts a String value into a PrivacyState or throws an exception
		 * if there is no comparable privacy state.
		 * 
		 * @param privacyState The privacy state to be converted into a 
		 * 					   PrivacyState enum.
		 * 
		 * @return A comparable PrivacyState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									PrivacyState enum.
		 */
		public static PrivacyState getValue(String privacyState) {
			return valueOf(privacyState.toUpperCase());
		}
		
		/**
		 * Converts the privacy state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private final PrivacyState privacyState;
	
	/**
	 * Known document roles.
	 * @author  John Jenkins
	 */
	public static enum Role {
		READER,
		WRITER,
		OWNER;
		
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
		 * Compares this Role with another Role that may be null. The order is
		 * OWNER > WRITER > READER > null. If 'role' is greater than this Role,
		 * 1 is returned. If they are the same, 0 is returned. Otherwise, -1 is
		 * returned.
		 * 
		 * @param role The Role to compare against this Role.
		 * 
		 * @return 1 if 'role' is greater than this role, 0 if they are the 
		 * 		   same, or -1 otherwise.
		 */
		public int compare(Role role) {
			if(this == OWNER) {
				if(role == OWNER) {
					return 0;
				}
			}
			else if(this == WRITER) {
				if(role == OWNER) {
					return 1;
				}
				else if(role == WRITER) {
					return 0;
				}
			}
			else if(this == READER) {
				if((role == OWNER) || (role == WRITER)) {
					return 1;
				}
				else if(role == READER) {
					return 0;
				}
			}
			
			return -1;
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	private Role maxRole;
	private Role userRole;
	private final Map<String, Role> campaignAndRole;
	private final Map<String, Role> classAndRole;
	
	/**
	 * Creates a new Document object that contains information about
	 * a document and keeps track of a list of roles for the user, campaigns, 
	 * and classes. All values should be taken from the database.
	 * 
	 * @param documentId A unique identifier for this document. 
	 * 
	 * @param name The name of the document.
	 * 
	 * @param description A description of the document.
	 * 
	 * @param lastModified The last time the document was modified.
	 * 
	 * @param size The size of the document in bytes.
	 * 
	 * @param privacyState The current privacy state of the document.
	 * 
	 * @throws DomainException Thrown if any of the parameters are null or 
	 * 						   invalid.
	 */
	public Document(
			final String documentId, 
			final String name, 
			final String description,
			final PrivacyState privacyState, 
			final DateTime lastModified, 
			final DateTime creationDate, 
			final int size, 
			final String creator)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_ID,
					"The document's ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_NAME,
					"The document's name cannot be null or whitespace only.");
		}
		else if(privacyState == null) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_PRIVACY_STATE,
					"The document's privacy state cannot be null.");
		}
		else if(lastModified == null) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The document's last modified value cannot be null.");
		}
		else if(creationDate == null) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The document's creation date cannot be null.");
		}
		else if(size < 0) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_CONTENTS,
					"The document's size cannot be negative.");
		}
		
		this.documentId = documentId;
		this.name = name;
		this.description = description;
		this.privacyState = privacyState;
		this.lastModified = lastModified;
		this.creationDate = creationDate;
		this.size = size;
		this.creator = creator;
		
		maxRole = null;
		campaignAndRole = new HashMap<String, Role>();
		classAndRole = new HashMap<String, Role>();
	}
	
	/**
	 * Creates a Document object from the data in the JSONObject.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @param documentInfo The document's information as a JSONObject.
	 * 
	 * @throws DomainException Thrown if one of the parameters is null or 
	 * 						   invalid.
	 */
	public Document(
			final String documentId, 
			final JSONObject documentInfo)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_ID,
					"The document ID is null.");
		}
		else if(documentInfo == null) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_ID,
					"The document information is null.");
		}
		
		this.documentId = documentId;
		
		try {
			name = documentInfo.getString(JSON_KEY_NAME);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_NAME,
					"The JSONObject is missing the name value.",
					e);
		}
		
		String tDescription = null;
		try {
			tDescription = documentInfo.getString(JSON_KEY_DESCRIPTION);
		}
		catch(JSONException e) {
			// The description is optional.
		}
		description = tDescription;
		
		try {
			privacyState = PrivacyState.getValue(documentInfo.getString(JSON_KEY_PRIVACY_STATE));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_PRIVACY_STATE,
					"The JSONObject is missing the privacy state value.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_PRIVACY_STATE,
					"The privacy state is an unknown privacy state.",
					e);
		}
		
		try {
			lastModified = 
					DateTimeUtils.getDateTimeFromString(
						documentInfo.getString(JSON_KEY_LAST_MODIFIED));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The JSONObject is missing the last modified value.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The date-time could not be parsed.",
					e);
		}
		
		try {
			creationDate = 
					DateTimeUtils.getDateTimeFromString(
						documentInfo.getString(JSON_KEY_CREATION_DATE));
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The JSONObject is missing the creation date value.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					ErrorCode.SERVER_INVALID_DATE,
					"The date-time could not be parsed.",
					e);
		}
		
		try {
			size = documentInfo.getInt(JSON_KEY_SIZE);
		}
		catch(JSONException e) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_CONTENTS,
					"The JSONObject is missing the size value.", 
					e);
		}
		
		try {
			creator = documentInfo.getString(JSON_KEY_CREATOR);
		}
		catch(JSONException e) {
			throw new DomainException(
					"The JSONObject is missing the creator value.", 
					e);
		}
		
		try {
			JSONObject campaignAndRoles = documentInfo.getJSONObject(JSON_KEY_CAMPAIGN_ROLE);
			campaignAndRole = new HashMap<String, Role>(campaignAndRoles.length());
			
			Iterator<?> keys = campaignAndRoles.keys();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				campaignAndRole.put(key, Role.getValue(campaignAndRoles.getString(key)));
			}
		}
		catch(JSONException e) {
			throw new DomainException(
					"The JSONObject is missing the campaign and role value.",
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					"The role is unknown.", 
					e);
		}
		
		try {
			JSONObject classAndRoles = documentInfo.getJSONObject(JSON_KEY_CLASS_ROLE);
			classAndRole = new HashMap<String, Role>(classAndRoles.length());
			
			Iterator<?> keys = classAndRoles.keys();
			while(keys.hasNext()) {
				String key = (String) keys.next();
				classAndRole.put(key, Role.getValue(classAndRoles.getString(key)));
			}
		}
		catch(JSONException e) {
			throw new DomainException(
					"The JSONObject is missing the class and role value.", 
					e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
					"The role is unknown.", 
					e);
		}
		
		try {
			userRole = Role.getValue(documentInfo.getString(JSON_KEY_USER_ROLE));
		}
		catch(JSONException e) {
			// It's an optional parameter.
		}
		catch(IllegalArgumentException e) {
			// We really should thrown an error here, but the output for 
			// document read states that if the role is unknown that we output
			// an empty string for the role. Instead, we should just omit the
			// parameter. Also, why do we not have the user's role on output?
			//throw new IllegalArgumentException("The role is unknown.", e);
		}
		
		try {
			maxRole = Role.getValue(documentInfo.getString(JSON_KEY_MAX_ROLE));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException("The maximum role for the user is missing.", e);
		}
		catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("The user's maximum role is unknown.", e);
		}
	}
	
	/**
	 * Returns the unique ID for this document.
	 * 
	 * @return The unique ID for this document.
	 */
	public String getDocumentId() {
		return documentId;
	}
	
	/**
	 * Returns the name of the document.
	 * 
	 * @return The name of the document.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the description of the document.
	 * 
	 * @return The description of the document. May be null.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the privacy state of the document.
	 * 
	 * @return The privacy state of the document.
	 */
	public PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns a timestamp of the last time the document was modified.
	 * 
	 * @return A timestamp of the last time the document was modified.
	 */
	public DateTime getLastModified() {
		return lastModified;
	}
	
	/**
	 * Returns a timestamp of when this document was created.
	 * 
	 * @return A timestamp of when this document was created.
	 */
	public DateTime getCreationDate() {
		return creationDate;
	}
	
	/**
	 * Returns the size of the document in bytes.
	 * 
	 * @return The size of the document in bytes.
	 */
	public int getSize() {
		return size;
	}
	
	/**
	 * Returns the username of the creator of this document.
	 * 
	 * @return The username of the creator of this document.
	 */
	public String getCreator() {
		return creator;
	}
	
	/**
	 * Specifies the document role for the user that owns this Document object.
	 * 
	 * @param documentRole The role for the user that owns this object.
	 * 
	 * @throws DomainException Thrown if the role is null.
	 */
	public void setUserRole(Role documentRole) 
			throws DomainException {
		
		if(documentRole == null) {
			throw new DomainException(
					ErrorCode.DOCUMENT_INVALID_ROLE,
					"The role is null.");
		}
		
		userRole = documentRole;
		
		if(maxRole == null) {
			maxRole = documentRole;
		}
		else if(maxRole.compare(documentRole) == 1) {
			maxRole = documentRole;
		}
	}
	
	/**
	 * Returns the document role for the user that owns this Document object.
	 * 
	 * @return The document role for the user that owns this Document object.
	 */
	public Role getUserRole() {
		return userRole;
	}
	
	/**
	 * Adds the campaign if it didn't already exist and then associates a
	 * document role with that campaign. If the campaign was already associated
	 * with this document, it removes the old role and replaces it with this
	 * new one, returning the old role.
	 * 
	 * @param campaignId A unique identifier for the campaign.
	 * 
	 * @param role A new role to be associated with this campaign for this 
	 * 			   document.
	 * 
	 * @return Returns the old role if one existed; otherwise, null.
	 * 
	 * @throws DomainException Thrown if the campaign ID or role are null.
	 */
	public Role addCampaignRole(String campaignId, Role role) 
			throws DomainException {
		
		if(campaignId == null) {
			throw new DomainException(
					ErrorCode.CAMPAIGN_INVALID_ID,
					"The campaign ID is null.");
		}
		else if(role == null) {
			throw new DomainException(
					ErrorCode.CAMPAIGN_INVALID_ROLE,
					"The role is null.");
		}

		if(maxRole == null) {
			maxRole = role;
		}
		else if(maxRole.compare(role) == 1) {
			maxRole = role;
		}
		
		return campaignAndRole.put(campaignId, role);
	}
	
	/**
	 * Returns a map of the campaigns and their role that this user has with
	 * this document.
	 * 
	 * @return A map of the campaigns and their role that this user has with
	 * 		   this document.
	 */
	public Map<String, Role> getCampaignsAndTheirRoles() {
		return Collections.unmodifiableMap(campaignAndRole);
	}
	
	/**
	 * Adds a class to the list of classes associated with this document if it
	 * didn't already exist. Then, it associates the document role to the class
	 * for this document. If the class was already associated, the old role is
	 * returned and the new role is stored.
	 * 
	 * @param classId A unique identifier for a class.
	 * 
	 * @param role The role to be associated with this class for this
	 * 			   document.
	 * 
	 * @return The old document role for this class for this user.
	 * 
	 * @throws DomainException Thrown if the class ID or role are null.
	 */
	public Role addClassRole(String classId, Role role) 
			throws DomainException {
		
		if(classId == null) {
			throw new DomainException(
					ErrorCode.CLASS_INVALID_ID,
					"The class ID is null.");
		}
		else if(role == null) {
			throw new DomainException(
					ErrorCode.CLASS_INVALID_ROLE,
					"The role is null.");
		}

		if(maxRole == null) {
			maxRole = role;
		}
		else if(maxRole.compare(role) == 1) {
			maxRole = role;
		}
		
		return classAndRole.put(classId, role);
	}
	
	/**
	 * Returns a map of the classes and their associated document roles. 
	 * 
	 * @return A map of the classes and their associated document roles.
	 */
	public Map<String, Role> getClassAndTheirRoles() {
		return classAndRole;
	}
	
	/**
	 * Directly overrides the maximum role for this document. This should only
	 * be used in instances where the maximum role needs to be overriden and
	 * before any other user/campaign/class associations will be performed 
	 * because those associations will refresh the maximum role with their role
	 * if the new role is higher than this one.
	 * 
	 * @param maxRole The new maximum role for this document.
	 * 
	 * @return The old maximum role for this document. This may be null if no
	 * 		   maximum role has been set and no user/campaign/class 
	 * 		   associations have been made.
	 */
	public Role setMaxRole(final Role maxRole) {
		Role tempRole = this.maxRole;
		
		this.maxRole = maxRole;
		
		return tempRole;
	}
	
	/**
	 * Returns the maximum role for this user based on the personal, campaign,
	 * and class roles that have already been associated with it. This may be
	 * higher than any of those roles if it has been directly overridden 
	 * through {@link #setMaxRole(Role)}.
	 * 
	 * @return The maximum role associated with or given to this document or
	 * 		   null if no such association has yet been made.
	 */
	public Role getMaxRole() {
		return maxRole;
	}
	
	/**
	 * Creates a JSONObject representing this document's information.
	 * 
	 * @return A JSONObject representing this document's information.
	 * 
	 * @throws JSONException Thrown if there was an error building the 
	 * 						   JSONObject.
	 */
	public JSONObject toJsonObject() 
			throws JSONException {

		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_NAME, name);
		result.put(JSON_KEY_DESCRIPTION, ((description ==  null) ? "" : description));
		result.put(JSON_KEY_PRIVACY_STATE, privacyState);
		result.put(JSON_KEY_LAST_MODIFIED, DateTimeUtils.getIso8601DateString(lastModified, true));
		result.put(JSON_KEY_CREATION_DATE, DateTimeUtils.getIso8601DateString(creationDate, true));
		result.put(JSON_KEY_SIZE, size);
		result.put(JSON_KEY_CREATOR, creator);
		
		result.put(JSON_KEY_USER_ROLE, ((userRole == null) ? "" : userRole));
		result.put(JSON_KEY_CAMPAIGN_ROLE, new JSONObject(campaignAndRole));
		result.put(JSON_KEY_CLASS_ROLE, new JSONObject(classAndRole));
		
		// If the user is an admin, they may not have any role with the 
		// document.
		if(maxRole != null) {
			result.put(JSON_KEY_MAX_ROLE, maxRole.toString());
		}
		
		return result;
	}

	/**
	 * Generates a hash code which must compare the same variables as 
	 * {@link #equals(Object)}.
	 * 
	 * @see #equals(Object)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((documentId == null) ? 0 : documentId.hashCode());
		return result;
	}

	/**
	 * The system should never have to Document objects with the 
	 * same document ID but different contents. Therefore, all we need to check
	 * is the document ID between the two objects.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Document other = (Document) obj;
		if (documentId == null) {
			if (other.documentId != null)
				return false;
		} else if (!documentId.equals(other.documentId))
			return false;
		return true;
	}
}
