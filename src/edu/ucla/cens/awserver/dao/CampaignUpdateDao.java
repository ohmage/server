package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Updates a campaign's information. Certain aspects cannot be modified if the
 * campaign has already begun, such as the XML itself.
 * 
 * @author John Jenkins
 */
public class CampaignUpdateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignUpdateDao.class);
	
	private static final String SQL_GET_CAMPAIGN_ID = "SELECT id " +
													  "FROM campaign " +
													  "WHERE urn = ?";

	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn = ?";
	
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
	
	private static final String SQL_GET_PRIVILEGED_ID = "SELECT id " +
														"FROM user_class_role " +
														"WHERE role = 'privileged'";

	private static final String SQL_GET_RESTRICTED_ID = "SELECT id " +
														"FROM user_class_role " +
														"WHERE role = 'restricted'";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_ID = "SELECT id " +
															"FROM campaign_class cc " +
															"WHERE campaign_id = ? " +
															"AND class_id = ?";
	
	private static final String SQL_GET_IS_ROLE = "SELECT count(*) " +
												  "FROM user u, user_role ur, user_role_campaign urc, campaign c " +
												  "WHERE u.login_id = ? " +
												  "AND u.id = urc.user_id " +
												  "AND ur.role = ? " +
												  "AND ur.id = urc.user_role_id " +
												  "AND c.urn = ? " +
												  "AND c.id = urc.campaign_id";
	
	private static final String SQL_GET_USER_CAN_MODIFY = "SELECT count(*) " +
														  "FROM user u, user_role ur, user_role_campaign urc, campaign c " +
														  "WHERE u.login_id = ? " +
														  "AND u.id = urc.user_id " +
														  "AND ur.role in ('supervisor', 'author') " +
														  "AND ur.id = urc.user_role_id " +
														  "AND c.urn = ? " +
														  "AND c.id = urc.campaign_id";
	
	private static final String SQL_GET_NUM_UPLOADS = "SELECT count(*) " +
													  "FROM campaign c, survey_response sr " +
													  "WHERE c.urn = ? " +
													  "AND c.id = sr.campaign_id";
	
	private static final String SQL_GET_CLASSES_FROM_CAMPAIGN = "SELECT cl.urn " +
																"FROM class cl, campaign ca, campaign_class cc " +
																"WHERE ca.urn = ? " +
																"AND ca.id = cc.campaign_id " +
																"AND cl.id = cc.class_id";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES = "SELECT user_role_id " +
																	   "FROM campaign_class_default_role " +
																	   "WHERE campaign_class_id = ? " +
																	   "AND user_class_role_id = ?";
	
	private static final String SQL_GET_USERS_FROM_CLASS = "SELECT user_id, user_class_role_id " +
														   "FROM user_class " +
														   "WHERE class_id = ?";
	
	private static final String SQL_GET_USER_ROLES_FOR_CAMPAIGN_FOR_ALL_CLASSES = "SELECT id " +
																				  "FROM user_role " +
																				  "WHERE id not in (" +
																				  	"SELECT distinct(ccdr.user_role_id) " +
																				  	"FROM campaign_class cc, user_class uc, campaign_class_default_role ccdr " +
																				  	"WHERE cc.campaign_id = ? " +
																				  	"AND cc.class_id != ? " +
																				  	"AND uc.user_id = ? " +
																				  	"AND uc.class_id = cc.class_id " +
																				  	"AND ccdr.user_class_role_id = uc.user_class_role_id" +
																				  ")";
	
	private static final String SQL_UPDATE_RUNNING_STATE = "UPDATE campaign " +
														   "SET running_state = ? " +
														   "WHERE urn = ?";
	
	private static final String SQL_UPDATE_PRIVACY_STATE = "UPDATE campaign " +
														   "SET privacy_state = ? " +
														   "WHERE urn = ?";
	
	private static final String SQL_UPDATE_DESCRIPTION = "UPDATE campaign " +
														 "SET description = ? " +
														 "WHERE urn = ?";
	
	private static final String SQL_UPDATE_XML = "UPDATE campaign " +
												 "SET xml = ? " +
												 "WHERE urn = ?";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS = "INSERT INTO campaign_class(campaign_id, class_id) " +
															"VALUES (?, ?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE = "INSERT INTO campaign_class_default_role(campaign_class_id, user_class_role_id, user_role_id) " +
																		 "VALUES (?, ?, ?)";
	
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN = "INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
																"VALUES (?, ?, ?)";
	
	private static final String SQL_DELETE_CAMPAIGN_CLASS = "DELETE FROM campaign_class " +
															"WHERE campaign_id = ? " +
															"AND class_id = ?";
	
	private static final String SQL_DELETE_USER_ROLE_CAMPAIGN = "DELETE FROM user_role_campaign " +
																"WHERE user_id = ? " +
																"AND campaign_id = ? " +
																"AND user_role_id = ?";
	
	/**
	 * A private inner class for the purposes of retrieving the user id and
	 * IDs when querying the users in a class.
	 * 
	 * @author John Jenkins
	 */
	private class UserAndRole {
		public int _userId;
		public int _roleId;
		
		UserAndRole(int userId, int roleId) {
			_userId = userId;
			_roleId = roleId;
		}
	}
	
	/**
	 * Creates a DAO for updating an existing campaign.
	 * 
	 * @param dataSource
	 */
	public CampaignUpdateDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Begins a transaction, updates the applicable components, and commits
	 * the transaction if there weren't any errors. If there were errors, it
	 * will roll the transaction back and throw a DataAccessException.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		if(! userCanModifyCampaign(awRequest)) {
			_logger.info("User has insufficient permissions to modify this campaign.");
			awRequest.setFailedRequest(true);
			return;
		}
		
		// Begin transaction
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Campaign update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				updateRunningState(awRequest);
				updatePrivacyState(awRequest);
				updateDescription(awRequest);
				updateXml(awRequest);
				updateClassList(awRequest);
			}
			catch(IllegalArgumentException e) {
				// Rollback transaction and throw a DataAccessException.
				_logger.error("Error while executing the update.", e);
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			catch(DataAccessException e) {
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			
			// Commit transaction.
			transactionManager.commit(status);
		}
		catch(TransactionException e) {
			_logger.error("Error while rolling back the transaction.", e);
			awRequest.setFailedRequest(true);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Returns true if the user is a supervisor or an author.
	 * 
	 * @param awRequest The request containing the user and campaign's
	 * 					information.
	 * 
	 * @return Returns true iff the user is allowed to modify any part of this
	 * 		   campaign except the XML.
	 */
	private boolean userCanModifyCampaign(AwRequest awRequest) {
		try {
			return getJdbcTemplate().queryForInt(SQL_GET_USER_CAN_MODIFY, 
												 new Object[] { awRequest.getUser().getUserName(), awRequest.getCampaignUrn() }) != 0;
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_CAN_MODIFY + "' with parameters: " + 
						  awRequest.getUser().getUserName() + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks the user's permissions and returns true if the user is a
	 * supervisor for the campaign or is an author and there are no uploads.
	 * 
	 * @param awRequest The request containing the user and campaign's 
	 * 					information.
	 * 
	 * @return True iff the user is allowed to modify the campaign's XML.
	 */
	private boolean userCanModifyCampaignXml(AwRequest awRequest) {
		boolean userIsSupervisor = false;
		try {
			userIsSupervisor = (getJdbcTemplate().queryForInt(SQL_GET_IS_ROLE, 
					new Object[] { awRequest.getUser().getUserName(), "supervisor", awRequest.getCampaignUrn() }) != 0);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_IS_ROLE + "' with parameters: " + 
						  awRequest.getUser().getUserName() + ", supervisor, " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		
		boolean userIsAuthorAndNoUploads = false;
		try {
			if(getJdbcTemplate().queryForInt(SQL_GET_IS_ROLE, 
					new Object[] { awRequest.getUser().getUserName(), "author", awRequest.getCampaignUrn() }) != 0) {
				try {
					userIsAuthorAndNoUploads = (getJdbcTemplate().queryForInt(SQL_GET_NUM_UPLOADS, 
							new Object [] { awRequest.getCampaignUrn() }) == 0);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_NUM_UPLOADS + "' with parameters: " + awRequest.getCampaignUrn(), e);
					throw new DataAccessException(e);
				}
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_IS_ROLE + "' with parameters: " + 
						  awRequest.getUser().getUserName() + ", author, " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		
		return userIsSupervisor || userIsAuthorAndNoUploads;
	}
	
	/**
	 * Checks that a valid running state exists in the request. If one does,
	 * it updates the campaign; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new running
	 * 					state.
	 */
	private void updateRunningState(AwRequest awRequest) {
		String runningState;
		try {
			runningState = (String) awRequest.getToProcessValue(InputKeys.RUNNING_STATE);
		}
		catch(IllegalArgumentException e) {
			// There was no running state to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_RUNNING_STATE, new Object[] { runningState, awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_RUNNING_STATE + "' with parameters: " + 
						  runningState + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks that a valid privacy state exists in the request. If one does,
	 * it updates the campaign; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new privacy
	 * 					state.
	 */
	private void updatePrivacyState(AwRequest awRequest) {
		String privacyState;
		try {
			privacyState = (String) awRequest.getToProcessValue(InputKeys.PRIVACY_STATE);
		}
		catch(IllegalArgumentException e) {
			// There was no privacy state to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { privacyState, awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " + privacyState + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks that a valid description exists in the request. If one does, it
	 * updates the campaign; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new
	 * 					description.
	 */
	private void updateDescription(AwRequest awRequest) {
		String description;
		try {
			description = (String) awRequest.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch(IllegalArgumentException e) {
			// There was no description to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { description, awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_DESCRIPTION + "' with parameters: " + description + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks that a valid XML exists in the request and that the user has
	 * sufficient permissions to modify the XML. If one does, it updates the
	 * XML; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new XML.
	 */
	private void updateXml(AwRequest awRequest) {
		if(! userCanModifyCampaignXml(awRequest)) {
			throw new DataAccessException("User is only an author and responses exist; therefore, they are not allowed to modify the XML.");
		}
		
		String xml;
		try {
			xml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException e) {
			// There was no XML to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_XML, new Object[] { xml, awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_XML + "' with parameters: " + xml + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Updates the list of classes by checking for which classes have been
	 * added and adding an association into the database. Also, it adds the
	 * members of those classes to the campaign.
	 * 
	 * Then, it checks the new list of classes for classes that it should no
	 * longer be associated with and removes the class-campaign association
	 * from the database. Finally, it removes the association of the
	 * individual students with the campaign.
	 * 
	 * @param awRequest The request that potentially contains the new list of
	 * 					classes. This new list may add classes by containing
	 * 					classes that this campaign wasn't associated with
	 * 					before, as well as remove classes by not having them
	 * 					in this list. A lack of a class in this list indicates
	 * 					that it should no longer be associated with this
	 * 					campaign.
	 */
	private void updateClassList(AwRequest awRequest) {
		// The new list of classes.
		String[] newClassList;
		try {
			newClassList = ((String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST)).split(",");
		}
		catch(IllegalArgumentException e) {
			// There were no classes in the request.
			return;
		}
		
		// The current list of classes.
		List<?> currentClassList;
		try {
			currentClassList = getJdbcTemplate().query(SQL_GET_CLASSES_FROM_CAMPAIGN, 
													   new Object[] { awRequest.getCampaignUrn() }, 
													   new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CLASSES_FROM_CAMPAIGN + "' with parameter: " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		
		// Get the campaign's ID.
		int campaignId;
		try {
			campaignId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_ID, new Object[] { awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ID + "' with parameter: " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
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
		
		// Get the ID for privileged users.
		int privilegedId;
		try {
			privilegedId = getJdbcTemplate().queryForInt(SQL_GET_PRIVILEGED_ID);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_PRIVILEGED_ID + "'", e);
			throw new DataAccessException(e);
		}
		
		// Get the ID for restricted users.
		int restrictedId;
		try {
			restrictedId = getJdbcTemplate().queryForInt(SQL_GET_RESTRICTED_ID);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_RESTRICTED_ID + "'", e);
			throw new DataAccessException(e);
		}
		
		// Find the new classes to add to the list of classes.
		for(int i = 0; i < newClassList.length; i++) {
			if(! currentClassList.contains(newClassList[i])) {
				// Get the new class' ID.
				int classId;
				try {
					classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { newClassList[i] });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parametes: " + newClassList[i], e);
					throw new DataAccessException(e);
				}
				
				// Add an entry in the campaign_class table.
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS + "' with parameters: " + campaignId + ", " + classId, e);
					throw new DataAccessException(e);
				}
				
				// Get the ID of that newly inserted row.
				int campaignClassId;
				try {
					campaignClassId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_CLASS_ID, new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_CLASS_ID + "' with parameters: " + campaignId + ", " + classId, dae);
					throw new DataAccessException(dae);
				}
				
				// Insert the default campaign_class_default_role
				// relationships for privileged users.
				// TODO: This should be a parameter in the API.
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, privilegedId, supervisorId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + privilegedId + ", " + supervisorId, dae);
					throw new DataAccessException(dae);
				}
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, privilegedId, participantId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + privilegedId + ", " + participantId, dae);
					throw new DataAccessException(dae);
				}
				
				// Insert the default campaign_class_default_role
				// relationships for restricted users.
				// TODO: This should be a parameter in the API.
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, restrictedId, analystId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + restrictedId + ", " + supervisorId, dae);
					throw new DataAccessException(dae);
				}
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, restrictedId, participantId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + restrictedId + ", " + participantId, dae);
					throw new DataAccessException(dae);
				}
				
				// Get the list of students in this class.
				List<?> users;
				try {
					users = getJdbcTemplate().query(SQL_GET_USERS_FROM_CLASS, 
													   new Object[] { classId }, 
													   new RowMapper() {
													   		@Override
													   		public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
													   			return new UserAndRole(rs.getInt("user_id"), rs.getInt("user_class_role_id"));
													   		}
													   });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USERS_FROM_CLASS + "' with parameter: " + classId, e);
					throw new DataAccessException(e);
				}
				
				_logger.debug("usersLength = " + users.size());
				
				// Associate the students with the campaign based on their
				// class role.
				ListIterator<?> usersIter = users.listIterator();
				while(usersIter.hasNext()) {
					UserAndRole uar = (UserAndRole) usersIter.next();
					
					_logger.debug("User id: " + uar._userId);
					_logger.debug("Role id: " + uar._roleId);
					
					// Get the list of default roles for a user in this class
					// associated with this campaign.
					List<?> defaultRoles;
					try {
						defaultRoles = getJdbcTemplate().query(SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES, new Object[] { campaignClassId, uar._roleId }, new SingleColumnRowMapper());
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES + "' with parameters: " + campaignClassId + ", " + uar._roleId, e);
						throw new DataAccessException(e);
					}
					
					// For each of these default roles
					ListIterator<?> defaultRolesIter = defaultRoles.listIterator();
					while(defaultRolesIter.hasNext()) {
						int defaultRole = (Integer) defaultRolesIter.next();
						
						_logger.debug("Inserting user " + uar._userId + " with role " + defaultRole + " into campaign " + campaignId);
						
						// Associate the user with the campaign and the
						// default role.
						try {
							getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { uar._userId, campaignId, defaultRole });
						}
						catch(org.springframework.dao.DataIntegrityViolationException e) {
							_logger.info("Attempting to add a user with the ID '" + uar._userId + "' into the user_role_campaign table with a role ID of '" +
									defaultRole + "'; however such an association already exists for campaign '" + campaignId + "'.");
						}
						catch(org.springframework.dao.DataAccessException e) {
							_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + 
										  uar._userId + ", " + campaignId + ", " + defaultRole, e);
							throw new DataAccessException(e);
						}
					}
				}
			}
			
			// Remove the item from the list so that after all this processing
			// is done we know which classes are associated with this campaign
			// but no longer should be.
			try {
				currentClassList.remove(newClassList[i]);
			}
			catch(UnsupportedOperationException e) {
				// TODO: This can probably be removed if it hasn't happened
				// yet. it was a safety check in case JdbcTemplate.query()
				// returned an immutable list.
				_logger.error("JdbcTemplate.query() returns an immutable list, so this class needs to be fixed ASAP.");
				throw new DataAccessException(e);
			}
		}
		
		// Find the classes that have been removed and remove the association.
		ListIterator<?> classesToRemoveIter = currentClassList.listIterator();
		while(classesToRemoveIter.hasNext()) {
			String currentClass = (String) classesToRemoveIter.next();
			
			// Get the class' ID.
			int classId;
			try {
				classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { currentClass });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_ID + "' with parameter: " + currentClass, e);
				throw new DataAccessException(e);
			}
			
			// Remove the campaign-class reference in the database.
			try {
				getJdbcTemplate().update(SQL_DELETE_CAMPAIGN_CLASS, new Object[] { campaignId, classId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_DELETE_CAMPAIGN_CLASS + "' with parameters: " + campaignId + ", " + classId, e);
				throw new DataAccessException(e);
			}
			
			// Get the list of students in this class.
			List<?> users;
			try {
				users = getJdbcTemplate().query(SQL_GET_USERS_FROM_CLASS, 
												new Object[] { classId }, 
												new RowMapper() {
													@Override
													public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
														return new UserAndRole(rs.getInt("user_id"), rs.getInt("user_class_role_id"));
													}
												});
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USERS_FROM_CLASS + "' with parameter: " + classId, e);
				throw new DataAccessException(e);
			}
			
			// Remove the user-role-campaign references.
			ListIterator<?> usersIter = users.listIterator();
			while(usersIter.hasNext()) {
				UserAndRole currentUser = (UserAndRole) usersIter.next();
				
				// Get all roles for a campaign for all classes except the one
				// being removed.
				List<?> userRolesForCampaign;
				try {
					userRolesForCampaign = getJdbcTemplate().query(SQL_GET_USER_ROLES_FOR_CAMPAIGN_FOR_ALL_CLASSES,
																   new Object[] { campaignId, classId, currentUser._userId },
																   new SingleColumnRowMapper());
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USER_ROLES_FOR_CAMPAIGN_FOR_ALL_CLASSES + "' with parameters: " +
								  campaignId + ", " + classId + ", " + currentUser._userId);
					throw new DataAccessException(e);
				}
				
				// Remove the non-existant user_role_campaigns except author.
				ListIterator<?> userRolesForCampaignIter = userRolesForCampaign.listIterator();
				while(userRolesForCampaignIter.hasNext()) {
					int nextRole = (Integer) userRolesForCampaignIter.next();
					
					if(nextRole == authorId) {
						// Don't delete that they are the auther no matter if
						// they are completely dissassociated with the
						// campaign.
						continue;
					}
					
					try {
						getJdbcTemplate().update(SQL_DELETE_USER_ROLE_CAMPAIGN, new Object[] { currentUser._userId, campaignId, nextRole });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + "' with parameters: " +
									  currentUser._userId + ", " + campaignId + ", " + nextRole);
						throw new DataAccessException(e);
					}
				}
			}
		}
	}
}