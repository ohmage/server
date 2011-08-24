package org.ohmage.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.UserCampaignDaos;
import org.ohmage.dao.UserMobilityDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains all of the services pertaining to reading and writing
 * user-Mobility information.
 * 
 * @author John Jenkins
 */
public class UserMobilityServices {
	private static final long MILLIS_IN_A_HOUR = 60 * 60 * 1000;
	private static final int HOURS_IN_A_DAY = 24;
	
	/**
	 * Default constructor. Made private so that it cannot be instantiated.
	 */
	private UserMobilityServices() {}
	
	/**
	 * Checks if some "requesting" user can view Mobility data of another data.
	 * It may be that there is no data or that all data is private; therefore,
	 * while it is acceptable for a requester to view the data about another
	 * user, there is no data to view. For this to return true, one of the 
	 * following rules must be true:<br />
	 * <ul>
	 * <li>The requesting user is attempting to view the own Mobility data.
	 * </li>
	 * <li>The requesting user is a supervisor in any campaign to which the user
	 *   belongs.</li>
	 * <li>The requesting user is an analyst in any campaign to which the user
	 *   belongs and the campaign is shared.</li>
	 * </ul>
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param requestersUsername The username of the user that is attempting to
	 * 							 view data about another user.
	 * 
	 * @param usersUsername The username of the user whose information is being
	 * 						queried.
	 * 
	 * @throws ServiceException Thrown if the requesting user doesn't have
	 * 							sufficient permissions to read Mobility 
	 * 							information about another user or if there is
	 * 							an error.
	 */
	public static void requesterCanViewUsersMobilityData(Request request, String requestersUsername, String usersUsername) throws ServiceException {
		try {
			if(requestersUsername.equals(usersUsername)) {
				return;
			}
			
			Set<String> campaignIds = UserCampaignDaos.getCampaignIdsAndNameForUser(usersUsername).keySet();
			for(String campaignId : campaignIds) {
				List<String> requestersCampaignRoles = UserCampaignDaos.getUserCampaignRoles(requestersUsername, campaignId);
				
				if(requestersCampaignRoles.contains(CampaignRoleCache.ROLE_SUPERVISOR)) {
					return;
				}
				else if(requestersCampaignRoles.contains(CampaignRoleCache.ROLE_ANALYST) && 
						CampaignPrivacyStateCache.PRIVACY_STATE_SHARED.equals(CampaignDaos.getCampaignPrivacyState(campaignId))) {
					return;
				}
			}
			
			request.setFailed(ErrorCodes.MOBILITY_INSUFFICIENT_PERMISSIONS, "Insufficient permissions to read Mobility information about another user.");
			throw new ServiceException("Insufficient permissions to read Mobility information about another user.");
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the number of hours since the last Mobility upload from a 
	 * user. If the user has never uploaded any Mobility points, the maximum
	 * value for a double is returned.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param username The username of the user in question.
	 * 
	 * @return Returns a double value representing the number of hours since 
	 * 		   the last time that some user uploaded Mobility points.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static double getHoursSinceLastMobilityUpload(Request request, String requestersUsername, String usersUsername) throws ServiceException {
		try {
			Timestamp lastMobilityUpload = UserMobilityDaos.getLastUploadForUser(requestersUsername, usersUsername);
			if(lastMobilityUpload == null) {
				return Double.MAX_VALUE;
			}
			else {
				long differenceInMillis = Calendar.getInstance().getTimeInMillis() - lastMobilityUpload.getTime();
				
				return new Double(differenceInMillis) / new Double(MILLIS_IN_A_HOUR);
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the percentage of non-null location values in all of the 
	 * updates in the last 24 hours.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user in question.
	 * 
	 * @return Returns a double value representing the percentage of non-null
	 * 		   location values from all of the Mobility uploads in the last 24
	 * 		   hours. If there were no Mobility uploads, -1.0 is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static double getPercentageOfNonNullLocationsOverPastDay(Request request, String requestersUsername, String usersUsername) throws ServiceException {
		try {
			Double percentage = UserMobilityDaos.getPercentageOfNonNullLocations(requestersUsername, usersUsername, HOURS_IN_A_DAY);
			if(percentage == null) {
				return -1.0;
			}
			else {
				return percentage;
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}