package org.ohmage.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;


/**
 * This class contains all of the functionality for reading and writing 
 * information specific to user-campaign relationships.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class UserCampaignQueries extends Query {
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
	
	private static UserCampaignQueries instance;
	
	/**
	 * Creates this object.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserCampaignQueries(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
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
	public static boolean userBelongsToCampaign(String username, String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForObject(SQL_EXISTS_USER_CAMPAIGN, new Object[] { campaignId, username }, Boolean.class);
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
	public static List<String> getUsersInCampaign(String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(SQL_GET_USERS_IN_CAMPAIGN, new Object[] { campaignId }, new SingleColumnRowMapper<String>());
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
	public static List<Campaign.Role> getUserCampaignRoles(String username, String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
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
	 * Retrieves all of the campaign IDs and their respective names to which a
	 * user is associated.
	 * 
	 * @param username The username of the user.
	 * 
	 * @return A Map of campaign IDs to campaign names for all of the campaigns
	 * 		   to which the user is associated.
	 */
	public static Map<String, String> getCampaignIdsAndNameForUser(String username) throws DataAccessException {
		try {
			final Map<String, String> result = new HashMap<String, String>();
			
			instance.getJdbcTemplate().query(
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
	public static List<String> getCampaignIdsForUserWithRole(String username, Campaign.Role role) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().query(
					SQL_GET_CAMPAIGN_IDS_FOR_USER_WITH_ROLE, 
					new Object[] { username, role.toString() },
					new SingleColumnRowMapper<String>());
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_CAMPAIGN_IDS_FOR_USER_WITH_ROLE + "' with parameters: " + 
					username + ", " + role, e);
		}
	}
}
