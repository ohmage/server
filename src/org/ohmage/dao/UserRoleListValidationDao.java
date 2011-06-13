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

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * Validates that all the users in the list exist and that all the roles in
 * the list exist.
 * 
 * @author John Jenkins
 */
public class UserRoleListValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleListValidationDao.class);
	
	private static final String SQL_GET_USER_EXISTS = "SELECT count(*) " +
													  "FROM user " +
													  "WHERE username = ?";
	
	private static final String SQL_GET_CAMPAIGN_ROLE_EXISTS = "SELECT count(*) " +
															   "FROM user_role " +
															   "WHERE role = ?";
	
	private static final String SQL_GET_USER_ROLES = "SELECT ur.role " +
													 "FROM user u, user_role ur, user_role_campaign urc, campaign c " +
													 "WHERE u.username = ?" +
													 "AND u.id = urc.user_id " +
													 "AND c.urn = ? " +
													 "AND c.id = urc.campaign_id " +
													 "AND ur.id = urc.user_role_id";
	
	private AwRequestAnnotator _invalidUsernameAnnotator;
	private AwRequestAnnotator _invalidRoleAnnotator;
	private AwRequestAnnotator _insufficientPermissionsAnnotator;
	
	private String _key;
	private boolean _required;
	
	/**
	 * Creates this DAO with the parameterized DataSource.
	 * 
	 * It also includes an invalidUsernameAnnotator to use when a username in
	 * the request is invalid as well as an invalidRoleAnnotator to use when a
	 * role in the request is invalid.
	 * 
	 * It also includes the key to use when querying the toProcess map to get
	 * the list object and a required flag to indicate if this parameter is
	 * required or can be skipped if missing.
	 * 
	 * @param dataSource The DataSource to use for all the DAO's queries.
	 * 
	 * @param invalidUsernameAnnotator The annotator to use reply with if a
	 * 								   username in the list is invalid.
	 * 
	 * @param invalidRoleAnnotator The annotator to use if a role in the list
	 * 							   is invalid.
	 * 
	 * @param key The key to use to lookup the list in the toProcess map.
	 * 
	 * @param required Whether or not this value is required in the query.
	 */
	public UserRoleListValidationDao(DataSource dataSource, 
									 AwRequestAnnotator invalidUsernameAnnotator, AwRequestAnnotator invalidRoleAnnotator, AwRequestAnnotator insufficientPermissionsAnnotator,
									 String key, boolean required) {
		super(dataSource);
		
		if(invalidUsernameAnnotator == null) {
			throw new IllegalArgumentException("invalidUsernameAnnotator cannot be null.");
		}
		else if(invalidRoleAnnotator == null) {
			throw new IllegalArgumentException("invalidRoleAnnotator cannot be null.");
		}
		else if(insufficientPermissionsAnnotator == null) {
			throw new IllegalArgumentException("insufficientPermissionsAnnotator cannot be null.");
		}
		else if(key == null) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		_invalidUsernameAnnotator = invalidUsernameAnnotator;
		_invalidRoleAnnotator = invalidRoleAnnotator;
		_insufficientPermissionsAnnotator = insufficientPermissionsAnnotator;
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that all the usernames in the list exist and that all the
	 * roles in the request exist.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the list of user-roles if they are present.
		String userAndRoleList;
		try {
			userAndRoleList = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			// If they didn't exist and were required, throw an error as this
			// is a logical error.
			if(_required) {
				_logger.error("Missing required " + _key + " value in the toProcess map for service validation.");
				throw new DataAccessException(e);
			}
			// If it doesn't exist and isn't required, then return.
			else {
				_logger.info("No user-role list in the toProcess map and it is not required, so skipping service validation.");
				return;
			}
		}
		
		// Get the list of roles for the currently logged in user.
		List<?> userRoles;
		try {
			userRoles = getJdbcTemplate().query(SQL_GET_USER_ROLES, 
												new Object[] { awRequest.getUser().getUserName(), awRequest.getCampaignUrn() },
												new SingleColumnRowMapper());
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error while executing SQL '" + SQL_GET_USER_ROLES + "' with parameters: " + awRequest.getUser().getUserName() + ", " + awRequest.getCampaignUrn(), e);
			throw new DataAccessException(e);
		}
		
		boolean addAnyRole = userRoles.contains(CampaignRoleCache.ROLE_SUPERVISOR);
		boolean addAuthors = userRoles.contains(CampaignRoleCache.ROLE_AUTHOR);
		if(!addAnyRole && !addAuthors) {
			_insufficientPermissionsAnnotator.annotate(awRequest, "The user doesn't have sufficient permissions to add any users with roles.");
			awRequest.setFailedRequest(true);
			return;
		}
		
		// Check each of the user-role pairs.
		String[] userAndRoleArray = userAndRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < userAndRoleArray.length; i++) {
			String[] userAndRole = userAndRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			String user = userAndRole[0];
			String role = userAndRole[1];
			
			// If the user doesn't exist, reject the request.
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_USER_EXISTS, new Object[] { user }) == 0) {
					_invalidUsernameAnnotator.annotate(awRequest, "The user, " + user + ", does not exist.");
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_GET_USER_EXISTS + "' with parameter: " + user, e);
				throw new DataAccessException(e);
			}
			
			// If the role doesn't exist, reject the request.
			try {
				if(getJdbcTemplate().queryForInt(SQL_GET_CAMPAIGN_ROLE_EXISTS, new Object[] { role }) == 0) {
					_invalidRoleAnnotator.annotate(awRequest, "The role, " + role + ", does not exist.");
					awRequest.setFailedRequest(true);
					return;
				}
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error while executing SQL '" + SQL_GET_CAMPAIGN_ROLE_EXISTS + "' with parameter: " + role, e);
				throw new DataAccessException(e);
			}
			
			// If the user cannot add any role, then check that they can add
			// authors and that this role is an author.
			if(!addAnyRole) {
				if(!addAuthors || !"author".equals(role)) {
					_insufficientPermissionsAnnotator.annotate(awRequest, "The user is not allowed to add users with the role: " + role);
					awRequest.setFailedRequest(true);
					return;
				}
			}
		}
	}
}
