package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;
import javax.swing.tree.RowMapper;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignDocumentQueries;

/**
 * This class contains the functionality for creating, reading, updating, and 
 * deleting campaign-document associations. 
 * 
 * @author John Jenkins
 */
public final class CampaignDocumentQueries extends Query implements ICampaignDocumentQueries {
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
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private CampaignDocumentQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignDocumentQueries#getCampaignsAssociatedWithDocument(java.lang.String)
	 */
	@Override
	public List<String> getCampaignsAssociatedWithDocument(String documentId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
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
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.ICampaignDocumentQueries#getCampaignDocumentRole(java.lang.String, java.lang.String)
	 */
	@Override
	public Document.Role getCampaignDocumentRole(String campaignId, String documentId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_CAMPAIGN_DOCUMENT_ROLE,
					new Object[] { campaignId, documentId },
					new RowMapper<Document.Role>() {
						@Override
						public Document.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return Document.Role.getValue(rs.getString("role"));
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