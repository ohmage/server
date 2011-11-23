package org.ohmage.query;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;

public interface ICampaignQueries {
	/**
	 * Creates a new campaign.
	 * 
	 * @param campaignId
	 *            The new campaign's unique identifier.
	 * 
	 * @param name
	 *            The new campaign's name.
	 * 
	 * @param xml
	 *            The XML defining the new campaign.
	 * 
	 * @param description
	 *            An optional description for the campaign.
	 * 
	 * @param runningState
	 *            The initial running state for the campaign.
	 * 
	 * @param privacyState
	 *            The initial privacy state for the campaign.
	 * 
	 * @param classIds
	 *            A List of classes with which this campaign should be
	 *            associated.
	 * 
	 * @param creatorUsername
	 *            The username of the creator of this campaign.
	 */
	void createCampaign(String campaignId, String name, String xml,
			String description, String iconUrl, String authoredBy,
			Campaign.RunningState runningState,
			Campaign.PrivacyState privacyState, Collection<String> classIds,
			String creatorUsername) throws DataAccessException;

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
	Timestamp getCreationTimestamp(String campaignId)
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
	List<String> getCampaignsOnOrAfterDate(Date date)
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
	List<String> getCampaignsOnOrBeforeDate(Date date)
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