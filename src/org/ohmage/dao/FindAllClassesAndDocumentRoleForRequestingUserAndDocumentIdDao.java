package org.ohmage.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.EntityAndRole;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.RowMapper;

/**
 * Gets all of the classes and their document role based on the document's ID.
 * 
 * @author John Jenkins
 */
public class FindAllClassesAndDocumentRoleForRequestingUserAndDocumentIdDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(FindAllClassesAndDocumentRoleForRequestingUserAndDocumentIdDao.class);
	
	public static final String SQL_GET_CLASSES_AND_ROLE_FOR_REQUESTING_USER = 
		"SELECT distinct(c.urn), dr.role " +
		"FROM class c, document d, document_role dr, document_class_role dclr " +
		"WHERE d.uuid = ? " +
		"AND dclr.document_id = d.id " +
		"AND dclr.document_role_id = dr.id " + 
		"AND dclr.class_id = c.id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public FindAllClassesAndDocumentRoleForRequestingUserAndDocumentIdDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the list of classes and their role for the document in the request
	 * and stores it as the result list in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of class IDs.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key in toProcess map: " + InputKeys.DOCUMENT_ID, e);
		}
		
		try {
			awRequest.setResultList(getJdbcTemplate().query(
					SQL_GET_CLASSES_AND_ROLE_FOR_REQUESTING_USER, 
					new Object[] { documentId }, 
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new EntityAndRole(rs.getString("urn"), rs.getString("role"));
						}
					}
			));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CLASSES_AND_ROLE_FOR_REQUESTING_USER + "' with parameters: " +
					documentId, e);
			throw new DataAccessException(e);
		}
	}
}