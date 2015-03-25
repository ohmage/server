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
package org.ohmage.request.campaign;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.ClassValidators;

/**
 * <p>A request to create a campaign. The creator must associate it with at 
 * least one class.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#XML}</td>
 *     <td>The XML file describing this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RUNNING_STATE}</td>
 *     <td>The initial running state of this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The initial privacy state of this campaign.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLASS_URN_LIST}</td>
 *     <td>A list of classes with which this campaign will initially be 
 *       associated.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DESCRIPTION}</td>
 *     <td>An optional description of this campaign.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class CampaignCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(CampaignCreationRequest.class);

	private final Campaign campaign;
	private final Collection<String> classIds;
	
	/**
	 * Builds a campaign creation request from the HttpServletRequest.
	 * 
	 * @param httpRequest The HttpServletRequest that contains the parameters
	 * 					  necessary for servicing this request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public CampaignCreationRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		Campaign tCampaign = null;
		Set<String> tClassIds = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a campaign creation request.");
			String[] t;
			
			try {
				Campaign.RunningState runningState = null;
				t = getParameterValues(InputKeys.RUNNING_STATE);
				if(t.length > 1) {  // duplicate input argument
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_RUNNING_STATE, 
						"Multiple running states were found: " +
							InputKeys.RUNNING_STATE);
				}
				else if(t.length == 1) {
					runningState = 
						CampaignValidators.validateRunningState(t[0]);
				}
				if(runningState == null) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_RUNNING_STATE, 
						"Missing required running state: " +
							InputKeys.RUNNING_STATE);
				}
				
				Campaign.PrivacyState privacyState = null;
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_PRIVACY_STATE,
						"Multiple privacy states were found: " +
							InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					privacyState = 
						CampaignValidators.validatePrivacyState(t[0]);
				}
				if(privacyState == null) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_PRIVACY_STATE,
						"Missing required privacy state: " +
							InputKeys.PRIVACY_STATE);
				}
				
				String description = null;
				t = getParameterValues(InputKeys.DESCRIPTION);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_DESCRIPTION,
						"Multiple descriptions were found: " +
							InputKeys.DESCRIPTION);
				}
				if(t.length == 1) {
					description = CampaignValidators.validateDescription(t[0]);
				}
				// description can be null
				
				String id = null;
				t = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_ID,
						"Multiple IDs were found: " + InputKeys.CAMPAIGN_URN);
				}
				else if(t.length == 1) {
					id = CampaignValidators.validateCampaignId(t[0]);
				}
				// campaign id can be null
				
				String name = null;
				t = getParameterValues(InputKeys.CAMPAIGN_NAME);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_NAME,
						"Multiple names were found: " +
							InputKeys.CAMPAIGN_NAME);
				}
				else if(t.length == 1) {
					if(! StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						name = t[0];
					}
				}
				// campaign name can be null

				byte[] xml = getParameter(httpRequest, InputKeys.XML);
				if(xml == null) {
					throw new ValidationException(
						ErrorCode.CAMPAIGN_INVALID_XML,
						"Missing required campaign XML: " + InputKeys.XML);
				}
				else {
					tCampaign = 
						CampaignValidators
							.validateCampaign(
								id,
								name,
								new String(xml),
								description, 
								runningState, 
								privacyState, 
								new Date());
				}
				
				tClassIds = ClassValidators.validateClassIdList(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
				if(tClassIds == null) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Missing the required class ID list.");
					throw new ValidationException("Missing required class ID list.");
				}
				// disallow multiple class ID lists.
				else if(httpRequest.getParameterValues(InputKeys.CLASS_URN_LIST).length > 1) {
					setFailed(ErrorCode.CLASS_INVALID_ID, "Multiple class ID lists were found.");
					throw new ValidationException("Multiple class ID lists were found.");
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		campaign = tCampaign;
		classIds = tClassIds;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the campaign creation request.");
		
		// HT: config: disallow new account access to services
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			boolean isAdmin;
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
				
				LOGGER.info("The user is an admin.");
				isAdmin = true;
			}
			catch(ServiceException e) {
				LOGGER.info("The user is not an admin.");
				isAdmin = false;

				LOGGER.info("Verifying that the user is allowed to create campaigns.");
				UserServices.instance().verifyUserCanCreateCampaigns(getUser().getUsername());
			}
			
			LOGGER.info("Verifying that the classes exist.");
			ClassServices.instance().checkClassesExistence(classIds, true);
			
			if(! isAdmin) {
				LOGGER.info("Verifying that the user is enrolled in call of the classes.");
				UserClassServices.instance().userBelongsToClasses(getUser().getUsername(), classIds);
			}
			
			LOGGER.info("Verifying that the campaign doesn't already exist.");
			CampaignServices
				.instance().checkCampaignExistence(campaign.getId(), false);
			
			LOGGER.info("Creating the campaign.");
			CampaignServices
				.instance()
					.createCampaign(
						campaign, 
						classIds, 
						getUser().getUsername());
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the request with a failure message should the request have
	 * failed or a success message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		respond(httpRequest, httpResponse, (JSONObject) null);
	}
}