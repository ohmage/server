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

import java.util.List;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.ClassRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;


/**
 * Updates the class with the information in the request.
 * 
 * @author John Jenkins
 */
public class ClassUpdateDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(ClassUpdateDao.class);
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id = ?";
	
	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn = ?";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_ID = "SELECT id " +
															"FROM campaign_class " +
															"WHERE campaign_id = ? " +
															"AND class_id = ?";
	
	private static final String SQL_GET_CLASS_CAMPAIGNS = "SELECT campaign_id " +
														  "FROM campaign_class " +
														  "WHERE class_id = ?";
	
	private static final String SQL_GET_DEFAULT_ROLES = "SELECT user_role_id " +
														"FROM campaign_class_default_role " +
														"WHERE campaign_class_id = ? " +
														"AND user_class_role_id = ?";
	
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
	
	private static final String SQL_UPDATE_NAME = "UPDATE class " +
												  "SET name = ? " +
												  "WHERE urn = ?";
	
	private static final String SQL_UPDATE_DESCRIPTION = "UPDATE class " +
														 "SET description = ? " +
														 "WHERE urn = ?";
	
	private static final String SQL_INSERT_USER_CLASS = "INSERT INTO user_class(user_id, class_id, user_class_role_id) " +
												  		"VALUES (?, ?, ?)";
	
	private static final String SQL_INSERT_USER_ROLE_CAMPAIGN = "INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
																"VALUES (?, ?, ?)";
	
	public static final String SQL_DELETE_USER_CLASS = "DELETE FROM user_class " +
													   "WHERE user_id = ? " +
													   "AND class_id = ?";
	
	private static final String SQL_DELETE_USER_ROLE_CAMPAIGN = "DELETE FROM user_role_campaign " +
																"WHERE user_id = ? " +
																"AND campaign_id = ? " +
																"AND user_role_id = ?";
	
	/**
	 * Sets up this DAO with the DataSource to query against.
	 * 
	 * @param dataSource The DataSource to query against.
	 */
	public ClassUpdateDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Updates the class with the information in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Class update.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				updateName(awRequest);
				updateDescription(awRequest);
				updateUserListAdd(awRequest, InputKeys.USER_LIST_ADD, ClassRoleCache.ROLE_RESTRICTED);
				updateUserListRemove(awRequest);
				updateUserListAdd(awRequest, InputKeys.PRIVILEGED_USER_LIST_ADD, ClassRoleCache.ROLE_PRIVILEGED);
			}
			catch(IllegalArgumentException e) {
				// Rollback the transaction and throw a DataAccessException.
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
	 * Updates the name of the class if such a parameter exists.
	 * 
	 * @param awRequest The request with the new name.
	 */
	private void updateName(AwRequest awRequest) {
		String name;
		try {
			name = (String) awRequest.getToProcessValue(InputKeys.CLASS_NAME);
		}
		catch(IllegalArgumentException e) {
			// There was no new description.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_NAME, new Object[] { name, awRequest.getToProcessValue(InputKeys.CLASS_URN) });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_UPDATE_NAME + "' with parameters: " + name + ", " + awRequest.getToProcessValue(InputKeys.CLASS_URN), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Updates the description of the class if such a parameter exists.
	 * 
	 * @param awRequest The request with the new description.
	 */
	private void updateDescription(AwRequest awRequest) {
		String description;
		try {
			description = (String) awRequest.getToProcessValue(InputKeys.DESCRIPTION);
		}
		catch(IllegalArgumentException e) {
			// There was no new description.
			return;
		}
		
		try {
			getJdbcTemplate().update(SQL_UPDATE_DESCRIPTION, new Object[] { description, awRequest.getToProcessValue(InputKeys.CLASS_URN) });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_UPDATE_DESCRIPTION + "' with parameters: " + description + ", " + awRequest.getToProcessValue(InputKeys.CLASS_URN), e);
			throw new DataAccessException(e);
		}
	}
	
	/**
	 * Inserts the user into the class and associates them with each campaign
	 * associated with the class and gives them the default roles based on
	 * their role in the class.
	 * 
	 * @param awRequest The request for adding the users to the class.
	 * 
	 * @param inputKey The key in the request for retrieving the list of
	 * 				   users.
	 * 
	 * @param userClassRole The class role that is to be given to each user in
	 * 						the request.
	 */
	private void updateUserListAdd(AwRequest awRequest, String inputKey, String userClassRole) {
		// Get the list of users if one does exist.
		String userList;
		try {
			userList = (String) awRequest.getToProcessValue(inputKey);
		}
		catch(IllegalArgumentException e) {
			// There was no new description.
			return;
		}
		
		// Get the class ID.
		int classId;
		try {
			classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { awRequest.getToProcessValue(InputKeys.CLASS_URN) });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_CLASS_ID + "' with parameters: " + awRequest.getToProcessValue(InputKeys.CLASS_URN), e);
			throw new DataAccessException(e);
		}
		
		// Get all the campaigns for the class.
		List<?> campaignIdList;
		try {
			campaignIdList = getJdbcTemplate().query(SQL_GET_CLASS_CAMPAIGNS,
													 new Object[] { classId }, 
													 new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_CLASS_CAMPAIGNS + "' with parameter: " + classId, e);
			throw new DataAccessException(e);
		}
		
		// For each of the users in the request,
		String[] users = userList.split(",");
		for(int i = 0; i < users.length; i++) {
			// Get the current user's ID.
			int userId;
			try {
				userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { users[i] });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_GET_USER_ID + "' with parameters: " + users[i], e);
				throw new DataAccessException(e);
			}
			
			// Get the user's class-role ID.
			int userClassRoleId;
			try {
				userClassRoleId = ClassRoleCache.instance().lookup(userClassRole);
			}
			catch(CacheMissException e) {
				_logger.error("Cache didn't know about known role " + userClassRole, e);
				throw new DataAccessException(e);
			}
			
			// Insert user into the class.
			try {
				getJdbcTemplate().update(SQL_INSERT_USER_CLASS, new Object[] { userId, classId, userClassRoleId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_INSERT_USER_CLASS + "' with parameters: " + userId + ", " + classId + ", " + userClassRoleId, e);
				throw new DataAccessException(e);
			}
			
			// For each campaign associated with this class,
			ListIterator<?> campaignIdListIter = campaignIdList.listIterator();
			while(campaignIdListIter.hasNext()) {
				long campaignId = (Long) campaignIdListIter.next();
				
				// Get the campaign class ID.
				int campaignClassId;
				try {
					campaignClassId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_CLASS_ID, new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error while executing SQL '" + SQL_GET_CAMPAIGN_CLASS_ID + "' with parameters: " + campaignId + ", " + classId, e);
					throw new DataAccessException(e);
				}
				
				// Get the default roles for this campaign class association.
				List<?> defaultRoleList;
				try {
					defaultRoleList = getJdbcTemplate().query(SQL_GET_DEFAULT_ROLES, 
															  new Object[] { campaignClassId, userClassRoleId }, 
															  new SingleColumnRowMapper());
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error while executing SQL '" + SQL_GET_DEFAULT_ROLES + "' with parameters: " + campaignClassId + ", " + userClassRoleId, e);
					throw new DataAccessException(e);
				}
				
				// For each of the default roles,
				ListIterator<?> defaultRoleListIter = defaultRoleList.listIterator();
				while(defaultRoleListIter.hasNext()) {
					int userRoleId = (Integer) defaultRoleListIter.next();
					
					// Associate the user with the campaign and the current,
					// default role.
					try {
						getJdbcTemplate().update(SQL_INSERT_USER_ROLE_CAMPAIGN,  new Object[] { userId, campaignId, userRoleId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error while executing SQL '" + SQL_INSERT_USER_ROLE_CAMPAIGN + "' with parameters: " + userId + ", " + campaignId + ", " + userRoleId, e);
						throw new DataAccessException(e);
					}
				}
			}
		}
	}
	
	/**
	 * Removes the users from the class and, if not associated with a campaign
	 * in that class in any other class, also removes their campaign
	 * association.
	 * 
	 * @param awRequest The request with the information about which users to
	 * 					remove.
	 */
	private void updateUserListRemove(AwRequest awRequest) {
		String userList;
		try {
			userList = (String) awRequest.getToProcessValue(InputKeys.USER_LIST_REMOVE);
		}
		catch(IllegalArgumentException e) {
			// There was no new description.
			return;
		}
		
		// Get the class ID.
		int classId;
		try {
			classId = getJdbcTemplate().queryForInt(SQL_GET_CLASS_ID, new Object[] { awRequest.getToProcessValue(InputKeys.CLASS_URN) });
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_CLASS_ID + "' with parameters: " + awRequest.getToProcessValue(InputKeys.CLASS_URN), e);
			throw new DataAccessException(e);
		}
		
		// Get the Author role ID.
		int authorId;
		try {
			authorId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_AUTHOR);
		}
		catch(CacheMissException dae) {
			_logger.error("Cache doesn't know about known role " + CampaignRoleCache.ROLE_AUTHOR, dae);
			throw new DataAccessException(dae);
		}
		
		// Get all the campaigns for the class.
		List<?> campaignIdList;
		try {
			campaignIdList = getJdbcTemplate().query(SQL_GET_CLASS_CAMPAIGNS,
												   new Object[] { classId }, 
												   new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_CLASS_CAMPAIGNS + "' with parameter: " + classId, e);
			throw new DataAccessException(e);
		}
		
		// For each of the users in the request,
		String[] users = userList.split(",");
		for(int i = 0; i < users.length; i++) {
			// Get the current user's ID.
			int userId;
			try {
				userId = getJdbcTemplate().queryForInt(SQL_GET_USER_ID, new Object[] { users[i] });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_GET_USER_ID + "' with parameters: " + users[i], e);
				throw new DataAccessException(e);
			}
			
			// Remove the user from the class.
			try {
				getJdbcTemplate().update(SQL_DELETE_USER_CLASS, new Object[] { userId, classId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_DELETE_USER_CLASS + "' with parameters: " + userId + ", " + classId, e);
				throw new DataAccessException(e);
			}
			
			// For each campaign associated with this class,
			ListIterator<?> campaignIdListIter = campaignIdList.listIterator();
			while(campaignIdListIter.hasNext()) {
				long campaignId = (Long) campaignIdListIter.next();
				
				// Get all roles for a campaign for all classes except the one
				// being removed.
				List<?> userRolesForCampaign;
				try {
					userRolesForCampaign = getJdbcTemplate().query(SQL_GET_USER_ROLES_FOR_CAMPAIGN_FOR_ALL_CLASSES,
																   new Object[] { campaignId, classId, userId },
																   new SingleColumnRowMapper());
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USER_ROLES_FOR_CAMPAIGN_FOR_ALL_CLASSES + "' with parameters: " +
								  campaignId + ", " + classId + ", " + userId);
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
						getJdbcTemplate().update(SQL_DELETE_USER_ROLE_CAMPAIGN, new Object[] { userId, campaignId, nextRole });
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_DELETE_USER_ROLE_CAMPAIGN + "' with parameters: " +
									  userId + ", " + campaignId + ", " + nextRole);
						throw new DataAccessException(e);
					}
				}
			}
		}
	}
}
