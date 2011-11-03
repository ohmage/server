package org.ohmage.query.impl;

import java.util.List;

import javax.sql.DataSource;

import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;
import org.ohmage.query.IClassDocumentQueries;
import org.springframework.jdbc.core.SingleColumnRowMapper;

/**
 * This class contains the functionality for creating, reading, updating, and 
 * deleting class-document associations. 
 * 
 * @author John Jenkins
 */
public class ClassDocumentQueries extends Query implements IClassDocumentQueries {
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
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private ClassDocumentQueries(DataSource dataSource) {
		super(dataSource);
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassDocumentQueries#getClassesAssociatedWithDocument(java.lang.String)
	 */
	public List<String> getClassesAssociatedWithDocument(String documentId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
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
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassDocumentQueries#getClassDocumentRole(java.lang.String, java.lang.String)
	 */
	public Document.Role getClassDocumentRole(String classId, String documentId) throws DataAccessException {
		try {
			return Document.Role.getValue(
					getJdbcTemplate().queryForObject(
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