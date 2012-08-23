/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request.visualization;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.UserBin;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.VisualizationServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;
import org.ohmage.validator.VisualizationValidators;

/**
 * <p>The parent request for all visualization requests. Please see the 
 * concrete visualization requests for further parameter requirements.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The unique identifier for the campaign to which this visualization 
 *       pertains.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#VISUALIZATION_HEIGHT}</td>
 *     <td>The desired height of the resulting visualization image.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#VISUALIZATION_WIDTH}</td>
 *     <td>The desired width of the resulting visualization image.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public abstract class VisualizationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(VisualizationRequest.class);
	
	private final String campaignId;
	private final int width;
	private final int height;
	
	private final DateTime startDate;
	private final DateTime endDate;
	private final SurveyResponse.PrivacyState privacyState;
	
	private byte[] result;
	
	/**
	 * Creates a new abstract visualization request.
	 * 
	 * @param httpRequest A HttpServletRequest with the basic requirements for
	 * 					  a visualization request plus the requirements for a
	 * 					  UserRequest.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public VisualizationRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.EITHER, null);
		
		String tCampaignId = null;
		Integer tWidth = 0;
		Integer tHeight = 0;
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		SurveyResponse.PrivacyState tPrivacyState = null;
		
		try {
			tCampaignId = CampaignValidators.validateCampaignId(httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
			if(tCampaignId == null) {
				setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The campaign ID is missing.");
				throw new ValidationException("The campaign ID is missing.");
			}
			
			tWidth = VisualizationValidators.validateWidth(httpRequest.getParameter(InputKeys.VISUALIZATION_WIDTH));
			if(tWidth == null) {
				setFailed(ErrorCode.VISUALIZATION_INVALID_WIDTH_VALUE, "The visualization width is missing.");
				throw new ValidationException("The visualization width is missing.");
			}
			
			tHeight = VisualizationValidators.validateHeight(httpRequest.getParameter(InputKeys.VISUALIZATION_HEIGHT));
			if(tHeight == null) {
				setFailed(ErrorCode.VISUALIZATION_INVALID_HEIGHT_VALUE, "The visualization height is missing.");
				throw new ValidationException("The visualization height is missing.");
			}
			
			tStartDate = VisualizationValidators.validateStartDate(getParameter(InputKeys.START_DATE));
			tEndDate = VisualizationValidators.validateEndDate(getParameter(InputKeys.END_DATE));
			tPrivacyState = SurveyResponseValidators.validatePrivacyState(getParameter(InputKeys.PRIVACY_STATE)); 
		}
		catch(ValidationException e) {
			e.failRequest(this);
			LOGGER.info(e.toString());
		}
		
		campaignId = tCampaignId;
		width = tWidth;
		height = tHeight;
		
		startDate = tStartDate;
		endDate = tEndDate;
		privacyState = tPrivacyState;
		
		result = null;
	}

	/**
	 * Checks that the campaign ID in the request belongs to an existing 
	 * campaign<br />
	 * <br />
	 * Note: This does not perform any user authentication.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a visualization request.");
		
		try {
			LOGGER.info("Verifying that the campaign exists.");
			CampaignServices.instance().checkCampaignExistence(campaignId, true);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Writes the resulting image to the response's output stream.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Writing the visualization response.");
		
		// Creates the writer that will write the response, success or fail.
		Writer writer;
		OutputStream os;
		try {
			os = getOutputStream(httpRequest, httpResponse);
			writer = new BufferedWriter(new OutputStreamWriter(os));
		}
		catch(IOException e) {
			LOGGER.error("Unable to create writer object. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching
		expireResponse(httpResponse);
		
		// Set the CORS headers.
		handleCORS(httpRequest, httpResponse);
		
		// If the request hasn't failed, attempt to write the file to the
		// output stream. 
		if(! isFailed() && (result != null)) {
			try {
				// Setup the response headers.
				httpResponse.setContentType("image/png");
				
				// If available, set the token.
				if(getUser() != null) {
					final String token = getUser().getToken(); 
					if(token != null) {
						CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
					}
				}
				
				os.write(result);
				os.flush();
				os.close();
			}
			catch(IOException e) {
				LOGGER.error("There was an error writing the image to the output stream.", e);
				return;
			}
		}
		
		// If the request ever failed, write an error message.
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			
			// Write the error response.
			try {
				writer.write(getFailureMessage()); 
			}
			catch(IOException e) {
				LOGGER.warn("Unable to write failed response message. Aborting.", e);
			}
			
			// Close it.
			try {
				writer.close();
			}
			catch(IOException e) {
				LOGGER.warn("Unable to close the writer.", e);
			}
		}
	}
	
	/**
	 * Returns the unique campaign identifier for this request.
	 * 
	 * @return The unique campaign identifier for this request.
	 */
	protected final String getCampaignId() {
		return campaignId;
	}
	
	/**
	 * Returns the desired width of the resulting image.
	 * 
	 * @return The desired width of the resulting image.
	 */
	protected final int getWidth() {
		return width;
	}
	
	/**
	 * Returns the desired height of the resulting image.
	 * 
	 * @return The desired height of the resulting image.
	 */
	protected final int getHeight() {
		return height;
	}
	
	/**
	 * Returns a map of the parameters to be passed to the visualization
	 * server.
	 *  
	 * @return A map of key-value pairs to be passed to the visualization 
	 * 		   server.
	 */
	protected final Map<String, String> getVisualizationParameters() {
		Map<String, String> result = new HashMap<String, String>();
		
		if(startDate != null) {
			result.put(VisualizationServices.PARAMETER_KEY_START_DATE, TimeUtils.getIso8601DateString(startDate, false));
		}
		
		if(endDate != null) {
			result.put(VisualizationServices.PARAMETER_KEY_END_DATE, TimeUtils.getIso8601DateString(endDate, false));
		}
		
		if(privacyState != null) {
			result.put(VisualizationServices.PARAMETER_KEY_PRIVACY_STATE, privacyState.toString());
		}
		
		return result;
	}
	
	/**
	 * Sets the image result that will be returned to the requester.
	 * 
	 * @param image The image as a byte array.
	 */
	protected final void setImage(final byte[] image) {
		result = image;
	}
}
