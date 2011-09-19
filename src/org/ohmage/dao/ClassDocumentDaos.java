package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains the functionality for creating, reading, updating, and 
 * deleting class-document associations. 
 * 
 * @author John Jenkins
 */
public class ClassDocumentDaos extends Dao {
	// Retrieves all of the classes associated with a document.
	private static final String SQL_GET_CLASSES_ASSOCIATED_WITH_DOCUMENT =
		"SELECT c.urn " +
		"FROM class c, document d, document_class_role dcr " +
		"WHERE d.uuid = ? " +
		"AND d.id = dcr.document_id " +
		"AND c.id = dcr.class_id";
	
	// Retrieves a class' document role.
	private static final String SQL_GET_CLASS_DOCUMENT_ROLE = 
		"SELECT dr.role " +
		"FROM class c, document d, document_class_role dcr, document_role dr " +
		"WHERE c.urn = ? " +
		"AND d.uuid = ? " +
		"AND c.id = dcr.class_id " +
		"AND d.id = dcr.document_id " +
		"AND dcr.document_role_id = dr.id";
	
	private static ClassDocumentDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private ClassDocumentDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Retrieves the list of classes associatd with a document.
	 * 
	 * @param documentId The unique identifier for the document.
	 * 
	 * @return A list of class IDs with which this document is associated.
	 */
	public static List<String> getClassesAssociatedWithDocument(String documentId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CLASSES_ASSOCIATED_WITH_DOCUMENT, 
					new Object[] { documentId }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
					SQL_GET_CLASSES_ASSOCIATED_WITH_DOCUMENT + 
						"' with parameter: " + documentId, 
					e);
		}
	}
	
	/**
	 * Retrieves a class' document role if it is associated with a document.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The class' role for some document or null if the class is not
	 * 		   associated with the document.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public static DocumentRoleCache.Role getClassDocumentRole(String classId, String documentId) throws DataAccessException {
		try {
			return DocumentRoleCache.Role.getValue(
					instance.getJdbcTemplate().queryForObject(
							SQL_GET_CLASS_DOCUMENT_ROLE,
							new Object[] { classId, documentId },
							String.class)
					);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("A ckass has more than one role with a document.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CLASS_DOCUMENT_ROLE +
					"' with parameters: " + classId + ", " + documentId, e);
		}
	}
}