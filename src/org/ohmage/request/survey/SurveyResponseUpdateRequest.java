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
package org.ohmage.request.survey;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserServices;
import org.ohmage.service.UserSurveyResponseServices;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Allows a requester to change the privacy state on a survey 
 * response. The access rules for this API are identical to 
 * those for deleting a survey response.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_KEY}</td>
 *     <td>The survey response's unique identifier.<br />
 *       <br />
 *       This is deprecated in favor of
 *       {@value org.ohmage.request.InputKeys#SURVEY_ID_LIST}</td>
 *     <td>Either this or 
 *       {@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_ID_LIST} but not
 *       both, but this is deprecated and 
 *       {@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_ID_LIST} should
 *       be favored.</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_ID_LIST}</td>
 *     <td>A list of survey responses' unique identifier.</td>
 *     <td>Either this or {@value org.ohmage.request.InputKeys#SURVEY_ID} but
 *       not both.</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>The new privacy state.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 * @see org.ohmage.request.survey.SurveyResponseDeleteRequest
 */
public class SurveyResponseUpdateRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseUpdateRequest.class);
	
	private final Set<UUID> surveyResponseIds;
	private final SurveyResponse.PrivacyState privacyState;
	
	/**
	 * Creates a survey response delete request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public SurveyResponseUpdateRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey response update request.");
		
		Set<UUID> tSurveyResponseIds = null;
		SurveyResponse.PrivacyState tPrivacyState = null;
		
		if(! isFailed()) {
			try {
				
				// FIXME: for survey_response/delete the parameter is called
				// survey_id, but for survey_response/update the parameter is
				// called survey_key. We should be using survey_id consistently.
				
				LOGGER.info("Validating survey_id parameter.");
				String[] surveyIds = getParameterValues(InputKeys.SURVEY_KEY);
				if(surveyIds.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_KEY_VALUE, 
							"Multiple survey ID parameters were given: " +
									InputKeys.SURVEY_KEY);
				}
				else if(surveyIds.length == 1) {
					tSurveyResponseIds = new HashSet<UUID>(1);
					UUID tSurveyResponseId =
							SurveyResponseValidators.validateSurveyResponseId(
									surveyIds[0]);
					
					if(tSurveyResponseId == null) {
						throw new ValidationException(
								ErrorCode.SURVEY_INVALID_SURVEY_KEY_VALUE, 
								"Missing the required survey key: " + 
										InputKeys.SURVEY_KEY);
					}
					else {
						tSurveyResponseIds.add(tSurveyResponseId);
					}
				}
				
				surveyIds = getParameterValues(InputKeys.SURVEY_RESPONSE_ID_LIST);
				if(surveyIds.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_ID,
							"Multiple survey ID lists were given: " +
									InputKeys.SURVEY_RESPONSE_ID_LIST);
				}
				else if(surveyIds.length == 1) {
					// FIXME: Remove this when the survey key parameter is
					// removed.
					if(tSurveyResponseIds != null) {
						throw new ValidationException(
								ErrorCode.SURVEY_INVALID_SURVEY_ID,
								"Conflicting parameters were given, " +
										InputKeys.SURVEY_KEY +
									" and " +
										InputKeys.SURVEY_RESPONSE_ID_LIST +
									". Please use " +
										InputKeys.SURVEY_RESPONSE_ID_LIST + 
									" as " +
										InputKeys.SURVEY_KEY +
									" is deprecated.");
					}
					
					tSurveyResponseIds =
							SurveyResponseValidators.validateSurveyResponseIds(
									surveyIds[0]);
				}
				
				if(tSurveyResponseIds == null) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_ID,
							"A parameter was missing: " +
									InputKeys.SURVEY_RESPONSE_ID_LIST);
				}
				
				LOGGER.info("Validating privacy_state parameter.");
				String[] privacyStates = getParameterValues(InputKeys.PRIVACY_STATE);
				if(privacyStates.length == 0) {
					setFailed(ErrorCode.SURVEY_INVALID_PRIVACY_STATE, "Missing the required privacy state: " + InputKeys.PRIVACY_STATE);
					throw new ValidationException("Missing the required privacy state: " + InputKeys.PRIVACY_STATE);
				}
				else if(privacyStates.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_PRIVACY_STATE, "Multiple privacy state parameters were given.");
					throw new ValidationException("Multiple privacy state parameters were given.");
				}
				else {
					tPrivacyState = SurveyResponseValidators.validatePrivacyState(privacyStates[0]);

					if(tPrivacyState == null) {
						setFailed(ErrorCode.SURVEY_INVALID_PRIVACY_STATE, "Privacy state is missing.");
						throw new ValidationException("Privacy state is missing.");
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		surveyResponseIds = tSurveyResponseIds;
		privacyState = tPrivacyState;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response update request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			}
			catch(ServiceException e) {
				LOGGER.info("Verifying that the user is allowed to update the survey response.");
				for(UUID surveyResponseId : surveyResponseIds) {
					UserSurveyResponseServices
						.instance()
							.verifyUserCanUpdateOrDeleteSurveyResponse(
									this.getUser().getUsername(), 
									surveyResponseId);
				}
			}
			
			LOGGER.info("Updating the survey response.");
			SurveyResponseServices.instance().updateSurveyResponsesPrivacyState(
					this.surveyResponseIds, 
					this.privacyState);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Replies to the user success or failure with a message.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		super.respond(httpRequest, httpResponse, null);
	}
}
