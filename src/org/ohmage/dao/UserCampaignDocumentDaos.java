package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-campaign-document relationships.
 * 
 * @author John Jenkins
 */
public final class UserCampaignDocumentDaos extends Dao {
	// Check if the user is a supervisor in any campaign with which the 
	// document is associated.
	private static final String SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT = 
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
				"document d, document_campaign_role dcr " +
			// Switch on the username
			"WHERE u.username = ? " +
			// and the document's ID.
			"AND d.uuid = ? " +
			// Ensure that they are a supervisor in the campaign.
			"AND u.id = urc.user_id " +
			"AND c.id = urc.campaign_id " +
			"AND ur.id = urc.user_role_id " +
			"AND ur.role = '" + CampaignRoleCache.Role.SUPERVISOR + "' " +
			// Ensure that the campaign is associated with the document.
			"AND d.id = dcr.document_id " +
			"AND c.id = dcr.campaign_id" +
		")";
	
	// Gets all of the document IDs visible to a user in a campaign.
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER = 
		"SELECT distinct(d.uuid) " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
			"document d, document_role dr, document_privacy_state dps, document_campaign_role dcar " +
		"WHERE u.username = ? " +
		"AND c.urn = ? " +
		"AND dcar.document_id = d.id " +
		"AND dcar.document_role_id = dr.id " +
		"AND dcar.campaign_id = c.id " +
		"AND dcar.campaign_id = urc.campaign_id " +
		"AND urc.user_id = u.id " +
		"AND urc.user_role_id = ur.id " +
		"AND d.privacy_state_id = dps.id " +
		"AND (" +
			"(dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "' " +
			"AND ur.role != '" + CampaignRoleCache.Role.PARTICIPANT + "')" +
			" OR " +
			"(ur.role = '" + CampaignRoleCache.Role.SUPERVISOR + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" +
		")";
	
	private static UserCampaignDocumentDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserCampaignDocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
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
	public static List<String> getVisibleDocumentsToUserInCampaign(String username, String campaignId) 
	 	throws DataAccessException {
		
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER, 
					new Object[] { username, campaignId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_CAMPAIGN_FOR_REQUESTING_USER + " with parameters: " +
					username + ", " + campaignId, e);
		}
	}
	
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
	public static Boolean getUserIsSupervisorInAnyCampaignAssociatedWithDocument(String username, String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT, 
					new Object[] { username, documentId }, 
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_USER_IS_SUPERVISOR_IN_ANY_CAMPAIGN_ASSOCIATED_WITH_DOCUMENT +
					"' with parameters: " + username + ", " + documentId, e);
		}
	}
}