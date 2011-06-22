/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.request.AwRequest;
import org.springframework.jdbc.core.RowMapper;


/**
 * Gets all the document IDs for the requesting user where they have the
 * parameterized roles with the document.
 * 
 * @author John Jenkins
 */
public class FindAllDocumentsForRequestingUserWithRolesDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllDocumentsForRequestingUserWithRolesDao.class);
	
	private static final String SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE =
			// The distinct here isn't actually necessary, but I am hoping it
			// will speed up the execution.
			"SELECT allDocuments.uuid, allDocuments.role " +
			"FROM (" +
				// Get all of the documents that belong to classes to which the
				// user also belongs.
				"SELECT d.uuid, u.login_id, dr.role " +
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
				// privileged in class.
				"AND (" +
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
					" OR " +
					"(ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "')" +
				")" +
				// Union
				" UNION " +
				// Get all of the documents that belong to campaigns to which
				// the user also belongs.
				"SELECT d.uuid, u.login_id, dr.role " +
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
				// for the campaign.
				"AND (" +
					"(d.privacy_state_id = dps.id " +
					"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "' " +
					"AND (" +
						"(ur.role = '" + CampaignRoleCache.ROLE_ANALYST + "')" +
						" OR " +
						"(ur.role = '" + CampaignRoleCache.ROLE_AUTHOR + "')" +
					"))" +
					" OR " +
					"(ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "')" +
				")" +
				// Union
				" UNION " +
				// Get all of the documents that are directly associated with
				// the user.
				"SELECT d.uuid, u.login_id, dr.role " +
				"FROM user u, document d, document_role dr, document_privacy_state dps, document_user_role dur " +
				// Get all of the documents that belong directly to the user.
				"WHERE dur.document_id = d.id " +
				"AND dur.document_role_id = dr.id " +
				"AND dur.user_id = u.id " +
				// ACL: The document must be shared or the user must be an
				// owner.
				"AND (" +
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
					" OR " +
					"(dr.role = '" + DocumentRoleCache.ROLE_OWNER + "')" +
				")" +
			// This is an aggregation of the form:
			// 		document.uuid, user.login_id, document_role.role
			// There are no duplicate rows, but there may be duplicate document
			// IDs as one user may have multiple roles with a single document
			// via document-campaign, document-class, and document-user
			// associations.
			") AS allDocuments " +
			// Switch on a single user.
			"WHERE allDocuments.login_id = ?";
	
	/**
	 * Keeps track of the document ID and the associated role for the user on
	 * which the query ran.
	 * 
	 * @author John Jenkins
	 */
	private class DocumentAndRole {
		public String _documentId;
		public String _role;
		
		public DocumentAndRole(String documentId, String role) {
			_documentId = documentId;
			_role = role;
		}
	}
				
	private List<String> _roles;

	/**
	 * Sets up this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 * 
	 * @param roles The list of document roles to query for. It is recommended,
	 * 				although not required, to use the constants defined in the
	 * 				document's role cache.
	 */
	public FindAllDocumentsForRequestingUserWithRolesDao(DataSource dataSource, List<String> roles) {
		super(dataSource);
		
		if((roles == null) || (roles.size() == 0)) {
			throw new IllegalArgumentException("The roles list cannot be null or empty.");
		}
		
		_roles = roles;
	}
	
	/**
	 * Gets all the documents with which the user is associated and the user's
	 * role with that document. Then, it picks out all the documents whose
	 * associated role is in the list of desired roles. It then returns this
	 * list of document IDs.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// For this user, get all the document IDs that they can see and the
		// user's associated document role.
		List<?> documentIdAndRoles;
		try {
			documentIdAndRoles = getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE, 
					new Object[] { awRequest.getUser().getUserName() }, 
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new DocumentAndRole(rs.getString("uuid"), rs.getString("role"));
						}
					}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE + "' with parameters: " +
					awRequest.getUser().getUserName(), e);
			throw new DataAccessException(e);
		}
		
		// The result set that will be converted into a list and returned to
		// the user. This is a HashSet for now so that no duplicate document
		// IDs will be returned.
		Set<String> result = new HashSet<String>();
		
		// Iterate through the list of documents and roles and place all of the
		// applicable document IDs in the list. 
		ListIterator<?> documentIdAndRolesIter = documentIdAndRoles.listIterator();
		while(documentIdAndRolesIter.hasNext()) {
			DocumentAndRole dar = (DocumentAndRole) documentIdAndRolesIter.next();
			
			if(_roles.contains(dar._role)) {
				result.add(dar._documentId);
			}
		}

		// Converts the result Set into a List and returns it in the document
		// list.
		awRequest.setResultList(new ArrayList<String>(result));
	}
}