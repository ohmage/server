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
import java.util.Set;

import org.joda.time.DateTime;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.impl.QueryResultsList;

public interface ICampaignQueries {
	/**
	 * Creates a new campaign.
	 * 
	 * @param campaign
	 * 			  The campaign to save.
	 * 
	 * @param classIds
	 *            A List of classes with which this campaign should be
	 *            associated.
	 * 
	 * @param creatorUsername
	 *            The username of the creator of this campaign.
	 */
	void createCampaign(
		final Campaign campaign,
		final Collection<String> classIds,
		final String creatorUsername) 
		throws DataAccessException;

	/**
	 * Returns whether or not a campaign with the unique campaign identifier
	 * 'campaignId' exists.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign whose existence is in
	 *            question.
	 * 
	 * @return Returns true if the campaign exists; false, otherwise.
	 */
	Boolean getCampaignExists(String campaignId) throws DataAccessException;

	/**
	 * Retrieves a campaign's name.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its name is returned. Otherwise, null is
	 *         returned.
	 */
	String getName(String campaignId) throws DataAccessException;

	/**
	 * Finds the configuration for the provided campaign id.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign..
	 * @throws DataAccessException
	 *             If an error occurs running the SQL.
	 */
	Campaign findCampaignConfiguration(String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves a campaign's description.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its description is returned. Otherwise,
	 *         null is returned.
	 */
	String getDescription(String campaignId) throws DataAccessException;

	/**
	 * Retrieves the campaign's privacy state.
	 * 
	 * @param campaignId
	 *            A campaign's unique identifier.
	 * 
	 * @return If the campaign exists, its PrivacyState enum is returned;
	 *         otherwise, null is returned.
	 */
	Campaign.PrivacyState getCampaignPrivacyState(String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves the campaign's running state.
	 * 
	 * @param campaignId
	 *            A campaign's unique identifier.
	 * 
	 * @return If the campaign exists, its running state String is returned;
	 *         otherwise, null is returned.
	 */
	Campaign.RunningState getCampaignRunningState(String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves a campaign's XML.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its XML is returned. Otherwise, null is
	 *         returned.
	 */
	String getXml(String campaignId) throws DataAccessException;

	/**
	 * Retrieves a campaign's icon's URL.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its icon URL is returned. Otherwise, null
	 *         is returned.
	 */
	String getIconUrl(String campaignId) throws DataAccessException;

	/**
	 * Retrieves a campaign's creation timestamp.
	 * 
	 * @param campaignId
	 *            The unique identifier for the campaign.
	 * 
	 * @return If the campaign exists, its timestamp is returned; otherwise,
	 *         null is returned.
	 */
	DateTime getCreationTimestamp(String campaignId)
			throws DataAccessException;

	/**
	 * Creates a new CampaignInformation object based on the information about
	 * some campaign.
	 * 
	 * @param campaignId
	 *            The campaign's unique identifier.
	 * 
	 * @return A CampaignInformation object with the required information about
	 *         a campaign or null if no such campaign exists.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	Campaign getCampaignInformation(final String campaignId)
			throws DataAccessException;

	/**
	 * Retrieves the unique identifier for all of the campaigns in the system.
	 * 
	 * @return A list of unique campaign identifiers for all of the campaigns in
	 *         the system.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getAllCampaignIds() throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose ID contains the partial ID.
	 * 
	 * @param partialCampaignId
	 *            The partial campaign ID.
	 * 
	 * @return The campaign IDs.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getCampaignsFromPartialId(final String partialCampaignId)
			throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose name contains the partial name.
	 * 
	 * @param partialCampaignName
	 *            The partial campaign Name.
	 * 
	 * @return The campaign IDs.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getCampaignsFromPartialName(final String partialCampaignName)
			throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose description contains the
	 * partial description.
	 * 
	 * @param partialCampaignDescription
	 *            The partial campaign description.
	 * 
	 * @return The campaign IDs.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getCampaignsFromPartialDescription(
			final String partialDescription) throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose XML contains the partial XML.
	 * 
	 * @param partialCampaignXml
	 *            The partial campaign XML.
	 * 
	 * @return The campaign IDs.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getCampaignsFromPartialXml(final String partialXml)
			throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose authored by value contains the
	 * partial authored by value.
	 * 
	 * @param partialCampaignId
	 *            The partial authored by value.
	 * 
	 * @return The campaign IDs.
	 * 
	 * @throws DataAccessException
	 *             Thrown if there is an error.
	 */
	List<String> getCampaignsFromPartialAuthoredBy(
			final String partialAuthoredBy) throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose creation timestamp was on or
	 * after some date.
	 * 
	 * @param date
	 *            The date as a Calendar.
	 * 
	 * @return A List of campaign IDs. This will never be null.
	 */
	List<String> getCampaignsOnOrAfterDate(DateTime date)
			throws DataAccessException;

	/**
	 * Retrieves the IDs for all campaigns whose creation timestamp was on or
	 * before some date.
	 * 
	 * @param date
	 *            The date as a Calendar.
	 * 
	 * @return A List of campaign IDs. This will never be null.
	 */
	List<String> getCampaignsOnOrBeforeDate(DateTime date)
			throws DataAccessException;

	/**
	 * Returns a list of campaign IDs for all of the campaigns with a specified
	 * privacy state.
	 * 
	 * @param privacyState
	 *            The privacy state in question.
	 * 
	 * @return Returns a list of campaign IDs whose is privacy state is
	 *         'privacyState'.
	 */
	List<String> getCampaignsWithPrivacyState(Campaign.PrivacyState privacyState)
			throws DataAccessException;

	/**
	 * Returns a list of campaign IDs for all of the campaigns with a specified
	 * running state.
	 * 
	 * @param runningState
	 *            The running state in question.
	 * 
	 * @return Returns a list of campaign IDs whose is running state is
	 *         'runningState'.
	 */
	List<String> getCampaignsWithRunningState(Campaign.RunningState runningState)
			throws DataAccessException;

	/**
	 * Returns the query results for all campaigns visible to the user that 
	 * match the given criteria. The username is required for ACL purposes but
	 * the rest of the information is optional.
	 * 
	 * @param username The requesting user's username.
	 * 
	 * @param campaignIds Limits the results to only campaigns whose ID is in
	 * 					  this collection.
	 * 
	 * @param classIds Limits the results to only those campaigns that are 
	 * 				   associated with a class whose ID is in this list.
	 * 
	 * @param nameTokens A collection of token strings which limit the results
	 * 					 to only those campaigns whose name contains at least 
	 * 					 one of the tokens.
	 * 
	 * @param descriptionTokens A collection of token strings which limit the
	 * 							results to only those campaigns that have a 
	 * 							description and whose description contains at
	 * 							least one of the tokens.
	 * 
	 * @param startDate Limits the results to only those campaigns that were 
	 * 					created on or after this date.
	 * 
	 * @param endDate Limits the results to only those campaigns that were
	 * 				  created on or before this date.
	 *  
	 * @param privacyState Limits the results to only those campaigns that have
	 * 					   this privacy state.
	 * 
	 * @param runningState Limits the results to only those campaigns that have
	 * 					   this running state.
	 * 
	 * @param role Limits the results to only those campaigns where the 
	 * 			   requesting user has at least this role in the campaign.
	 * 
	 * @return The query results which contain the total number of campaigns 
	 * 		   that matched this criteria as well as the list of campaigns in
	 * 		   the current page.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public QueryResultsList<Campaign> getCampaignInformation(
			final String username,
			final Collection<String> campaignIds,
			final Collection<String> classIds,
			final Collection<String> nameTokens,
			final Collection<String> descriptionTokens,
			final DateTime startDate,
			final DateTime endDate,
			final Campaign.PrivacyState privacyState,
			final Campaign.RunningState runningState,
			final Campaign.Role role)
			throws DataAccessException;
	
	/**
	 * Queries all of the campaigns in the system limited by the parameters.
	 * The ordering is by creation timestamp for paging.
	 * 
	 * @param campaignIds Limits the campaigns to only those whose ID is in 
	 * 					  this list.
	 * 
	 * @param surveyIds Limits the campaigns to only those that have a survey
	 * 					whose ID is in this list.
	 * 
	 * @param promptIds Limits the campaigns to only those that have a prompt
	 * 					whose ID is in this list.
	 * 
	 * @param startDate Limits the campaigns to only those whose creation 
	 * 					timestamp is on or after this date.
	 * 
	 * @param endDate Limits the campaigns to only those whose creation 
	 * 				  timestamp is on or before this date.
	 * 
	 * @param privacyState Limits the results to only those whose privacy state
	 * 					   matches this value.
	 * 
	 * @param runningState Limits the results to only those whose running state
	 * 					   matches this value.
	 * 
	 * @param numToSkip The number of campaigns to skip.
	 * 
	 * @param numToReturn The number of campaigns to return.
	 * 
	 * @return The list of campaigns in chronological order based on their
	 * 		   creation timestamp.
	 * 
	 * @throws DataAccessException There was an error.
	 */
	public List<Campaign> getCampaigns(
		final Collection<String> campaignIds,
		final Collection<String> surveyIds,
		final Collection<String> promptIds,
		final Collection<String> classIds,
		final DateTime startDate,
		final DateTime endDate,
		final Campaign.PrivacyState privacyState,
		final Campaign.RunningState runningState,
		final long numToSkip,
		final long numToReturn)
		throws DataAccessException;

	/**
	 * Updates a campaign. The 'request' and 'campaignId' are required; however,
	 * the remaining parameters may be null indicating that they should not be
	 * updated.
	 * 
	 * @param request
	 *            The Request that is performing this service.
	 * 
	 * @param campaignId
	 *            The campaign's unique identifier.
	 * 
	 * @param xml
	 *            The new XML for the campaign or null if the XML should not be
	 *            updated.
	 * 
	 * @param description
	 *            The new description for the campaign or null if the
	 *            description should not be updated.
	 * 
	 * @param runningState
	 *            The new running state for the campaign or null if the running
	 *            state should not be updated.
	 * 
	 * @param privacyState
	 *            The new privacy state for the campaign or null if the privacy
	 *            state should not be updated.
	 * 
	 * @param classesToAdd
	 *            The collection of classes to associate with the campaign.
	 * 
	 * @param classesToRemove
	 *            The collection of classes to disassociate from the campaign.
	 * 
	 * @param usersAndRolesToAdd
	 *            A map of usernames to a list of roles that the users should be
	 *            granted in the campaign or null if no users should be granted
	 *            any new roles.
	 * 
	 * @param usersAndRolesToRemove
	 *            A map of usernames to a list of roles that should be revoked
	 *            from the user in the campaign or null if no users should have
	 *            any of their roles revoked.
	 */
	void updateCampaign(String campaignId, String xml, String description,
			Campaign.RunningState runningState,
			Campaign.PrivacyState privacyState,
			Collection<String> classesToAdd,
			Collection<String> classesToRemove,
			Map<String, Set<Campaign.Role>> usersAndRolesToAdd,
			Map<String, Set<Campaign.Role>> usersAndRolesToRemove)
			throws DataAccessException;

	/**
	 * Deletes a campaign.
	 * 
	 * @param campaignId
	 *            The unique identifier of the campaign to be deleted.
	 */
	void deleteCampaign(String campaignId) throws DataAccessException;

}
