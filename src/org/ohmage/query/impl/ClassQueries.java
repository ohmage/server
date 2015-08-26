/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.Clazz.Role;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.ICampaignClassQueries;
import org.ohmage.query.IClassQueries;
import org.ohmage.query.IUserCampaignClassQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserClassQueries;
import org.ohmage.query.impl.QueryResultsList.QueryResultListBuilder;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting classes. While it may read information pertaining to
 * other entities, the information it takes and provides should pertain to 
 * classes only with the exception of linking other entities to classes such as
 * class update which needs to be able to associate users with a class in a
 * single transaction.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class ClassQueries extends Query implements IClassQueries {
	
	private static Logger LOGGER = Logger.getLogger(ClassQueries.class);
	
	private ICampaignClassQueries campaignClassQueries; 
	private IUserCampaignClassQueries userCampaignClassQueries;
	private IUserClassQueries userClassQueries;
	private IUserCampaignQueries userCampaignQueries;
	
	// Returns a boolean as to whether or not the given class exists.
	private static final String SQL_EXISTS_CLASS = 
		"SELECT EXISTS(" +
			"SELECT urn " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Returns all class IDs in the system.
	private static final String SQL_GET_ALL_CLASS_IDS =
		"SELECT urn " +
		"FROM class";
	
	// Returns the class IDs that contain the paritial class ID. Be sure to add
	// the "%" around the given value.
	private static final String SQL_GET_LIKE_CLASS_ID =
		"SELECT urn " +
		"FROM class " +
		"WHERE urn LIKE ?";
	
	// Returns the class IDs that contain the partial class name. Be sure to 
	// add the "%" around the given value.
	private static final String SQL_GET_LIKE_CLASS_NAME =
		"SELECT urn " +
		"FROM class " +
		"WHERE name LIKE ?";
	
	// Returns the class IDs that contain the partial class description. Be 
	// sure to add the "%" around the given value.
	private static final String SQL_GET_LIKE_CLASS_DESCRIPTION =
		"SELECT urn " +
		"FROM class " +
		"WHERE description LIKE ?";
	
	// Inserts a new class.
	private static final String SQL_INSERT_CLASS =
		"INSERT INTO class(urn, name, description, creation_timestamp) " +
		"VALUES (?,?,?, NOW())";
	
	// Associates a user with a class.
	private static final String SQL_INSERT_USER_CLASS = 
		"INSERT INTO user_class(user_id, class_id, user_class_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_class_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Associates a user with a campaign.
	private static final String SQL_INSERT_USER_CAMPAIGN =
		"INSERT INTO user_role_campaign(user_id, campaign_id, user_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			"), (" +
				"SELECT id " +
				"FROM campaign " +
				"WHERE urn = ?" +
			"), (" +
				"SELECT id " +
				"FROM user_role " +
				"WHERE role = ?" +
			")" +
			"" +
		")";
	
	// Updates a class' name.
	private static final String SQL_UPDATE_CLASS_NAME =
		"UPDATE class " +
		"SET name = ? " +
		"WHERE urn = ?";
	
	// Updates a class' description.
	private static final String SQL_UPDATE_CLASS_DESCRIPTION = 
		"UPDATE class " +
		"SET description = ? " +
		"WHERE urn = ?";
	
	// Updates a user's role in a class.
	private static final String SQL_UPDATE_USER_CLASS =
		"UPDATE user_class " +
		"SET user_class_role_id = (" +
			"SELECT id " +
			"FROM user_class_role " +
			"WHERE role = ?" +
		")" +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		")" +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Deletes a class.
	private static final String SQL_DELETE_CLASS = 
		"DELETE FROM class " + 
		"WHERE urn = ?";
	
	// Deletes a user from a class.
	private static final String SQL_DELETE_USER_FROM_CLASS =
		"DELETE FROM user_class " +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		") " +
		"AND class_id = (" +
			"SELECT id " +
			"FROM class " +
			"WHERE urn = ?" +
		")";
	
	// Deletes a user from a campaign as long as they have the given role.
	private static final String SQL_DELETE_USER_FROM_CAMPAIGN =
		"DELETE FROM user_role_campaign " +
		"WHERE user_id = (" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		") " +
		"AND campaign_id = (" +
			"SELECT id " +
			"FROM campaign " +
			"WHERE urn = ?" +
		") " +
		"AND user_role_id = (" +
			"SELECT id " +
			"FROM user_role " +
			"WHERE role = ?" +
		")";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private ClassQueries(DataSource dataSource, ICampaignClassQueries iCampaignClassQueries, 
			IUserCampaignClassQueries iUserCampaignClassQueries, IUserClassQueries iUserClassQueries, IUserCampaignQueries iUserCampaignQueries) {
		super(dataSource);
		
		if(iCampaignClassQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignClassQueries is a required argument.");
		}
		
		if(iUserCampaignClassQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignClassQueries is a required argument.");
		}
		
		if(iUserClassQueries == null) {
			throw new IllegalArgumentException("An instance of IUserClassQueries is a required argument.");
		}
		
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is a required argument.");
		}
		
		campaignClassQueries = iCampaignClassQueries;
		userCampaignClassQueries = iUserCampaignClassQueries;
		userClassQueries = iUserClassQueries;
		userCampaignQueries = iUserCampaignQueries;
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#createClass(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createClass(String classId, String className, String classDescription) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the class.
			try {
				getJdbcTemplate().update(SQL_INSERT_CLASS, new Object[] { classId, className, classDescription });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_CLASS + "' with parameters: " +
						classId + ", " + className + ", " + classDescription, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#getClassExists(java.lang.String)
	 */
	@Override
	public Boolean getClassExists(String classId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_EXISTS_CLASS, new Object[] { classId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_CLASS + "' with parameters: " + classId, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IClassQueries#getAllClassIds()
	 */
	@Override
	public List<String> getAllClassIds() throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_ALL_CLASS_IDS, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_ALL_CLASS_IDS, 
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IClassQueries#getClassIdsFromPartial(java.lang.String)
	 */
	@Override
	public List<String> getClassIdsFromPartialId(String partialId)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_CLASS_ID, 
					new Object[] { "%" + partialId + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_LIKE_CLASS_ID + 
						"' with parameter: " + 
						"%" + partialId + "%", 
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IClassQueries#getClassIdsFromPartialName(java.lang.String)
	 */
	@Override
	public List<String> getClassIdsFromPartialName(String partialName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_CLASS_NAME, 
					new Object[] { "%" + partialName + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_LIKE_CLASS_ID + 
						"' with parameter: " + 
						"%" + partialName + "%", 
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IClassQueries#getClassIdsFromPartialDescription(java.lang.String)
	 */
	@Override
	public List<String> getClassIdsFromPartialDescription(
			String partialDescription) 
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_LIKE_CLASS_DESCRIPTION, 
					new Object[] { "%" + partialDescription + "%" }, 
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_LIKE_CLASS_ID + 
						"' with parameter: " + 
						"%" + partialDescription + "%", 
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#getClassesInformation(java.util.Collection, java.lang.String)
	 */
	@Override
	public QueryResultsList<Clazz> getClassesInformation(
			final String username,
			final Collection<String> classIds,
			final Collection<String> classNameTokens,
			final Collection<String> classDescriptionTokens,
			final Clazz.Role role) 
			throws DataAccessException {
		
		// Build the default part of the query.
		StringBuilder sqlBuilder = 
				new StringBuilder(
						// This is the outer part of the query that will allow
						// us to attach the user's role on the resulting 
						// classes.
						"SELECT r.urn, r.name, r.description, ucr.role " +
						"FROM (" +
							// Get the basic information about the class as 
							// well as the requesting user's ID and the class'
							// ID.
							"SELECT u.id AS user_id, c.id AS class_id, " +
								"c.urn, c.name, c.description " +
							"FROM class c, user u " +
							"WHERE u.username = ? " +
							// ACL.
							"AND (" +
								"(u.admin = true)" +
								" OR " +
								"(" +
									"c.id IN (" +
										"SELECT uc.class_id " +
										"FROM user_class uc " +
										"WHERE u.id = uc.user_id" +
									")" +
								")" +
							")");
		
		// Add the requesting user's username to the parameters as this is 
		// always required for the ACL.
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		
		// If we are going to intentionally limit the results to only a set of
		// classes, then do so here.
		if(classIds != null) {
			if(classIds.size() == 0) {
				return (new QueryResultListBuilder<Clazz>()).getQueryResult();
			}
			
			sqlBuilder.append(
					" AND c.urn IN " +
					StringUtils.generateStatementPList(classIds.size()));
					
			parameters.addAll(classIds);
		}
		
		// If there are name search tokens, add them as a group of ORs.
		if(classNameTokens != null) {
			if(classNameTokens.size() == 0) {
				return (new QueryResultListBuilder<Clazz>()).getQueryResult();
			}
			
			boolean firstPass = true;
			sqlBuilder.append(" AND (");
			for(String nameToken : classNameTokens) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sqlBuilder.append(" OR ");
				}
				
				sqlBuilder.append("c.name LIKE ?");
				parameters.add('%' + nameToken + '%');
			}
			sqlBuilder.append(")");
		}
		
		// If there are description search tokens, add them as a group of ORs.
		if(classDescriptionTokens != null) {
			if(classDescriptionTokens.size() == 0) {
				return (new QueryResultListBuilder<Clazz>()).getQueryResult();
			}
			
			boolean firstPass = true;
			sqlBuilder.append(" AND (");
			for(String descriptionToken : classDescriptionTokens) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sqlBuilder.append(" OR ");
				}
				
				sqlBuilder.append("c.description LIKE ?");
				parameters.add('%' + descriptionToken + '%');
			}
			sqlBuilder.append(")");
		}
		
		// Finally, tack on the JOIN that will give us the user's role or limit
		// the results by the given role if one was given.
		sqlBuilder.append(
				") AS r " +
				"LEFT JOIN (user_class uc, user_class_role ucr) " +
					"ON r.user_id = uc.user_id " +
					"AND r.class_id = uc.class_id " +
					"AND ucr.id = uc.user_class_role_id");
		if(role != null) {
			sqlBuilder.append(" WHERE ucr.role = ?");
			parameters.add(role.toString());
		}
		
		try {
			return getJdbcTemplate().query(
					sqlBuilder.toString(), 
					parameters.toArray(), 
					new ResultSetExtractor<QueryResultsList<Clazz>>() {
						/**
						 * Creates class objects for each of the classes that
						 * match the results.
						 */
						@Override
						public QueryResultsList<Clazz> extractData(
								final ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							QueryResultListBuilder<Clazz> resultBuilder =
									new QueryResultListBuilder<Clazz>();
							
							try {
								while(rs.next()) {
									Clazz.Role role = null;
									String roleString = rs.getString("role");
									if(roleString != null) {
										try {
											role = 
												Clazz.Role.getValue(roleString);
										}
										catch(IllegalArgumentException e) {
											throw new SQLException(
													"The class role is unknown: " +
														roleString,
													e);
										}
									}
										
									resultBuilder.addResult(
											new Clazz(
												rs.getString("urn"),
												rs.getString("name"),
												rs.getString("description"),
												role));
								}
								
								return resultBuilder.getQueryResult();
							}
							catch(DomainException e) {
								throw new SQLException(e);
							}
						}
					}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						sqlBuilder.toString() +
						"' with parameters: " +
						parameters,
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#getUserRolePairs(java.lang.String)
	 */
	@Override
	public Map<String, Clazz.Role> getUserRolePairs(
			final String username,
			final String classId) 
			throws DataAccessException {
		
		String sql = 
				"SELECT u.username, " +
					"CASE WHEN (" +
						"SELECT EXISTS (" +
							"SELECT ru.id " +
							"FROM user ru, " +
								"user_class ruc, user_class_role rucr " +
							"WHERE ru.username = ? " +
							"AND (" +
								"(ru.admin = true)" +
								" OR " +
								"(c.id = ruc.class_id " +
									"AND ru.id = ruc.user_id " +
									"AND rucr.id = ruc.user_class_role_id " +
									"AND rucr.role = '" + 
									Clazz.Role.PRIVILEGED.toString().toLowerCase() + 
									"'" +
								")" +
							")" +
						")" +
					") " +
					"THEN ucr.role " +
					"ELSE NULL " +
					"END " +
					"AS role " +
				"FROM user u, user_class uc, user_class_role ucr, class c " +
				"WHERE c.urn = ? " +
				"AND c.id = uc.class_id " +
				"AND u.id = uc.user_id " +
				"AND ucr.id = uc.user_class_role_id";
		
		try {
			return getJdbcTemplate().query(
					sql, 
					new Object[] { username, classId }, 
					new ResultSetExtractor<Map<String, Clazz.Role>>() {
						@Override
						public Map<String, Role> extractData(
								final ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							Map<String, Clazz.Role> result =
									new HashMap<String, Clazz.Role>();
							
							while(rs.next()) {
								String role = rs.getString("role");
								
								if(role == null) {
									result.put(rs.getString("username"), null);
								}
								else {
									try {
										result.put(
												rs.getString("username"), 
												Clazz.Role.getValue(role));
									}
									catch(IllegalArgumentException e) {
										throw new SQLException(
												"The role is unknown.",
												e);
									}
								}
							}
							
							return result;
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL_EXISTS_CLASS '" + 
							sql + 
							"' with parameter: " + 
							classId, 
					e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#updateClass(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Collection)
	 */
	@Override
	public List<String> updateClass(String classId, String className, String classDescription, Map<String, Clazz.Role> userAndRolesToAdd, Collection<String> usersToRemove)
		throws DataAccessException {
		// Note: This function is ugly. We need to stop using a class as a 
		// mechanism to add users to a campaign and start using it like a 
		// group, where a user's roles in a campaign are the union of those
		// given to them directly from the user_role_campaign table and the
		// roles given to each of the classes to which they belong.
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the name if it's not null.
			if(className != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_CLASS_NAME, new Object[] { className, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error while executing SQL '" + SQL_UPDATE_CLASS_NAME + "' with parameters: " + 
							className + ", " + classId, e);
				}
			}
			
			// Update the description if it's not null.
			if(classDescription != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_CLASS_DESCRIPTION, new Object[] { classDescription, classId });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error while executing SQL '" + SQL_UPDATE_CLASS_DESCRIPTION + "' with parameters: " + 
							classDescription + ", " + classId, e);
				}
			}
			
			// If either of the user lists are non-empty, we grab the list of
			// campaigns associated with the class now as it will be needed, 
			// and we don't want to grab it multiple times.
			List<String> campaignIds = Collections.emptyList();
			if((usersToRemove != null) || (userAndRolesToAdd != null)) {
				try {
					campaignIds = campaignClassQueries.getCampaignsAssociatedWithClass(classId);
				}
				catch(DataAccessException e) {
					transactionManager.rollback(status);
					throw e;
				}
			}
			
			// Delete the users before adding the new ones. This facilitates
			// upgrading a user from one role to another.
			if(usersToRemove != null) {
				for(String username : usersToRemove) {
					// Get the user's role in the class before removing
					// it.
					Clazz.Role classRole;
					try {
						classRole = userClassQueries.getUserClassRole(classId, username);
					}
					catch(DataAccessException e) {
						transactionManager.rollback(status);
						throw e;
					}
					
					// To prevent a concurrency bug, the user may have already
					// been deleted from the class which will be indicated by
					// having a null value for the role. When this happens, we
					// may continue on with the next user.
					if(classRole == null) {
						continue;
					}
					
					// Remove the user from the class.
					try {
						getJdbcTemplate().update(SQL_DELETE_USER_FROM_CLASS, new Object[] { username, classId });
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error while executing SQL '" + SQL_DELETE_USER_FROM_CLASS + "' with parameters: " + 
								username + ", " + classId, e);
					}
					
					// For all of the campaigns associated with the class, see
					// if the user is associated with any of them in any other
					// capacity, and, if not, dissociate them from the 
					// campaign.
					for(String campaignId : campaignIds) {
						// If they are associated with the campaign through 
						// no classes, then we are going to remove any 
						// campaign-class associations that may exist.
						int numClasses;
						try {
							numClasses = userCampaignClassQueries.getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(username, campaignId);
						}
						catch(DataAccessException e) {
							transactionManager.rollback(status);
							throw e;
						}
						
						if(numClasses == 0) {
							// Get the default roles which are to be revoked
							// from the user.
							List<Campaign.Role> defaultRoles;
							try {
								defaultRoles = campaignClassQueries.getDefaultCampaignRolesForCampaignClass(campaignId, classId, classRole);
							}
							catch(DataAccessException e) {
								transactionManager.rollback(status);
								throw e;
							}
							
							// For each of the default roles, remove that role
							// from the user.
							for(Campaign.Role defaultRole : defaultRoles) {
								try {
									getJdbcTemplate().update(
											SQL_DELETE_USER_FROM_CAMPAIGN,
											new Object[] { username, campaignId, defaultRole.toString() });
								}
								catch(org.springframework.dao.DataAccessException e) {
									transactionManager.rollback(status);
									throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_FROM_CAMPAIGN + "' with parameters: " +
											username + ", " + campaignId + ", " + defaultRole, e);
								}
							}
						}
					}
				}
			}
			
			// Create the list of warning messages to be returned to the 
			// caller.
			List<String> warningMessages = new LinkedList<String>();
			
			// Add the users to the class.
			if(userAndRolesToAdd != null) {
				
				for(String username : userAndRolesToAdd.keySet()) {
					
					// Get the user's (new) role.
					Clazz.Role role = userAndRolesToAdd.get(username);
					
					boolean addDefaultRoles = false;
					
					try {
						
						if(! userClassQueries.userBelongsToClass(classId, username)) {
							
							if(LOGGER.isDebugEnabled()) {
								LOGGER.debug("The user did not exist in the class so the user is being added before any updates are attemped.");
							}
							
							try {
								getJdbcTemplate()
									.update(
										SQL_INSERT_USER_CLASS,
										new Object[] { 
											username, 
											classId, 
											role.toString() } 
										);
								addDefaultRoles = true;
							}
							catch(org.springframework.dao.DuplicateKeyException e) {
								// The use is already associated with the
								// class, so this is to mitigate concurrency
								// issues.
								addDefaultRoles = false;
							}
						}
						
						else  {
							
							if(LOGGER.isDebugEnabled()) {
								LOGGER.debug("The user already has a role in the class so only updates will be performed.");
							}
							
							// Get the user's current role.
							Clazz.Role originalRole = null;
							try {
								originalRole = userClassQueries.getUserClassRole(classId, username);
							}
							catch(DataAccessException e) {
								transactionManager.rollback(status);
								throw e;
							}
							
							// If their new role is the same as their old role, we
							// will ignore this update.
							if(! role.equals(originalRole)) {
								
								if(LOGGER.isDebugEnabled()) {
									LOGGER.debug("Changing user's class role from " + originalRole + " to " + role);
								}
								
								// Update their role to the new role.
								try {
									if(getJdbcTemplate().update(SQL_UPDATE_USER_CLASS, new Object[] { role.toString(), username, classId }) > 0) {
										warningMessages.add("The user '" + username + 
												"' was already associated with the class '" + classId + 
												"'. Their role has been updated from '" + originalRole +
												"' to '" + role + "'");
									}
								}
								catch(org.springframework.dao.DataAccessException e) {
									transactionManager.rollback(status);
									throw new DataAccessException("Error while executing SQL '" + SQL_UPDATE_USER_CLASS + "' with parameters: " + 
											role + ", " + username + ", " + classId, e);
								}
								
								// For each of the campaigns associated with this
								// class,
								for(String campaignId : campaignIds) {
									// If they are only associated with this 
									// campaign in this class.
									int numClasses;
									try {
										numClasses = userCampaignClassQueries.getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(username, campaignId);
									}
									catch(DataAccessException e) {
										transactionManager.rollback(status);
										throw e;
									}
									
									if(numClasses == 1) {
										
										if(LOGGER.isDebugEnabled()) {
											LOGGER.debug("The user only belonged to the campaign " + campaignId + " via one class" +
												" and their class role is changing so all default campaign roles are being deleted.");
										}
										
										// Remove the current roles with the 
										// campaign and add a new role with the
										// campaign.
										List<Campaign.Role> defaultRoles;
										try {
											defaultRoles = campaignClassQueries.getDefaultCampaignRolesForCampaignClass(campaignId, classId, originalRole);
										}
										catch(DataAccessException e) {
											transactionManager.rollback(status);
											throw e;
										}
										
										for(Campaign.Role defaultRole : defaultRoles) {
											try {
												getJdbcTemplate().update(
														SQL_DELETE_USER_FROM_CAMPAIGN,
														new Object[] { username, campaignId, defaultRole.toString() });
											}
											catch(org.springframework.dao.DataAccessException e) {
												transactionManager.rollback(status);
												throw new DataAccessException("Error executing SQL '" + SQL_DELETE_USER_FROM_CAMPAIGN + "' with parameters: " +
														username + ", " + campaignId + ", " + defaultRole, e);
											}
										}
									}
									
									addDefaultRoles = true;
								}
							}
							else {
								
								if(LOGGER.isDebugEnabled()) {
									LOGGER.debug("Nothing to do because the user's class role is not changing.");
								}
							}
						}
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_USER_CLASS + "' with parameters: " + 
								username + ", " + classId + ", " + role, e);
					}
					catch(DataAccessException e) {
						transactionManager.rollback(status);
						throw e;
					}

					if(addDefaultRoles) {
						// For each of the campaign's associated with the 
						// class, add them to the campaign with the default
						// roles.
						for(String campaignId : campaignIds) {
							List<Campaign.Role> defaultRoles;
							try {
								defaultRoles = campaignClassQueries.getDefaultCampaignRolesForCampaignClass(campaignId, classId, role);
							}
							catch(DataAccessException e) {
								transactionManager.rollback(status);
								throw e;
							}
							
							for(Campaign.Role defaultRole : defaultRoles) {
								try {
									final Object[] params = new Object[] {username, campaignId, defaultRole.toString()};
									
									if(LOGGER.isDebugEnabled()) {
										LOGGER.debug("Assigning the user a default campaign role of " + defaultRole + " in campaign " + campaignId);
									}
									
									// The user may already have the role in
									// the campaign via another class or the 
									// user may have not been in any class
									// at all.
									if(! userCampaignQueries.getUserCampaignRoles(username, campaignId).contains(defaultRole)) {
									
										getJdbcTemplate().update(SQL_INSERT_USER_CAMPAIGN, params);
									} 
									else {
										
										if(LOGGER.isDebugEnabled()) {
											LOGGER.debug("User already has this role in the campaign: " + Arrays.asList(params));
										}			
									}
								}
								catch(org.springframework.dao.DataAccessException e) {
									transactionManager.rollback(status);
									throw new DataAccessException("Error executing SQL '" + SQL_INSERT_USER_CAMPAIGN + "' with parameters: " +
											username + ", " + campaignId + ", " + defaultRole, e);
								}
								catch(DataAccessException e) {
									transactionManager.rollback(status);
									throw e;
								}
							}
						}
					}
				}
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
			
			return warningMessages;
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IClassQueries#deleteClass(java.lang.String)
	 */
	@Override
	public void deleteClass(String classId) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a class.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			try {
				getJdbcTemplate().update(SQL_DELETE_CLASS, new Object[] { classId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_DELETE_CLASS + "' with parameter: " + classId, e);
			}
			
			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while committing the transaction.", e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
}
