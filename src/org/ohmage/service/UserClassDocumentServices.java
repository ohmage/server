package org.ohmage.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.dao.UserClassDaos;
import org.ohmage.dao.UserClassDocumentDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;


public class UserClassDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserClassDocumentServices() {}
	
	/**
	 * Verifies that the user can associate documents with a class. Currently,
	 * the only restriction is that the user must belong to the class in some
	 * capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param classId The class ID of the class in question.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user 
	 * 							does not belong to the class in any capacity.
	 */
	public static void userCanAssociateDocumentsWithClass(Request request, String username, String classId) throws ServiceException {
		try {
			ClassRoleCache.Role classRole = UserClassDaos.getUserClassRole(classId, username);
			
			if(classRole == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is not a member of the following class and, therefore, cannot associate documents with it: " + classId);
				throw new ServiceException("The user is not a member of the following class and, therefore, cannot associate documents with it: " + classId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user can disassociate documents from a class. 
	 * Currently, the only restriction is that the user must belong to the 
	 * class in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param classId The class ID of the class in question.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user 
	 * 							does not belong to the class in any capacity.
	 */
	public static void userCanDisassociateDocumentsWithClass(Request request, String username, String classId) throws ServiceException {
		try {
			ClassRoleCache.Role classRole = UserClassDaos.getUserClassRole(classId, username);
			
			if(classRole == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "The user is not a member of the following class and, therefore, cannot disassociate documents from it: " + classId);
				throw new ServiceException("The user is not a member of the following class and, therefore, cannot disassociate documents from it: " + classId);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user can associate documents with all of the classes 
	 * in the list. Currently, the only restriction is that the user must 
	 * belong to each of the classes in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param classIds A List of class IDs where the user must belong to all of
	 * 				   the classes in some capacity.
	 * 				 
	 * @throws ServiceException Thrown if there is an error or if the user 
	 * 							doesn't belong to one or more of the classes.
	 */
	public static void userCanAssociateDocumentsWithClasses(Request request, String username, Collection<String> classIds) throws ServiceException {
		for(String classId : classIds) {
			userCanAssociateDocumentsWithClass(request, username, classId);
		}
	}
	
	/**
	 * Verifies that the user can disassociate documents with all of the  
	 * classes in the list. Currently, the only restriction is that the user  
	 * must belong to each of the classes in some capacity.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param classIds A List of class IDs where the user must belong to all of
	 * 				   the classes in some capacity.
	 * 				 
	 * @throws ServiceException Thrown if there is an error or if the user 
	 * 							doesn't belong to one or more of the classes.
	 */
	public static void userCanDisassociateDocumentsWithClasses(Request request, String username, Collection<String> classIds) throws ServiceException {
		for(String classId : classIds) {
			userCanDisassociateDocumentsWithClass(request, username, classId);
		}
	}
	
	/**
	 * Retrieves a list of all of the documents associated with a class.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @return A list of all of unique identifiers for all of the documents 
	 * 		   associated with the class. The list may be empty but will never
	 * 		   be null.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getVisibleDocumentsSpecificToClass(Request request, String username, String classId) throws ServiceException {
		try {
			return UserClassDocumentDaos.getVisibleDocumentsToUserInClass(username, classId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a list of all of the documents associated with all of the 
	 * classes in a collection.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param classIds A list of unique identifiers of classes.
	 * 
	 * @return A list of the unique identifiers for all of the documents 
	 * 		   associated with the class. This list may be empty but will never
	 * 		   be null and will contain only unique entries.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<String> getVisibleDocumentsSpecificToClasses(Request request, String username, Collection<String> classIds) throws ServiceException {
		Set<String> resultSet = new HashSet<String>();
		for(String classId : classIds) {
			resultSet.addAll(getVisibleDocumentsSpecificToClass(request, username, classId));
		}
		return new ArrayList<String>(resultSet);
	}
	
	/**
	 * Retrieves whether or not the user is privileged in any class with which
	 * the document is associated.
	 * 
	 * @param request The Request performing this service.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return Whether or not he user is privileged in any class with which the
	 * 		   document is associated.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static boolean getUserIsPrivilegedInAnyClassAssociatedWithDocument(Request request, String username, String documentId) throws ServiceException {
		try {
			return UserClassDocumentDaos.getUserIsPrivilegedInAnyClassAssociatedWithDocument(username, documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
