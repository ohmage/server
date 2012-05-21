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
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
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
 * <p>Deletes a survey response and any images associated with it. The 
 * requesting user must be the owner of the survey response and the campaign
 * must be running or they must be a supervisor in the campaign to which the
 * survey response belongs.</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_ID}</td>
 *     <td>The survey response's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class SurveyResponseDeleteRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseDeleteRequest.class);
	
	private final UUID surveyResponseId;
	
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
	public SurveyResponseDeleteRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey response delete request.");
		
		UUID tSurveyResponseId = null;
		
		if(! isFailed()) {
			try {
				String[] surveyIds = getParameterValues(InputKeys.SURVEY_KEY);
				if(surveyIds.length == 0) {
					setFailed(ErrorCode.SURVEY_INVALID_SURVEY_ID, "Missing the required survey ID: " + InputKeys.SURVEY_KEY);
					throw new ValidationException("Missing the required survey ID: " + InputKeys.SURVEY_KEY);
				}
				else if(surveyIds.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_SURVEY_ID, "Multiple survey ID parameters were given.");
					throw new ValidationException("Multiple survey ID parameters were given.");
				}
				else {
					tSurveyResponseId = SurveyResponseValidators.validateSurveyResponseId(surveyIds[0]);
					
					if(tSurveyResponseId == null) {
						setFailed(ErrorCode.SURVEY_INVALID_SURVEY_ID, "Missing the required survey ID: " + InputKeys.SURVEY_KEY);
						throw new ValidationException("Missing the required survey ID: " + InputKeys.SURVEY_KEY);
					}
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		surveyResponseId = tSurveyResponseId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response delete request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			try {
				LOGGER.info("Checking if the user is an admin.");
				UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			}
			catch(ServiceException e) {
				LOGGER.info("Verifying that the user is allowed to delete the survey response.");
				UserSurveyResponseServices.instance().verifyUserCanUpdateOrDeleteSurveyResponse(getUser().getUsername(), surveyResponseId);
			}
			
			LOGGER.info("Deleting the survey response.");
			SurveyResponseServices.instance().deleteSurveyResponse(surveyResponseId);
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
