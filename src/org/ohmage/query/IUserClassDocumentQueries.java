package org.ohmage.query;

import java.util.List;

import org.ohmage.exception.DataAccessException;

public interface IUserClassDocumentQueries {

	/**
	 * Gathers the unique identifiers for all of the documents associated with
	 * a class.
	 * 
	 * @param username The username of the requesting user.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @return A list of the documents associated with a class. The list may be
	 * 		   empty but never null.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getVisibleDocumentsToUserInClass(String username,
			String classId) throws DataAccessException;

	/**
	 * Retrieves whether or not the user is privileged any of the classes 
	 * associated with the document.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique identifier of the document.
	 * 
	 * @return Returns true if the user is privileged in any class that is 
	 * 		   associated with the campaign.
	 */
	Boolean getUserIsPrivilegedInAnyClassAssociatedWithDocument(
			String username, String documentId) throws DataAccessException;

}