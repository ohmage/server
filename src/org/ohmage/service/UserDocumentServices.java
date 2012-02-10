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
package org.ohmage.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Document;
import org.ohmage.domain.Document.Role;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignDocumentQueries;
import org.ohmage.query.IClassDocumentQueries;
import org.ohmage.query.IDocumentQueries;
import org.ohmage.query.IUserDocumentQueries;
import org.ohmage.request.Request;

/**
 * This class is responsible for gathering and writing information about 
 * user-document relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserDocumentServices {
	private static UserDocumentServices instance;
	
	private ICampaignDocumentQueries campaignDocumentQueries;
	private IClassDocumentQueries classDocumentQueries;
	private IDocumentQueries documentQueries;
	private IUserDocumentQueries userDocumentQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignDocumentQueries or 
	 * iClassDocumentQueries or iDocumentQueries or iUserDocumentQueries is 
	 * null
	 */
	private UserDocumentServices(ICampaignDocumentQueries iCampaignDocumentQueries,
			IDocumentQueries iDocumentQueries, IClassDocumentQueries iClassDocumentQueries, 
			IUserDocumentQueries iUserDocumentQueries) {
				
		if(iCampaignDocumentQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignDocumentQueries is required.");
		}
		if(iDocumentQueries == null) {
			throw new IllegalArgumentException("An instance of IDocumentQueries is required.");
		}
		if(iClassDocumentQueries == null) {
			throw new IllegalArgumentException("An instance of IClassDocumentQueries is required.");
		}
		if(iUserDocumentQueries == null) {
			throw new IllegalArgumentException("An instance of IUserDocumentQueries is required.");
		}

		campaignDocumentQueries = iCampaignDocumentQueries;
		documentQueries = iDocumentQueries;
		classDocumentQueries = iClassDocumentQueries;
		userDocumentQueries = iUserDocumentQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserDocumentServices instance() {
		return instance;
	}
	
	/**
	 * Retrieves the ID for all documents directly associated with the user.
	 * 
	 * @param username The username of the user.
	 * 
	 * @return A list of document IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public List<String> getDocumentsSpecificToUser(final String username) throws ServiceException {
		try {
			return userDocumentQueries.getVisibleDocumentsSpecificToUser(username);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can read a document.
	 * 
	 * @param username The username of the user that is being checked that they
	 * 				   can read this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot read this document or
	 * 							if there is an error.
	 */
	public void userCanReadDocument(final String username, 
			final String documentId) throws ServiceException {
		
		try {
			List<Document.Role> roles = userDocumentQueries.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To read a document, it simply has to be visible to the user in
			// some capacity.
			if(roles.size() == 0) {
				throw new ServiceException(
						ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
						"The user does not have sufficient permissions to read the document.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can modify a document.
	 * 
	 * @param username The username of the user that is being checkec that they
	 * 				   can modify this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot modify this document
	 * 							or if there is an error.
	 */
	public void userCanModifyDocument(final String username, 
			final String documentId) throws ServiceException {
		try {
			List<Document.Role> roles = userDocumentQueries.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To modify a document, the user must be a writer or owner or a
			// supervisor in any of the campaigns to which the document is 
			// associated or privileged in any of the classes to which the 
			// document is associated.
			if((! roles.contains(Document.Role.OWNER)) && 
			   (! roles.contains(Document.Role.WRITER)) &&
			   (! UserCampaignDocumentServices.instance().getUserIsSupervisorInAnyCampaignAssociatedWithDocument(username, documentId)) &&
			   (! UserClassDocumentServices.instance().getUserIsPrivilegedInAnyClassAssociatedWithDocument(username, documentId))) {
				throw new ServiceException(
						ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
						"The user does not have sufficient permissions to modify the document.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can delete a document.
	 * 
	 * @param username The username of the user that is being checked that they
	 * 				   can delete this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot modify this document
	 * 							or if there is an error.
	 */
	public void userCanDeleteDocument(final String username, 
			final String documentId) throws ServiceException {
		
		try {
			List<Document.Role> roles = userDocumentQueries.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To modify a document, the user must be a writer or owner or a
			// supervisor in any of the campaigns to which the document is 
			// associated or privileged in any of the classes to which the 
			// document is associated.
			if((! roles.contains(Document.Role.OWNER)) &&
			   (! UserCampaignDocumentServices.instance().getUserIsSupervisorInAnyCampaignAssociatedWithDocument(username, documentId)) &&
			   (! UserClassDocumentServices.instance().getUserIsPrivilegedInAnyClassAssociatedWithDocument(username, documentId))) {
				throw new ServiceException(
						ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
						"The user does not have sufficient permissions to delete the document.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and a user based on the user's role with the document.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the class and document. 
	 * 
	 * @param username The other user's username.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate the user and document.
	 */
	public void ensureRoleHighEnoughToDisassociateOtherUserFromDocument(
			final Document.Role role, final String username, 
			final String documentId) throws ServiceException {
		
		Document.Role otherUserRole = getHighestDocumentRoleForUserForDocument(username, documentId);
		
		if(role.compare(otherUserRole) < 0) {
			throw new ServiceException(
					ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
					"Insufficient permissions to disassociate the document '" + 
						documentId +
						"' with the user '" + 
						username + 
						"' as the user has a higher role.");
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and each of the users in a collection based on the users' 
	 * individual roles with the document.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the classes and document. 
	 * 
	 * @param usernames The other users' usernames.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate a user and document.
	 */
	public void ensureRoleHighEnoughToDisassociateDocumentFromOtherUsers(
			final Document.Role role, final Collection<String> usernames, 
			final String documentId) throws ServiceException {
		
		for(String username : usernames) {
			ensureRoleHighEnoughToDisassociateOtherUserFromDocument(role, username, documentId);
		}
	}
	
	/**
	 * Returns the highest role for a user for a document or null if the user 
	 * is not associated with the document. This is across all possible 
	 * relationships, campaign, class, and direct.
	 * 
	 * @param username The username of the user whose highest role with the 
	 * 				   document is desired.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return Returns the users highest role for the document across all 
	 * 		   campaigns, classes, and direct associations. If the user is not
	 * 		   associated with the document in any capacity or the document
	 * 		   doesn't exist, null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Document.Role getHighestDocumentRoleForUserForDocument(
			final String username, final String documentId) 
			throws ServiceException {
		
		try {
			List<Document.Role> roles = userDocumentQueries.getDocumentRolesForDocumentForUser(username, documentId);
			
			if(roles.contains(Document.Role.OWNER)) {
				return Document.Role.OWNER;
			}
			else if(roles.contains(Document.Role.WRITER)) {
				return Document.Role.WRITER;
			}
			else if(roles.contains(Document.Role.READER)) {
				return Document.Role.READER;
			}
			else {
				return null;
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the information about a document and also populates the role
	 * of a specific user, all of the campaigns, and all of the classes.
	 * 
	 * @param username The user's username.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return A DocumentInformation object that contains all of the 
	 * 		   information about a single doucment as well as a user's specific
	 * 		   role, all of the campaigns associated with the document and 
	 * 		   their role, and all of the classes associated with the document
	 * 		   and their role.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public Document getDocumentInformationForDocumentWithUser(
			final String username, final String documentId) 
			throws ServiceException {
		
		try {
			// Get the document's basic information.
			Document result = documentQueries.getDocumentInformation(documentId);
			
			// Get the user's specific role.
			Document.Role userRole = userDocumentQueries.getDocumentRoleForDocumentSpecificToUser(username, documentId);
			if(userRole != null) {
				result.setUserRole(userRole);
			}
			
			// For all of the campaigns associated with the document, get their
			// role.
			for(String campaignId : campaignDocumentQueries.getCampaignsAssociatedWithDocument(documentId)) {
				Document.Role campaignRole = campaignDocumentQueries.getCampaignDocumentRole(campaignId, documentId);
				if(campaignRole != null) {
					result.addCampaignRole(campaignId, campaignRole);
				}
			}
			
			// For all of the classes associated with the document, get their
			// role.
			for(String classId : classDocumentQueries.getClassesAssociatedWithDocument(documentId)) {
				Document.Role classRole = classDocumentQueries.getClassDocumentRole(classId, documentId);
				if(classRole != null) {
					result.addClassRole(classId, classRole);
				}
			}
			
			// If they are a supervisor in any campaign associated with this
			// document or privileged in any class associated with this 
			// document,
			if(UserCampaignDocumentServices.instance().getUserIsSupervisorInAnyCampaignAssociatedWithDocument(username, documentId) ||
			   UserClassDocumentServices.instance().getUserIsPrivilegedInAnyClassAssociatedWithDocument(username, documentId)) {
				
				// And if the current maximum role is a reader or none,
				if(Role.WRITER.compare(result.getMaxRole()) == -1) {
					// Automatically increase their privileges to writer.
					result.setMaxRole(Role.WRITER);
				}
			}
			
			return result;
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		} 
		catch(DomainException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Creates a list of DocumentInformation objects, one for each document,
	 * populating each with the user's document role, all of the campaigns
	 * associated with the document and their role, and all of the classes 
	 * associated with the document and their role.
	 * 
	 * @param username The user's username.
	 * 
	 * @param documentIds The document's unique identifier.
	 * 
	 * @return A list of DocumentInformation objects each representing one of
	 * 		   the document IDs.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 * 
	 * @see #getDocumentInformationForDocumentWithUser(Request, String, String)
	 */
	public List<Document> getDocumentInformationForDocumentsWithUser(
			final String username, final Collection<String> documentIds) 
			throws ServiceException {
		
		List<Document> result = new LinkedList<Document>();
		for(String documentId : documentIds) {
			result.add(getDocumentInformationForDocumentWithUser(username, documentId));
		}
		return result;
	}
}
