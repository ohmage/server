package edu.ucla.cens.awserver.request;


/**
 * State for /app/survey_response/update.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseUpdateAwRequest extends ResultListAwRequest {
	
	/**
	 * All parameters are required and all parameters are added to the toValidate Map in the request.
	 * 
	 * @param authToken the token to be used for authentication
	 * @param campaignUrn a campaign URN that the user must belong to 
	 * @param surveyKey a survey id representing the survey to update
	 * @param privacyState the new privacy state
	 */
	public SurveyResponseUpdateAwRequest(String campaignUrn, String surveyKey, String privacyState) {
		addToValidate(InputKeys.CAMPAIGN_URN, campaignUrn, true);
		addToValidate(InputKeys.SURVEY_KEY, surveyKey, true);
		addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
	}
}
