package edu.ucla.cens.awserver.dao;

import java.io.IOException;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

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
	
	private static final String SQL_GET_SUPERVISOR_ID = "SELECT id " +
														"FROM user_role " +
														"WHERE role = 'supervisor'";

	private static final String SQL_GET_ANALYST_ID = "SELECT id " +
													 "FROM user_role " +
													 "WHERE role = 'analyst'";
	
	private static final String SQL_GET_AUTHOR_ID = "SELECT id " +
	  												"FROM user_role " +
	  												"WHERE role='author'";

	private static final String SQL_GET_PARTICIPANT_ID = "SELECT id " +
														 "FROM user_role " +
														 "WHERE role = 'participant'";
	
	private static final String SQL_GET_STUDENTS_FROM_CLASS = "SELECT uc.user_id, uc.class_role " +
	  														  "FROM class c, user_class uc " +
	  														  "WHERE c.urn = ? " +
	  														  "AND c.id = uc.class_id";
	
	private static final String SQL_INSERT_CAMPAIGN = "INSERT INTO campaign(description, xml, running_state, privacy_state, name, urn, creation_timestamp) " +
											 		  "VALUES (?,?,?,?,?,?,?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS = "INSERT INTO campaign_class(campaign_id, class_id) " +
															"VALUES (?,?)";
	
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN = "INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
																"VALUES (?,?,?)";
	
	/**
	 * A private inner class for the purposes of retrieving the user id and
	 * IDs when querying the users in a class.
	 * 
	 * @author John Jenkins
	 */
	private class UserAndRole {
		public int _userId;
		public String _role;
		
		UserAndRole(int userId, String role) {
			_userId = userId;
			_role = role;
		}
	}
	
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
		
		// Note: This function is a bear, but I tried to document it well and
		// give it a nice flow. May need refactoring if major changes are to
		// be made.
		
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException e) {
			throw new DataAccessException(e);
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
		String campaignName = root.query("/campaign/campaignName").get(0).getValue();
		
		Calendar now = Calendar.getInstance();
		String nowFormatted = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " +
							  now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
	
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Campaign creation and user/class hookups.");
		
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
		TransactionStatus status = transactionManager.getTransaction(def);
		
		try {
			getJdbcTemplate().update(SQL_INSERT_CAMPAIGN, 
									 new Object[] { ((awRequest.existsInToProcess(InputKeys.DESCRIPTION)) ? awRequest.getToProcessValue(InputKeys.DESCRIPTION) : "" ), 
									 				awRequest.getToProcessValue(InputKeys.XML), 
									 				awRequest.getToProcessValue(InputKeys.RUNNING_STATE), 
									 				awRequest.getToProcessValue(InputKeys.PRIVACY_STATE), 
									 				campaignName,
									 				campaignUrn,
									 				nowFormatted});
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN + "' with parameters: " +
						  ((awRequest.existsInToProcess(InputKeys.DESCRIPTION)) ? awRequest.getToProcessValue(InputKeys.DESCRIPTION) : "" ) + ", " + 
						  awRequest.getToProcessValue(InputKeys.XML) + ", " +
						  awRequest.getToProcessValue(InputKeys.RUNNING_STATE) + ", " +
						  awRequest.getToProcessValue(InputKeys.PRIVACY_STATE) + ", " +
						  campaignName + ", " +
						  campaignUrn + ", " +
						  nowFormatted, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		catch(IllegalArgumentException e) {
			transactionManager.rollback(status);
			throw new DataAccessException("Missing parameter in the toProcess map.", e);
		}
		
		// Get campaign ID.
		int campaignId;
		try {
			campaignId = getJdbcTemplate().queryForInt(SQL_GET_NEW_CAMPAIGN_ID, new Object[] { campaignUrn });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_NEW_CAMPAIGN_ID + "' with parameter: " + campaignUrn, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Get the campaign role supervisor's ID.
		int supervisorId;
		try {
			supervisorId = getJdbcTemplate().queryForInt(SQL_GET_SUPERVISOR_ID);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_SUPERVISOR_ID + "'", e);
			throw new DataAccessException(e);
		}
		
		// Get the campaign role analyst's ID.
		int analystId;
		try {
			analystId = getJdbcTemplate().queryForInt(SQL_GET_ANALYST_ID);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_ANALYST_ID + "'", e);
			throw new DataAccessException(e);
		}
		
		// Get the Author role ID.
		int authorId;
		try {
			authorId = getJdbcTemplate().queryForInt(SQL_GET_AUTHOR_ID);
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_AUTHOR_ID + "'", dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Get the campaign role participant's ID.
		int participantId;
		try {
			participantId = getJdbcTemplate().queryForInt(SQL_GET_PARTICIPANT_ID);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_PARTICIPANT_ID + "'", e);
			throw new DataAccessException(e);
		}
		
		// Get the currently logged in user's ID.
		int userId;
		try {
			userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { awRequest.getUser().getUserName() });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Make the current user the creator.
		try {
			getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, authorId });
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + authorId, dae);
			transactionManager.rollback(status);
			throw new DataAccessException(dae);
		}
		
		// Hookup to classes and users.
		String[] classes = ((String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST)).split(",");
		for(int i = 0; i < classes.length; i++) {
			// Get the current class' ID.
			int classId;
			try {
				classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { classes[i] });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + classes[i], dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
			}
			
			// Hookup the current class with the campaign.
			try {
				getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + campaignId + ", " + classId, dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
			}
			
			// TODO: Hookup all users in the current class with this campaign and
			// their respective roles.
			// If the role is author and the current user is the creator, then
			// don't attempt to insert them again as that will cause a 
			// DataAccessException.
			
			// Get the list of students in this class.
			List<?> students;
			try {
				students = getJdbcTemplate().query(SQL_GET_STUDENTS_FROM_CLASS, 
												   new Object[] { classes[i] }, 
												   new RowMapper() {
												   		@Override
												   		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
												   			return new UserAndRole(rs.getInt("user_id"), rs.getString("class_role"));
												   		}
												   });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_STUDENTS_FROM_CLASS + "' with parameter: " + classes[i], e);
				throw new DataAccessException(e);
			}
			
			// Associate the students with the campaign based on their
			// class role.
			ListIterator<?> studentsIter = students.listIterator();
			while(studentsIter.hasNext()) {
				UserAndRole uar = (UserAndRole) studentsIter.next();
				
				if("privileged".equals(uar._role)) {
					// Insert them as participants and supervisors.
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, 
												 new Object[] { uar._userId, campaignId, participantId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
									  uar._userId + ", " + campaignId + ", " + participantId, e);
						throw new DataAccessException(e);
					}
					
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { uar._userId, campaignId, supervisorId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
									  uar._userId + ", " + campaignId + ", " + supervisorId, e);
						throw new DataAccessException(e);
					}
				}
				else if("restricted".equals(uar._role)) {
					// Insert them as participants and analysts.
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { uar._userId, campaignId, participantId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
									  uar._userId + ", " + campaignId + ", " + participantId, e);
						throw new DataAccessException(e);
					}
					
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { uar._userId, campaignId, analystId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
									  uar._userId + ", " + campaignId + ", " + analystId, e);
						throw new DataAccessException(e);
					}
				}
				else {
					throw new DataAccessException("Unkown user-class role: " + uar._role);
				}
			}
		}
		
		try {
			transactionManager.commit(status);
		}
		catch(TransactionException e) {
			_logger.error("Error while attempting to rollback transaction.");
			transactionManager.rollback(status);
			throw new DataAccessException(e);
		}
	}
}