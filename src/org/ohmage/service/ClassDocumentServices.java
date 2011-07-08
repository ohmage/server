package org.ohmage.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.UserClassDocumentDaos;
import org.ohmage.domain.DocumentInformation;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to class-document 
 * associations.
 * 
 * @author John Jenkins
 */
public class ClassDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ClassDocumentServices() {}
	
	/**
	 * Retrieves a List of DocumentInformation for each of the visible 
	 * documents associated with a class. Visibility is based on the user's
	 * role in the class and the documents' privacy state.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param classId The class ID for the campaign whose documents are
	 * 				  desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   represents a document associated with this class.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<DocumentInformation> getDocumentsSpecificToClass(Request request, String username, String classId) throws ServiceException {
		try {
			return UserClassDocumentDaos.getVisibleDocumentsToUserInClass(username, classId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves a List of DocumentInformation objects for all of the visible
	 * documents associated with all of the class. Visibility is based on the
	 * user's role in the classes and the documents' privacy state. 
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param username The username of the requester.
	 * 
	 * @param classIds A List of campaign IDs for the classes whose documents
	 * 				   are desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   represents a document associated with any of the classes in the
	 * 		   list. This will not contain duplicates.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static List<DocumentInformation> getDocumentsSpecificToClasses(Request request, String username, List<String> classIds) throws ServiceException {
		Set<DocumentInformation> resultSet = new HashSet<DocumentInformation>();
		for(String classId : classIds) {
			resultSet.addAll(getDocumentsSpecificToClass(request, username, classId));
		}
		return new ArrayList<DocumentInformation>(resultSet);
	}
}
