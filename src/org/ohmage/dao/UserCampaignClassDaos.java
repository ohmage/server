package org.ohmage.dao;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;

/**
 * This class contains all of the functionality for reading information about
 * user-campaign-class relationships.
 * 
 * @author John Jenkins
 */
public final class UserCampaignClassDaos extends Dao {
	// Retrieves the number of ways a user is associated with a campaign via
	// their class relationships.
	private static final String SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES =
		"SELECT COUNT(cc.id) " +
		"FROM user u, campaign c, campaign_class cc, user_class uc " +
		"WHERE u.username = ? " +
		"AND u.id = uc.user_id " +
		"AND uc.class_id = cc.class_id " +
		"AND c.urn = ? " +
		"AND c.id = cc.campaign_id";
	
	private static UserCampaignClassDaos instance;
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource A DataSource object to use when querying the database.
	 */
	private UserCampaignClassDaos(DataSource dataSource) {
		super(dataSource);
		
		instance = this;
	}

	/**
	 * Retrieves the number of classes through which the user is associated 
	 * with a campaign. Another way of phrasing it is, this determines how many
	 * classes to which the user is associated and then returns the number of
	 * those classes which are associated with the campaign.<br />
	 * <br />
	 * Note: This may be misleading in our current implementation. Our current
	 * method of handling this information grants all of the users in a class
	 * direct associations with the campaign when the class is associated. If a
	 * user were to then have those roles revoked, there would be no way to
	 * detect this meaning that this call would still return that class'
	 * association despite having those roles removed.
	 * 
	 * @param username The username of the user.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return The number of classes that are associated with the campaign and
	 * 		   of which the user is a member.
	 */
	public static int getNumberOfClassesThroughWhichUserIsAssociatedWithCampaign(String username, String campaignId) throws DataAccessException {
		try {
			return instance.getJdbcTemplate().queryForInt(SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES, username, campaignId);
		}
		catch(org.springframework.dao.DataAccessException e) {
			throw new DataAccessException("Error executing SQL '" + SQL_COUNT_USER_ASSOCIATED_WITH_CAMPAIGN_THROUGH_CLASSES + "' with parameters: " +
					username + ", " + campaignId, e);
		}
	}
}