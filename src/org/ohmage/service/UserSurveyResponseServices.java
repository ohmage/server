package org.ohmage.service;

import java.sql.Timestamp;
import java.util.Calendar;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.CampaignQueries;
import org.ohmage.query.CampaignSurveyResponseQueries;
import org.ohmage.query.UserCampaignQueries;
import org.ohmage.query.UserSurveyResponseQueries;

/**
 * This class contains all of the services pertaining to reading and writing
 * user-survey information.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserSurveyResponseServices {
	private static final long MILLIS_IN_A_HOUR = 60 * 60 * 1000;
	private static final int HOURS_IN_A_DAY = 24;
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UserSurveyResponseServices() {}
	
	/**
	 * Verifies that the requesting user has sufficient permissions to delete
	 * the survey response.
	 * 
	 * @param requesterUsername The username of the user that is attempting to
	 * 							delete the point.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws ServiceException Thrown if there is an error or if the user 
	 * 							doesn't have sufficient permissions to delete
	 * 							the survey response.
	 */
	public static void verifyUserCanUpdateOrDeleteSurveyResponse(
			final String requesterUsername, final Long surveyResponseId) 
			throws ServiceException {
		
		try {
			// Get the response's campaign.
			String campaignId = CampaignSurveyResponseQueries.getCampaignIdFromSurveyId(surveyResponseId);
			
			if(UserCampaignQueries.getUserCampaignRoles(requesterUsername, campaignId).contains(Campaign.Role.SUPERVISOR)) {
				return;
			}
			
			if(Campaign.RunningState.RUNNING.equals(CampaignQueries.getCampaignRunningState(campaignId))) {
				if(requesterUsername.equals(UserSurveyResponseQueries.getSurveyResponseOwner(surveyResponseId))) {
					return;
				}
			}
			
			throw new ServiceException(
					ErrorCode.SURVEY_INSUFFICIENT_PERMISSIONS, 
					"The user does not have sufficient permissions to delete this survey response.");
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the number of hours since the last uploaded survey was taken
	 * that is visible to the requesting user.
	 * 
	 * @param requestersUsername The username of the user that is making this
	 * 							 request.
	 * 
	 * @param usersUsername The username of the user that to which the data
	 * 						pertains.
	 * 
	 * @return The number of hours since the last time a survey was taken.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static double getHoursSinceLastSurveyUplaod(
			final String requestersUsername, final String usersUsername) 
			throws ServiceException {
		
		try {
			Timestamp lastUpload = UserSurveyResponseQueries.getLastUploadForUser(requestersUsername, usersUsername);
			if(lastUpload == null) {
				return Double.MAX_VALUE;
			}
			else {
				long differenceInMillis = Calendar.getInstance().getTimeInMillis() - lastUpload.getTime();
				
				return new Double(differenceInMillis) / new Double(MILLIS_IN_A_HOUR);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the percentage of non-null location points to total location
	 * points for all surveys uploaded in the last 24 hours.
	 * 
	 * @param requestersUsername The username of the user that is requesting
	 * 							 this information.
	 * 
	 * @param usersUsername The username of the user to which the survey points
	 * 						belong.
	 * 
	 * @return Returns the percentage of non-null location values uploaded by
	 * 		   'usersUsername' over the last 24 hours. If there are no points,
	 * 		   -1.0 is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error. 
	 */
	public static double getPercentageOfNonNullLocationsOverPastDay(
			final String requestersUsername, final String usersUsername) 
			throws ServiceException {
		
		try {
			Double percentage = UserSurveyResponseQueries.getPercentageOfNonNullSurveyLocations(requestersUsername, usersUsername, HOURS_IN_A_DAY);
			if(percentage == null) {
				return -1.0;
			}
			else {
				return percentage;
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}