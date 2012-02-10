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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.VisualizationServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.VisualizationValidators;

/**
 * <p>A request for a graph of the number of answers for each possible answer
 * of a specific prompt over time.<br />
 * <br />
 * See {@link org.ohmage.request.visualization.VisualizationRequest} for other 
 * required parameters.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT_ID}</td>
 *     <td>A prompt ID in the campaign's XML.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class VizPromptTimeseriesRequest extends VisualizationRequest {
private static final Logger LOGGER = Logger.getLogger(VizPromptTimeseriesRequest.class);
	
	private static final String REQUEST_PATH = "timeplot/png";
	
	private final String promptId;
	private final Integer aggregate;
	
	/**
	 * Creates a prompt timeseries visualization request.
	 * 
	 * @param httpRequest The HttpServletRequest with the required parameters.
	 */
	public VizPromptTimeseriesRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a prompt timeseries request.");
		
		String tPromptId = null;
		Integer tAggregate = null;
		
		try {
			String[] t;
		
			tPromptId = CampaignValidators.validatePromptId(httpRequest.getParameter(InputKeys.PROMPT_ID));
			if(tPromptId == null) {
				throw new ValidationException(
						ErrorCode.SURVEY_INVALID_PROMPT_ID, 
						"Missing the parameter: " + InputKeys.PROMPT_ID);
			}
			
			t = getParameterValues(InputKeys.VISUALIZATION_AGGREGATE);
			if(t.length > 1) {
				throw new ValidationException(
						ErrorCode.VISUALIZATION_INVALID_AGGREGATE_VALUE,
						"Multiple values given for the same parameter: " +
								InputKeys.VISUALIZATION_AGGREGATE);
			}
			else if(t.length == 1) {
				tAggregate = VisualizationValidators.validateAggregate(t[0]);
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		
		promptId = tPromptId;
		aggregate = tAggregate;
	}
	
	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the prompt timeseries visualization request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the prompt ID exists in the campaign's XML");
			CampaignServices.instance().ensurePromptExistsInCampaign(getCampaignId(), promptId);
			
			Map<String, String> parameters = getVisualizationParameters();
			parameters.put(VisualizationServices.PARAMETER_KEY_PROMPT_ID, promptId);
			
			if(aggregate != null) {
				parameters.put(
						VisualizationServices.PARAMETER_KEY_AGGREGATE, 
						aggregate.toString());
			}
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(REQUEST_PATH, getUser().getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
}
