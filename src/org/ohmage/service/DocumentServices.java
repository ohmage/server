package org.ohmage.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.DocumentDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to documents.
 * 
 * @author John Jenkins
 */
public class DocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private DocumentServices() {}
	
	/**
	 * Creates a new document in the database.
	 * 
	 * @param request The request that is calling the service.
	 * 
	 * @param contents The contents of the new document.
	 * 
	 * @param name The name of the document.
	 * 
	 * @param description An optional description of the document.
	 * 
	 * @param privacyState The initial privacy state of the document.
	 * 
	 * @param campaignRoleMap A Map of campaign IDs to document roles to which
	 * 						  this document will initially be associated.
	 * 
	 * @param classRoleMap A Map of class IDs to document roles to which this
	 * 					   document will initially be associated.
	 * 
	 * @param creatorUsername The creator of this document's username.
	 * 
	 * @return Returns a unique identifier for this document.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static String createDocument(Request request, byte[] contents, String name, String description, DocumentPrivacyStateCache.PrivacyState privacyState,
			Map<String, DocumentRoleCache.Role> campaignRoleMap, Map<String, DocumentRoleCache.Role> classRoleMap, String creatorUsername) throws ServiceException {
		try {
			return DocumentDaos.createDocument(contents, name, description, privacyState, campaignRoleMap, classRoleMap, creatorUsername);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Ensures that a document exists. 
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param documentId The unique document ID for the document in question.
	 * 
	 * @throws ServiceException Thrown if the document doesn't exist or there 
	 * 							is an error.
	 */
	public static void ensureDocumentExistence(Request request, String documentId) throws ServiceException {
		try {
			if(! DocumentDaos.getDocumentExists(documentId)) {
				request.setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "The document with the given document ID does not exist: " + documentId);
				throw new ServiceException("The document with the given document ID does not exist: " + documentId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the document's name.
	 * 
	 * @param request The request that wants the document's name.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return A String representing the document's name or null if the 
	 * 		   document doesn't exist or the user doesn't have permission to
	 * 		   view it.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static String getDocumentName(Request request, String documentId) throws ServiceException {
		try {
			return DocumentDaos.getDocumentName(documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a role is not less than any of the other roles in a List
	 * of roles. If so, it will fail the request stating that the user is
	 * attempting to grant or revoke more permissions than they have. If the 
	 * role is null or not a valid role, this will still succeed if the list of
	 * roles is empty. If the List is null, it will throw an exception.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param role The document role that cannot be less than any other roles 
	 * 			   in the List.
	 * 
	 * @param roles A List of document roles where no role can be greater than
	 * 				'role'.
	 * 
	 * @throws ServiceException Thrown if the role is less than some role in
	 * 							the List.
	 * 
	 * @throws IllegalArgumentException The List of roles is null.
	 */
	public static void ensureRoleNotLessThanRoles(Request request, DocumentRoleCache.Role role, Collection<DocumentRoleCache.Role> roles) throws ServiceException {
		if(roles == null) {
			throw new IllegalArgumentException("The list of roles is null.");
		}
		
		if(DocumentRoleCache.Role.OWNER.equals(role)) {
			return;
		}
		else if(roles.contains(DocumentRoleCache.Role.OWNER)) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is attempting to grant or revoke document ownership when they are not an owner themselves.");
			throw new ServiceException("The user is attempting to grant or revoke document ownership when they are not an owner themselves.");
		}
		else if(DocumentRoleCache.Role.WRITER.equals(role)) {
			return;
		}
		else if(roles.contains(DocumentRoleCache.Role.WRITER)) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is attempting to grant or revoke the document write ability when they are not a document writer themselves.");
			throw new ServiceException("The user is attempting to grant or revoke the document write ability when they are not a document writer themselves.");
		}
		else if(DocumentRoleCache.Role.READER.equals(role)) {
			return;
		}
		else if(roles.contains(DocumentRoleCache.Role.READER)) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is attempting to grant or revoke the document read ability when they are not a document readers themselves.");
			throw new ServiceException("The user is attempting to grant or revoke the document read ability when they are not a document readers themselves.");
		}
		else if(roles.size() != 0) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is attempting to grant or revoke permissions when they are not associated with the document.");
			throw new ServiceException("The user is attempting to grant or revoke permissions when they are not associated with the document.");
		}
	}
	
	/**
	 * Retrieves an InputStream to the contents of the document. This may be 
	 * the contents of any URL, so it should be closed as soon as it is no 
	 * longer needed.
	 * 
	 * @param request The request that is requesting an InputStream to a 
	 * 				  document.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return An InputStream connected to the document.
	 * 
	 * @throws ServiceException Thrown if there is an error retrieving the 
	 * 							document's URL or if there is an issue opening
	 * 							the connection or InputStream.
	 */
	public static InputStream getDocumentInputStream(Request request, String documentId) throws ServiceException {
		try {
			return (new URL(DocumentDaos.getDocumentUrl(documentId))).openConnection().getInputStream();
			
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
		catch(MalformedURLException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
		catch(IOException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Updates a document's information and/or contents. Except for the 
	 * 'request' and the 'documentId', any parameter can be null indicating
	 * that its value should not be updated.
	 * 
	 * @param request The Request that is performing this update.
	 * 
	 * @param documentId The unique identifier for the document to be updated.
	 * 
	 * @param newContents The new contents of the document.
	 * 
	 * @param newName The new name of the document.
	 * 
	 * @param newDescription The new description for the document.
	 * 
	 * @param newPrivacyState The new privacy state for the document.
	 * 
	 * @param campaignAndRolesToAssociateOrUpdate A Map of campaign IDs to 
	 * 											  document roles that should
	 * 											  either be associated with the
	 * 											  document or, if already 
	 * 											  associated, have their role 
	 * 											  updated.
	 * 
	 * @param campaignsToDisassociate A List of campaigns that should no longer
	 * 								  be associated with the document.
	 * 
	 * @param classAndRolesToAssociateOrUpdate A Map of class IDs to document
	 * 										   roles that should either be
	 * 										   associated with the document or,
	 * 										   if already associated, have 
	 * 										   their role updated.
	 * 
	 * @param classesToDisassociate A List of classes that should no longer be
	 * 								be associated with the document.
	 * 
	 * @param userAndRolesToAssociateOrUpdate A Map of user IDs to document 
	 * 										  roles that should either be 
	 * 										  associated with the document or,
	 * 										  if already associated, have their
	 * 										  role updated.
	 * 
	 * @param usersToDisassoicate A List of users that should no longer be 
	 * 							  associated with the document.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updateDocument(Request request, String documentId, byte[] newContents, String newName, String newDescription, 
			DocumentPrivacyStateCache.PrivacyState newPrivacyState,
			Map<String, DocumentRoleCache.Role> campaignAndRolesToAssociateOrUpdate, List<String> campaignsToDisassociate,
			Map<String, DocumentRoleCache.Role> classAndRolesToAssociateOrUpdate, List<String> classesToDisassociate,
			Map<String, DocumentRoleCache.Role> userAndRolesToAssociateOrUpdate, List<String> usersToDisassoicate) throws ServiceException {
		try {
			DocumentDaos.updateDocument(documentId, newContents, newName, newDescription, newPrivacyState, 
					campaignAndRolesToAssociateOrUpdate, campaignsToDisassociate, 
					classAndRolesToAssociateOrUpdate, classesToDisassociate, 
					userAndRolesToAssociateOrUpdate, usersToDisassoicate);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Deletes a document.
	 * 
	 * @param request The request that is attempting to delete the document.
	 * 
	 * @param documentId The unique ID for the document to be deleted.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteDocument(Request request, String documentId) throws ServiceException {
		try {
			DocumentDaos.deleteDocument(documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
