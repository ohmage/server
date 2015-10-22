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
	 * Returns the document role map for a document list that is directly associated
	 * with a user. If the user is not directly associated with the document or
	 * it doesn't exist, null is returned.
	 * 
	 * @param docSqlStmt Sql statement to retrieve a list of valid doc ids. 
	 * 
	 * @param docSqlParameters Sql parameters to be used with the above sql.
	 * 					
	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 * 
	 * @return If the document exist and the user is directly associated with 
	 * 		   it, then their document role with said document is returned.
	 * 		   Otherwise, null is returned.
	 */
	public Map<String, Document.Role> getDocumentRoleForDocumentsSpecificToUser(
			final String docSqlStmt, 
			final Collection<Object> docSqlParameters, 
			final String username)
			throws DataAccessException;
	
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
