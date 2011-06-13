package org.ohmage.request;

/**
 * A request for an image representation of a users responses to a prompt over
 * time.
 * 
 * @author John Jenkins
 */
public class VizUserTimeseriesRequest extends VisualizationRequest {
	/**
	 * Builds a new User Timeseries visualization request.
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
	 * 
	 * @param userId The ID for the user from which the requester is attempting
	 * 				 to generate information.
	 */
	public VizUserTimeseriesRequest(String token, String width, String height, String campaignId, String promptId, String userId) {
		super(token, width, height, campaignId);
		
		addToProcess(InputKeys.PROMPT_ID, promptId, true);
		addToProcess(InputKeys.USER_ID, userId, true);
	}
}
