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
 * Gets all of the usernames and their document roles and stores them in the
 * request.
 *  
 * @author John Jenkins
 */
public class FindAllUsersAndDocumentRoleForDocumentIdDao extends AbstractDao {
	public static final Logger _logger = Logger.getLogger(FindAllUsersAndDocumentRoleForDocumentIdDao.class);
	
	private static final String SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER =
		"SELECT distinct(u.login_id), dr.role " +
		"FROM user u, document d, document_role dr, document_user_role dur " +
		"WHERE d.uuid = ? " +
		"AND dur.document_id = d.id " +
		"AND dur.document_role_id = dr.id " +
		"AND dur.user_id = u.id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DatSource to use when querying the database.
	 */
	public FindAllUsersAndDocumentRoleForDocumentIdDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the document's ID then retrieves all the users and their respective
	 * document roles. It stores this list in the request.
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
		
		// Get the list of users and their document role.
		try {
			awRequest.setResultList(getJdbcTemplate().query(
					SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER, 
					new Object[] { documentId }, 
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new EntityAndRole(rs.getString("login_id"), rs.getString("role"));
						}
					}
			));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_DOCUMENTS_SPECIFIC_TO_REQUESTING_USER + "' with parameters: " +
					documentId, e);
			throw new DataAccessException(e);
		}
	}
}