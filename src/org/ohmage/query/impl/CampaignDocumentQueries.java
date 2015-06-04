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
package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.ICampaignDocumentQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

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
	 * @see org.ohmage.query.impl.ICampaignDocumentQueries#getCampaignsAssociatedWithDocument(java.lang.String)
	 */
	@Override
	public List<Document.UserContainerRole> 
	getCampaignRolesAssociatedWithDocumentSet(String username, Collection<Integer> documentIds) throws DataAccessException {
		
		if (documentIds == null || documentIds.size() == 0)
			throw new DataAccessException("Document list is empty");

		StringBuilder sql = 
				new StringBuilder(
						"SELECT d.id, c.urn, dr.role, " +
							"GROUP_CONCAT(DISTINCT ur.role ORDER by ur.role ASC SEPARATOR ',') as user_roles " +
						"FROM document d " +
						  "JOIN document_campaign_role dcr on (d.id = dcr.document_id) " +
						  "JOIN document_role dr on (dcr.document_role_id=dr.id) " +
						// "JOIN document_privacy_state dps on (d.privacy_state_id = dps.id) " +
						  "JOIN user_role_campaign urc on (dcr.campaign_id = urc.campaign_id) " +
						  "JOIN user_role ur on (ur.id = urc.user_role_id) " +
						  "JOIN campaign c on (c.id = dcr.campaign_id) " +
						  "JOIN user u on (urc.user_id = u.id) " +
						"WHERE u.username = ? " +
						  "AND d.id in ");
		sql.append(StringUtils.generateStatementPList(documentIds.size()));	
		sql.append(		  
						//  " AND ( dps.privacy_state = 'shared' " +
						//	  "OR ur.role = 'supervisor' " +
						//	  "OR dr.role = 'owner') " +
						 " GROUP BY d.id, dcr.campaign_id ");
		
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		parameters.addAll(documentIds);

		try {
			
			return (getJdbcTemplate().query(
					sql.toString(), 
					parameters.toArray(), 
					new RowMapper<Document.UserContainerRole>() {
						@Override
						public Document.UserContainerRole mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							try {
								return new Document.UserContainerRole(
												rs.getInt("id"),
												rs.getString("urn"), 
												Document.Role.getValue(rs.getString("role")), 
												rs.getString("user_roles"));
							} catch (final Exception e) {
								throw new SQLException("Can't create a role with parameter: " + rs.getInt("id") + "," + rs.getString("urn"), e);
							}
						}
					}
				));
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + 
					"' with parameters: " + username + ", " + documentIds.toString(), e);
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
