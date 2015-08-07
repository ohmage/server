/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
	 * Retrieves the list of classes, document role, and user's class role 
	 * associated with a set of document for a given user.
	 * 
	 * @param docSqlStmt SQL statement to retrieve a list of documents
	 * 					 visible by the requesting user. 
	 * 
	 * @param docSqlParameters SQL parameters to be used with the above statement. 
	 * 
	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 * 
	 * @return A map of document id and classes and their roles associated with the document.
	 */
	public Map<String, Collection<Document.UserContainerRole>> getClassesAndRolesForDocuments(
			final String docSqlStmt,
			final Collection<Object> docSqlParameters,
			final String username) throws DataAccessException;

	
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
