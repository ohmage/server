package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains the functionality for creating, reading, updating, and 
 * deleting campaign-document associations. 
 * 
 * @author John Jenkins
 */
public final class CampaignDocumentDaos extends Dao {
	// Retrieves all of the campaigns associated with a document.
	private static final String SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_DOCUMENT =
		"SELECT c.urn " +
		"FROM campaign c, document d, document_campaign_role dcr " +
		"WHERE d.uuid = ? " +
		"AND d.id = dcr.document_id " +
		"AND c.id = dcr.campaign_id";
	
	// Retrieves a campaign's document role.
	private static final String SQL_GET_CAMPAIGN_DOCUMENT_ROLE = 
		"SELECT dr.role " +
		"FROM campaign c, document d, document_campaign_role dcr, document_role dr " +
		"WHERE c.urn = ? " +
		"AND d.uuid = ? " +
		"AND c.id = dcr.campaign_id " +
		"AND d.id = dcr.document_id " +
		"AND dcr.document_role_id = dr.id";
	
	private static CampaignDocumentDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignDocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves a List of String objects where each String represents the 
	 * unique identifier for a campaign to which this document is associated.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return A List of campaign IDs with which this document is associated.
	 */
	public static List<String> getCampaignsAssociatedWithDocument(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_DOCUMENT, 
					new Object[] { documentId }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_DOCUMENT + 
						"' with parameter: " + documentId, 
					e);
		}
	}
	
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
	public static DocumentRoleCache.Role getCampaignDocumentRole(String campaignId, String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_DOCUMENT_ROLE,
					new Object[] { campaignId, documentId },
					new RowMapper<DocumentRoleCache.Role>() {
						@Override
						public DocumentRoleCache.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return DocumentRoleCache.Role.getValue(rs.getString("role"));
						}
					}
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("A campaign has more than one role with a document.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_DOCUMENT_ROLE +
					"' with parameters: " + campaignId + ", " + documentId, e);
		}
	}
}