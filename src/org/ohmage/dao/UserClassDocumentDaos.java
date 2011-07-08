package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.ClassRoleCache;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.domain.DocumentInformation;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-class-document relationships.
 * 
 * @author John Jenkins
 */
public class UserClassDocumentDaos extends Dao {
	// Retrieves the list of documents visible to a user in a class.
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER = 
		"SELECT distinct(d.uuid) " +
		"FROM user u, class c, user_class uc, user_class_role ucr, " +
			"document d, document_role dr, document_privacy_state dps, document_class_role dclr " +
		"WHERE u.username = ? " +
		"AND c.urn = ? " +
		"AND dclr.document_id = d.id " +
		"AND dclr.document_role_id = dr.id " + 
		"AND dclr.class_id = c.id " +
		"AND dclr.class_id = uc.class_id " +
		"AND uc.user_id = u.id " +
		"AND uc.user_class_role_id = ucr.id " +
		"AND (" +
			"(d.privacy_state_id = dps.id " +
			"AND dps.privacy_state = '" + DocumentPrivacyStateCache.PRIVACY_STATE_SHARED + "')" +
			" OR " +
			"(ucr.role = '" + ClassRoleCache.ROLE_PRIVILEGED + "')" +
			" OR " +
			"(dr.role = '" + DocumentRoleCache.ROLE_OWNER + "')" +
		")";
	
	private static UserClassDocumentDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserClassDocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Gathers the information about all of the documents in a class that are
	 * visible to the requesting user.
	 * 
	 * @param username The username of the requesting user.
	 * 
	 * @param classId The class ID for the class whose information is desired.
	 * 
	 * @return Returns a List of DocumentInformation objects where each object
	 * 		   represents a document visible to the user in the class.
	 */
	public static List<DocumentInformation> getVisibleDocumentsToUserInClass(String username, String classId) {
		List<String> documentList;
		try {
			documentList = instance.jdbcTemplate.query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER, 
					new Object[] { username, classId },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_CLASS_FOR_REQUESTING_USER + " with parameters: " +
					username + ", " + classId, e);
		}
		
		List<DocumentInformation> result = new LinkedList<DocumentInformation>();
		
		for(String documentId : documentList) {
			result.add(DocumentDaos.getDocumentInformation(documentId));
		}
		
		return result;
	}
}
