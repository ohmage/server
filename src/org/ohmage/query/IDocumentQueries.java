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

public interface IDocumentQueries {

	/**
	 * Creates a new document entry in the database. It saves the file to disk
	 * and the database entry contains a reference to that file.
	 * 
	 * @param contents The contents of the file.
	 * 
	 * @param name The name of the file.
	 * 
	 * @param description A description for the file.
	 * 
	 * @param privacyState The initial privacy state of the file.
	 * 
	 * @param campaignRoleMap A Map of campaign IDs to document roles for which
	 * 						  this document will have an initial association.
	 * 
	 * @param classRoleMap A Map of class IDs to document roles for which this
	 * 					   document will have an initial association.
	 * 
	 * @param creatorUsername The username of the creator of this document.
	 * 
	 * @return Returns a unique identifier for this document.
	 */
	String createDocument(byte[] contents, String name, String description,
			Document.PrivacyState privacyState,
			Map<String, Document.Role> campaignRoleMap,
			Map<String, Document.Role> classRoleMap, String creatorUsername)
			throws DataAccessException;

	/**
	 * Returns whether or not a document with the unique document identifier
	 * 'documentId' exists.
	 * 
	 * @param documentId The unique document identifier of a document.
	 * 
	 * @return Returns true if the document exists and false otherwise.
	 */
	boolean getDocumentExists(String documentId) throws DataAccessException;

	/**
	 * Returns the URL of the document.
	 * 
	 * @param documentId The unique document identifier of the document in 
	 * 					 question.
	 * 
	 * @return Returns the URL of the document.
	 */
	String getDocumentUrl(String documentId) throws DataAccessException;

	/**
	 * Returns the name of the document.
	 * 
	 * @param documentId The unique document identifier of the document in 
	 * 					 question.
	 * 
	 * @return Returns the name of the document.
	 */
	String getDocumentName(String documentId) throws DataAccessException;

	/**
	 * Retrieves the information about the documents that match any of the 
	 * criteria. If all of the criteria are null, it will return all documents
	 * visible to the requesting user.
	 * 
	 * @param username This is the username of the requesting user and is 
	 * 				   required.
	 * 
	 * @param personalDocuments If true will include the documents directly 
	 * 							associated with this user; if false, it will 
	 * 							not return the documents directly associated 
	 * 							with the user unless they also happen to be 
	 * 							associated with any class or campaign to which
	 * 							the user belongs. If null, it will be treated 
	 * 							as false.
	 * 
	 * @param campaignIds A collection of campaign unique identifiers that will
	 * 					  increase the results to include all documents in all
	 * 					  of these campaigns.
	 * 
	 * @param classIds A collection of class unqiue identifiers that will
	 * 				   increase the results to include all documents in all of
	 * 				   these classes.
	 * 
	 * @param nameTokens A collection of tokens that limits the list to only
	 * 					 those documents whose name contains any of the tokens.
	 * 
	 * @param descriptionTokens A collection of tokens that limits the list to
	 * 							only those documents that have a description
	 * 							and where that description contains any of 
	 * 							these tokens.
	 *  
	 * @return A DocumentInformation object representing the information about
	 * 		   this document.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	List<Document> getDocumentInformation(
			final String username,
			final Boolean personalDocuments,
			final Collection<String> campaignIds,
			final Collection<String> classIds,
			final Collection<String> nameTokens,
			final Collection<String> descriptionTokens) 
			throws DataAccessException;

	/**
	 * Updates a document. The 'documentId' cannot be null as this is used to
	 * indicate which document is being updated, but the remaining parameters
	 * may all be null.
	 * 
	 * @param documentId The unique identifier for the document to be updated.
	 * 
	 * @param contents The new contents of the document.
	 * 
	 * @param name The new name of the document.
	 * 
	 * @param description The new description for the document.
	 * 
	 * @param privacyState The new privacy state for the document.
	 * 
	 * @param campaignAndRolesToAdd A Map of campaign IDs to document roles 
	 * 								where the document should be associated to
	 * 								the campaign with the given role or, if
	 * 								already associated, should have its role
	 * 								updated with the new role.
	 * 
	 * @param campaignsToRemove A List of campaign IDs that should no longer be
	 * 							associated with this document.
	 * 
	 * @param classAndRolesToAdd A Map of class IDs to document roles where the
	 * 							 document should be associated to the class 
	 * 							 with the given role or, if already associated,
	 * 							 should have its role updated with the new 
	 * 							 role.
	 * 
	 * @param classesToRemove A List of class IDs that should no longer be
	 * 						  associated with this document.
	 * 
	 * @param userAndRolesToAdd A Map of usernames to document roles where the
	 * 							document should be associated to the user with
	 * 							the given role or, if already associated, 
	 * 							should have its role updated with the new role.
	 * 
	 * @param usersToRemove A List of usernames that should no longer be 
	 * 						associated with this document.
	 */
	void updateDocument(String documentId, byte[] contents, String name,
			String description, Document.PrivacyState privacyState,
			Map<String, Document.Role> campaignAndRolesToAdd,
			List<String> campaignsToRemove,
			Map<String, Document.Role> classAndRolesToAdd,
			Collection<String> classesToRemove,
			Map<String, Document.Role> userAndRolesToAdd,
			Collection<String> usersToRemove) throws DataAccessException;

	/**
	 * Deletes a document.
	 * 
	 * @param documentId The unique identifier for the document to be deleted.
	 */
	void deleteDocument(String documentId) throws DataAccessException;

}
