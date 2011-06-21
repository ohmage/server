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
 * Adds the campaign to the database.
 * 
 * @author John Jenkins
 */
public class CampaignCreationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignCreationDao.class);
	
	private static final String SQL_GET_CAMPAIGN_ID = "SELECT id " +
													  "FROM campaign " +
													  "WHERE urn = ?";

	private static final String SQL_GET_CLASS_ID = "SELECT id " +
												   "FROM class " +
												   "WHERE urn = ?";
	
	private static final String SQL_GET_USER_ID = "SELECT id " +
												  "FROM user " +
												  "WHERE login_id = ?";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_ID = "SELECT id " +
															"FROM campaign_class cc " +
															"WHERE campaign_id = ? " +
															"AND class_id = ?";
	
	private static final String SQL_GET_USERS_FROM_CLASS = "SELECT user_id, user_class_role_id " +
	  													   "FROM user_class " +
	  													   "WHERE class_id = ?";
	
	private static final String SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES = "SELECT user_role_id " +
																	   "FROM campaign_class_default_role " +
																	   "WHERE campaign_class_id = ? " +
																	   "AND user_class_role_id = ?";
	
	private static final String SQL_INSERT_CAMPAIGN = "INSERT INTO campaign(description, xml, running_state_id, privacy_state_id, name, urn, creation_timestamp) " +
											 		  "VALUES (?,?,?,?,?,?,?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS = "INSERT INTO campaign_class(campaign_id, class_id) " +
															"VALUES (?,?)";
	
	private static final String SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE = "INSERT INTO campaign_class_default_role(campaign_class_id, user_class_role_id, user_role_id) " +
																		 "VALUES (?,?,?)";
	
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
		public int _roleId;
		
		UserAndRole(int userId, int roleId) {
			_userId = userId;
			_roleId = roleId;
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
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue().trim();
		String campaignName = root.query("/campaign/campaignName").get(0).getValue().trim();
		
		Calendar now = Calendar.getInstance();
		String nowFormatted = now.get(Calendar.YEAR) + "-" + now.get(Calendar.MONTH) + "-" + now.get(Calendar.DAY_OF_MONTH) + " " +
							  now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND);
	
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Campaign creation and user/class hookups.");
		
		try {
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def); 
		
			try {
				getJdbcTemplate().update(SQL_INSERT_CAMPAIGN, 
										 new Object[] { ((awRequest.existsInToProcess(InputKeys.DESCRIPTION)) ? awRequest.getToProcessValue(InputKeys.DESCRIPTION) : "" ), 
										 				awRequest.getToProcessValue(InputKeys.XML), 
										 				CampaignRunningStateCache.instance().lookup((String) awRequest.getToProcessValue(InputKeys.RUNNING_STATE)), 
										 				CampaignPrivacyStateCache.instance().lookup((String) awRequest.getToProcessValue(InputKeys.PRIVACY_STATE)), 
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
			catch(CacheMissException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Unknown running or privacy state in the cache.", e);
			}
			catch(IllegalArgumentException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Missing parameter in the toProcess map.", e);
			}
			
			// Get campaign ID.
			int campaignId;
			try {
				campaignId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_ID, new Object[] { campaignUrn });
			}
			catch(org.springframework.dao.DataAccessException dae) {
				_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ID + "' with parameter: " + campaignUrn, dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
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
			
			// Get the campaign role author's ID.
			int authorId;
			try {
				authorId = CampaignRoleCache.instance().lookup(CampaignRoleCache.ROLE_AUTHOR);
			}
			catch(CacheMissException dae) {
				_logger.error("The cache doesn't know about known role " + CampaignRoleCache.ROLE_AUTHOR, dae);
				transactionManager.rollback(status);
				throw new DataAccessException(dae);
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
			String[] classes = ((String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST)).split(InputKeys.LIST_ITEM_SEPARATOR);
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
				
				// Get the ID of that newly inserted row.
				int campaignClassId;
				try {
					campaignClassId = getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_CLASS_ID, new Object[] { campaignId, classId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_CLASS_ID + "' with parameters: " + campaignId + ", " + classId, dae);
					transactionManager.rollback(status);
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
					transactionManager.rollback(status);
					throw new DataAccessException(dae);
				}
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, privilegedId, participantId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + privilegedId + ", " + participantId, dae);
					transactionManager.rollback(status);
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
					transactionManager.rollback(status);
					throw new DataAccessException(dae);
				}
				try {
					getJdbcTemplate().update(SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE, new Object[] { campaignClassId, restrictedId, participantId });
				}
				catch(org.springframework.dao.DataAccessException dae) {
					_logger.error("Error executing SQL '" + SQL_INSERT_CAMPAIGN_CLASS_DEFAULT_ROLE + "' with parameters: " + campaignClassId + ", " + restrictedId + ", " + participantId, dae);
					transactionManager.rollback(status);
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
					transactionManager.rollback(status);
					throw new DataAccessException(e);
				}
				
				// Associate the students with the campaign based on their
				// class role.
				ListIterator<?> usersIter = users.listIterator();
				while(usersIter.hasNext()) {
					UserAndRole uar = (UserAndRole) usersIter.next();
					
					// Get the list of default roles for a user in this class
					// associated with this campaign.
					List<?> defaultRoles;
					try {
						defaultRoles = getJdbcTemplate().query(SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES, new Object[] { campaignClassId, uar._roleId }, new SingleColumnRowMapper());
					}
					catch(org.springframework.dao.DataAccessException e) {
						_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_CLASS_DEFAULT_ROLES + "' with parameters: " + campaignClassId + ", " + uar._roleId, e);
						transactionManager.rollback(status);
						throw new DataAccessException(e);
					}
					
					// For each of these default roles
					ListIterator<?> defaultRolesIter = defaultRoles.listIterator();
					while(defaultRolesIter.hasNext()) {
						int defaultRole = (Integer) defaultRolesIter.next();
						
						if((uar._userId == userId) && (defaultRole == authorId)) {
							// This already happened above.
							continue;
						}
						
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
			
			// Try to commit everything and finish.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				_logger.error("Error while attempting to commit the transaction. Attempting to rollback.");
				transactionManager.rollback(status);
				throw new DataAccessException(e);
			}
		}
		catch(TransactionException e) {
			_logger.error("Error while attempting to rollback transaction.", e);
			throw new DataAccessException(e);
		}
	}
}
