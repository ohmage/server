package org.ohmage.request;

/**
 * A request for a prompt timeseries visualization.
 * 
 * @author John Jenkins
 */
public class VizPromptTimeseriesRequest extends VisualizationRequest {
	/**
	 * Builds a new prompt timeseries visualization request.
	 * 
	 * @param token The authentication / session token from this request.
	 * 
	 * @param width The desired width of the resulting image.
	 * 
	 * @param height The desired height of the resulting image.
	 * 
	 * @param campaignId An ID for the campaign that contains the prompt.
	 * 
	 * @param promptId An ID for the prompt in the campaign whose results are
	 * 				   desired.
	 */
	public VizPromptTimeseriesRequest(String token, String width, String height, String campaignId, String promptId) {
		super(token, width, height, campaignId);
		
		addToProcess(InputKeys.PROMPT_ID, promptId, true);
	}
}
