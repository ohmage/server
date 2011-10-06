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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * A class to represent documents in the database. 
 * 
 * @author John Jenkins
 */
public class DocumentInformation {	
	private static final Logger LOGGER = Logger.getLogger(DocumentInformation.class);
	
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
	
	private final String documentId;
	private final String name;
	private final String description;
	private final DocumentPrivacyStateCache.PrivacyState privacyState;
	private final Date lastModified;
	private final Date creationDate;
	private final int size;
	private final String creator;
	
	private String userRole;
	private final Map<String, DocumentRoleCache.Role> campaignAndRole;
	private final Map<String, DocumentRoleCache.Role> classAndRole;
	
	/**
	 * Creates a new DocumentInformation object that contains information about
	 * a document and keeps track of a list of roles for the user, campaigns, 
	 * and classes. All values should be taken from the database.
	 * 
	 * @param documentId A unique identifier for this document. 
	 * 
	 * @param name The name of the document.
	 * 
	 * @param description A description of the document.
	 * 
	 * @param privacyState The current privacy state of the document.
	 * 
	 * @param lastModified The last time the document was modified.
	 * 
	 * @param size The size of the document in bytes.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are
	 * 									null and/or invalid.
	 */
	public DocumentInformation(String documentId, String name, String description, DocumentPrivacyStateCache.PrivacyState privacyState, 
			Date lastModified, Date creationDate, int size, String creator) {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document's ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The document's name cannot be null or whitespace only.");
		}
		else if(privacyState == null) {
			throw new IllegalArgumentException("The document's privacy state cannot be null.");
		}
		else if(lastModified == null) {
			throw new IllegalArgumentException("The document's last modified value cannot be null.");
		}
		else if(creationDate == null) {
			throw new IllegalArgumentException("The document's creation date cannot be null.");
		}
		else if(size < 0) {
			throw new IllegalArgumentException("The document's size cannot be negative.");
		}
		
		this.documentId = documentId;
		this.name = name;
		this.description = description;
		this.privacyState = privacyState;
		this.lastModified = lastModified;
		this.creationDate = creationDate;
		this.size = size;
		this.creator = creator;

		campaignAndRole = new HashMap<String, DocumentRoleCache.Role>();
		classAndRole = new HashMap<String, DocumentRoleCache.Role>();
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
	public DocumentPrivacyStateCache.PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns a timestamp of the last time the document was modified.
	 * 
	 * @return A timestamp of the last time the document was modified.
	 */
	public Date getLastModified() {
		return lastModified;
	}
	
	/**
	 * Returns a timestamp of when this document was created.
	 * 
	 * @return A timestamp of when this document was created.
	 */
	public Date getCreationDate() {
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
	 * Specifies the document for the user that owns this Document object.
	 * 
	 * @param documentRole The role for the user that owns this object.
	 * 
	 * @throws IllegalArgumentException Thrown if the role is unknown.
	 */
	public void addUserRole(String documentRole) {
		if(! DocumentRoleCache.instance().getKeys().contains(documentRole)) {
			throw new IllegalArgumentException("The role is unknown.");
		}
		
		userRole = documentRole;
	}
	
	/**
	 * Returns the document role for the user that owns this Document object.
	 * 
	 * @return The document role for the user that owns this Document object.
	 */
	public String getUserRole() {
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
	 * @param documentRole A new role to be associated with this campaign for
	 * 					   this document.
	 * 
	 * @return Returns the old role if one existed; otherwise, null.
	 * 
	 * @throws IllegalArgumentException Thrown if the campaign ID is null or
	 * 									whitespace only or if the documentRole
	 * 									is unknown.
	 */
	public DocumentRoleCache.Role addCampaignRole(String campaignId, DocumentRoleCache.Role documentRole) {
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new IllegalArgumentException("The campaign ID cannot be null or whitespace only.");
		}
		else if(documentRole == null) {
			throw new IllegalArgumentException("The role cannot be null.");
		}
		
		return campaignAndRole.put(campaignId, documentRole);
	}
	
	/**
	 * Returns a map of the campaigns and their role that this user has with
	 * this document.
	 * 
	 * @return A map of the campaigns and their role that this user has with
	 * 		   this document.
	 */
	public Map<String, DocumentRoleCache.Role> getCampaignsAndTheirRoles() {
		return campaignAndRole;
	}
	
	/**
	 * Adds a class to the list of classes associated with this document if it
	 * didn't already exist. Then, it associates the document role to the class
	 * for this document. If the class was already associated, the old role is
	 * returned and the new role is stored.
	 * 
	 * @param classId A unique identifier for a class.
	 * 
	 * @param documentRole The role to be associated with this class for this
	 * 					   document.
	 * 
	 * @return The old document role for this class for this user.
	 * 
	 * @throws IllegalArgumentException Thrown if the class ID is null or 
	 * 									whitespace only or if the document role
	 * 									is unknown.
	 */
	public DocumentRoleCache.Role addClassRole(String classId, DocumentRoleCache.Role documentRole) {
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			throw new IllegalArgumentException("The class ID cannot be null or whitespace only.");
		}
		else if(documentRole == null) {
			throw new IllegalArgumentException("The role cannot be null.");
		}
		
		return classAndRole.put(classId, documentRole);
	}
	
	/**
	 * Returns a map of the classes and their associated document roles. 
	 * 
	 * @return A map of the classes and their associated document roles.
	 */
	public Map<String, DocumentRoleCache.Role> getClassAndTheirRoles() {
		return classAndRole;
	}
	
	/**
	 * Creates a JSONObject representing this document's information.
	 * 
	 * @return A JSONObject representing this document's information or returns
	 * 		   null if there was an error building the JSONObject.
	 */
	public JSONObject toJsonObject() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_NAME, name);
			result.put(JSON_KEY_DESCRIPTION, ((description ==  null) ? "" : description));
			result.put(JSON_KEY_PRIVACY_STATE, privacyState);
			result.put(JSON_KEY_LAST_MODIFIED, TimeUtils.getIso8601DateTimeString(lastModified));
			result.put(JSON_KEY_CREATION_DATE, TimeUtils.getIso8601DateTimeString(creationDate));
			result.put(JSON_KEY_SIZE, size);
			result.put(JSON_KEY_CREATOR, creator);
			
			result.put(JSON_KEY_USER_ROLE, ((userRole == null) ? "" : userRole));
			result.put(JSON_KEY_CAMPAIGN_ROLE, new JSONObject(campaignAndRole));
			result.put(JSON_KEY_CLASS_ROLE, new JSONObject(classAndRole));
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("Error creating a JSONObject for this object.", e);
			return null;
		}
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
	 * The system should never have to DocumentInformation objects with the 
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
		DocumentInformation other = (DocumentInformation) obj;
		if (documentId == null) {
			if (other.documentId != null)
				return false;
		} else if (!documentId.equals(other.documentId))
			return false;
		return true;
	}
}