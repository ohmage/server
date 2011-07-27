package org.ohmage.request;

/**
 * A survey response privacy state over time visualization request.
 * 
 * @author John Jenkins
 */
public class VizSurveyResponsesPrivacyStateTimeRequest extends VisualizationRequest {
	/**
	 * Builds the survey response count request.
	 * 
	 * @param token The authentication / session token.
	 * 
	 * @param width The desired width of the image.
	 * 
	 * @param height The desired height of the image.
	 * 
	 * @param campaignId The ID for the campaign whose usage is desired.
	 * 
	 * @param privacyState The privacy state of the responses to be queried. If
	 * 					   null, it will be ignored.
	 * 
	 * @param startDate The start date of all survey responses to be queried. 
	 * 					If this is null, it will be ignored.
	 * 
	 * @param endDate The end date of all survey responses to be queried. If
	 * 				  this is null, it will be ignored.
	 */
	public VizSurveyResponsesPrivacyStateTimeRequest(String token, String width, String height, String campaignId, String privacyState, String startDate, String endDate) {
		super(token, width, height, campaignId, privacyState, startDate, endDate);
	}
}
