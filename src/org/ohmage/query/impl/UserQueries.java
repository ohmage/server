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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.UserInformation;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.IUserQueries;
import org.ohmage.query.impl.QueryResultsList.QueryResultListBuilder;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting users. While it may read information pertaining to
 * other entities, the information it takes and provides should pertain to 
 * users only.
 * 
 * @author John Jenkins
 */
public class UserQueries extends Query implements IUserQueries {
	// Returns a boolean representing whether or not a user exists
	private static final String SQL_EXISTS_USER = 
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user is an admin or not.
	private static final String SQL_EXISTS_USER_IS_ADMIN = 
		"SELECT admin " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user's account is enabled.
	private static final String SQL_EXISTS_USER_IS_ENABLED = 
		"SELECT enabled " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a single, boolean row if the user exists which explains if the
	// user's account is new.
	private static final String SQL_EXISTS_USER_IS_NEW_ACCOUNT = 
		"SELECT new_account " +
		"FROM user " +
		"WHERE username = ?";
	
	// Returns a boolean representing whether a user can create campaigns or 
	// not. If the user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND campaign_creation_privilege = true" +
		")";
	
	// Returns a boolean representing whether a user can create classes or not. 
	// If the user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_CAN_CREATE_CLASSES =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND class_creation_privilege = true" +
		")";
	
	// Returns a boolean representing whether a user can create setup other
	// users or not. If the user doesn't exist, false is returned.
	private static final String SQL_EXISTS_USER_CAN_SETUP_USER =
		"SELECT EXISTS(" +
			"SELECT username " +
			"FROM user " +
			"WHERE username = ? " +
			"AND user_setup_privilege = true" +
		")";
	
	// Returns a boolean representing whether or not a user has a personal
	// information entry.
	private static final String SQL_EXISTS_USER_PERSONAL =
		"SELECT EXISTS(" +
			"SELECT user_id " +
			"FROM user_personal " +
			"WHERE user_id = (" +
				"SELECT Id " +
				"FROM user " +
				"WHERE username = ?" +
			")" +
		")";
	
	// Returns a boolean representing whether or not a registration ID exists.
	private static final String SQL_EXISTS_REGISTRATION_ID =
		"SELECT EXISTS(" +
			"SELECT id " +
			"FROM user_registration " +
			"WHERE registration_id = ?" +
		")";
	
	private static final String SQL_GET_ALL_USERNAMES =
		"SELECT username " +
		"FROM user";
	
	private static final String SQL_GET_USERNAMES_LIKE_USERNAME =
		"SELECT username " +
		"FROM user " +
		"WHERE username LIKE ?";
	
	private static final String SQL_GET_USERNAMES_WITH_ADMIN_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE admin = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_ENABLED_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE enabled = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE =
		"SELECT username " +
		"FROM user " +
		"WHERE new_account = ?";
	
	private static final String SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE =
		"SELECT username " +
		"FROM user " +
		"WHERE campaign_creation_privilege = ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_FIRST_NAME =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND first_name LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_LAST_NAME =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND last_name LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_ORGANIZATION =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND organization LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_PERSONAL_ID =
		"SELECT username " +
		"FROM user, user_personal " +
		"WHERE user.id = user_id " +
		"AND personal_id LIKE ?";
	
	private static final String SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS =
		"SELECT username " +
		"FROM user " +
		"WHERE email_address LIKE ?";
	
	// Retrieves the personal information about a user.
	private static final String SQL_GET_USER_PERSONAL =
		"SELECT up.first_name, up.last_name, up.organization, up.personal_id " +
		"FROM user u, user_personal up " +
		"WHERE u.username = ? " +
		"AND u.id = up.user_id";
	
	// Returns the milliseconds since epoch at which time this registration was
	// made.
	private static final String SQL_GET_REGISTRATION_REQUEST_TIME =
		"SELECT request_timestamp " +
		"FROM user_registration " +
		"WHERE registration_id = ?";
	
	// Returns the milliseconds since epoch at which time this registration was
	// accepted.
	private static final String SQL_GET_REGISTRATION_ACCEPTED_TIME =
		"SELECT accepted_timestamp " +
		"FROM user_registration " +
		"WHERE registration_id = ?";
	
	// Inserts a new user.
	private static final String SQL_INSERT_USER = 
		"INSERT INTO user(username, password, plaintext_password, email_address, admin, enabled, new_account, campaign_creation_privilege) " +
		"VALUES (?,?,?,?,?,?,?,?)";
	
	// Inserts a new personal information record for a user. Note: this doesn't
	// insert the email address or JSON data; to add these, update the record
	// immediately after using this to insert it.
	private static final String SQL_INSERT_USER_PERSONAL =
		"INSERT INTO user_personal(user_id, first_name, last_name, organization, personal_id) " +
		"VALUES ((" +
			"SELECT id " +
			"FROM user " +
			"WHERE username = ?" +
		"),?,?,?,?)";
	
	// Inserts a new registration for a user.
	private static final String SQL_INSERT_REGISTRATION =
		"INSERT INTO user_registration(user_id, registration_id, request_timestamp) " +
		"VALUES ((SELECT id FROM user WHERE username = ?), ?, ?)";
	
	// Inserts a single user into a single class with a given role.
	private static final String SQL_INSERT_USER_CLASS =
		"INSERT INTO user_class(user_id, class_id, user_class_role_id) " +
		"VALUES (" +
			"(" +
				"SELECT id " +
				"FROM user " +
				"WHERE username = ?" +
			")," +
			"(" +
				"SELECT id " +
				"FROM class " +
				"WHERE urn = ?" +
			")," +
			"(" +
				"SELECT id " +
				"FROM user_class_role " +
				"WHERE role = ?" +
			")" +
		")";
	
	// Updates the user's password.
	private static final String SQL_UPDATE_PASSWORD = 
		"UPDATE user " +
		"SET password = ? " +
		"WHERE username = ?";
	
	// Updates a user's admin value.
	private static final String SQL_UPDATE_ADMIN =
		"UPDATE user " +
		"SET admin = ? " +
		"WHERE username = ?";
	
	// Updates a user's enabled value.
	private static final String SQL_UPDATE_ENABLED =
		"UPDATE user " +
		"SET enabled = ? " +
		"WHERE username = ?";
	
	// Updates a user's new account value.
	private static final String SQL_UPDATE_NEW_ACCOUNT =
		"UPDATE user " +
		"SET new_account = ? " +
		"WHERE username = ?";
	
	// Updates a user's campaign creation privilege.
	private static final String SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE =
		"UPDATE user " +
		"SET campaign_creation_privilege = ? " +
		"WHERE username = ?";
	
	// Updates a user's class creation privilege.
	private static final String SQL_UPDATE_CLASS_CREATION_PRIVILEGE =
		"UPDATE user " +
		"SET class_creation_privilege = ? " +
		"WHERE username = ?";
	
	// Updates a user's user setup privilege.
	private static final String SQL_UPDATE_USER_SETUP_PRIVILEGE =
		"UPDATE user " +
		"SET user_setup_privilege = ? " +
		"WHERE username = ?";
	
	// Updates a user's first name in their personal information record.
	private static final String SQL_UPDATE_FIRST_NAME = 
		"UPDATE user_personal " +
		"SET first_name = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's last name in their personal information record.
	private static final String SQL_UPDATE_LAST_NAME = 
		"UPDATE user_personal " +
		"SET last_name = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's organization in their personal information record.
	private static final String SQL_UPDATE_ORGANIZATION = 
		"UPDATE user_personal " +
		"SET organization = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's personal ID in their personal information record.
	private static final String SQL_UPDATE_PERSONAL_ID = 
		"UPDATE user_personal " +
		"SET personal_id = ? " +
		"WHERE user_id = (" +
			"SELECT Id " +
			"FROM user " +
			"WHERE username = ?" +
		")";
	
	// Updates a user's email address in their personal information record.
	private static final String SQL_UPDATE_EMAIL_ADDRESS = 
		"UPDATE user " +
		"SET email_address = ? " +
		"WHERE username = ?";
	
	// Uses a registration ID to update a user's enabled status.
	private static final String SQL_UPDATE_ENABLED_FROM_REGISTRATION_ID =
		"UPDATE user u, user_registration ur " +
		"SET u.enabled = ? " +
		"WHERE u.id = ur.user_id " +
		"AND ur.registration_id = ?";
	
	// Updates the accepted timestamp of the registration.
	private static final String SQL_UPDATE_ACCEPTED_TIMESTAMP =
		"UPDATE user_registration " +
		"SET accepted_timestamp = ? " +
		"WHERE registration_id = ?";
	
	// Deletes the user.
	private static final String SQL_DELETE_USER = 
		"DELETE FROM user " +
		"WHERE username = ?";
	
	// Deletes a user's personal information.
	private static final String SQL_DELETE_USER_PERSONAL = 
		"DELETE user_personal " +
		"FROM user, user_personal " +
		"WHERE user.id = user_personal.user_id " +
		"AND user.username = ?";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	private UserQueries(final DataSource dataSource) {
		super(dataSource);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean)
	 */
	@Override
	public void createUser(
			final String username, 
			final String plaintextPassword,
			final String hashedPassword, 
			final String emailAddress, 
			final Boolean admin, 
			final Boolean enabled, 
			final Boolean newAccount, 
			final Boolean campaignCreationPrivilege) 
			throws DataAccessException {
		
		Boolean tAdmin = admin;
		if(tAdmin == null) {
			tAdmin = false;
		}
		
		Boolean tEnabled = enabled;
		if(tEnabled == null) {
			tEnabled = false;
		}
		
		Boolean tNewAccount = newAccount;
		if(tNewAccount == null) {
			tNewAccount = true;
		}
		
		Boolean tCampaignCreationPrivilege = campaignCreationPrivilege;
		if(tCampaignCreationPrivilege == null) {
			try {
				tCampaignCreationPrivilege = PreferenceCache.instance().lookup(PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE).equals("true");
			}
			catch(CacheMissException e) {
				throw new DataAccessException("Cache doesn't know about 'known' value: " + PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE, e);
			}
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				getJdbcTemplate().update(SQL_INSERT_USER, new Object[] { username, hashedPassword, plaintextPassword, emailAddress, tAdmin, tEnabled, tNewAccount, tCampaignCreationPrivilege });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_USER + "' with parameters: " +
						username + ", " + hashedPassword + ", " + plaintextPassword + ", " + emailAddress + ", " + tAdmin + ", " + tEnabled + ", " + tNewAccount + ", " + tCampaignCreationPrivilege, e);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#createUserRegistration(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public void createUserRegistration(
			final String username,
			final String hashedPassword,
			final String emailAddress,
			final String registrationId)
			throws DataAccessException {
		
		// Get the public class.
		String publicClassId;
		try {
			publicClassId = 
					PreferenceCache.instance().lookup(
							PreferenceCache.KEY_PUBLIC_CLASS_ID);
		}
		catch(CacheMissException e) {
			throw new DataAccessException(
					"The public class is not configured");
		}
		
		Boolean defaultCampaignCreationPrivilege;
		try {
			String privilegeString =
					PreferenceCache.instance().lookup(
							PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE);
			
			if(privilegeString == null) {
				throw new DataAccessException(
						"The default campaign creation privilege is missing.");
			}
			
			defaultCampaignCreationPrivilege = 
					StringUtils.decodeBoolean(privilegeString);
			
			if(defaultCampaignCreationPrivilege == null) {
				throw new DataAccessException(
						"The default campaign creation privilege is not a valid boolean.");
			}
		}
		catch(CacheMissException e) {
			throw new DataAccessException("Cache doesn't know about 'known' value: " + PreferenceCache.KEY_DEFAULT_CAN_CREATE_PRIVILIEGE, e);
		}
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a user registration request.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
					new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the new user.
			try {
				getJdbcTemplate().update(
						SQL_INSERT_USER, 
						new Object[] { 
								username, 
								hashedPassword, 
								emailAddress, 
								false, 
								false, 
								false, 
								defaultCampaignCreationPrivilege
							}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + 
							SQL_INSERT_USER + 
							"' with parameters: " +
							username + ", " + 
							emailAddress + ", " + 
							hashedPassword + ", " + 
							false + ", " + 
							false + ", " + 
							false + ", " + 
							"null", 
						e);
			}
			
			// Insert the new user into the class.
			try {
				getJdbcTemplate().update(
						SQL_INSERT_USER_CLASS, 
						new Object[] { 
								username, 
								publicClassId, 
								Clazz.Role.RESTRICTED.toString() });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + 
							SQL_INSERT_USER_CLASS + 
							"' with parameters: " +
							username + ", " + 
							publicClassId + ", " + 
							Clazz.Role.RESTRICTED.toString(), 
						e);
			}
			
			// Get the list of campaigns for this class.
			String sqlGetCampaignIds =
				"SELECT ca.urn " +
					"FROM campaign ca, class cl, campaign_class cc " +
					"WHERE cl.urn = ? " +
					"AND cl.id = cc.class_id " +
					"AND ca.id = cc.campaign_id";
			List<String> campaignIds;
			try {
				campaignIds =
					getJdbcTemplate().query(
						sqlGetCampaignIds,
						new Object[] { publicClassId },
						new SingleColumnRowMapper<String>());
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" +
						sqlGetCampaignIds +
						"' with parameter: " +
						publicClassId,
					e);
			}
			
			// Construct the parameter map for the batch update.
			List<Object[]> batchParameters = 
				new ArrayList<Object[]>(campaignIds.size());
			for(String campaignId : campaignIds) {
				String[] parameters = new String[3];
				parameters[0] = username;
				parameters[1] = campaignId;
				parameters[2] = Campaign.Role.PARTICIPANT.toString();
				batchParameters.add(parameters);
			}
			
			// Perform the batch update.
			String sqlInsertUserCampaign =
				"INSERT INTO user_role_campaign" +
					"(user_id, campaign_id, user_role_id) " +
					"VALUES (" +
						"(SELECT id FROM user WHERE username = ?), " +
						"(SELECT id FROM campaign WHERE urn = ?), " +
						"(SELECT id FROM user_role WHERE role = ?)" +
					")";
			try {
				getJdbcTemplate()
					.batchUpdate(sqlInsertUserCampaign, batchParameters);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" +
						sqlInsertUserCampaign +
						"'.",
					e);
			}
			
			// Insert the user's registration information into the 
			try {
				getJdbcTemplate().update(
						SQL_INSERT_REGISTRATION,
						new Object[] { 
								username, 
								registrationId, 
								(new Date()).getTime() 
							}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error while executing SQL '" + 
							SQL_INSERT_REGISTRATION + 
							"' with parameters: " +
							username + ", " +
							registrationId + ", " +
							(new Date()).getTime(), 
						e);
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
	
	/**
	 * Returns whether or not a user exists.
	 * 
	 * @param username The username for which to check.
	 * 
	 * @return Returns true if the user exists; false, otherwise.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public Boolean userExists(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER, 
					new Object[] { username }, 
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getEmailAddress(java.lang.String)
	 */
	@Override
	public String getEmailAddress(
			final String username) 
			throws DataAccessException {
		
		String sql = "SELECT email_address FROM user WHERE username = ?";
		
		try {
			return getJdbcTemplate().queryForObject(
				sql,
				new Object[] { username },
				String.class
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			// If the user doesn't exist, return null.
			if(e.getActualSize() == 0) {
				return null;
			}
			
			throw new DataAccessException(
					"Multiple users have the same username: " + username, 
					e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing '" + 
						sql + 
						"' with parameter: " + 
						username,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getPlaintextPassword(java.lang.String)
	 */
	@Override
	public String getPlaintextPassword(
			final String username) 
			throws DataAccessException {
		
		String sql = "SELECT plaintext_password FROM user WHERE username = ?";
		
		try {
			return getJdbcTemplate().queryForObject(
				sql,
				new Object[] { username },
				String.class
				);
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			// If the user doesn't exist, return null.
			if(e.getActualSize() == 0) {
				return null;
			}
			
			throw new DataAccessException(
					"Multiple users have the same username: " + username, 
					e);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing '" + 
						sql + 
						"' with parameter: " + 
						username,
					e);
		}
	}
	
	/**
	 * Gets whether or not the user is an admin.
	 * 
	 * @param username The username to check.
	 * 
	 * @return Whether or not they are an admin.
	 * 
	 * @throws DataAccessException Thrown if there is a problem running the
	 * 							   query.
	 */
	public Boolean userIsAdmin(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_ADMIN, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ADMIN + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userIsEnabled(java.lang.String)
	 */
	@Override
	public Boolean userIsEnabled(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_ENABLED, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_ENABLED + "' with parameter: " + username, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userHasNewAccount(java.lang.String)
	 */
	@Override
	public Boolean userHasNewAccount(String username)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_IS_NEW_ACCOUNT, 
					new String[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_IS_NEW_ACCOUNT + "' with parameter: " + username, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userCanCreateCampaigns(java.lang.String)
	 */
	public Boolean userCanCreateCampaigns(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_CAN_CREATE_CAMPAIGNS + "' with parameter: " + username, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userCanCreateClasses(java.lang.String)
	 */
	public Boolean userCanCreateClasses(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_CAN_CREATE_CLASSES, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw
				new DataAccessException(
					"Error executing the following SQL '" +
						SQL_EXISTS_USER_CAN_CREATE_CLASSES +
						"' with parameter: " +
						username,
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userCanCreateClasses(java.lang.String)
	 */
	public Boolean userCanSetupUsers(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_CAN_SETUP_USER, 
					new Object[] { username }, 
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw
				new DataAccessException(
					"Error executing the following SQL '" +
						SQL_EXISTS_USER_CAN_SETUP_USER +
						"' with parameter: " +
						username,
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#userHasPersonalInfo(java.lang.String)
	 */
	public Boolean userHasPersonalInfo(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_USER_PERSONAL,
					new Object[] { username },
					Boolean.class
					);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_EXISTS_USER_PERSONAL + "' with parameter: " + username, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#registrationIdExists(java.lang.String)
	 */
	public boolean registrationIdExists(
			final String registrationId)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().queryForObject(
					SQL_EXISTS_REGISTRATION_ID,
					new Object[] { registrationId },
					Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_EXISTS_REGISTRATION_ID +
						"' with parameter: " +
						registrationId,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getAllUsernames()
	 */
	@Override
	public List<String> getAllUsernames() throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_ALL_USERNAMES,
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_ALL_USERNAMES,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialUsername(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialUsername(String username)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_USERNAME, 
					new Object[] { "%" + username + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_USERNAME +
						"' with parameter: " +
						"%" + username + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithAdminValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithAdminValue(Boolean admin)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_ADMIN_VALUE, 
					new Object[] { admin }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_ADMIN_VALUE +
						"' with parameter: " +
						admin,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithEnabledValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithEnabledValue(Boolean enabled)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_ENABLED_VALUE, 
					new Object[] { enabled }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_ENABLED_VALUE +
						"' with parameter: " +
						enabled,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithNewAccountValue(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithNewAccountValue(Boolean newAccount)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE, 
					new Object[] { newAccount }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_NEW_ACCOUNT_VALUE +
						"' with parameter: " +
						newAccount,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesWithCampaignCreationPrivilege(java.lang.Boolean)
	 */
	@Override
	public List<String> getUsernamesWithCampaignCreationPrivilege(
			Boolean campaignCreationPrivilege) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE, 
					new Object[] { campaignCreationPrivilege }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_WITH_CAMPAIGN_CREATION_PRIVILEGE +
						"' with parameter: " +
						campaignCreationPrivilege,
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialFirstName(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialFirstName(String partialFirstName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_FIRST_NAME, 
					new Object[] { "%" + partialFirstName + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_FIRST_NAME +
						"' with parameter: " +
						"%" + partialFirstName + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialLastName(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialLastName(String partialLastName)
			throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_LAST_NAME, 
					new Object[] { "%" + partialLastName + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_LAST_NAME +
						"' with parameter: " +
						"%" + partialLastName + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialOrganization(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialOrganization(
			String partialOrganization) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_ORGANIZATION, 
					new Object[] { "%" + partialOrganization + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_ORGANIZATION +
						"' with parameter: " +
						"%" + partialOrganization + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialPersonalId(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialPersonalId(
			String partialPersonalId) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_PERSONAL_ID, 
					new Object[] { "%" + partialPersonalId + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_PERSONAL_ID +
						"' with parameter: " +
						"%" + partialPersonalId + "%",
					e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUsernamesFromPartialEmailAddress(java.lang.String)
	 */
	@Override
	public List<String> getUsernamesFromPartialEmailAddress(
			String partialEmailAddress) throws DataAccessException {

		try {
			return getJdbcTemplate().query(
					SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS, 
					new Object[] { "%" + partialEmailAddress + "%" }, 
					new SingleColumnRowMapper<String>()
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_USERNAMES_LIKE_EMAIL_ADDRESS +
						"' with parameter: " +
						"%" + partialEmailAddress + "%",
					e);
		}
	}
	
	/**
	 * Retrieves the personal information for a user or null if the user 
	 * doesn't have any personal information.
	 *
	 * @param username The username of the user whose information is being
	 * 				   retrieved.
	 * 
	 * @return If the user has a personal entry in the database, a UserPersonal
	 * 		   object with that information is returned; otherwise, null is
	 * 		   returned.
	 * 
	 * @throws DataAccessException Thrown if there is an error.
	 */
	public UserPersonal getPersonalInfoForUser(String username) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(
					SQL_GET_USER_PERSONAL, 
					new Object[] { username }, 
					new RowMapper<UserPersonal>() {
						@Override
						public UserPersonal mapRow(
								final ResultSet rs, 
								final int rowNum) 
								throws SQLException {
							
							try {
								return new UserPersonal(
										rs.getString("first_name"),
										rs.getString("last_name"),
										rs.getString("organization"),
										rs.getString("personal_id"));
							} 
							catch(DomainException e) {
								throw new SQLException(
										"Error creating the user's personal information.",
										e);
							}
						}
					});
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() > 1) {
				throw new DataAccessException("There are multiple users with the same username.", e);
			}
			
			return null;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing the following SQL '" + SQL_GET_USER_PERSONAL + "' with parameter: " + username, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getRegistrationRequestedDate(java.lang.String)
	 */
	public Date getRegistrationRequestedDate(
			final String registrationId)
			throws DataAccessException {
		
		try {
			return new Date(
					getJdbcTemplate().queryForLong(
							SQL_GET_REGISTRATION_REQUEST_TIME,
							new Object[] { registrationId }));
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() == 0) {
				return null;
			}
			else {
				throw new DataAccessException(
						"Multiple results were returned for the request timestamp.");
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_REGISTRATION_REQUEST_TIME +
						"' with parameter: " +
						registrationId,
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getRegistrationAcceptedDate(java.lang.String)
	 */
	public Date getRegistrationAcceptedDate(
			final String registrationId)
			throws DataAccessException {
		
		try {
			Long acceptedLong = 
					getJdbcTemplate().queryForLong(
							SQL_GET_REGISTRATION_ACCEPTED_TIME,
							new Object[] { registrationId });
			
			// This is what SQL returns when it is SQL NULL.
			if(acceptedLong == 0) {
				return null;
			}
			else {
				return new Date(acceptedLong);
			}
		}
		catch(org.springframework.dao.IncorrectResultSizeDataAccessException e) {
			if(e.getActualSize() == 0) {
				return null;
			}
			else {
				throw new DataAccessException(
						"Multiple results were returned for the accepted timestamp.");
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" +
						SQL_GET_REGISTRATION_ACCEPTED_TIME +
						"' with parameter: " +
						registrationId,
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#getUserInformation(java.lang.String, java.util.Collection, java.util.Collection, java.util.Collection, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection, long, long)
	 */
	@Override
	public QueryResultsList<UserInformation> getUserInformation(
		final String requesterUsername,
		final Collection<String> usernames,
		final Collection<String> emailAddresses,
		final Boolean admin,
		final Boolean enabled,
		final Boolean newAccount,
		final Boolean canCreateCampaigns,
		final Boolean canCreateClasses,
		final Collection<String> firstNames,
		final Collection<String> lastNames,
		final Collection<String> organizations,
		final Collection<String> personalIds,
		final Collection<String> campaignIds,
		final Collection<String> classIds,
		final long numToSkip,
		final long numToReturn,
		final boolean settingUpUser)
		throws DataAccessException {
		
		// The initial SELECT selects everything.
		StringBuilder sql = 
				new StringBuilder(
						"SELECT u.username, " +
							"u.email_address, " +
							"u.admin, " +
							"u.enabled, " +
							"u.new_account, " +
							"u.campaign_creation_privilege, " +
							"u.class_creation_privilege, " +
							"u.user_setup_privilege, " +
							"up.first_name, " +
							"up.last_name, " +
							"up.organization, " +
							"up.personal_id " +
						"FROM user u " +
							"LEFT JOIN user_personal up ON " +
								"u.id = up.user_id, " +
							"user ru " +
						"WHERE ru.username = ? " +
						// ACL
						"AND (" +
								settingUpUser +
							" OR " +
								"(ru.admin = true)" +
							" OR " +
								"EXISTS(" +
								// If the requesting user shares a campaign
								// with the desired user and is a 
								// supervisor in that campaign.
								"SELECT ru.id " +
								"FROM user_role ur, " +
									"user_role_campaign urc, " +
									"user_role_campaign rurc " +
								// The requesting user is associated with a
								// campaign.
								"WHERE ru.id = rurc.user_id " +
								// The requesting user is a supervisor in
								// that campaign.
								"AND ur.id = rurc.user_role_id " +
								"AND ur.role = '" +
									Campaign.Role.SUPERVISOR.toString() +
									"' " +
								// The queried user is also in a campaign.
								"AND u.id = urc.user_id " +
								// And that campaign is the same as the one
								// in which the requesting user is a 
								// supervisor.
								"AND urc.campaign_id = rurc.campaign_id" +
							")" +
							" OR EXISTS(" +
								// If the requesting user shares a class 
								// with the desired user and is privileged
								// in that class.
								"SELECT ru.id " +
								"FROM user_class_role ucr, " +
									"user_class uc, " +
									"user_class ruc " +
								// The requesting user is associated with a
								// class.
								"WHERE ru.id = ruc.user_id " +
								// The requesting user is privileged in 
								// that class.
								"AND ucr.id = ruc.user_class_role_id " +
								"AND ucr.role = '" +
									Clazz.Role.PRIVILEGED.toString() +
									"' " +
								// The queried user is also in a class.
								"AND u.id = uc.user_id " +
								// And that class is the same as the one in
								// which the requesting user is privileged.
								"AND uc.class_id = ruc.class_id" +
							")" +
						")"
				);
		
		// The initial parameter list doesn't have any items.
		Collection<Object> parameters = new LinkedList<Object>();
		parameters.add(requesterUsername);
		
		// If the list of usernames is present, add a WHERE clause component
		// that limits the results to only those users whose exact username is
		// in the list.
		if(usernames != null) {
			if(usernames.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String username : usernames) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(u.username) LIKE ?");
				
				parameters.add(username);
			}
			
			sql.append(")");
		}
		
		// If the list of email addresses is present, add a WHERE clause that
		// that contains all of the tokens in their own OR.
		if(emailAddresses != null) {
			if(emailAddresses.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String emailAddressToken : emailAddresses) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(u.email_address) LIKE ?");
				
				parameters.add(emailAddressToken);
			}
			
			sql.append(")");
		}
		
		// If "admin" is present, add a WHERE clause component that limits the
		// results to only those whose admin boolean is the same as this 
		// boolean.
		if(admin != null) {
			sql.append(" AND u.admin = ?");
			
			parameters.add(admin);
		}
		
		// If "enabled" is present, add a WHERE clause component that limits 
		// the results to only those whose enabled value is the same as this
		// boolean
		if(enabled != null) {
			sql.append(" AND u.enabled = ?");
			
			parameters.add(enabled);
		}
		
		// If "newAccount" is present, add a WHERE clause component that limits
		// the results to only those whose new account status is the same as
		// this boolean.
		if(newAccount != null) {
			sql.append(" AND u.new_account = ?");
			
			parameters.add(newAccount);
		}
		
		// If "canCreateCampaigns" is present, add a WHERE clause component 
		// that limits the results to only those whose campaign creation
		// privilege is the same as this boolean.
		if(canCreateCampaigns != null) {
			sql.append(" AND u.campaign_creation_privilege = ?");
			
			parameters.add(canCreateCampaigns);
		}
		
		// If "canCreateClasses" is present, add a WHERE clause component that 
		// limits the results to only those whose campaign creation privilege
		// is the same as this boolean.
		if(canCreateClasses != null) {
			sql.append(" AND u.class_creation_privilege = ?");
			
			parameters.add(canCreateClasses);
		}
		
		// If the list of first name tokens is present, add a WHERE clause that
		// contains all of the tokens in their own OR.
		if(firstNames != null) {
			if(firstNames.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String firstName : firstNames) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(up.first_name) LIKE ?");
				
				parameters.add(firstName);
			}
			
			sql.append(")");
		}
		
		// If the list of last name tokens is present, add a WHERE clause that
		// contains all of the tokens in their own OR.
		if(lastNames != null) {
			if(lastNames.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String lastName : lastNames) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(up.last_name) LIKE ?");
				
				parameters.add(lastName);
			}
			
			sql.append(")");
		}
		
		// If the list of organization tokens is present, add a WHERE clause
		// that contains all of the tokens in their own OR.
		if(organizations != null) {
			if(organizations.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String organization : organizations) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(up.organization) LIKE ?");
				
				parameters.add(organization);
			}
			
			sql.append(")");
		}
		
		// If the list of personal ID tokens is present, add a WHERE clause 
		// that contains all of the tokens in their own OR.
		if(personalIds != null) {
			if(personalIds.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(" AND (");
			
			boolean firstPass = true;
			for(String personalId : personalIds) {
				if(firstPass) {
					firstPass = false;
				}
				else {
					sql.append(" OR ");
				}
				
				sql.append("LOWER(up.personal_id) LIKE ?");
				
				parameters.add(personalId);
			}
			
			sql.append(")");
		}
		
		// If a collection of campaign IDs is present, add a WHERE clause 
		// component that limits the results to only those in any of the  
		// campaigns.
		if(campaignIds != null) {
			if(campaignIds.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(
					" AND u.id IN (" +
						"SELECT urc.user_id " +
						"FROM campaign c, user_role_campaign urc " +
						"WHERE c.urn IN " +
							StringUtils.generateStatementPList(campaignIds.size()) + " " +
						"AND c.id = urc.campaign_id" +
					")");
			
			parameters.add(campaignIds);
		}
		
		// If a collection of class IDs is present, add a WHERE clause 
		// component that limits the results to only those in any of the 
		// classes.
		if(classIds != null) {
			if(classIds.size() == 0) {
				return
					(new QueryResultListBuilder<UserInformation>())
						.getQueryResult();
			}
			
			sql.append(
					" AND u.id IN (" +
						"SELECT uc.id " +
						"FROM class c, user_class uc " +
						"WHERE c.urn IN " +
							StringUtils.generateStatementPList(classIds.size()) + " " +
						"AND c.id = uc.class_id" +
					")");
			
			parameters.add(classIds);
		}
		
		// Always order the results by username to facilitate paging.
		sql.append(" ORDER BY u.username");
		
		// Returns the results as queried by the database.
		try {
			return getJdbcTemplate().query(
					sql.toString(), 
					parameters.toArray(),
					new ResultSetExtractor<QueryResultsList<UserInformation>>() {
						/**
						 * Extracts the data into the results and then returns
						 * the total number of results found.
						 */
						@Override
						public QueryResultsList<UserInformation> extractData(
								final ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							QueryResultListBuilder<UserInformation> builder =
									new QueryResultListBuilder<UserInformation>();
							
							int numSkipped = 0;
							while(numSkipped++ < numToSkip) {
								if(rs.next()) {
									builder.increaseTotalNumResults();
								}
								else {
									return builder.getQueryResult();
								}
							}
							
							long numReturned = 0;
							while(numReturned++ < numToReturn) {
								if(rs.next()) {
									builder.addResult(mapRow(rs));
								}
								else {
									return builder.getQueryResult();
								}
							}
							
							while(rs.next()) {
								builder.increaseTotalNumResults();
							}
							
							return builder.getQueryResult();
						}
						
						/**
						 * Creates a new UserInformation object from the 
						 * user information.
						 */
						private UserInformation mapRow(
								final ResultSet rs)
								throws SQLException {
							
							String username = rs.getString("username");
							String emailAddress = 
									rs.getString("email_address");
							
							boolean admin = rs.getBoolean("admin");
							boolean enabled = rs.getBoolean("enabled");
							boolean newAccount = rs.getBoolean("new_account");
							boolean canCreateCampaigns =
								rs.getBoolean(
										"campaign_creation_privilege");
							boolean canCreateClasses =
								rs.getBoolean(
										"class_creation_privilege");
							boolean canSetupUsers =
								rs.getBoolean(
										"user_setup_privilege");
							
							String firstName = rs.getString("first_name");
							String lastName = rs.getString("last_name");
							String organization = rs.getString("organization");
							String personalId = rs.getString("personal_id");
							
							UserPersonal personalInfo = null;
							if(
								(firstName != null) &&
								(lastName != null) &&
								(organization != null) &&
								(personalId != null)) {
								
								try {
									personalInfo =
										new UserPersonal(
											firstName,
											lastName,
											organization,
											personalId);
								} 
								catch(DomainException e) {
									throw new SQLException(
											"Error creating the user's " +
												"personal information.",
											e);
								}
							}
							
							try {
								return
									new UserInformation(
										username,
										emailAddress,
										admin,
										enabled,
										newAccount,
										canCreateCampaigns,
										canCreateClasses,
										canSetupUsers,
										null,
										null,
										personalInfo);
							}
							catch(DomainException e) {
								throw new SQLException(
										"Error creating the user's information.",
										e);
							}
						}
					}
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing the following SQL '" + 
						sql.toString() + 
						"' with parameter(s): " + 
						parameters);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#updateUser(java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateUser(
			final String username, 
			final String emailAddress,
			final Boolean admin, 
			final Boolean enabled, 
			final Boolean newAccount, 
			final Boolean campaignCreationPrivilege,
			final Boolean classCreationPrivilege,
			final Boolean userSetupPrivilege,
			final String firstName,
			final String lastName,
			final String organization,
			final String personalId,
			final boolean deletePersonalInfo) 
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's privileges and information.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			if(emailAddress != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_EMAIL_ADDRESS, emailAddress, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_EMAIL_ADDRESS + "' with parameters: " + 
							emailAddress + ", " + username, e);
				}
			}
			
			// Update the admin value if it's not null.
			if(admin != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_ADMIN, admin, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_ADMIN + "' with parameters: " + 
							admin + ", " + username, e);
				}
			}
			
			// Update the enabled value if it's not null.
			if(enabled != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_ENABLED, enabled, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_ENABLED + "' with parameters: " + 
							enabled + ", " + username, e);
				}
			}
			
			// Update the new account value if it's not null.
			if(newAccount != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, newAccount, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_NEW_ACCOUNT + "' with parameters: " + 
							newAccount + ", " + username, e);
				}
			}
			
			// Update the campaign creation privilege value if it's not null.
			if(campaignCreationPrivilege != null) {
				try {
					getJdbcTemplate().update(SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_CAMPAIGN_CREATION_PRIVILEGE + "' with parameters: " + 
							campaignCreationPrivilege + ", " + username, e);
				}
			}
			
			// Update the class creation privilege value if it's not null.
			if(classCreationPrivilege != null) {
				try {
					getJdbcTemplate()
						.update(
							SQL_UPDATE_CLASS_CREATION_PRIVILEGE,
							classCreationPrivilege,
							username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw
						new DataAccessException(
							"Error executing the following SQL '" +
								SQL_UPDATE_CLASS_CREATION_PRIVILEGE +
								"' with parameters: " + 
								classCreationPrivilege +
								", " +
								username,
							e);
				}
			}
			
			// Update the user setup privilege value if it's not null.
			if(userSetupPrivilege != null) {
				try {
					getJdbcTemplate()
						.update(
							SQL_UPDATE_USER_SETUP_PRIVILEGE,
							userSetupPrivilege,
							username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw
						new DataAccessException(
							"Error executing the following SQL '" +
								SQL_UPDATE_USER_SETUP_PRIVILEGE +
								"' with parameters: " + 
								userSetupPrivilege +
								", " +
								username,
							e);
				}
			}
			
			// If we are deleting the user's personal information, then we 
			// won't add new or update existing personal information.
			if(deletePersonalInfo) {
				try {
					getJdbcTemplate().update(
							SQL_DELETE_USER_PERSONAL, 
							new Object[] { username });
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException(
						"Error executing SQL '" + 
							SQL_DELETE_USER_PERSONAL + 
							"' with parameter: " +
							username,
						e);
				}
			}
			else {
				if(userHasPersonalInfo(username)) {
					if(firstName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_FIRST_NAME, firstName, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_FIRST_NAME + "' with parameters: " +
									firstName + ", " + username, e);
						}
					}
					
					if(lastName != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_LAST_NAME, lastName, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_LAST_NAME + "' with parameters: " +
									lastName + ", " + username, e);
						}
					}
					
					if(organization != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_ORGANIZATION, organization, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_ORGANIZATION + "' with parameters: " +
									organization + ", " + username, e);
						}
					}
					
					if(personalId != null) {
						try {
							getJdbcTemplate().update(SQL_UPDATE_PERSONAL_ID, personalId, username);
						}
						catch(org.springframework.dao.DataAccessException e) {
							transactionManager.rollback(status);
							throw new DataAccessException("Error executing SQL '" + SQL_UPDATE_PERSONAL_ID + "' with parameters: " +
									personalId + ", " + username, e);
						}
					}
				}
				else if(
						(firstName != null) && 
						(lastName != null) && 
						(organization != null) && 
						(personalId != null)) {
					
					try {
						getJdbcTemplate().update(
								SQL_INSERT_USER_PERSONAL, 
								username, 
								firstName, 
								lastName, 
								organization, 
								personalId);
					}
					catch(org.springframework.dao.DataAccessException e) {
						transactionManager.rollback(status);
						throw new DataAccessException(
								"Error executing SQL '" + SQL_INSERT_USER_PERSONAL + "' with parameters: " +
									username + ", " + 
									firstName + ", " + 
									lastName + ", " + 
									organization + ", " + 
									personalId, 
								e);
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
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
	
	/**
	 * Updates a user's password.
	 * 
	 * @param username The username of the user to be updated.
	 * 
	 * @param hashedPassword The new, hashed password for the user.
	 */
	public void updateUserPassword(String username, String hashedPassword, boolean setNewAccount) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a user's password.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Update the password.
			try {
				getJdbcTemplate().update(SQL_UPDATE_PASSWORD, hashedPassword, username);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_PASSWORD + "' with parameters: " + 
						hashedPassword + ", " + username, e);
			}
			
			// Ensure that this user is no longer a new user.
			try {
				getJdbcTemplate().update(SQL_UPDATE_NEW_ACCOUNT, setNewAccount, username);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error executing the following SQL '" + SQL_UPDATE_NEW_ACCOUNT + "' with parameters: " + 
						setNewAccount + ", " + username, e);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#activateUser(java.lang.String)
	 */
	public void activateUser(
			final String registrationId) 
			throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Activating a user's account.");
			
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Make the account not disabled.
			try {
				getJdbcTemplate().update(
						SQL_UPDATE_ENABLED_FROM_REGISTRATION_ID, 
						new Object[] { true, registrationId });
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing the following SQL '" + 
							SQL_UPDATE_ENABLED_FROM_REGISTRATION_ID + 
							"' with parameters: " + 
							true + ", " + 
							registrationId, 
						e);
			}
			
			// Update the accepted timestamp in the registration table.
			try {
				getJdbcTemplate().update(
						SQL_UPDATE_ACCEPTED_TIMESTAMP,
						new Object[] { 
								(new Date()).getTime(), 
								registrationId 
							}
					);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
						"Error executing the following SQL '" + 
							SQL_UPDATE_ENABLED_FROM_REGISTRATION_ID + 
							"' with parameter: " +
							registrationId, 
						e);
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
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserQueries#deleteExpiredRegistration(long)
	 */
	public void deleteExpiredRegistration(
			final long duration)
			throws DataAccessException {
		
		String sql =
			"DELETE u, ur " +
			"FROM user u, user_registration ur " +
			"WHERE u.id = ur.user_id " +
			"AND accepted_timestamp IS null " +
			"AND request_timestamp < ?";
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Activating a user's account.");
			
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = 
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			long earliestTime = (new Date()).getTime() - duration;
		
			try {
				getJdbcTemplate().update(sql, earliestTime);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error while executing SQL '" +
						sql +
						"' with parameter: " +
						earliestTime,
					e);
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
	
	/**
	 * Deletes all of the users in a Collection.
	 * 
	 * @param usernames A Collection of usernames for the users to delete.
	 */
	public void deleteUsers(Collection<String> usernames) throws DataAccessException {
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a user.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Delete the users.
			for(String username : usernames) {
				try {
					getJdbcTemplate().update(SQL_DELETE_USER, username);
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error executing the following SQL '" + SQL_DELETE_USER + "' with parameters: " + 
							username, e);
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
		}
		catch(TransactionException e) {
			throw new DataAccessException("Error while attempting to rollback the transaction.", e);
		}
	}
}
