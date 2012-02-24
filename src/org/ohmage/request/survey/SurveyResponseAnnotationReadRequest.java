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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Annotation;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.UserAnnotationServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Reads the annotations for a survey response.</p>
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
 * </table>
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseAnnotationReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseAnnotationReadRequest.class);
	
	private final UUID surveyId;
	
	private List<Annotation> annotationsToReturn;
	
	/**
	 * Creates a new survey annotation creation request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public SurveyResponseAnnotationReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey annotation read request.");
		
		UUID tSurveyId = null;
				
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
				
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		this.surveyId = tSurveyId;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey annotation creation request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			Set<String> campaignIds = UserCampaignServices.instance().getCampaignsForUser(getUser().getUsername(), null, null, null, null, null, null, null);
			
			if(campaignIds.isEmpty()) {
				throw new ServiceException("The user does not belong to any campaigns.");
			}
			
			LOGGER.info("Verifying that the logged in user can read survey response annotations.");
			UserAnnotationServices.instance().userCanAccessSurveyResponseAnnotation(getUser().getUsername(), campaignIds, surveyId);
			
			LOGGER.info("Reading survey response annotations.");
			annotationsToReturn = UserAnnotationServices.instance().readSurveyResponseAnnotations(this.surveyId);
			
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
		LOGGER.info("Responding to the survey response annotation read request.");
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
		try {
			JSONObject result = new JSONObject();
						
			for(Annotation annotation : annotationsToReturn) {
				JSONObject bucket = new JSONObject();
				bucket.put("text", annotation.getText());
				bucket.put("time", annotation.getEpochMillis());
				bucket.put("timezone", annotation.getTimezone().getID());
				result.put(annotation.getId().toString(), bucket);	
			}
			
			super.respond(httpRequest, httpResponse, result);
		}	
		catch(JSONException e) {
			LOGGER.error("There was a problem creating the response.", e);
			setFailed();
			super.respond(httpRequest, httpResponse, null);
		}		
	}
}
