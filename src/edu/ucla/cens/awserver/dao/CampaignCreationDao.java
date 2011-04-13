package edu.ucla.cens.awserver.dao;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;

import javax.sql.DataSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;
import edu.ucla.cens.awserver.service.ServiceException;

/**
 * Adds the campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationDao.class);
	
	private static final String SQL_INSERT = "INSERT INTO campaign(description, xml, running_state, privacy_state, name, urn, creation_timestamp)" +
											 " VALUES (?,?,?,?,?,?,?)";
	
	/**
	 * Creates a basic DAO.
	 * 
	 * @param dataSource The DataSource that we will run our queries against.
	 */
	public CampaignCreationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Adds the campaign to the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Inserting campaign into database.");
		
		CampaignCreationAwRequest request;
		try {
			request = (CampaignCreationAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Attempting to add campaign on a non-CampaignCreationAwRequest object.");
			throw new ServiceException("Invalid request.");
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(request.getCampaign()));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unable to read XML.", e);
			throw new ServiceException("XML was unreadable.");
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Invalid XML.", e);
			throw new ServiceException("XML was invalid.");
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unparcelable XML.", e);
			throw new ServiceException("XML was unparcelable.");
		}
		
		Element root = document.getRootElement();
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue();
		String campaignName = root.query("/campaign/campaignName").get(0).getValue();
		
		Calendar now = Calendar.getInstance();
		String nowFormatted = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " +
							  now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
		
		try {
			getJdbcTemplate().query(SQL_INSERT, 
									new Object[] { request.getDesciption(), 
									request.getCampaign(), 
									request.getRunningState(), 
									request.getPrivacyState(), 
									campaignName,
									campaignUrn,
									nowFormatted}, 
									new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT + "' with parameters: " +
						  request.getDesciption() + ", " + 
						  request.getCampaign() + ", " +
						  request.getRunningState() + ", " +
						  request.getPrivacyState() + ", " +
						  campaignName + ", " +
						  campaignUrn + ", " +
						  nowFormatted);
			throw new DataAccessException(dae);
		}
	}
}
