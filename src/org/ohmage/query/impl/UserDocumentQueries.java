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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.Document;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IUserDocumentQueries;
import org.ohmage.request.document.DocumentReadRequest;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-document relationships.
 * 
 * @author John Jenkins
 */
public final class UserDocumentQueries extends Query implements IUserDocumentQueries {
	private static final Logger LOGGER = Logger.getLogger(UserDocumentQueries.class);
	
	// Gets the list of documents visible and specific to a user.
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER =
		"SELECT distinct(d.uuid) " +
		"FROM user u, document d, document_role dr, document_privacy_state dps, document_user_role dur " +
		"WHERE u.username = ? " +
		"AND dur.document_id = d.id " +
		"AND dur.document_role_id = dr.id " +
		"AND dur.user_id = u.id " +
		"AND (" +
			"(d.privacy_state_id = dps.id " +
			"AND dps.privacy_state = '" + Document.PrivacyState.SHARED + "')" +
			" OR " +
			"(dr.role = '" + Document.Role.OWNER + "')" + 
		")";
	
	// Gets the list of roles for a single document that is directly associated
	// with a user provided that the document exists and is visible to the 
	// user.
	private static final String SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_SPECIFIC_TO_REQUESTING_USER =
		"SELECT dr.role " +
		"FROM user u, document d, document_role dr, document_privacy_state dps, document_user_role dur " +
		"WHERE u.username = ? " +
		"AND d.uuid = ? " +
		"AND dur.user_id = u.id " +
		"AND dur.document_id = d.id " +
		"AND dur.document_role_id = dr.id " +
		"AND d.privacy_state_id = dps.id " +
		"AND (" +
			"(dps.privacy_state = '" + Document.PrivacyState.SHARED + "')" +
			" OR " +
			"(dr.role = '" + Document.Role.OWNER + "')" + 
		")";
	
	private static final String SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_FOR_USER =
		// The distinct here isn't actually necessary, but I am hoping it
		// will speed up the execution.
		"SELECT allDocuments.role " +
		"FROM (" +
				// Get all of the documents that belong to classes to which the
				// user also belongs.
				"SELECT d.uuid, u.username, dr.role " +
				"FROM user u, class c, user_class uc, user_class_role ucr, " +
					"document d, document_role dr, document_privacy_state dps, document_class_role dclr " +
				// Get all of the classes to which the user belongs.
				"WHERE c.urn IN (" +
					"SELECT c1.urn " +
					"FROM class c1, user_class uc1 " +
					"WHERE u.id = uc1.user_id " +
					"AND uc1.class_id = c1.id" +
				") " +
				// Get all of the documents that belong to the current class.
				"AND dclr.document_id = d.id " +
				"AND dclr.document_role_id = dr.id " +
				"AND dclr.class_id = c.id " +
				"AND dclr.class_id = uc.class_id " +
				"AND uc.user_id = u.id " +
				"AND uc.user_class_role_id = ucr.id " +
				// ACL: The document must be shared or the user must be
				// privileged in class or the role of the class is 'owner'.
				"AND (" +
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + Document.PrivacyState.SHARED + "')" +
					" OR " +
					"(ucr.role = '" + Clazz.Role.PRIVILEGED + "')" +
					" OR " +
					"(dr.role = '" + Document.Role.OWNER + "')" +
				")" +
			// Union
			" UNION " +
				// Get all of the documents that belong to campaigns to which
				// the user also belongs.
				"SELECT d.uuid, u.username, dr.role " +
				"FROM user u, campaign c, user_role ur, user_role_campaign urc, " +
					"document d, document_role dr, document_privacy_state dps, document_campaign_role dcar " +
				// Get all of the campaigns to which the user belongs.
				"WHERE c.urn IN (" +
					"SELECT c1.urn " +
					"FROM campaign c1, user_role_campaign urc1 " +
					"WHERE u.id = urc1.user_id " +
					"AND urc1.campaign_id = c1.id" +
				") " +
				// Get all of the documents that belong to the current 
				// campaign.
				"AND dcar.document_id = d.id " +
				"AND dcar.document_role_id = dr.id " +
				"AND dcar.campaign_id = c.id " +
				"AND dcar.campaign_id = urc.campaign_id " +
				"AND urc.user_id = u.id " +
				"AND urc.user_role_id = ur.id " +
				// ACL: The document must be shared and the user be an analyst
				// or author of the campaign or the user must be a supervisor
				// for the campaign or the role of the campaign is 'owner'.
				"AND (" +
					"(d.privacy_state_id = dps.id " +
					"AND dps.privacy_state = '" + Document.PrivacyState.SHARED + "' " +
					"AND (" +
						"(ur.role = '" + Campaign.Role.ANALYST + "')" +
						" OR " +
						"(ur.role = '" + Campaign.Role.AUTHOR + "')" +
					"))" +
				" OR " +
					"(ur.role = '" + Campaign.Role.SUPERVISOR + "')" +
				" OR " +
					"(dr.role = '" + Document.Role.OWNER + "')" +
				")" +
			// Union
			" UNION " +
				// Get all of the documents that are directly associated with
				// the user.
				"SELECT d.uuid, u.username, dr.role " +
				"FROM user u, document d, document_role dr, document_privacy_state dps, document_user_role dur " +
				// Get all of the documents that belong directly to the user.
				"WHERE dur.document_id = d.id " +
				"AND dur.document_role_id = dr.id " +
				"AND dur.user_id = u.id " +
				// ACL: The document must be shared or the user must be an
				// owner.
				"AND (" +
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + Document.PrivacyState.SHARED + "')" +
					" OR " +
					"(dr.role = '" + Document.Role.OWNER + "')" +
				")" +
			// This is an aggregation of the form:
			// 		document_role.role
			// There are no duplicate rows, but there may be duplicate document
			// IDs as one user may have multiple roles with a single document
			// via document-campaign, document-class, and document-user
			// associations.
			") AS allDocuments " +
		// Switch on a single user.
		"WHERE allDocuments.username = ? " +
		"AND allDocuments.uuid = ?";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use when accessing the database.
	 */
	private UserDocumentQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Retrieves the unique identifiers for all of the documents directly
	 * associated with a user.
	 * 
	 * @param username The username of the user whose documents are desired.
	 * 
	 * @return A list of document IDs.
	 */
	public List<String> getVisibleDocumentsSpecificToUser(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER, 
					new Object[] { username }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Returns the document role for a document that is directly associated
	 * with a user. If the user is not directly associated with the document or
	 * it doesn't exist, null is returned.
	 * 
	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 * 
	 * @param documentId The unique identifier for the document whose role is
	 * 					 desired.
	 * 
	 * @return If the document exist and the user is directly associated with 
	 * 		   it, then their document role with said document is returned.
	 * 		   Otherwise, null is returned.
	 */
	public Document.Role getDocumentRoleForDocumentSpecificToUser(String username, String documentId) throws DataAccessException {
		try {
			return Document.Role.getValue(
					getJdbcTemplate().queryForObject(
							SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_SPECIFIC_TO_REQUESTING_USER, 
							new Object[] { username, documentId }, 
							String.class
						)
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("A user has more than one role with a document.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_SPECIFIC_TO_REQUESTING_USER + 
					"' with parameters: " + username + ", " + documentId, e);
		}
	}
	
	/**
	 * Returns the document role for a document that is directly associated
	 * with a user. If the user is not directly associated with the document or
	 * it doesn't exist, null is returned.
	 * 
	 * @param username The username of the user whose personal documents are
	 * 				   being checked.
	 * 
	 * @param documentId The unique identifier for the document whose role is
	 * 					 desired.
	 * 
	 * @return If the document exist and the user is directly associated with 
	 * 		   it, then their document role with said document is returned.
	 * 		   Otherwise, null is returned.
	 */
	public Map<Integer, Document.Role> getDocumentRoleForDocumentSetSpecificToUser(String username, Collection<Integer> documentIds) throws DataAccessException {
		
		if (documentIds == null || documentIds.size() == 0)
			throw new DataAccessException("Document list is empty");

		LOGGER.debug("HT: begging of DocumentRoleForDocumentSet *****");
		StringBuilder sql = 
				new StringBuilder(
						"select d.id, dr.role " +
						"from document d " +
						  "join document_user_role dur on (d.id = dur.document_id) " +
						  "join document_role dr on (dur.document_role_id = dr.id) " +
	//					  "join document_privacy_state dps on (d.privacy_state_id = dps.id) " +
						  "join user u on (dur.user_id = u.id) "+  
						"where u.username = ? " +
						  "and dur.document_id in ");
		sql.append(StringUtils.generateStatementPList(documentIds.size()));
	// no need to check for ACL
	//	sql.append(		  " and ( dps.privacy_state = 'shared')" +
	//						"or dr.role = 'owner')");
		
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		parameters.addAll(documentIds);
	
		/*
		LOGGER.debug("HT: sql=" + sql.toString());
		String param = "";
		for (Object i : parameters) {
			param += i.toString() + " ";
		}
		LOGGER.debug("HT: params=" + param);
		*/
		
		try {
			final Map<Integer, Document.Role> docRoleMap = new HashMap<Integer, Document.Role>();
			getJdbcTemplate().query(
					sql.toString(), 
					parameters.toArray(), 
					new RowMapper<Object>() {
						@Override
						public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							try {
								docRoleMap.put(rs.getInt("id"), Document.Role.getValue(rs.getString("role")));
								return null;
							} catch (Exception e) {
								LOGGER.debug("HT: something is wrong......");
								throw new SQLException("Can't create a role with parameter: " + rs.getInt("id") + "," + rs.getString("role"), e);
							}
						}
					}
				);
			return docRoleMap;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + 
					"' with parameters: " + username + ", " + documentIds.toString(), e);
		}
	}
	
	/**
	 * Retrieves all of the document roles for a user across their personal
	 * documents as well as documents with which they are associated in 
	 * campaigns and classes.  
	 * 
	 * @param username The username of the user whose document roles is 
	 * 				   desired.
	 * 
	 * @param documentId The unique document identifier of the document.
	 * 
	 * @return Returns a, possibly empty, List of document roles for the user
	 * 		   specific to the document.
	 */
	public List<Document.Role> getDocumentRolesForDocumentForUser(String username, String documentId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_FOR_USER, 
					new Object[] { username, documentId }, 
					new RowMapper<Document.Role>() {
						@Override
						public Document.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return Document.Role.getValue(rs.getString("role"));
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
					SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_FOR_USER + 
					"' with parameters: " +
					username + ", " + documentId, 
					e);
		}
	}
}
