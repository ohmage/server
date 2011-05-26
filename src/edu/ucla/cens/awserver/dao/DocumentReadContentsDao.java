package edu.ucla.cens.awserver.dao;

import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentReadContentsAwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Gets the URL of the document being requested and stores it in the toReturn
 * map.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentReadContentsDao.class);
	
	private static final String SQL_GET_DOCUMENT_URL = "SELECT url " +
													   "FROM document " +
													   "WHERE uuid = ?";
	
	/**
	 * Creates this service.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public DocumentReadContentsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the URL from the database and puts it in the toReturn map.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException("Missing required key '" + InputKeys.DOCUMENT_ID + "'.");
		}
		
		List<?> urlList;
		try {
			urlList = getJdbcTemplate().query(SQL_GET_DOCUMENT_URL, 
											  new Object[] { documentId }, 
											  new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_DOCUMENT_URL + "' with parameter: " + documentId, e);
			throw new DataAccessException(e);
		}
		
		if(urlList.size() == 0) {
			_logger.error("Document doesn't exist, but this should have been caught sooner.");
			awRequest.setFailedRequest(true);
		}
		else if(urlList.size() > 1) {
			_logger.error("Data integrity error. More than one document has the same UUID.");
			awRequest.setFailedRequest(true);
		}
		else {
			awRequest.addToReturn(DocumentReadContentsAwRequest.KEY_DOCUMENT_FILE, urlList.listIterator().next(), true);
		}
	}

}
