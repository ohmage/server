package org.ohmage.service;

import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserClassDaos;
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
			String classRole = UserClassDaos.userClassRole(classId, username);
			
			if(classRole == null) {
				request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_ASSOCIATE_CLASS, "The user is not a member of the following class and, therefore, cannot associate documents with it: " + classId);
				throw new ServiceException("The user is not a member of the following class and, therefore, cannot associate documents with it: " + classId);
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
	 * belong to the class in some capacity.
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
	public static void userCanAssociateDocumentsWithClasses(Request request, String username, List<String> classIds) throws ServiceException {
		for(String classId : classIds) {
			userCanAssociateDocumentsWithClass(request, username, classId);
		}
	}
}
