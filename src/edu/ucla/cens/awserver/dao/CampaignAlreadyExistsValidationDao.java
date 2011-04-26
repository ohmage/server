package edu.ucla.cens.awserver.dao;

import java.io.IOException;
import java.io.StringReader;

import javax.sql.DataSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Validates the contents of the XML with regards to what is in the database 
 * to ensure that not collisions will take place.
 * 
 * @author John Jenkins
 */
public class CampaignAlreadyExistsValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignAlreadyExistsValidationDao.class);
	
	private static final String SQL = "SELECT count(*)" +
									  " FROM campaign" +
									  " WHERE urn=?";

	/**
	 * Sets up the data source for this DAO.
	 * 
	 * @param dataSource The data source that will be used to query the
	 * 					 database for information.
	 */
	public CampaignAlreadyExistsValidationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Parses the XML for the campaign information, checks it against the
	 * database, and makes sure that no collisions will take place.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException e) {
			_logger.info("No campaign XML in the toProcess map, so skipping service validation.");
			return;
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(campaignXml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unable to read XML.", e);
			throw new DataAccessException("XML was unreadable.");
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Invalid XML.", e);
			throw new DataAccessException("XML was invalid.");
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unparcelable XML.", e);
			throw new DataAccessException("XML was unparcelable.");
		}
		
		Element root = document.getRootElement();
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue();
		
		try {
			int others = getJdbcTemplate().queryForInt(SQL, new Object[] { campaignUrn });
			
			if(others != 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("An error occurred while running the following SQL '" + SQL + "' with the parameter: " + campaignUrn);
			throw new DataAccessException(e);
		}
	}
}
