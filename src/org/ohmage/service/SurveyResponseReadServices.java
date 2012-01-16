package org.ohmage.service;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Campaign.Role;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ICampaignQueries;
import org.ohmage.query.IUserCampaignQueries;
import org.ohmage.util.StringUtils;

/**
 * Services for survey response read.
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadServices {
	private static SurveyResponseReadServices instance;
	private ICampaignQueries campaignQueries;
	private IUserCampaignQueries userCampaignQueries;
	
	/**
	 * Default constructor. Privately instantiated via dependency injection
	 * (reflection).
	 * 
	 * @throws IllegalStateException if an instance of this class already
	 * exists.
	 * 
	 * @throws IllegalArgumentException if iCampaignQueries or
	 * iUserCampaignQueries is null.
	 */
	private SurveyResponseReadServices(ICampaignQueries iCampaignQueries, IUserCampaignQueries iUserCampaignQueries) { 
		if(instance != null) {
			throw new IllegalStateException("An instance of this class already exists.");
		}
		if(iCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of ICampaignQueries is required.");
		}
		if(iUserCampaignQueries == null) {
			throw new IllegalArgumentException("An instance of IUserCampaignQueries is required.");
		}
		
		campaignQueries = iCampaignQueries;
		userCampaignQueries = iUserCampaignQueries;
		
		instance = this;
	}
	
	/**
	 * @return  Returns the singleton instance of this class.
	 */
	public static SurveyResponseReadServices instance() {
		return instance;
	}
	
	/**
	 * Verifies that the promptIdList contains prompts that are present in the
	 * configuration.
	 * 
	 * @param request  The request to fail should the promptIdList be invalid.
	 * 
	 * @param promptIdList  The prompt IDs to validate.
	 * 
	 * @param configuration  The configuration to use for prompt id lookup.
	 * 
	 * @throws ServiceException if an invalid prompt id is detected.
	 * 
	 * @throws IllegalArgumentException if request, promptIdList, or
	 * configuration are null.
	 */
	public void verifyPromptIdsBelongToConfiguration(
			final Collection<String> promptIdList, 
			final Campaign configuration)
		throws ServiceException {
		
		// check for logical errors
		if(promptIdList == null || configuration == null) {
			throw new IllegalArgumentException("A non-null request, promptIdList, and configuration are required");
		}
		
		for(String promptId : promptIdList) {
			try {
				configuration.getSurveyIdForPromptId(promptId);
			}
			catch(IllegalArgumentException e) {
				StringBuilder sb = new StringBuilder();
				sb.append("The configuration for campaign ");
				sb.append(configuration.getId());
				sb.append(" did not contain the prompt id ");
				sb.append(promptId);
				String msg = sb.toString();
				throw new ServiceException(ErrorCode.SURVEY_INVALID_PROMPT_ID, msg);
			}
		}
	}
	
	/**
	 * Verifies that the surveyIdList contains surveys that are present in the
	 * configuration.
	 * 
	 * @param surveyIdList  The survey ids to validate.
	 * 
	 * @param configuration  The configuration to use for survey id lookup
	 * 
	 * @throws ServiceException if an invalid survey id is detected.
	 * 
	 * @throws IllegalArgumentException if request, surveyIdList, or
	 * configuration are null.
	 */
	public void verifySurveyIdsBelongToConfiguration(
			final Collection<String> surveyIdList, 
			final Campaign configuration)
		throws ServiceException {
		
		// check for logical errors
		if(surveyIdList == null || configuration == null) {
			throw new IllegalArgumentException("A non-null request, surveyIdList, and configuration are required");
		}
		
		for(String surveyId : surveyIdList) {
			if(! configuration.surveyIdExists(surveyId)) {
				StringBuilder sb = new StringBuilder();
				sb.append("The configuration for campaign ");
				sb.append(configuration.getId());
				sb.append(" did not contain the survey id ");
				sb.append(surveyId);
				String msg = sb.toString();
				throw new ServiceException(ErrorCode.SURVEY_INVALID_SURVEY_ID, msg);
			}
		}
	}
	
	/**
	 * <p>Filters the provided surveyResponseList according to our ACL rules for
	 * survey responses:</p>
	 * <ul>
	 * <li>Owners of responses can view their data anytime.</li>
	 * <li>Supervisors can view any user's data anytime.</li>
	 * <li>Authors can view shared responses if the campaign is private.</li> 
	 * </ul> 
	 * 
	 * <p>This method assumes that the user's role has already been checked
	 * 
	 * @param user The requester's username.
	 * 
	 * @param campaignId The campaign's unique identifier to which all of the
	 * 					 survey responses belong.
	 *  
	 * @param surveyResponseList The collection of survey responses to filter.
	 * 
	 * @return A filtered list of survey response results.
	 */
	public void performPrivacyFilter(
			final String username, 
			final String campaignId,
			final Collection<SurveyResponse> surveyResponseList) 
			throws ServiceException {
		
		// check for logical errors
		if(StringUtils.isEmptyOrWhitespaceOnly(username) || 
				StringUtils.isEmptyOrWhitespaceOnly(campaignId) || 
				surveyResponseList == null) {
			
			throw new IllegalArgumentException("user, campaignId, and surveyResponseList must all be non-null");
		}
		
		if(surveyResponseList.isEmpty()) {
			return;
		}
		
		try {
			List<Role> userRoles = userCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			// Supervisors can read all data all the time
			if(! userRoles.contains(Campaign.Role.SUPERVISOR)) {
				Campaign.PrivacyState privacyState = 
					campaignQueries.getCampaignPrivacyState(campaignId);
				
				Collection<SurveyResponse> responsesToRemove = new LinkedList<SurveyResponse>();
				for(SurveyResponse currentResult : surveyResponseList) {
					// If they own it, it's ok.
					if(currentResult.getUsername().equals(username)) {
						continue;
					}
					
					// If the survey response is shared,
					if(SurveyResponse.PrivacyState.SHARED.equals(
							currentResult.getPrivacyState())) {
						
						// Authors can always view shared data
						// regardless of the state of the campaign
						if(userRoles.contains(Campaign.Role.AUTHOR)) {
							continue;
						}
						
						// If the campaign is shared and the user is an author
						// or an analyst, it's ok.
						if(Campaign.PrivacyState.SHARED.equals(privacyState) &&
								userRoles.contains(Campaign.Role.ANALYST))
							{
							
							continue;
						}
					}
					
					// If none of the above rules apply, it will fall to this and
					// remove the survey response.
					responsesToRemove.add(currentResult);
				}
				
				surveyResponseList.removeAll(responsesToRemove);
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}