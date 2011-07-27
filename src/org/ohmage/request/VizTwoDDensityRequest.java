package org.ohmage.request;

/**
 * A 2D density visualization request.
 * 
 * @author John Jenkins
 */
public class VizTwoDDensityRequest extends VisualizationRequest {
	/**
	 * Builds a new 2D density visualization request.
	 * 
	 * @param token The authentication / session token from this request.
	 * 
	 * @param width The desired width of the resulting image.
	 * 
	 * @param height The desired height of the resulting image.
	 * 
	 * @param campaignId An ID for the campaign that contains the prompts.
	 * 
	 * @param promptId An ID for the prompt in the campaign whose results are
	 * 				   desired.
	 * 
	 * @param prompt2Id An ID for the second prompt in the campaign whose 
	 * 					results are desired.
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
	public VizTwoDDensityRequest(String token, String width, String height, String campaignId, String promptId, String prompt2Id, String privacyState, String startDate, String endDate) {
		super(token, width, height, campaignId, privacyState, startDate, endDate);
		
		addToProcess(InputKeys.PROMPT_ID, promptId, true);
		addToProcess(InputKeys.PROMPT2_ID, prompt2Id, true);
	}
}
