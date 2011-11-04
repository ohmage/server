package org.ohmage.query;

import java.util.List;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;

/**
 * Interface to facilitate mocking concrete implementations for test cases. 
 * 
 * @author John Jenkins
 * @author Joshua Selsky
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