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
