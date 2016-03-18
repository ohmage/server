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
package org.ohmage.request.survey.annotation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Annotation;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserAnnotationServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.UserServices;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Reads the annotations for a prompt response.</p>
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
 *     <td>An id (a UUID) indicating which survey to annotate.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT_ID}</td>
 *     <td>A String representing a prompt id present in the survey referenced 
 *     by the survey id above.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#REPEATABLE_SET_ID}</td>
 *     <td>An optional String representing a repeatable set id present in the  
 *     survey referenced by the survey id above.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#REPEATABLE_SET_ITERATION}</td>
 *     <td>An optional integer indicating the iteration of the repeatable set.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public class PromptResponseAnnotationReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(PromptResponseAnnotationReadRequest.class);
	
	private static final String RESULT_KEY = "data";
	
	private final UUID surveyId;
	private final String promptId;
	private final String repeatableSetId;
	private final Integer repeatableSetIteration;
	
	private List<Annotation> annotationsToReturn;
	
	/**
	 * Creates a new survey annotation creation request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public PromptResponseAnnotationReadRequest(HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		super(httpRequest, null, TokenLocation.PARAMETER, null);
		
		LOGGER.info("Creating a prompt response annotation read request.");
		
		UUID tSurveyId = null;
		String tPromptId = null;
		String tRepeatableSetId = null;
		Integer tRepeatableSetIteration = null;
				
		if(! isFailed()) {
			try {
				Map<String, String[]> parameters = getParameters();
				
				// Validate the survey ID
				String[] t = parameters.get(InputKeys.SURVEY_ID);
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.SURVEY_INVALID_SURVEY_ID, "survey_id is missing or there is more than one.");
					throw new ValidationException("survey_id is missing or there is more than one.");
				} else {
					tSurveyId = SurveyResponseValidators.validateSurveyResponseId(t[0]);
					
					if(tSurveyId == null) {
						setFailed(ErrorCode.SURVEY_INVALID_SURVEY_ID, "The survey ID is invalid.");
						throw new ValidationException("The survey ID is invalid.");
					}
				}
				
				// Validate the prompt ID
				t = parameters.get(InputKeys.PROMPT_ID);
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.SURVEY_INVALID_PROMPT_ID, "prompt_id is missing or there is more than one.");
					throw new ValidationException("prompt_id is missing or there is more than one.");
				}
				else {
					
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						setFailed(ErrorCode.SURVEY_INVALID_PROMPT_ID, "The prompt ID is invalid.");
						throw new ValidationException("The prompt ID is invalid.");
					}
					
					// Just grab the only item in the set
					tPromptId = t[0];
				}

				// Validate the optional repeatable set ID
				t = parameters.get(InputKeys.REPEATABLE_SET_ID);
				if(t != null) {
					
					if(t.length != 1) {
						setFailed(ErrorCode.SURVEY_INVALID_REPEATABLE_SET_ID, "there is more than one repeatable set ID.");
						throw new ValidationException("there is more than one repeatable set ID.");
					}
					
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						tRepeatableSetId = null;
					}
					else {
						tRepeatableSetId = t[0];
					}
					
				} else {
					
					tRepeatableSetId = null;
					
				}
	
				// Validate the optional repeatable set iteration
				// If a repeatable set id is present, an iteration is required and vice versa
				t = parameters.get(InputKeys.REPEATABLE_SET_ITERATION);
				if(t != null) {
					
					if(t.length != 1) {
						setFailed(ErrorCode.SURVEY_INVALID_REPEATABLE_SET_ITERATION, "there is more than one repeatable set iteration.");
						throw new ValidationException("there is more than one repeatable set iteration.");
					}
					
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0]) ) {
						tRepeatableSetIteration = null;
					} 
					else {
						
						if(tRepeatableSetId == null) {
							setFailed(ErrorCode.SURVEY_INVALID_REPEATABLE_SET_ITERATION, "repeatable set iteration does not make sense without a repeatable set id");
							throw new ValidationException("repeatable set iteration does not make sense without a repeatable set id");
						}

						try {
							
							tRepeatableSetIteration = Integer.parseInt(t[0]);
						}
						catch (NumberFormatException e) {
							setFailed(ErrorCode.SURVEY_INVALID_REPEATABLE_SET_ITERATION, "The repeatable set iteration is invalid.");
							throw new ValidationException("The repeatable set iteration is invalid.");
						}
					}
					
				    if(tRepeatableSetIteration == null && tRepeatableSetId != null) {
					
						setFailed(ErrorCode.SURVEY_INVALID_REPEATABLE_SET_ITERATION, "repeatable set id does not make sense without a repeatable set iteration");
						throw new ValidationException("repeatable set id does not make sense without a repeatable set iteration");
				    }
				}
				else {
					
					tRepeatableSetIteration = null;
				}
				
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		this.surveyId = tSurveyId;
		this.promptId = tPromptId;
		this.repeatableSetId = tRepeatableSetId;
		this.repeatableSetIteration = tRepeatableSetIteration;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a prompt response annotation read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			
			if(! UserServices.instance().isUserAnAdmin(this.getUser().getUsername())) {
				Set<String> campaignIds = UserCampaignServices.instance().getCampaignsForUser(getUser().getUsername(), null, null, null, null, null, null, null);
				
				if(campaignIds.isEmpty()) {
					throw new ServiceException("The user does not belong to any campaigns.");
				}
				
				LOGGER.info("Verifying that the logged in user can read prompt response annotations.");
				UserAnnotationServices.instance().userCanAccessSurveyResponseAnnotation(getUser().getUsername(), campaignIds, surveyId);
			}
			
			LOGGER.info("Reading prompt response annotations.");
			annotationsToReturn = UserAnnotationServices.instance().readPromptResponseAnnotations(this.surveyId, this.promptId, this.repeatableSetId, this.repeatableSetIteration);
			
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the this request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the prompt response annotation read request.");
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, (JSONObject) null);
			return;
		}
		
		// Build the result object.
		JSONArray resultJson = new JSONArray();
		for(Annotation result : annotationsToReturn) {
			resultJson.put(result.toJson());
		}
		super.respond(httpRequest, httpResponse, RESULT_KEY, resultJson);		
	}
}
