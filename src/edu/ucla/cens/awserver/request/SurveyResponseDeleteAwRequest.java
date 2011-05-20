package edu.ucla.cens.awserver.request;


/**
 * State for /app/survey_response/delete.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteAwRequest extends ResultListAwRequest {
	
	/**
	 * All parameters are required and all parameters are added to the toValidate Map in the request.
	 * 
	 * @param campaignUrn a campaign URN that the user must belong to 
	 * @param surveyKey a survey id representing the survey to delete
	 */
	public SurveyResponseDeleteAwRequest(String campaignUrn, String surveyKey) {
		addToValidate(InputKeys.CAMPAIGN_URN, campaignUrn, true);
		addToValidate(InputKeys.SURVEY_KEY, surveyKey, true);
	}
}
