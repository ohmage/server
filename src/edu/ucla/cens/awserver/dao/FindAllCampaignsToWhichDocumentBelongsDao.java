package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Gets all the campaigns with which this document is associated.
 * 
 * @author John Jenkins
 */
public class FindAllCampaignsToWhichDocumentBelongsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllCampaignsToWhichDocumentBelongsDao.class);
	
	private static final String SQL_GET_CAMPAIGNS_FOR_DOCUMENT = "SELECT c.urn " +
																 "FROM campaign c, document d, document_campaign_role dcr " +
																 "WHERE d.uuid = ? " +
																 "AND d.id = dcr.document_id " +
																 "AND c.id = dcr.campaign_id";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public FindAllCampaignsToWhichDocumentBelongsDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets all the campaigns with which this document is associated.
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
			awRequest.setResultList(getJdbcTemplate().query(SQL_GET_CAMPAIGNS_FOR_DOCUMENT, new Object[] { documentId }, new SingleColumnRowMapper()));
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGNS_FOR_DOCUMENT + "' with parameter: " + documentId, e);
			throw new DataAccessException(e);
		}
	}

}
