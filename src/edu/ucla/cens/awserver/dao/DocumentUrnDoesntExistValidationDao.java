package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Checks the database such that no other document exists with the same URN.
 * 
 * @author John Jenkins
 */
public class DocumentUrnDoesntExistValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(DocumentUrnDoesntExistValidationDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM document " +
									  "WHERE urn = ?";
	
	/**
	 * Sets up this DAO with the DataSource to use to query the database.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public DocumentUrnDoesntExistValidationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks if another document exists with the same name. If so, it sets
	 * the request as failed.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String urn;
		try {
			urn = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_URN);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required key in toProcess map: " + InputKeys.DOCUMENT_URN);
			throw new DataAccessException(e);
		}
		
		try {
			if(getJdbcTemplate().queryForInt(SQL, new Object[] { urn }) != 0) {
				_logger.info("Another document with the URN '" + urn + "' already exists.");
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(e);
		}
	}
}