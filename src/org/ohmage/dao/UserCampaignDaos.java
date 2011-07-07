package org.ohmage.dao;

import java.util.List;

import javax.sql.DataSource;

public class UserCampaignDaos extends Dao {
	// Retrieves the roles for a user in a campaign.
	private static final String SQL_GET_USER_CAMPAIGN_ROLES =
		"SELECT ur.role " +
		"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
		"WHERE u.username = ? " +
		"AND u.id = urc.user_id " +
		"AND c.urn = ? " +
		"AND c.id = urc.campaign_id " +
		"AND urc.user_role_id = ur.id";
	
	private static UserCampaignDaos instance;
	
	/**
	 * Sets up this DAO with a shared DataSource to use. This is called from
	 * Spring and is an error to call within application code.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	public UserCampaignDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}
	
	/**
	 * Returns a List of roles for this user in this campaign.
	 * 
	 * @param username The username of the user that whose roles are desired.
	 * 
	 * @param campaignId The campaign ID for the campaign that the user's roles
	 * 					 are being requested.
	 * 
	 * @return A, possibly empty, List of roles for this user in this campaign.
	 */
	public static List<String> getUserCampaignRoles(String username, String campaignId) {
		try {
			return instance.jdbcTemplate.queryForList(
					SQL_GET_USER_CAMPAIGN_ROLES, 
					new Object[] { username, campaignId }, 
					String.class);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_GET_USER_CAMPAIGN_ROLES + "' with parameters: " + 
					username + ", " + campaignId, e);
		}
	}
}
