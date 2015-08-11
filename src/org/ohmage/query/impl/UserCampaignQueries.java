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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Campaign.Role;
import org.ohmage.domain.campaign.CampaignMask;
import org.ohmage.domain.campaign.CampaignMask.MaskId;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.DomainException;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.util.StringUtils;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-campaign relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 * @author Hongsuda T.
 */
public final class UserCampaignQueries extends Query implements IUserCampaignQueries {
	// Retrieves whether or not a user has any role in a campaign.
	private static final String SQL_EXISTS_USER_CAMPAIGN =
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM user u, campaign c, user_role_campaign urc " +
			"WHERE c.urn = ? " +
			"AND u.username = ? " +
			"AND c.id = urc.campaign_id " +
			"AND u.id = urc.user_id" +
		")";
	
	// Retrieves the users in a campaign.
	private static final String SQL_GET_USERS_IN_CAMPAIGN = 
		"SELECT u.username " +
		"FROM user u, campaign c, user_role_campaign urc " +
		"WHERE c.urn = ? " +
		"AND urc.campaign_id = c.id " +
		"AND urc.user_id = u.id";
	
	// Retrieves the roles for a user in a campaign.
	private static final String SQL_GET_USER_CAMPAIGN_ROLES =
		"SELECT ur.role " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
		"WHERE u.username = ? " +
		"AND u.id = urc.user_id " +
		"AND c.urn = ? " +
		"AND c.id = urc.campaign_id " +
		"AND urc.user_role_id = ur.id";
	
	// Retrieves all of the campaigns and respective roles for a user. Each row
	// is a unique campaign-role combination.
	private static final String SQL_GET_CAMPAIGNS_AND_ROLES_FOR_USER =
			"SELECT c.urn, ur.role " +
			"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
			"WHERE u.username = ? " +
			"AND u.id = urc.user_id " +
			"AND urc.campaign_id = c.id " +
			"AND urc.user_role_id = ur.id";
	
	// Retrieves all of the users for a campaign and all of their associated
	// roles.
	private static final String SQL_GET_USERS_AND_CAMPAIGN_ROLES = 
			"SELECT u.username, ur.role " +
			"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
			"WHERE c.urn = ? " +
			"AND c.id = urc.campaign_id " +
			"AND u.id = urc.user_id " +
			"AND ur.id = urc.user_role_id";

	// Retrieves the ID and name for all of the campaign to which the user is
	// associated.
	private static final String SQL_GET_CAMPAIGN_ID_AND_NAMES_FOR_USER = 
		"SELECT c.urn, c.name " +
		"FROM user u, campaign c, user_role_campaign urc " +
		"WHERE u.username = ? " +
		"AND u.id = urc.user_id " +
		"AND c.id = urc.campaign_id";
	
	// Retrieves the list of campaign IDs for all campaigns associated with a
	// user where the user has a specified role.
	private static final String SQL_GET_CAMPAIGN_IDS_FOR_USER_WITH_ROLE = 
		"SELECT c.urn " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
		"WHERE u.username = ? " +
		"AND u.id = urc.user_id " +
		"AND ur.role = ? " +
		"AND ur.id = urc.user_role_id " +
		"AND c.id = urc.campaign_id";
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserCampaignQueries(final DataSource dataSource) {
		super(dataSource);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#createUserCampaignMask(java.lang.String, java.lang.String, java.util.Set, java.util.UUID, long)
	 */
	@Override
	public void createUserCampaignMask(
		final CampaignMask mask)
		throws DataAccessException {

		// Create the transaction.
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Creating an observer.");

		try {
			// Begin the transaction.
			PlatformTransactionManager transactionManager =
				new DataSourceTransactionManager(getDataSource());
			TransactionStatus status = transactionManager.getTransaction(def);
			
			// Campaign mask creation SQL.
			final String campaignMaskSql =
				"INSERT INTO campaign_mask(" +
						"assigner_user_id, " +
						"assignee_user_id, " +
						"campaign_id, " +
						"mask_id, " +
						"creation_time) " +
					"VALUES (" +
						"(SELECT id FROM user WHERE username = ?), " +
						"(SELECT id FROM user WHERE username = ?), " +
						"(SELECT id FROM campaign WHERE urn = ?), " +
						"?, " +
						"?)";
			
			// Campaign mask creation statement with parameters.
			PreparedStatementCreator maskCreator =
				new PreparedStatementCreator() {
					/*
					 * (non-Javadoc)
					 * @see org.springframework.jdbc.core.PreparedStatementCreator#createPreparedStatement(java.sql.Connection)
					 */
					@Override
					public PreparedStatement createPreparedStatement(
							final Connection connection)
							throws SQLException {
						
						PreparedStatement ps =
							connection.prepareStatement(
								campaignMaskSql,
								new String[] { "id" });

						ps.setString(1, mask.getAssignerUserId());
						ps.setString(2, mask.getAssigneeUserId());
						ps.setString(3, mask.getCampaignId());
						ps.setString(4, mask.getId().toString());
						ps.setLong(5, mask.getCreationTime().getMillis());
						
						return ps;
					}

				};
				
			// The auto-generated key for the observer.
			KeyHolder maskKeyHolder = new GeneratedKeyHolder();
			
			// Create the observer.
			try {
				getJdbcTemplate().update(maskCreator, maskKeyHolder);
			}
			catch(org.springframework.dao.DataAccessException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error executing SQL '" + 
						campaignMaskSql + 
						"' with parameters: " +
						mask.getAssignerUserId() + ", " +
						mask.getAssigneeUserId() + ", " +
						mask.getCampaignId() + ", " +
						mask.getId().toString() + ", " +
						mask.getCreationTime().getMillis(),
					e);
			}
			
			// Get the mask's DB ID.
			long key = maskKeyHolder.getKey().longValue();
			
			// Create each of the masks.
			final String campaignMaskPromptIdSql =
				"INSERT INTO campaign_mask_survey_prompt_map(" +
						"campaign_mask_id, " +
						"survey_id, " +
						"prompt_id)" +
					"VALUES (?, ?, ?)";
			
			// Get the survey IDs from the mask.
			Map<String, Set<String>> promptIds = mask.getSurveyPromptMap();
			
			// Create the list of parameters for each of the survey IDs.
			List<Object[]> maskPromptIdParameters =
				new ArrayList<Object[]>(promptIds.size());
				
			// Cycle through the survey IDs building the parameters list.
			for(String surveyId : promptIds.keySet()) {
				for(String promptId : promptIds.get(surveyId)) {
					maskPromptIdParameters
						.add(new Object[] { key, surveyId, promptId });
				}
			}
			
			// Add the mask survey IDs.
			getJdbcTemplate()
				.batchUpdate(campaignMaskPromptIdSql, maskPromptIdParameters);

			// Commit the transaction.
			try {
				transactionManager.commit(status);
			}
			catch(TransactionException e) {
				transactionManager.rollback(status);
				throw new DataAccessException(
					"Error while committing the transaction.",
					e);
			}
		}
		catch(TransactionException e) {
			throw new DataAccessException(
				"Error while attempting to rollback the transaction.",
				e);
		}
		
		
	}
	
	/**
	 * Retrieves whether or not a user belongs to a campaign in any capacity.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @param campaignId The campaign ID of the campaign in question.
	 * 
	 * @return Whether or not the user exists in a campaign.
	 */
	public boolean userBelongsToCampaign(String username, String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().queryForObject(SQL_EXISTS_USER_CAMPAIGN, new Object[] { campaignId, username }, Boolean.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_EXISTS_USER_CAMPAIGN + "' with parameters: " +
					campaignId + ", " + username, e);
		}
	}
	
	/**
	 * Retrieves all of the users from a campaign.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return A List of usernames for the users in the campaign.
	 */
	public List<String> getUsersInCampaign(String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(SQL_GET_USERS_IN_CAMPAIGN, new Object[] { campaignId }, new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_USERS_IN_CAMPAIGN + "' with parameter: " +
					campaignId, e);
		}
	}
	
	/**
	 * Returns a List of roles for this user in this campaign.
	 * 
	 * @param username The username of the user that whose roles are desired.
	 * 
	 * @param campaignId The campaign ID for the campaign that the user's roles
	 * 					 are being requested.
	 * 
	 * @return A possibly empty List of roles for this user in this campaign.
	 */
	public List<Campaign.Role> getUserCampaignRoles(String username, String campaignId) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_USER_CAMPAIGN_ROLES, 
					new Object[] { username, campaignId }, 
					new RowMapper<Campaign.Role>() {
						@Override
						public Campaign.Role mapRow(ResultSet rs, int rowNum) throws SQLException {
							return Campaign.Role.getValue(rs.getString("role"));
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_CAMPAIGN_ROLES + "' with parameters: " + 
					username + ", " + campaignId, e);
		}
	}
	
	/**
	 * Returns a List of roles for this user in a set of campaign.
	 * 
	 * @param username The username of the user that whose roles are desired.
	 * 
	 * @param campaignId The campaign ID for the campaign that the user's roles
	 * 					 are being requested.
	 * 
	 * @return A possibly empty List of roles for this user in this campaign.
	 */
	public Map<String, Collection<Campaign.Role>> getCampaignsAndRolesForUserAndCampaigns(
			final String username, 
			final String campaignListSubSelect,
			final Collection<Object> SubSelectParameters) throws DataAccessException {

		List<Object> parameters = new LinkedList<Object>();
		
		StringBuilder sql = new StringBuilder(
			 	"SELECT c.urn, GROUP_CONCAT(ur.role SEPARATOR ',') roles " + 
			 	"FROM user u JOIN user_role_campaign urc on (u.id = urc.user_id) " +
			 	"  JOIN campaign c ON (c.id = urc.campaign_id) " +
			 	"  JOIN user_role ur on (ur.id = urc.user_role_id) " + 
		     	"WHERE u.username = ? " +
		     	"  AND c.id in ");
		sql.append(	   "(" + campaignListSubSelect + ")");
		sql.append(" GROUP BY c.id");
		
		parameters.add(username);
		parameters.addAll(SubSelectParameters);
		
		final Map<String, Collection<Campaign.Role>> campaignUserRoles = new HashMap<String, Collection<Campaign.Role>>();

		try {			
			getJdbcTemplate().query(
					sql.toString(),
					parameters.toArray(),
					new RowMapper<Object> () {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {				
							Collection<Campaign.Role> userRoles = new LinkedList<Campaign.Role>();
							for (String role : rs.getString("roles").split(",")){
								userRoles.add(Campaign.Role.getValue(role));
							}
							campaignUserRoles.put(rs.getString("urn"), userRoles);
							return null;
						}
					}
		);
		
		return campaignUserRoles;
		} 
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + sql + 
					"' with parameters: " + campaignListSubSelect, 
					e);
		}
			
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getCampaignAndRolesForUser(java.lang.String)
	 */
	// deprecated. It is inefficient to get information for one campaign at a time. 
	public Map<String, Set<Campaign.Role>> getCampaignAndRolesForUser(
			final String username)
			throws DataAccessException {
		
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGNS_AND_ROLES_FOR_USER,
					new Object[] { username },
					new ResultSetExtractor<Map<String, Set<Campaign.Role>>>() {
						/**
						 * Extracts the data combining the roles into the same
						 * set. 
						 */
						@Override
						public Map<String, Set<Role>> extractData(
								final ResultSet rs)
								throws SQLException,
								org.springframework.dao.DataAccessException {
							
							Map<String, Set<Campaign.Role>> result =
									new HashMap<String, Set<Campaign.Role>>();
							
							while(rs.next()) {
								Set<Campaign.Role> roles =
										result.get(rs.getString("urn"));
								
								if(roles == null) {
									roles = new HashSet<Campaign.Role>();
									result.put(rs.getString("urn"), roles);
								}
								
								try {
									roles.add(
											Campaign.Role.getValue(
													rs.getString("role")));
								}
								catch(IllegalArgumentException e) {
									throw new SQLException(
											"The role is not a valid role.",
											e);
								}
							}
							
							return result;
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
						SQL_GET_CAMPAIGNS_AND_ROLES_FOR_USER
						+ "' with parameter: " + 
						username, 
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getCampaignAndRolesForUsers(java.lang.String, java.lang.Collection)
	 */
	public Map<String, Map<String, Set<Campaign.Role>>> getCampaignAndRolesForUsers(
			final String userSubSelectStmt,
			final Collection<Object> userSubSelectParameters)
			throws DataAccessException {
		

		// the user_campaign_roles are collapsed into a string separated by "," by mysql GROUP_CONCAT.
		// If a different database is used, GROUP_CONCAT needs to be replaced with something equivalent,
		// or return individual roles and implement the role aggregation in Java. 
		StringBuilder sql = 
				new StringBuilder(
						"select u.username, c.urn, GROUP_CONCAT(ur.role SEPARATOR ',') roles " + 
						"from user u join user_role_campaign urc on (u.id = urc.user_id) "+
						  "join campaign c on (c.id = urc.campaign_id) " +
						  "join user_role ur on (ur.id = urc.user_role_id) " +
						"WHERE " +
						  "u.id in ");
		sql.append(" ( " + userSubSelectStmt + " ) ");
		sql.append(     " GROUP BY u.username, c.urn");
	
		try {
			final Map<String, Map<String, Set<Campaign.Role>>> userCampaignRoleMap = 
					new HashMap<String, Map<String, Set<Campaign.Role>>>();
			
			getJdbcTemplate().query(
					sql.toString(), 
					userSubSelectParameters.toArray(), 
					new RowMapper<Object>() {
						@Override
						public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							try {
								
								String username = rs.getString("username");
								String urn = rs.getString("urn");
								String roles = rs.getString("roles");
								
								// create Campaign.roles
								Set<Campaign.Role> croles = new HashSet<Campaign.Role>();
								for (String eachRole : roles.split(",")) {
									try {
										croles.add(Campaign.Role.getValue(eachRole));
									} catch (IllegalArgumentException e) {
										throw new SQLException("The role is invalid:", eachRole);
									}
								}
								
								Map<String, Set<Campaign.Role>> campaignRoles = userCampaignRoleMap.get(username);
								if (campaignRoles == null) {
									campaignRoles = new HashMap<String, Set<Campaign.Role>>();
									userCampaignRoleMap.put(username, campaignRoles);
								} 
								campaignRoles.put(urn,croles);								
								return null;
							} 
							catch (Exception e) {
								throw new SQLException("Can't create a role with parameters: " + 
										rs.getString("username") + "," + rs.getString("urn") + "," + rs.getString("roles"), e);
							}
						}
					}
				);
			return userCampaignRoleMap;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql.toString() + 
					"' with parameters: " + userSubSelectStmt, e);
		}
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getUsersAndRolesForCampaign(java.lang.String)
	 */
	@Override
	public Map<String, Collection<Role>> getUsersAndRolesForCampaign(
			final String campaignId)
			throws DataAccessException {
		
		try {
			final Map<String, Collection<Campaign.Role>> result =
					new HashMap<String, Collection<Campaign.Role>>();
			
			getJdbcTemplate().query(
					SQL_GET_USERS_AND_CAMPAIGN_ROLES,
					new Object[] { campaignId },
					new RowMapper<Object>() {
						/**
						 * Adds the user's role to the map.
						 */
						@Override
						public Object mapRow(ResultSet rs, int rowNum)
								throws SQLException {
							
							String username = rs.getString("username");
							Collection<Campaign.Role> roles = result.get(username);
							
							if(roles == null) {
								roles = new HashSet<Campaign.Role>();
								result.put(username, roles);
							}
							
							roles.add(
									Campaign.Role.getValue(
											rs.getString("role")));
							
							return null;
						}
					});
			
			return result;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							SQL_GET_USERS_AND_CAMPAIGN_ROLES + 
						"' with parameters: " + 
							campaignId, 
					e);
		}
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getUsersAndRolesForCampaignList(java.lang.String)
	 */
	@Override
	public Map<String, Map<String, Collection<Role>>> getUsersAndRolesForCampaigns(
			final String subSelectStmt, 
			final Collection<Object> subSelectParameters)
			throws DataAccessException {

		StringBuilder sql = new StringBuilder(
			 	"SELECT c.urn, u.username, GROUP_CONCAT(ur.role SEPARATOR ',') roles " + 
			 	"FROM user u JOIN user_role_campaign urc on (u.id = urc.user_id) " +
			 	"  JOIN campaign c ON (c.id = urc.campaign_id) " +
			 	"  JOIN user_role ur on (ur.id = urc.user_role_id) " + 
		     	"WHERE c.id in ");
		sql.append(	   "(" + subSelectStmt + ")");
		sql.append(" GROUP BY c.id, u.id");
	
		final Map<String, Map<String, Collection<Campaign.Role>>> campaignUserRoleMap = 
				new HashMap<String, Map<String, Collection<Campaign.Role>>>();

		try {

			getJdbcTemplate().query(
					sql.toString(), 
					subSelectParameters.toArray(), 
					new RowMapper<Object>() {
						@Override
						public Object mapRow(final ResultSet rs, final int rowNum) throws SQLException {
							try {
								
								String username = rs.getString("username");
								String urn = rs.getString("urn");
								String roles = rs.getString("roles");
								
								// create Campaign.roles
								Set<Campaign.Role> croles = new HashSet<Campaign.Role>();
								for (String eachRole : roles.split(",")) {
									try {
										croles.add(Campaign.Role.getValue(eachRole));
									} catch (IllegalArgumentException e) {
										throw new SQLException("The role is invalid:", eachRole);
									}
								}
								
								Map<String, Collection<Campaign.Role>> userRoles = campaignUserRoleMap.get(urn);
								if (userRoles == null) {
									userRoles = new HashMap<String, Collection<Campaign.Role>>();
									campaignUserRoleMap.put(urn, userRoles);
								} 
								userRoles.put(username,croles);								
								return null;
							} 
							catch (Exception e) {
								throw new SQLException("Can't create a role with parameters: " + 
										rs.getString("username") + "," + rs.getString("urn") + "," + rs.getString("roles"), e);
							}
						}
					}
				);
			return campaignUserRoleMap;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + sql + 
					"' with parameters: " + subSelectStmt, 
					e);
		}
				
		
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getAuthorsForCampaign(java.lang.String)
	 */
	@Override
	public List<String> getAuthorsForCampaign(
			final String campaignId)
			throws DataAccessException {
		
		String sql_get_authors = 
				 "SELECT u.username " + 
			     "FROM user u JOIN user_role_campaign urc on (u.id = urc.user_id) " +
			     "  JOIN campaign c ON (c.id = urc.campaign_id) " +
			     "  JOIN user_role ur on (ur.id = urc.user_role_id) " + 
			     "WHERE c.urn = ? AND ur.role = 'author'";
		
		try {		
			
			return getJdbcTemplate().query(
						sql_get_authors,
						new Object[] { campaignId },
						new SingleColumnRowMapper<String>());			
		} 
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + 
							sql_get_authors + 
						"' with parameters: " + 
							campaignId, 
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getAuthorsForCampaignList(java.lang.String)
	 */
	public Map<String, Collection<String>> getAuthorsForCampaigns(
			final String campaignListSubSelect, 
			final Collection<Object> parameters)
			throws DataAccessException {
		
		StringBuilder sql = new StringBuilder(
				 	"SELECT c.urn, GROUP_CONCAT(u.username SEPARATOR ',') authors " + 
				 	"FROM user u JOIN user_role_campaign urc on (u.id = urc.user_id) " +
				 	"  JOIN campaign c ON (c.id = urc.campaign_id) " +
				 	"  JOIN user_role ur on (ur.id = urc.user_role_id) " + 
			     	"WHERE ur.role = 'author' AND c.id in ");
		sql.append(	   "(" + campaignListSubSelect + ")");
		sql.append(" GROUP BY c.id");
		
		final Map<String, Collection<String>> campaignAuthors = new HashMap<String, Collection<String>>();

		try {			
			getJdbcTemplate().query(
						sql.toString(),
						parameters.toArray(),
						new RowMapper<Object> () {
							@Override
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {				
								// List<String> authors = new ArrayList<String>(Arrays.asList(rs.getString("authors").split(",")));
								Collection<String> authors = Arrays.asList(rs.getString("authors").split(","));
								campaignAuthors.put(rs.getString("urn"), authors);
								return null;
							}
						}
			);
			
			return campaignAuthors;
		} 
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException(
					"Error executing SQL '" + sql + 
					"' with parameters: " + campaignListSubSelect, 
					e);
		}
	}
	
	
	/**
	 * Retrieves all of the campaign IDs and their respective names to which a
	 * user is associated.
	 * 
	 * @param username The username of the user.
	 * 
	 * @return A Map of campaign IDs to campaign names for all of the campaigns
	 * 		   to which the user is associated.
	 */
	public Map<String, String> getCampaignIdsAndNamesForUser(String username) throws DataAccessException {
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_ID_AND_NAMES_FOR_USER, 
					new Object[] { username }, 
					new RowMapper<Object> () {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							result.put(rs.getString("urn"), rs.getString("name"));
							return null;
						}
					}
				);
			
			return result;
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_ID_AND_NAMES_FOR_USER + "' with parameter: " + username, e);
		}
	}
	
	/**
	 * Retrieves all of the campaign IDs that are associated with a user and
	 * the user has a specific role.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param role The campaign role that the user must have.
	 * 
	 * @return A List of unique identifiers for all campaigns with which the 
	 * 		   user is associated and has the given role. 
	 */
	public List<String> getCampaignIdsForUserWithRole(String username, Campaign.Role role) throws DataAccessException {
		try {
			return getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_IDS_FOR_USER_WITH_ROLE, 
					new Object[] { username, role.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_IDS_FOR_USER_WITH_ROLE + "' with parameters: " + 
					username + ", " + role, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getCampaignMask(org.ohmage.domain.campaign.CampaignMask.MaskId, org.joda.time.DateTime, org.joda.time.DateTime, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public List<CampaignMask> getCampaignMasks(
		final MaskId maskId,
		final DateTime startDate,
		final DateTime endDate,
		final String assignerUserId,
		final String assigneeUserId,
		final String campaignId)
		throws DataAccessException {
		
		// Build the default SQL.
		StringBuilder sqlBuilder =
			new StringBuilder(
				"SELECT " +
					"cm.mask_id AS mask_id, " +
					"cm.creation_time AS creation_time, " +
					"assigner_user.username AS assigner_user_id, " +
					"assignee_user.username AS assignee_user_id, " +
					"c.urn AS campaign_id, " +
					"cmpi.survey_id AS survey_id, " +
					"cmpi.prompt_id AS prompt_id " +
				"FROM " +
					"user assigner_user, " +
					"user assignee_user, " +
					"campaign c, " +
					"campaign_mask cm, " +
					"campaign_mask_survey_prompt_map cmpi " +
				"WHERE assigner_user.id = cm.assigner_user_id " +
				"AND assignee_user.id = cm.assignee_user_id " +
				"AND c.id = cm.campaign_id " +
				"AND cm.id = cmpi.campaign_mask_id");
		// Build the list of required parameters.
		List<Object> parameters = new LinkedList<Object>();
		
		// If the mask is given, add it to the SQL and the parameters list.
		if(maskId != null) {
			sqlBuilder.append(" AND cm.id = ?");
			parameters.add(maskId.toString());
		}
		
		// If the start date is given, add it to the SQL and the parameters
		// list.
		if(startDate != null) {
			sqlBuilder.append(" AND cm.creation_time >= ?");
			parameters.add(startDate.getMillis());
		}
		
		// If the end date is given, add it to the SQL and the parameters.
		if(endDate != null) {
			sqlBuilder.append(" AND cm.creation_time <= ?");
			parameters.add(endDate.getMillis());
		}
		
		// If the assigner's user ID was given, add it to the SQL and the
		// parameters.
		if(assignerUserId != null) {
			sqlBuilder.append(" AND assigner_user.username = ?");
			parameters.add(assignerUserId);
		}
		
		// If the assignee's user ID was given, add it to the SQL and the
		// parameters.
		if(assigneeUserId != null) {
			sqlBuilder.append(" AND assignee_user.username = ?");
			parameters.add(assigneeUserId);
		}
		
		// If the campaign's ID was given, add it to the SQL and the
		// parameters.
		if(campaignId != null) {
			sqlBuilder.append(" AND c.urn = ?");
			parameters.add(campaignId);
		}

		// Make the query and return the results.
		try {
			return
				getJdbcTemplate().query(
					sqlBuilder.toString(), 
					parameters.toArray(),
					new ResultSetExtractor<List<CampaignMask>>() {
						/*
						 * (non-Javadoc)
						 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
						 */
						@Override
						public List<CampaignMask> extractData(
							final ResultSet resultSet)
							throws SQLException,
							org.springframework.dao.DataAccessException {
							
							// Create a map of mask IDs to their builders.
							Map<MaskId, CampaignMask.Builder> builders =
								new HashMap<MaskId, CampaignMask.Builder>();
							
							// For each of the results, create a new builder if
							// the mask has never been seen before or just 
							// append to the list of survey IDs if it has been
							// seen before.
							while(resultSet.next()) {
								MaskId maskId;
								try {
									maskId =
										MaskId
											.decodeString(
												resultSet
													.getString("mask_id"));
								}
								catch(DomainException e) {
									throw
										new SQLException(
											"Error decoding the mask ID.",
											e);
								}
								
								// Attempt to get the builder.
								CampaignMask.Builder builder =
									builders.get(maskId);
								
								// If the builder doesn't exist, create it.
								if(builder == null) {
									// Create the builder.
									builder = new CampaignMask.Builder();
									
									// Add the builder to the map.
									builders.put(maskId, builder);
									
									// Set the mask ID.
									builder.setMaskId(maskId);
									
									// Get the creation time and set it.
									DateTime creationTime = 
										new DateTime(
											resultSet
												.getLong("creation_time"));
									builder.setCreationTime(creationTime);
									
									// Get the assigner's user ID and set it.
									String assignerUserId =
										resultSet
											.getString("assigner_user_id");
									builder.setAssignerUserId(assignerUserId);
									
									// Get the assignee's user ID and set it.
									String assigneeUserId =
										resultSet
											.getString("assignee_user_id");
									builder.setAssigneeUserId(assigneeUserId);
									
									// Get the campaign's ID and set it.
									String campaignId =
										resultSet
											.getString("campaign_id");
									builder.setCampaignId(campaignId);
									
									// Set the default set of survey IDs to be
									// empty.
									builder
										.setPromptIds(
											new HashMap<String, Set<String>>());
								}
								
								// Add the survey ID.
								builder
									.addPromptId(
										resultSet.getString("survey_id"),
										resultSet.getString("prompt_id"));
							}
							
							// Cycle through the builders, build them, and add
							// them to the result list.
							List<CampaignMask> result =
								new ArrayList<CampaignMask>(builders.size());
							for(CampaignMask.Builder builder : builders.values()) {
								try {
									result.add(builder.build());
								}
								catch(DomainException e) {
									throw
										new SQLException(
											"There was a problem building the campaign mask.",
											e);
								}
							}
							
							// Return the result.
							return result;
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw
				new DataAccessException(
					"Error executing SQL '" + 
							sqlBuilder.toString() + 
						"' with parameters: " + 
							parameters.toString(),
					e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.query.IUserCampaignQueries#getCampaignMaskForCampaignList
	 */
	@Override
	public Map<String, Collection<CampaignMask>> getCampaignMasksForCampaigns(
		final String campaignSubSelectStmt,
		final Collection<Object> subSelectParameters,
		final MaskId maskId,
		final DateTime startDate,
		final DateTime endDate,
		final String assignerUserId,
		final String assigneeUserId)
		throws DataAccessException {
		
		// Build the default SQL.
		StringBuilder sqlBuilder =
			new StringBuilder(
				"SELECT " +
					"cm.mask_id AS mask_id, " +
					"cm.creation_time AS creation_time, " +
					"assigner_user.username AS assigner_user_id, " +
					"assignee_user.username AS assignee_user_id, " +
					"c.urn AS campaign_id, " +
					"cmpi.survey_id AS survey_id, " +
					"cmpi.prompt_id AS prompt_id " +
				"FROM " +
					"user assigner_user, " +
					"user assignee_user, " +
					"campaign c, " +
					"campaign_mask cm, " +
					"campaign_mask_survey_prompt_map cmpi " +
				"WHERE assigner_user.id = cm.assigner_user_id " +
				"AND assignee_user.id = cm.assignee_user_id " +
				"AND c.id = cm.campaign_id " +
				"AND cm.id = cmpi.campaign_mask_id");
		// Build the list of required parameters.
		List<Object> parameters = new LinkedList<Object>();
		
		// If the mask is given, add it to the SQL and the parameters list.
		if(maskId != null) {
			sqlBuilder.append(" AND cm.id = ?");
			parameters.add(maskId.toString());
		}
		
		// If the start date is given, add it to the SQL and the parameters
		// list.
		if(startDate != null) {
			sqlBuilder.append(" AND cm.creation_time >= ?");
			parameters.add(startDate.getMillis());
		}
		
		// If the end date is given, add it to the SQL and the parameters.
		if(endDate != null) {
			sqlBuilder.append(" AND cm.creation_time <= ?");
			parameters.add(endDate.getMillis());
		}
		
		// If the assigner's user ID was given, add it to the SQL and the
		// parameters.
		if(assignerUserId != null) {
			sqlBuilder.append(" AND assigner_user.username = ?");
			parameters.add(assignerUserId);
		}
		
		// If the assignee's user ID was given, add it to the SQL and the
		// parameters.
		if(assigneeUserId != null) {
			sqlBuilder.append(" AND assignee_user.username = ?");
			parameters.add(assigneeUserId);
		}
		
		// If the campaign's ID was given, add it to the SQL and the
		// parameters.
		if(campaignSubSelectStmt != null) {
			sqlBuilder.append(" AND c.id IN ");
			sqlBuilder.append("( " + campaignSubSelectStmt + " )");
			parameters.addAll(subSelectParameters);
		}

		// Make the query and return the results.
		try {
			return
				getJdbcTemplate().query(
					sqlBuilder.toString(), 
					parameters.toArray(),
					new ResultSetExtractor<Map<String, Collection<CampaignMask>>>() {
						/*
						 * (non-Javadoc)
						 * @see org.springframework.jdbc.core.ResultSetExtractor#extractData(java.sql.ResultSet)
						 */
						@Override
						public Map<String, Collection<CampaignMask>> extractData(
							final ResultSet resultSet)
							throws SQLException,
							org.springframework.dao.DataAccessException {
							
							// create a map between campaign and MaskId 
							Map<String, List<MaskId>> campaignMaskIdList = new HashMap<String, List<MaskId>>();
							
							// Create a map of mask IDs to their builders.
							Map<MaskId, CampaignMask.Builder> builders =
								new HashMap<MaskId, CampaignMask.Builder>();
							
							// For each of the results, create a new builder if
							// the mask has never been seen before or just 
							// append to the list of survey IDs if it has been
							// seen before.
							while(resultSet.next()) {
								MaskId maskId;
								try {
									maskId = MaskId.decodeString(resultSet.getString("mask_id"));
								}
								catch(DomainException e) {
									throw
										new SQLException("Error decoding the mask ID.",	e);
								}
			
								String campaignId = resultSet.getString("campaign_id");
								
								// Attempt to get the builder.
								CampaignMask.Builder builder = builders.get(maskId);
								
								// If the builder doesn't exist, create it.
								if(builder == null) {
									// Create the builder.
									builder = new CampaignMask.Builder();
									
									// Add the builder to the map.
									builders.put(maskId, builder);
									
									// Set the mask ID.
									builder.setMaskId(maskId);
									
									// Get the creation time and set it.
									DateTime creationTime = new DateTime(resultSet.getLong("creation_time"));
									builder.setCreationTime(creationTime);
									
									// Get the assigner's user ID and set it.
									String assignerUserId = resultSet.getString("assigner_user_id");
									builder.setAssignerUserId(assignerUserId);
									
									// Get the assignee's user ID and set it.
									String assigneeUserId = resultSet.getString("assignee_user_id");
									builder.setAssigneeUserId(assigneeUserId);
									
									// Get the campaign's ID and set it.
									//String campaignId = resultSet.getString("campaign_id");
									builder.setCampaignId(campaignId);
									
									// Set the default set of survey IDs to be
									// empty.
									builder.setPromptIds(new HashMap<String, Set<String>>());
								}
								
								// Add the survey ID.
								builder
									.addPromptId(
										resultSet.getString("survey_id"),
										resultSet.getString("prompt_id"));

								
								List<MaskId> maskIdList = campaignMaskIdList.get(campaignId);
								if (maskIdList == null) {
									maskIdList = new LinkedList<MaskId>();
									campaignMaskIdList.put(campaignId,  maskIdList);
								}
								maskIdList.add(maskId);
								
							}
					
							// create a map between campaign and Masks
							Map<String, Collection<CampaignMask>> campaignMasks = new HashMap<String, Collection<CampaignMask>>();
							for (String campaignId : campaignMaskIdList.keySet()){
								List<MaskId> maskIdList = campaignMaskIdList.get(campaignId);
								List<CampaignMask> maskList = new ArrayList<CampaignMask>(maskIdList.size());
								for (MaskId maskId : maskIdList) {
									try { 
										maskList.add(builders.get(maskId).build());
									} catch(DomainException e) {
									throw new SQLException(
											"There was a problem building the campaign mask.",
											e);
									}
								}	
								campaignMasks.put(campaignId,maskList);
							}

							// Return the result.
							return campaignMasks;
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw
				new DataAccessException(
					"Error executing SQL '" + 
							sqlBuilder.toString() + 
						"' with parameters: " + 
							parameters.toString(),
					e);
		}
	}
}