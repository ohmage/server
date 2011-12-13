package org.ohmage.service;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.ICampaignSurveyResponseQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.query.IUserSurveyResponseQueries;

/**
 * This class contains all of the services pertaining to reading and writing
 * user-survey information.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class UserSurveyResponseServices {
	private static UserSurveyResponseServices instance;
	
	private static final long MILLIS_IN_A_HOUR = 60 * 60 * 1000;
	private static final int HOURS_IN_A_DAY = 24;
	
	private ICampaignQueries campaignQueries;
	private ICampaignSurveyResponseQueries campaignSurveyResponseQueries;
	private IUserCampaignQueries userCampaignQueries;
	private IUserSurveyResponseQueries userSurveyResponseQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists
	 * 
	 * @throws IllegalArgumentException if iCampaignQueries or 
	 * iCampaignSurveyResponseQueries or iUserCampaignQueries or
	 * iUserSurveyResponseQueries is null
	 */
	private UserSurveyResponseServices(ICampaignQueries iCampaignQueries, 
			ICampaignSurveyResponseQueries iCampaignSurveyResponseQueries, IUserCampaignQueries iUserCampaignQueries,
			IUserSurveyResponseQueries iUserSurveyResponseQueries) {
		
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		
		if(iCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignQueries is required.");
		}
		if(iCampaignSurveyResponseQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignSurveyResponseQueries is required.");
		}
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is required.");
		}
		if(iUserSurveyResponseQueries == null) {
			throw new IllegalArgumentException("An instance of IUserSurveyResponseQueries is required.");
		}
		
		campaignQueries = iCampaignQueries;
		userCampaignQueries = iUserCampaignQueries;
		campaignSurveyResponseQueries = iCampaignSurveyResponseQueries;
		userSurveyResponseQueries = iUserSurveyResponseQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static UserSurveyResponseServices instance() {
		return instance;
	}
	
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
	public void verifyUserCanUpdateOrDeleteSurveyResponse(
			final String requesterUsername, final UUID surveyResponseId) 
			throws ServiceException {
		
		try {
			// Get the response's campaign.
			String campaignId = campaignSurveyResponseQueries.getCampaignIdFromSurveyId(surveyResponseId);
			
			if(userCampaignQueries.getUserCampaignRoles(requesterUsername, campaignId).contains(Campaign.Role.SUPERVISOR)) {
				return;
			}
			
			if(Campaign.RunningState.RUNNING.equals(campaignQueries.getCampaignRunningState(campaignId))) {
				if(requesterUsername.equals(userSurveyResponseQueries.getSurveyResponseOwner(surveyResponseId))) {
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
	public double getHoursSinceLastSurveyUplaod(
			final String requestersUsername, final String usersUsername) 
			throws ServiceException {
		
		try {
			Timestamp lastUpload = userSurveyResponseQueries.getLastUploadForUser(requestersUsername, usersUsername);
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
	public double getPercentageOfNonNullLocationsOverPastDay(
			final String requestersUsername, final String usersUsername) 
			throws ServiceException {
		
		try {
			Double percentage = userSurveyResponseQueries.getPercentageOfNonNullSurveyLocations(requestersUsername, usersUsername, HOURS_IN_A_DAY);
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