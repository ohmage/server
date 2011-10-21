package org.ohmage.service;

import java.util.Collection;
import java.util.List;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Campaign.Role;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.CampaignQueries;
import org.ohmage.query.UserCampaignQueries;
import org.ohmage.util.StringUtils;

/**
 * Services for survey response read.
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadServices {
	/**
	 * Private to prevent instantiation.
	 */
	private SurveyResponseReadServices() { }
	
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
	public static void verifyPromptIdsBelongToConfiguration(
			final Collection<String> promptIdList, 
			final Campaign configuration)
		throws ServiceException {
		
		// check for logical errors
		if(promptIdList == null || configuration == null) {
			throw new IllegalArgumentException("A non-null request, promptIdList, and configuration are required");
		}
		
		for(String promptId : promptIdList) {
			if(configuration.getSurveyIdForPromptId(promptId) == null) {
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
	public static void verifySurveyIdsBelongToConfiguration(
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
	 * @param campaignId The campaign URN for the
	 *  
	 * @param surveyResponseList
	 *  
	 * @param privacyState
	 * 
	 * @return A filtered list of survey response results.
	 */
	public static void performPrivacyFilter(final String username, 
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
			List<Role> userRoles = UserCampaignQueries.getUserCampaignRoles(username, campaignId);
			
			// Supervisors can read all data all the time
			if(! userRoles.contains(Campaign.Role.SUPERVISOR)) {
				Campaign.PrivacyState privacyState = 
					CampaignQueries.getCampaignPrivacyState(campaignId);
				
				for(SurveyResponse currentResult : surveyResponseList) {
					// If they own it, it's ok.
					if(currentResult.getUsername().equals(username)) {
						continue;
					}
					
					// If it isn't shared,
					if(! SurveyResponse.PrivacyState.SHARED.equals(
							currentResult.getPrivacyState())) {
						
						// If the campaign is shared and the user is an author or
						// analyst, it's ok.
						if(Campaign.PrivacyState.SHARED.equals(privacyState) &&
								(userRoles.contains(Campaign.Role.AUTHOR) ||
										userRoles.contains(Campaign.Role.ANALYST))
								) {
							
							continue;
						}
					}
					// If it is shared,
					else {
						// If the user is an author or analyst, it's ok.
						if(userRoles.contains(Campaign.Role.AUTHOR) ||
								userRoles.contains(Campaign.Role.ANALYST)) {
							
							continue;
						}
					}
					
					// If none of the above rules apply, it will fall to this and
					// remove the survey response.
					surveyResponseList.remove(currentResult);
				}
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}