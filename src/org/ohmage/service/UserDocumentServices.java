package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserDocumentDaos;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.request.Request;

/**
 * This class is responsible for gathering and writing information about 
 * user-document relationships.
 * 
 * @author John Jenkins
 */
public class UserDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserDocumentServices() {}
	
	/**
	 * Retrieves the document information about all of the documents that 
	 * belong directly to the user and that are visible to the user.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user whose documents' information is
	 * 				   desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   in the list pertains to a unique document that is directly 
	 * 		   associated and visible to the user.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<DocumentInformation> getDocumentsSpecificToUser(Request request, String username) throws ServiceException {
		try {
			return UserDocumentDaos.getVisibleDocumentsSpecificToUser(username);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can read a document.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user that is being checked that they
	 * 				   can read this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot read this document or
	 * 							if there is an error.
	 */
	public static void userCanReadDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To read a document, it simply has to be visible to the user in
			// some capacity.
			if(roles.size() == 0) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_READ, "The user does not have sufficient permissions to read the document.");
				throw new ServiceException("The user does not have sufficient permissions to read the document.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a user can modify a document.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user that is being checkec that they
	 * 				   can modify this document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @throws ServiceException Thrown if the user cannot modify this document
	 * 							or if there is an error.
	 */
	public static void userCanModifyDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			// To modify a document, the user must be a writer or owner or a
			// supervisor in any of the campaigns to which the document is 
			// associated or privileged in any of the classes to which the 
			// document is associated.
			if((! roles.contains(DocumentRoleCache.ROLE_OWNER)) && 
			   (! roles.contains(DocumentRoleCache.ROLE_WRITER)) &&
			   (! UserCampaignDocumentServices.getUserIsSupervisorInAnyCampaignAssociatedWithDocument(request, username, documentId)) &&
			   (! UserClassDocumentServices.getUserIsPrivilegedInAnyClassAssociatedWithDocument(request, username, documentId))) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_MODIFY, "The user does not have sufficient permissions to modify the document.");
				throw new ServiceException("The user does not have sufficient permissions to modify the document.");
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Returns the highest role for a user for a document or null if the user 
	 * is not associated with the document. This is across all possible 
	 * relationships, campaign, class, and direct.
	 * 
	 * @param request The request that is performing this service.
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
	public static String getHighestDocumentRoleForUserForDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			List<String> roles = UserDocumentDaos.getDocumentRolesForDocumentForUser(username, documentId);
			
			if(roles.contains(DocumentRoleCache.ROLE_OWNER)) {
				return DocumentRoleCache.ROLE_OWNER;
			}
			else if(roles.contains(DocumentRoleCache.ROLE_WRITER)) {
				return DocumentRoleCache.ROLE_WRITER;
			}
			else if(roles.contains(DocumentRoleCache.ROLE_READER)) {
				return DocumentRoleCache.ROLE_READER;
			}
			else {
				return null;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}