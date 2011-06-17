/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.dao;

import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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

	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE username = ?";
	
	private static final String SQL_GET_CLASS_ROLE_ID = "SELECT id " +
														"FROM user_role " +
														"WHERE role = ?";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_ID = "SELECT id " +
															"FROM campaign_class cc " +
															"WHERE campaign_id = ? " +
															"AND class_id = ?";

	private static final String SQL_GET_SURVEY_RESPONSES_EXIST = "SELECT EXISTS(" +
																	"SELECT sr.id " +
																	"FROM campaign c, survey_response sr " +
																	"WHERE c.urn = ? " +
																	"AND c.id = sr.campaign_id" +
																")";
	
	private static final String SQL_GET_USER_CAN_MODIFY = "SELECT count(*) " +
														  "FROM user u, user_role ur, user_role_campaign urc, campaign c " +
														  "WHERE u.username = ? " +
														  "AND u.id = urc.user_id " +
														  "AND ur.role in ('" + CampaignRoleCache.ROLE_SUPERVISOR + "', '" + CampaignRoleCache.ROLE_AUTHOR + "') " +
														  "AND ur.id = urc.user_role_id " +
														  "AND c.urn = ? " +
														  "AND c.id = urc.campaign_id";
	
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
														   "SET running_state_id = ? " +
														   "WHERE urn = ?";
	
	private static final String SQL_UPDATE_PRIVACY_STATE = "UPDATE campaign " +
														   "SET privacy_state_id = ? " +
														   "WHERE urn = ?";
	
	private static final String SQL_UPDATE_DESCRIPTION = "UPDATE campaign " +
														 "SET description = ? " +
														 "WHERE urn = ?";
	
	private static final String SQL_UPDATE_XML_AND_CREATION_TIMESTAMP = "UPDATE campaign " +
												 "SET xml = ?, creation_timestamp = ? " +
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
				
				// Be sure to do this specifically after adding the classes as
				// it may be that they want to add a class and then
				// immediately remove users from that class from the campaign.
				updateUserRoleList(awRequest, InputKeys.USER_ROLE_LIST_ADD, true);
				updateUserRoleList(awRequest, InputKeys.USER_ROLE_LIST_REMOVE, false);
			}
			catch(IllegalArgumentException e) {
				// Rollback transaction and throw a DataAccessException.
				_logger.error("Error while executing the update.", e);
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			catch(CacheMissException e) {
				_logger.error("Error while reading from the cache.", e);
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw new DataAccessException(e);
			}
			catch(DataAccessException e) {
				transactionManager.rollback(status);
				awRequest.setFailedRequest(true);
				throw e;
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
		try {
			if(getJdbcTemplate().queryForInt(SQL_GET_SURVEY_RESPONSES_EXIST, new Object[] { awRequest.getCampaignUrn() }) != 0) {
				return false;
			}
			else {
				return true;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_SURVEY_RESPONSES_EXIST + "' with parameters: " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks that a valid running state exists in the request. If one does,
	 * it updates the campaign; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new running
	 * 					state.
	 * 
	 * @throws CacheMissException Thrown if there is an unknown running state
	 * 							  in the request. 
	 */
	private void updateRunningState(AwRequest awRequest) throws CacheMissException {
		String runningState;
		try {
			runningState = (String) awRequest.getToProcessValue(InputKeys.RUNNING_STATE);
		}
		catch(IllegalArgumentException e) {
			// There was no running state to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_RUNNING_STATE, new Object[] { CampaignRunningStateCache.instance().lookup(runningState), awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_RUNNING_STATE + "' with parameters: " + 
					CampaignRunningStateCache.instance().lookup(runningState) + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		catch(InvalidParameterException e) {
			_logger.error("Unknown campaign running state in cache: " + runningState, e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Checks that a valid privacy state exists in the request. If one does,
	 * it updates the campaign; if not, it just quits as it isn't required.
	 * 
	 * @param awRequest The request that potentially contains the new privacy
	 * 					state.
	 * 
	 * @throws CacheMissException Thrown if there is an unknown privacy state
	 * 							  in the request. 
	 */
	private void updatePrivacyState(AwRequest awRequest) throws CacheMissException {
		String privacyState;
		try {
			privacyState = (String) awRequest.getToProcessValue(InputKeys.PRIVACY_STATE);
		}
		catch(IllegalArgumentException e) {
			// There was no privacy state to update.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_PRIVACY_STATE, new Object[] { CampaignPrivacyStateCache.instance().lookup(privacyState), awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_PRIVACY_STATE + "' with parameters: " + 
					CampaignPrivacyStateCache.instance().lookup(privacyState) + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		catch(InvalidParameterException e) {
			_logger.error("Unknown campaign privacy state in cache: " + privacyState, e);
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
			throw new DataAccessException("Responses exist; therefore, the requester is not allowed to modify the XML.");
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
			getJdbcTemplate().update(SQL_UPDATE_XML_AND_CREATION_TIMESTAMP, new Object[] { xml, new Timestamp(Calendar.getInstance().getTimeInMillis()), awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_UPDATE_XML_AND_CREATION_TIMESTAMP + "' with parameters: " + xml + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Ensures that the currently logged in user can add users with the given
	 * permissions to the campaign and, if so, adds the entries to the
	 * database.
	 * 
	 * @param awRequest The request with the campaign's URN, the user's info
	 * 					and the list of user-role combinations to add.
	 */
	private void updateUserRoleList(AwRequest awRequest, String key, boolean add) {
		// Get the list of user-roles from the request if it exists.
		String userRoleList;
		try {
			userRoleList = (String) awRequest.getToProcessValue(key);
		}
		catch(IllegalArgumentException e) {
			// There was no userRoleAddList to update.
			return;
		}
		
		// Get the campaign ID for this request.
		int campaignId;
		try {
			campaignId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_ID, new Object[] { awRequest.getCampaignUrn() });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ID + "' with parameter: " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		
		// Handle each user-role pair individually.
		String[] userRoleArray = userRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < userRoleArray.length; i++) {
			String[] userAndRole = userRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			// Get the user's ID for this particular pair.
			int userId;
			try {
				userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { userAndRole[0] });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USER_ID + "' with parameter: " + userAndRole[0], e);
				throw new DataAccessException(e);
			}
			
			// Get the role for this particular pair.
			// Here's a thought, we will be accessing the database many times
			// to get the same information. We could instead just create a
			// lookup table first and then get this information.
			int roleId;
			try {
				roleId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ROLE_ID, new Object[] { userAndRole[1] });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_CLASS_ROLE_ID + "' with parameter: " + userAndRole[1], e);
				throw new DataAccessException(e);
			}
			
			// If this is an add request, add the user and role combination to
			// the user_role_campaign table.
			if(add) {
				try {
					getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, roleId });
				}
				catch(org.springframework.dao.DataIntegrityViolationException e) {
					_logger.info("The user already had the given role. Ignoring.");
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + roleId, e);
					throw new DataAccessException(e);
				}
			}
			// If this is a remove request, attempt to remove the user-role
			// combination from the user_role_campaign table.
			else {
				try {
					getJdbcTemplate().update(SQL_DELETE_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, roleId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + roleId, e);
					throw new DataAccessException(e);
				}
			}
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
			newClassList = ((String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST)).split(InputKeys.LIST_ITEM_SEPARATOR);
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
			supervisorId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_SUPERVISOR);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + CampaignRoleCache.ROLE_SUPERVISOR, e);
			throw new DataAccessException(e);
		}
		
		// Get the campaign role analyst's ID.
		int analystId;
		try {
			analystId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_ANALYST);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + CampaignRoleCache.ROLE_ANALYST, e);
			throw new DataAccessException(e);
		}
		
		// Get the Author role ID.
		int authorId;
		try {
			authorId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_AUTHOR);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + CampaignRoleCache.ROLE_AUTHOR, e);
			throw new DataAccessException(e);
		}
		
		// Get the campaign role participant's ID.
		int participantId;
		try {
			participantId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_PARTICIPANT);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + CampaignRoleCache.ROLE_PARTICIPANT, e);
			throw new DataAccessException(e);
		}
		
		// Get the ID for privileged users.
		int privilegedId;
		try {
			privilegedId = ClassRoleCache.instance().lookup(ClassRoleCache.ROLE_PRIVILEGED);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + ClassRoleCache.ROLE_PRIVILEGED, e);
			throw new DataAccessException(e);
		}
		
		// Get the ID for restricted users.
		int restrictedId;
		try {
			restrictedId = ClassRoleCache.instance().lookup(ClassRoleCache.ROLE_RESTRICTED);
		}
		catch(CacheMissException e) {
			_logger.error("The cache doesn't know about known role " + ClassRoleCache.ROLE_RESTRICTED, e);
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
			currentClassList.remove(newClassList[i]);
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
