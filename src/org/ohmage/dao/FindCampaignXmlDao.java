package org.ohmage.dao;

import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;

/**
 * Gets the XML for the campaign in the request.
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
	 * Gets the XML and stores it in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			// Get the XML from the database.
			String xml = (String) getJdbcTemplate().queryForObject(SQL_GET_XML, new Object[] { awRequest.getCampaignUrn() }, String.class);
			
			// Create the result list and put the XML in it.
			List<String> resultList = new LinkedList<String>();
			resultList.add(xml);
			
			// Set the result list in the request.
			awRequest.setResultList(resultList);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_XML + "' with parameter: " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
}