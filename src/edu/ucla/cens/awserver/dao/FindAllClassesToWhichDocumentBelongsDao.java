package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Gets all the classes to which this document belongs and stores it in the 
 * result list for the request.
 * 
 * @author John Jenkins
 */
public class FindAllClassesToWhichDocumentBelongsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllClassesToWhichDocumentBelongsDao.class);
	
	private static final String SQL_GET_CLASSES_FOR_DOCUMENT = "SELECT c.urn " +
	 														   "FROM class c, document d, document_class_role dcr " +
	 														   "WHERE d.uuid = ? " +
	 														   "AND d.id = dcr.document_id " +
	 														   "AND c.id = dcr.class_id";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public FindAllClassesToWhichDocumentBelongsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets all the classes for which this document is associated.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the document's ID.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.DOCUMENT_ID);
			throw new DataAccessException(e);
		}
		
		// Set the result list with all the campaigns with which this document
		// is associated.
		try {
			awRequest.setResultList(getJdbcTemplate().query(SQL_GET_CLASSES_FOR_DOCUMENT, new Object[] { documentId }, new SingleColumnRowMapper()));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CLASSES_FOR_DOCUMENT + "' with parameter: " + documentId, e);
			throw new DataAccessException(e);
		}
	}
}