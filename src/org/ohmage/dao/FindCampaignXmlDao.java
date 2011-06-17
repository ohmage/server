package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;

/**
 * Gets the campaign XML from the campaign parameter in the request.
 * 
 * @author John Jenkins
 */
public class FindCampaignXmlDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(FindCampaignXmlDao.class);
	
	private static final String SQL_GET_XML =
		"SELECT xml " +
		"FROM campaign " +
		"WHERE urn = ?";
	
	/**
	 * Builds this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public FindCampaignXmlDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets the XML for the campaign whose ID was in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the ID for the campaign from the request.
		String campaignId = awRequest.getCampaignUrn();
		
		// If it was never set, flag an error.
		if(campaignId == null) {
			_logger.error("The required campaign ID was not set in the request.");
			throw new DataAccessException("The required campaign ID was not set in the request.");
		}
		
		try {
			// Get the XML.
			String xml = (String) getJdbcTemplate().queryForObject(
					SQL_GET_XML, 
					new Object [] { campaignId }, 
					String.class);
			
			// Create the result list and add the XML.
			List<String> resultList = new LinkedList<String>();
			resultList.add(xml);
			
			// Add the result list to the request.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			int actualSize = e.getActualSize();
			
			if(actualSize == 0) {
				_logger.error("No such campaign XML in the database.");
				throw new DataAccessException(e);
			}
			else if(actualSize > 1) {
				_logger.error("More than one campaign has the same URN.");
				throw new DataAccessException(e);
			}
			else {
				_logger.error("...wait, what?");
				throw new DataAccessException(e);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_XML + "' with parameter: " + campaignId, e);
			throw new DataAccessException(e);
		}
	}
}