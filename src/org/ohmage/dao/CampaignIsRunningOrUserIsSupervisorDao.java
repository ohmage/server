package org.ohmage.dao;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.request.AwRequest;

/**
 * Checks that either the campaign in the request is running or that the 
 * requester is a supervisor in the campaign. If neither is true, it sets the
 * request is failed.
 * 
 * @author John Jenkins
 */
public class CampaignIsRunningOrUserIsSupervisorDao extends AbstractDao {
	private static final Logger _logger = Logger.getLogger(CampaignIsRunningOrUserIsSupervisorDao.class);
	
	private static final String SQL_GET_CAMPAIGN_IS_RUNNING = 
		"SELECT EXISTS(" +
			"SELECT c.urn " +
			"FROM campaign c, campaign_running_state crs " +
			"WHERE c.urn = ? " +
			"AND c.running_state_id = crs.id " +
			"AND crs.running_state = '" + CampaignRunningStateCache.RUNNING_STATE_RUNNING + "'" +
		")";
	
	private static final String SQL_GET_USER_IS_SUPERVISOR = 
		"SELECT EXISTS(" +
			"SELECT u.username " +
			"FROM user u, campaign c, user_role ur, user_role_campaign urc " +
			"WHERE u.username = ? " +
			"AND u.id = urc.user_id " +
			"AND c.urn = ? " +
			"AND c.id = urc.campaign_id " +
			"AND ur.role = '" + CampaignRoleCache.ROLE_SUPERVISOR + "' " +
			"AND ur.id = urc.user_role_id" +
		")";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use when querying the database.
	 */
	public CampaignIsRunningOrUserIsSupervisorDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Checks if the campaign is running. If not, checks if the user is a 
	 * supervisor. If not, sets the request as failed.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// If the campaign is running, then we are fine.
		final String campaignId = awRequest.getCampaignUrn();
		try {
			if((Boolean) getJdbcTemplate().queryForObject(SQL_GET_CAMPAIGN_IS_RUNNING, new Object[] { campaignId }, Boolean.class)) {
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_IS_RUNNING + "' with parameter: " + campaignId, e);
			throw new DataAccessException(e);
		}
		
		// If the user is a supervisor in the campaign, then we are fine.
		final String username = awRequest.getUser().getUserName();
		try {
			if((Boolean) getJdbcTemplate().queryForObject(SQL_GET_USER_IS_SUPERVISOR, new Object[] { username, campaignId }, Boolean.class)) {
				return;
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_USER_IS_SUPERVISOR + "' with parameter: " + 
					username + ", " + campaignId, e);
			throw new DataAccessException(e);
		}
		
		// Otherwise, the request has failed.
		awRequest.setFailedRequest(true);
	}
}