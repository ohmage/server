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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.util.StringUtils;

/**
 * A class to represent documents in the database. 
 * 
 * @author John Jenkins
 */
public class DocumentInformation {	
	private static Logger LOGGER = Logger.getLogger(DocumentInformation.class);
	
	private final String documentId;
	private final String name;
	private final String description;
	private final String privacyState;
	private final Date lastModified;
	private final Date creationDate;
	private final int size;
	private final String creator;
	
	private String userRole;
	private final Map<String, String> campaignAndRole;
	private final Map<String, String> classAndRole;
	
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
	public DocumentInformation(String documentId, String name, String description, String privacyState, 
			Date lastModified, Date creationDate, int size, String creator) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document's ID cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The document's name cannot be null or whitespace only.");
		}
		else if(! DocumentPrivacyStateCache.instance().getKeys().contains(privacyState)) {
			throw new IllegalArgumentException("The document's privacy state cannot be null or whitespace only.");
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

		campaignAndRole = new HashMap<String, String>();
		classAndRole = new HashMap<String, String>();
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
	public String getPrivacyState() {
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
	 * @throws IllegalArgumentException Thrown if the role is null or 
	 * 									whitespace only.
	 */
	public void addUserRole(String documentRole) {
		if(! DocumentRoleCache.instance().getKeys().contains(documentRole)) {
			throw new IllegalArgumentException("The role cannot be null or whitespace only.");
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
	public String addCampaignRole(String campaignId, String documentRole) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw new IllegalArgumentException("The campaign ID cannot be null or whitespace only.");
		}
		else if(! DocumentRoleCache.instance().getKeys().contains(documentRole)) {
			throw new IllegalArgumentException("The role cannot be null or whitespace only.");
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
	public Map<String, String> getCampaignsAndTheirRoles() {
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
	public String addClassRole(String classId, String documentRole) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(classId)) {
			throw new IllegalArgumentException("The class ID cannot be null or whitespace only.");
		}
		else if(! DocumentRoleCache.instance().getKeys().contains(documentRole)) {
			throw new IllegalArgumentException("The role cannot be null or whitespace only.");
		}
		
		return classAndRole.put(classId, documentRole);
	}
	
	/**
	 * Returns a map of the classes and their associated document roles. 
	 * 
	 * @return A map of the classes and their associated document roles.
	 */
	public Map<String, String> getClassAndTheirRoles() {
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
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			result.put("name", name);
			result.put("description", ((description ==  null) ? "" : description));
			result.put("privacy_state", privacyState);
			result.put("last_modified", formatter.format(lastModified));
			result.put("creation_date", formatter.format(lastModified));
			result.put("size", size);
			result.put("creator", creator);
			
			result.put("user_role", ((userRole == null) ? "" : userRole));
			result.put("campaign_roles", new JSONObject(campaignAndRole));
			result.put("class_role", new JSONObject(classAndRole));
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("Error creating a JSONObject for this object.", e);
			return null;
		}
	}
	
	/**
	 * Generates a hash code for this object.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((campaignAndRole == null) ? 0 : campaignAndRole.hashCode());
		result = prime * result
				+ ((classAndRole == null) ? 0 : classAndRole.hashCode());
		result = prime * result
				+ ((creationDate == null) ? 0 : creationDate.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((documentId == null) ? 0 : documentId.hashCode());
		result = prime * result
				+ ((lastModified == null) ? 0 : lastModified.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((privacyState == null) ? 0 : privacyState.hashCode());
		result = prime * result + size;
		result = prime * result
				+ ((userRole == null) ? 0 : userRole.hashCode());
		return result;
	}

	/**
	 * Compares this object to another object and returns whether or not they
	 * are equal. 
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
		if (campaignAndRole == null) {
			if (other.campaignAndRole != null)
				return false;
		} else if (!campaignAndRole.equals(other.campaignAndRole))
			return false;
		if (classAndRole == null) {
			if (other.classAndRole != null)
				return false;
		} else if (!classAndRole.equals(other.classAndRole))
			return false;
		if (creationDate == null) {
			if (other.creationDate != null)
				return false;
		} else if (!creationDate.equals(other.creationDate))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (documentId == null) {
			if (other.documentId != null)
				return false;
		} else if (!documentId.equals(other.documentId))
			return false;
		if (lastModified == null) {
			if (other.lastModified != null)
				return false;
		} else if (!lastModified.equals(other.lastModified))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (privacyState == null) {
			if (other.privacyState != null)
				return false;
		} else if (!privacyState.equals(other.privacyState))
			return false;
		if (size != other.size)
			return false;
		if (userRole == null) {
			if (other.userRole != null)
				return false;
		} else if (!userRole.equals(other.userRole))
			return false;
		return true;
	}
}