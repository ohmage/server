package org.ohmage.query;

import java.util.List;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;

public interface IUserDocumentQueries {

	/**
	 * Retrieves the unique identifiers for all of the documents directly
	 * associated with a user.
	 * 
	 * @param username The username of the user whose documents are desired.
	 * 
	 * @return A list of document IDs.
	 */
	List<String> getVisibleDocumentsSpecificToUser(String username)
			throws DataAccessException;

	/**
	 * Returns the document role for a document that is directly associated
	 * with a user. If the user is not directly associated with the document or
	 * it doesn't exist, null is returned.
	 * 
	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 * 
	 * @param documentId The unique identifier for the document whose role is
	 * 					 desired.
	 * 
	 * @return If the document exist and the user is directly associated with 
	 * 		   it, then their document role with said document is returned.
	 * 		   Otherwise, null is returned.
	 */
	Document.Role getDocumentRoleForDocumentSpecificToUser(String username,
			String documentId) throws DataAccessException;

	/**
	 * Retrieves all of the document roles for a user across their personal
	 * documents as well as documents with which they are associated in 
	 * campaigns and classes.  
	 * 
	 * @param username The username of the user whose document roles is 
	 * 				   desired.
	 * 
	 * @param documentId The unique document identifier of the document.
	 * 
	 * @return Returns a, possibly empty, List of document roles for the user
	 * 		   specific to the document.
	 */
	List<Document.Role> getDocumentRolesForDocumentForUser(String username,
			String documentId) throws DataAccessException;

}