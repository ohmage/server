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

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignCreationAwRequest;

/**
 * Adds the campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationDao.class);
	
	private static final String SQL_GET_NEW_CAMPAIGN_ID = "SELECT id " +
														  "FROM campaign " +
														  "WHERE urn=?";

	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn=?";
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id=?";
	
	private static final String SQL_GET_ROLE_ID = "SELECT id " +
												  "FROM user_role " +
												  "WHERE role='author'";
	
	private static final String SQL_INSERT_CAMPAIGN = "INSERT INTO campaign(description, xml, running_state, privacy_state, name, urn, creation_timestamp) " +
											 		  "VALUES (?,?,?,?,?,?,?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS = "INSERT INTO campaign_class(campaign_id, class_id) " +
															"VALUES (?,?)";
	
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN = "INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
																"VALUES (?,?,?)";
	
	/**
	 * Creates a basic DAO.
	 * 
	 * @param dataSource The DataSource that we will run our queries against.
	 */
	public CampaignCreationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Adds the campaign to the database, connects the classes to the
	 * campaign, and sets the currently logged in user as the creator.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Inserting campaign into the database.");
		
		CampaignCreationAwRequest request;
		try {
			request = (CampaignCreationAwRequest) awRequest;
		}
		catch(ClassCastException e) {
			_logger.error("Attempting to add campaign on a non-CampaignCreationAwRequest object.");
			throw new DataAccessException("Invalid request.");
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
		String campaignName = root.query("/campaign/campaignName").get(0).getValue();
		
		Calendar now = Calendar.getInstance();
		String nowFormatted = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " +
							  now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
	
		try {
			getJdbcTemplate().update(SQL_INSERT_CAMPAIGN, 
									 new Object[] { request.getDesciption(), 
									 				request.getCampaign(), 
									 				request.getRunningState(), 
									 				request.getPrivacyState(), 
									 				campaignName,
									 				campaignUrn,
									 				nowFormatted});
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN + "' with parameters: " +
						  request.getDesciption() + ", " + 
						  request.getCampaign() + ", " +
						  request.getRunningState() + ", " +
						  request.getPrivacyState() + ", " +
						  campaignName + ", " +
						  campaignUrn + ", " +
						  nowFormatted, dae);
			throw new DataAccessException(dae);
		}
		
		// Get campaign ID.
		int campaignId;
		try {
			campaignId = getJdbcTemplate().queryForInt(SQL_GET_NEW_CAMPAIGN_ID, new Object[] { campaignUrn });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_NEW_CAMPAIGN_ID + "' with parameter: " + campaignUrn, dae);
			throw new DataAccessException(dae);
		}
		
		// Hookup to classes.
		String[] classes = request.getCommaSeparatedListOfClasses().split(",");
		for(int i = 0; i < classes.length; i++) {
			int classId;
			try {
				classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { classes[i] });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + classes[i], dae);
				throw new DataAccessException(dae);
			}
			
			try {
				getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + campaignId + ", " + classId, dae);
				throw new DataAccessException(dae);
			}
		}
		
		// Make the current user the creator.
		int userId;
		try {
			userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { request.getUser().getUserName() });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + request.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		int roleId;
		try {
			roleId = getJdbcTemplate().queryForInt(SQL_GET_ROLE_ID);
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_ROLE_ID + "'", dae);
			throw new DataAccessException(dae);
		}
		
		try {
			getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, roleId });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + roleId, dae);
			throw new DataAccessException(dae);
		}
	}
}
