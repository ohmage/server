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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.AccessRequest;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.IAccessRequestQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for creating, reading, 
 * updating, and deleting access_request. 
 * 
 * @author Hongsuda T.
 */
public class AccessRequestQueries extends Query implements IAccessRequestQueries {	
	private static Logger LOGGER = Logger.getLogger(AccessRequestQueries.class);
		
	// Inserts a new class.
	private static final String SQL_INSERT_REQUEST =
		"INSERT INTO access_request(uuid, user_id, email_address, type, content, status, creation_timestamp) " +
		"VALUES (?, (select id from user where username = ?), ?, ?, ?, ?, NOW())";
	

	// Returns a boolean as to whether or not the given class exists.
	private static final String SQL_EXISTS_REQUEST = 
		"SELECT EXISTS(" +
			"SELECT id " +
			"FROM access_request " +
			"WHERE uuid = ?" +
		")";
			

	private static final String SQL_GRANT_USER_SETUP_PRIVILEGES = 
		"UPDATE user u JOIN access_request ar ON (u.id = ar.user_id) " +
		"SET class_creation_privilege = true, user_setup_privilege = true " +
		"WHERE ar.uuid = ?";

	private static final String SQL_REVOKE_USER_SETUP_PRIVILEGES = 
			"UPDATE user u JOIN access_request ar ON (u.id = ar.user_id) " +
			"SET class_creation_privilege = false, user_setup_privilege = false " +
			"WHERE ar.uuid = ?";

	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private AccessRequestQueries(DataSource dataSource) {
		super(dataSource);		
	}
	
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupReqeustQueries#createAccessRequest(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createAccessRequest(
			final String requestId, 
			final String username, 
			final String emailAddress, 
			final JSONObject requestContent, 
			final String requestType,
			final String requestStatus) throws DataAccessException {
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating a new access_request.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Insert the class.
			try {
				getJdbcTemplate().update(SQL_INSERT_REQUEST, 
						new Object[] { requestId, username, emailAddress, requestType, requestContent.toString(), requestStatus} );
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + SQL_INSERT_REQUEST + "' with parameters: " +
						requestId + ", " + username + ", " + emailAddress + ", " + requestContent.toString() + ", " + requestStatus, 
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


	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#getRequestExists(java.lang.String)
	 */
	@Override
	public Boolean getRequestExists(String requestId) throws DataAccessException {
		
		try {
			return getJdbcTemplate().queryForObject(SQL_EXISTS_REQUEST, new Object[] { requestId }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_REQUEST + "' with parameters: " + requestId, e);
		}
	}


	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#getRequestExists(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean getRequestExists(String username, String requestType, String requestStatus) throws DataAccessException {
		StringBuilder sql = new StringBuilder(
				"SELECT EXISTS( " +
				 "SELECT ar.id " +
				 "FROM access_request ar JOIN user u on (ar.user_id = u.id) " +
				 "WHERE ");
		boolean firstPass = true;
		List<Object>parameters = new ArrayList<Object>(2);
		
		if (username != null) {
			sql.append(" u.username = ? ");
			firstPass = false;
			parameters.add(username);
		}
		
		if (requestType != null) {
			if (firstPass)
				sql.append("type = ? ");
			else 
				sql.append("AND type = ? ");	
			parameters.add(requestType);
		}
		
		if (requestStatus != null) {
			if (firstPass)
				sql.append("status = ? ");
			else 
				sql.append("AND status = ? ");	
			parameters.add(requestStatus);
		}
		sql.append(")");

		try {
			return getJdbcTemplate().queryForObject(sql.toString(), parameters.toArray(), Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + "' with parameters: " + parameters.toString(), e);
		}
	}


	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#getRequestExists(java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean getRequestsExist(Collection<String> requestIds) throws DataAccessException {
		
		StringBuilder sql = new StringBuilder("SELECT count(*) from access_request where uuid IN ");
		sql.append(StringUtils.generateStatementPList(requestIds.size()));

		try {
			int numPresentRequests = getJdbcTemplate().queryForObject(sql.toString(), requestIds.toArray(), Integer.class);
			if (numPresentRequests == requestIds.size()) {
				return true;
			} else {
				return false;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + "' with parameters: " + requestIds.toString(), e);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#getUserSetupPrivilegesExist(java.lang.String)
	 */
	@Override
	public Boolean getUserSetupPrivilegesExist(String username) throws DataAccessException {
		
		String sql = 
				"SELECT EXISTS ( " +
				  "SELECT id FROM user u WHERE u.username = ? " + 
				    "AND u.class_creation_privilege = true " +
				    "AND u.user_setup_privilege = true " +
				  ")";
	
		try {
			return getJdbcTemplate().queryForObject(sql, new Object[]{ username }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + "' with parameters: " + username.toString(), e);
		}
		
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserSetupRequestQueries#getUserCanAccessRequest(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public Boolean getUserCanAccessRequest(final String requestId, final String username) throws DataAccessException {
		// user can access request if the request belong to the user or the user is an admin
		String sql = "SELECT EXISTS ( " + 
					   "SELECT ar.id FROM access_request ar, user u " +
					   "WHERE ar.uuid = ? AND u.username = ? " + 
					     "AND (u.admin = true OR ar.user_id = u.id) " +
					 ")";

		try {
			return getJdbcTemplate().queryForObject(sql, new Object[]{ requestId, username }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql + "' with parameters: " + 
					requestId.toString() + ", " + username.toString(), e);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserSetupRequestQueries#getUserCanAccessRequests(
	 * java.util.Collection, java.lang.String)
	 */
	@Override
	public Boolean getUserCanAccessRequests(final Collection<String> requestIds, final String username) throws DataAccessException {
		// user can access request if the request belong to the user or the user is an admin
		StringBuilder sql = new StringBuilder(
				"SELECT COUNT(ar.id) FROM access_request ar, user u " +
				"WHERE u.username = ? " + 
				  "AND (u.admin = true OR ar.user_id = u.id) " +
				  "AND ar.uuid IN ");
		sql.append(StringUtils.generateStatementPList(requestIds.size()));
		
		List<Object> parameters = new LinkedList<Object>();
		parameters.add(username);
		parameters.addAll(requestIds);

		try {
			int numAccessibleRequests = getJdbcTemplate().queryForObject(sql.toString(), parameters.toArray(), Integer.class);
			if (numAccessibleRequests == requestIds.size())
				return true;
			else return false;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql + "' with parameters: " + 
					sql.toString() + ", " + parameters.toString(), e);
		}
	}

	

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserSetupRequestQueries#getAccessRequests(
	 * java.lang.String, java.util.Collection, java.util.Collection, java.util.Collection, java.util.Collection,
	 * org.ohmage.domain.UserSetupRequest.Status, org.joda.time.DateTime, org.joda.time.DateTime)
	 */
	@Override
	public List<AccessRequest> getAccessRequests(
			final String requester,
			final Collection<String> requestIds,
			final Collection<String> userIds,
			final Collection<String> emailAddressTokens,
			final Collection<String> requestContentTokens,
			final String requestType,
			final String requestStatus,
			final DateTime fromDate,
			final DateTime toDate)
		throws DataAccessException {	

		boolean firstPass = true;
		List<Object> parameters = new LinkedList<Object>();
		StringBuilder sql = new StringBuilder(
				"SELECT ar.uuid, u.username, ar.email_address, " +
				  "ar.content, ar.type, ar.status, ar.creation_timestamp, ar.last_modified_timestamp " +
				"FROM access_request ar JOIN user u ON (ar.user_id = u.id), " +
				"  user ru " +
				"WHERE ru.username = ? " + // requester 
				"  AND ( ru.admin = true OR u.username = ru.username ) "); // requester
		parameters.add(requester);
		
		// filter by uuid
		if ((requestIds != null) && (! requestIds.isEmpty())) {
			sql.append("AND ar.uuid in ");
			sql.append("( " + StringUtils.generateStatementPList(requestIds.size()) + " )");
			parameters.addAll(requestIds);
		}
				
		// filter by userIds
		if ((userIds != null) && (! userIds.isEmpty())) {
			sql.append("AND u.username in ");
			sql.append("( " + StringUtils.generateStatementPList(userIds.size()) + " )");
			parameters.addAll(userIds);
		}
		
		// filter by emailAddressTokens
		if ((emailAddressTokens != null) && (! emailAddressTokens.isEmpty())) {
			sql.append("AND (");
			firstPass = true;
			for (String token : emailAddressTokens) {
				if (firstPass) {
					sql.append("ar.email_address LIKE ? ");
					firstPass = false;
				} else { 
					sql.append("AND ar.email_address LIKE ? ");
				}
				parameters.add("%" + token + "%");
			}
			sql.append(") ");
		}

		// filter by requestContentTokens
		if ((requestContentTokens != null) && (! requestContentTokens.isEmpty())) {
			sql.append("AND (");
			firstPass = true;
			for (String token : requestContentTokens) {
				if (firstPass) {
					sql.append("ar.content LIKE ? ");
					firstPass = false;
				} else {
					sql.append("AND ar.content LIKE ? ");
				}
				parameters.add("%" + token + "%");
			}
			sql.append(") ");
		}

		// status
		// filter by status
		if (requestType != null) {
			sql.append("AND ar.type = ? ");
			parameters.add(requestType);
		}

		// status
		// filter by status
		if (requestStatus != null) {
			sql.append("AND ar.status = ? ");
			parameters.add(requestStatus);
		}
		
		// filter by fromDate
		if (fromDate != null) {
			sql.append("AND ar.creation_timestamp > ? ");
			parameters.add(fromDate.toString());
		}
		
		// filter by toDate
		if (toDate != null) {
			sql.append("AND ar.creation_timestamp < ? ");
			parameters.add(toDate.toString());
		}
		
		try {
			return getJdbcTemplate().query(
					sql.toString(), 
					parameters.toArray(),
					new RowMapper<AccessRequest>() {
						@Override
						public AccessRequest mapRow(
								final ResultSet rs, 
								final int rowNum) 
								throws SQLException {
							
							try {
								JSONObject requestContent = null;
								try {
									requestContent = new JSONObject(rs.getString("content"));
								}
								catch (JSONException e) {
									throw new DomainException(
											"Error parsing the request content.",
											e);
								}

								return new AccessRequest(
										rs.getString("uuid"),		
										rs.getString("username"),
										rs.getString("email_address"),
										requestContent,
										rs.getString("type"),
										rs.getString("status"),
										new DateTime(rs.getTimestamp("creation_timestamp").getTime()),
										new DateTime(rs.getTimestamp("last_modified_timestamp").getTime())
										);
							} 
							catch(DomainException e) {
								throw new SQLException(
										"Error creating the userSetupRequest.",
										e);
							}
						}
					});
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + sql + 
					" with parameters " + parameters.toString(),
					e);
		}
	}
	
	
	/**
	 * get request information from the database
	 * 
	 * @param requester The requester's username.
	 * 
	 * @param requestId The request uuid to get the information for.
	 * 
	 * @return AccessRequest corresponding to the requestId. Return null 
	 * 				if the request uuid doesn't exist or the user has 
	 * 				no right to access it. 
	 * 
	 * @throws DataAccessException
	 */
	public AccessRequest getAccessRequest(final String requester, String requestId) throws DataAccessException {
		if (requestId == null)
			return null;

		LinkedList<String> requestIds = new LinkedList<String>(); 
		requestIds.add(requestId);
		
		Collection<AccessRequest> requests = getAccessRequests(requester, requestIds, null, null, null, null, null, null, null);
		if (! requests.isEmpty()) {
			return requests.iterator().next();
		} else 
			return null;
	}
		
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#updateRequest(
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String )
	 */
	@Override
	public void updateAccessRequest(String requestId, String emailAddress, JSONObject requestContent, 
			String requestType, String requestStatus, Boolean updateUserPrivileges)
		throws DataAccessException {

		StringBuilder sql = new StringBuilder("UPDATE access_request SET ");
		boolean firstClause = true;
		List<Object> parameters = new LinkedList<Object>();
		String updateUserPrivilegesSql = null;
		
		if (emailAddress != null) {
			if (firstClause) {
				sql.append(" email_address = ? ");
				firstClause = false;
			} else 
				sql.append(", email_address = ? ");				
			parameters.add(emailAddress);
		}


		if (requestContent != null) {
			if (firstClause) {
				sql.append(" content = ? ");
				firstClause = false;
			} else 
				sql.append(", content = ? ");				
			parameters.add(requestContent.toString());
		}

		if (requestType != null) {
			if (firstClause) {
				sql.append(" type = ? ");
				firstClause = false;
			} else 
				sql.append(", type = ? ");
			parameters.add(requestType);
		}
		
		if (requestStatus != null) {
			if (firstClause) {
				sql.append(" status = ? ");
				firstClause = false;
			} else 
				sql.append(", status = ? ");
			parameters.add(requestStatus);
		}
		
		// if the flag is null, don't do anything. 
		if (updateUserPrivileges != null) {
			if (updateUserPrivileges == true) 
				updateUserPrivilegesSql = SQL_GRANT_USER_SETUP_PRIVILEGES; 
			else // if false, revoke user' privileges		
				updateUserPrivilegesSql = SQL_REVOKE_USER_SETUP_PRIVILEGES;	
		}

		sql.append("WHERE uuid = ? ");
		parameters.add(requestId);
		
		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Updating a access_request.");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);	
			
			// update the user setup request 
			try {
				LOGGER.debug("updating user setup table");
				getJdbcTemplate().update(sql.toString(), parameters.toArray());
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + sql.toString() + "' with parameter: " + parameters.toString(), e);
			}

			if (updateUserPrivilegesSql != null) {
				// update user's privileges
				try {
					LOGGER.debug("updating user setup privileges in the user table");
					getJdbcTemplate().update(updateUserPrivilegesSql, new Object[]{requestId});
				}
				catch(org.springframework.dao.DataAccessException e) {
					transactionManager.rollback(status);
					throw new DataAccessException("Error while executing SQL '" + updateUserPrivilegesSql + "' with parameter: " + requestId, e);
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
	
	/* (non-Javadoc)
	 * @see org.ohmage.query.impl.IUserSetupRequestQueries#deleteRequests()
	 */
	@Override
	public void deleteAccessRequests(Collection<String> requestIds) throws DataAccessException {
		// Deletes a request
		StringBuilder sql = new StringBuilder(
				"DELETE FROM access_request " + 
				"WHERE uuid IN ");
		sql.append(StringUtils.generateStatementPList(requestIds.size()));

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Deleting a access_request");
		
		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			
			// delete the request
			try {
				getJdbcTemplate().update(sql.toString(), requestIds.toArray());
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException("Error while executing SQL '" + sql.toString() + "' with parameter: " + requestIds.toString(), e);
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
