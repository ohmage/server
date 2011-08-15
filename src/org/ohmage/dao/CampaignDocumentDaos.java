package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains the functionality for creating, reading, updating, and 
 * deleting campaign-document associations. 
 * 
 * @author John Jenkins
 */
public final class CampaignDocumentDaos extends Dao {
	private static final String SQL_GET_CAMPAIGNS_ASSOCIATED_WITH_DOCUMENT =
		"SELECT c.urn " +
		"FROM campaign c, document d, document_campaign_role dcr " +
		"WHERE d.uuid = ? " +
		"AND d.id = dcr.document_id " +
		"AND c.id = dcr.campaign_id";
	
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
			return instance.jdbcTemplate.query(
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
}
