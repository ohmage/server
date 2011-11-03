package org.ohmage.query;

import java.util.List;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;

public interface IClassDocumentQueries {

	/**
	 * Retrieves the list of classes associatd with a document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return A list of class IDs with which this document is associated.
	 */
	List<String> getClassesAssociatedWithDocument(String documentId)
			throws DataAccessException;

	/**
	 * Retrieves a class' document role if it is associated with a document.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The class' role for some document or null if the class is not
	 * 		   associated with the document.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Document.Role getClassDocumentRole(String classId, String documentId)
			throws DataAccessException;

}