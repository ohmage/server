package org.ohmage.query;

import java.util.List;

import org.ohmage.exception.DataAccessException;

public interface IUserCampaignDocumentQueries {

	/**
	 * Retrieves the list of document IDs for all of the documents associated
	 * with a campaign.
	 * 
	 * @param username The username of the requesting user.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return A list of document IDs.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	List<String> getVisibleDocumentsToUserInCampaign(String username,
			String campaignId) throws DataAccessException;

	/**
	 * Checks if the user is a supervisor in any class to which the document is
	 * associated.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param documentId The unique document identifier for the document.
	 * 
	 * @return Returns true if the user is a supervisor in any of the classes
	 * 		   to which the document is associated.
	 */
	Boolean getUserIsSupervisorInAnyCampaignAssociatedWithDocument(
			String username, String documentId) throws DataAccessException;

}