package org.ohmage.service;

import java.util.Collection;
import java.util.List;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.User;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;
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
	 * @param promptIdList  The prompt ids to validate.
	 * 
	 * @param configuration  The configuration to use for prompt id lookup.
	 * 
	 * @throws ServiceException if an invalid prompt id is detected.
	 * 
	 * @throws IllegalArgumentException if request, promptIdList, or
	 * configuration are null.
	 */
	public static void verifyPromptIdsBelongToConfiguration(Request request, List<String> promptIdList, Campaign configuration)
		throws ServiceException {
		
		// check for logical errors
		if(request == null || promptIdList == null || configuration == null) {
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
				request.setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, msg);
				throw new ServiceException(msg);
			}
		}
	}
	
	/**
	 * Verifies that the surveyIdList contains surveys that are present in the
	 * configuration.
	 * 
	 * @param request  The request to fail should the surveyIdList be invalid.
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
	public static void verifySurveyIdsBelongToConfiguration(Request request, List<String> surveyIdList, Campaign configuration)
		throws ServiceException {
		
		// check for logical errors
		if(request == null || surveyIdList == null || configuration == null) {
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
				request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, msg);
				throw new ServiceException(msg);
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
	 * 
	 * @param user The requester behind the survey response query.
	 * 
	 * @param campaignId The campaign URN for the
	 *  
	 * @param surveyResponseList
	 *  
	 * @param privacyState
	 * 
	 * @return A filtered list of survey response results.
	 */
	public static void performPrivacyFilter(User user, String campaignId,
		Collection<SurveyResponse> surveyResponseList, SurveyResponse.PrivacyState privacyState) {
		
		// check for logical errors
		if(user == null  || StringUtils.isEmptyOrWhitespaceOnly(campaignId) || surveyResponseList == null) {
			throw new IllegalArgumentException("user, campaignId, and surveyResponseList must all be non-null");
		}
		
		if(surveyResponseList.isEmpty()) {
			return;
		}
		
		// Supervisors can read all data all the time
		if(! user.isSupervisorInCampaign(campaignId)) {
			for(SurveyResponse currentResult : surveyResponseList) {
				
				// Filter based on our ACL rules
				
				if( 
				    // Owners and supervisors can see unshared responses
					(resultIsUnshared(currentResult) && ! currentResult.getUsername().equals(user.getUsername()))
					
					|| 
					
					((! resultIsUnshared(currentResult)) && (! currentResult.getUsername().equals(user.getUsername()) 
							&& ! user.isAuthorInCampaign(campaignId) && ! user.isAnalystInCampaign(campaignId))) 
					
					||
					
					// Owners, supervisors, and authors can see shared responses if the campaign is private 
					((user.getCampaignsAndRoles().get(campaignId).getCampaign().getPrivacyState().equals(Campaign.PrivacyState.PRIVATE) 
						&& currentResult.getPrivacyState().equals(SurveyResponse.PrivacyState.SHARED)) 
						&& ! user.isAuthorInCampaign(campaignId) 
						&& ! currentResult.getUsername().equals(user.getUsername()))
						
				  ) {
					
					surveyResponseList.remove(currentResult);
				}
			}
		}
		
		// Filter based on the optional privacy state query parameter
		
		if(privacyState != null) {
			for(SurveyResponse currentResult : surveyResponseList) {
				if(! currentResult.getPrivacyState().equals(privacyState)) { 
					surveyResponseList.remove(currentResult);
				}
			}
		}
	}
	
	/**
	 * Checks whether the result is shared or unshared.
	 * 
	 * @param result The result to check.
	 * @return true if the result is shared; false otherwise.
	 */
	private static boolean resultIsUnshared(SurveyResponse result) {
		return ! SurveyResponse.PrivacyState.SHARED.equals(result.getPrivacyState());
	}
}