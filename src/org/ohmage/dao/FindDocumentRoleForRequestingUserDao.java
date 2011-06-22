package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * Finds the requesters role with the document parameter.
 * 
 * @author John Jenkins
 */
public class FindDocumentRoleForRequestingUserDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(FindDocumentRoleForRequestingUserDao.class);
	
	private static final String SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE =
		// The distinct here isn't actually necessary, but I am hoping it
		// will speed up the execution.
		"SELECT allDocuments.role " +
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
		"WHERE allDocuments.login_id = ? " +
		"AND allDocuments.uuid = ?";

	/**
	 * Keeps track of the document ID and the associated role for the user on
	 * which the query ran.
	 * 
	 * @author John Jenkins
	 */
	public final class DocumentAndRole {
		public String _documentId;
		public String _role;
		
		public DocumentAndRole(String documentId, String role) {
			_documentId = documentId;
			_role = role;
		}
	}
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public FindDocumentRoleForRequestingUserDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the role of the requester with the document in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID, e);
			throw new DataAccessException(e);
		}
		
		// Get all of the requester's roles for the parameterized document ID
		// and set their role as the highest of them.
		try {
			List<?> roles = getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE, 
					new Object[] { awRequest.getUser().getUserName(), documentId }, 
					new SingleColumnRowMapper());
			
			if(roles.contains(DocumentRoleCache.ROLE_OWNER)) {
				awRequest.getUser().setDocumentRole(DocumentRoleCache.ROLE_OWNER);
			}
			else if(roles.contains(DocumentRoleCache.ROLE_WRITER)) {
				awRequest.getUser().setDocumentRole(DocumentRoleCache.ROLE_WRITER);
			}
			else if(roles.contains(DocumentRoleCache.ROLE_READER)) {
				awRequest.getUser().setDocumentRole(DocumentRoleCache.ROLE_READER);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_FOR_USER_WITH_DOCUMENT_ROLE + "' with parameters: " +
					awRequest.getUser().getUserName() + ", " + documentId, e);
			throw new DataAccessException(e);
		}
	}
}