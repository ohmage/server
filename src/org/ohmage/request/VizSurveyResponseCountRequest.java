package org.ohmage.request;

/**
 * A survey response count visualization request.
 * 
 * @author John Jenkins
 */
public class VizSurveyResponseCountRequest extends VisualizationRequest {
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
	 */
	public VizSurveyResponseCountRequest(String token, String width, String height, String campaignId) {
		super(token, width, height, campaignId);
	}
}
