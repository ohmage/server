package org.ohmage.service;

import java.util.List;

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
}