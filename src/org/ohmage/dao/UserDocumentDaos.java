package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-document relationships.
 * 
 * @author John Jenkins
 */
public final class UserDocumentDaos extends Dao {
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
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" + 
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
			"(dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" + 
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
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "')" +
					" OR " +
					"(ucr.role = '" + ClassRoleCache.Role.PRIVILEGED + "')" +
					" OR " +
					"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" +
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
					"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "' " +
					"AND (" +
						"(ur.role = '" + CampaignRoleCache.Role.ANALYST + "')" +
						" OR " +
						"(ur.role = '" + CampaignRoleCache.Role.AUTHOR + "')" +
					"))" +
				" OR " +
					"(ur.role = '" + CampaignRoleCache.Role.SUPERVISOR + "')" +
				" OR " +
					"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" +
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
					"(d.privacy_state_id = dps.id AND dps.privacy_state = '" + DocumentPrivacyStateCache.PrivacyState.SHARED + "')" +
					" OR " +
					"(dr.role = '" + DocumentRoleCache.Role.OWNER + "')" +
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
	
	private static UserDocumentDaos instance;

	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when accessing the database.
	 */
	private UserDocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves the unique identifiers for all of the documents directly
	 * associated with a user.
	 * 
	 * @param username The username of the user whose documents are desired.
	 * 
	 * @return A list of document IDs.
	 */
	public static List<String> getVisibleDocumentsSpecificToUser(String username) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
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
	public static String getDocumentRoleForDocumentSpecificToUser(String username, String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(
					SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_SPECIFIC_TO_REQUESTING_USER, 
					new Object[] { username, documentId }, 
					String.class);
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
	public static List<DocumentRoleCache.Role> getDocumentRolesForDocumentForUser(String username, String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_DOCUMENT_ROLES_FOR_DOCUMENT_FOR_USER, 
					new Object[] { username, documentId }, 
					new RowMapper<DocumentRoleCache.Role>() {
						@Override
						public DocumentRoleCache.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return DocumentRoleCache.Role.getValue(rs.getString("role"));
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
