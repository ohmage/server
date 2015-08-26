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

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 * @author Hongsuda T.
 */
public interface ICampaignDocumentQueries {

	/**
	 * Retrieves a List of String objects where each String represents the 
	 * unique identifier for a campaign to which this document is associated.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return A List of campaign IDs with which this document is associated.
	 */
	List<String> getCampaignsAssociatedWithDocument(String documentId)
			throws DataAccessException;

	/**
	 * Retrieves a List of Campaigns and their roles associated with each document 
	 * in the list. 
	 * 
	 * @param docSqlStmt SQL statement to retrieve a list of documents
	 * 					 visible by the requesting user. 
	 * 
	 * @param docSqlParameters SQL parameters to be used with the above statement. 

	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 *  
	 * @return A map of document id and campaigns as well as their roles associated 
	 * with the document.
	 */
	public Map<String, Collection<Document.UserContainerRole>> getCampaignsAndRolesForDocuments(
	final String docSqlStmt,
	final Collection<Object> docSqlParameters,
	final String username) throws DataAccessException;
	
	/**
	 * Retrieves a camaign's document role if it is associated with a document.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The campaign's role for some document or null if the campaign is
	 * 		   not associated with the document.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	Document.Role getCampaignDocumentRole(String campaignId, String documentId)
			throws DataAccessException;

}
