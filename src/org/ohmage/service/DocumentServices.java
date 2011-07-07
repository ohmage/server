package org.ohmage.service;

import java.util.Map;

import org.ohmage.dao.DataAccessException;
import org.ohmage.dao.DocumentDaos;
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
	public static String createDocument(Request request, byte[] contents, String name, String description, String privacyState,
			Map<String, String> campaignRoleMap, Map<String, String> classRoleMap, String creatorUsername) throws ServiceException {
		try {
			return DocumentDaos.createDocument(contents, name, description, privacyState, campaignRoleMap, classRoleMap, creatorUsername);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
