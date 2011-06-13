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
	 */
	public VizTwoDDensityRequest(String token, String width, String height, String campaignId, String promptId, String prompt2Id) {
		super(token, width, height, campaignId);
		
		addToProcess(InputKeys.PROMPT_ID, promptId, true);
		addToProcess(InputKeys.PROMPT2_ID, prompt2Id, true);
	}
}
