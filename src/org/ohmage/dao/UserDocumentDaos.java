package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.domain.DocumentInformation;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-document relationships.
 * 
 * @author John Jenkins
 */
public class UserDocumentDaos extends Dao {
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
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.ROLE_OWNER + "')" + 
		")";
	
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
	 * Gets the information for all of the documents that belong explicitly to
	 * a user and that are visible to the user.
	 * 
	 * @param username The username of the user whose documents' information 
	 * 				   are desired.
	 * 
	 * @return Returns a List of DocumentInformation objects for all of the
	 * 		   documents that are visible to the user.
	 */
	public static List<DocumentInformation> getVisibleDocumentsSpecificToUser(String username) {
		// Get the list of documents specific to the user.
		List<String> userDocuments;
		try {
			userDocuments = instance.jdbcTemplate.query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER, 
					new Object[] { username }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER + "' with parameter: " + username, e);
		}
		
		// Create the list to be returned to the caller.
		List<DocumentInformation> resultList = new LinkedList<DocumentInformation>();
		
		// Get the document information for each of the documents in the list.
		for(String documentId : userDocuments) {
			resultList.add(DocumentDaos.getDocumentInformation(documentId));
		}
		
		return resultList;
	}
}
