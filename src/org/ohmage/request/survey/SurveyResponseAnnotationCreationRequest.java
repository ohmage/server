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

import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Stores a survey and its associated images (if any are present in the payload)</p>
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
 *     <td>{@value org.ohmage.request.InputKeys#TIME}</td>
 *     <td>A long value representing the milliseconds since the UNIX epoch at
 *     which this annotation was created..</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#TIMEZONE}</td>
 *     <td>A String timezone identifier representing the timezone at which the
 *     annotation was created.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#ANNOTATION}</td>
 *     <td>The annoatation text.</td>
 *     <td>true</td>
 *   </tr>         
 * </table>
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseAnnotationCreationRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseAnnotationCreationRequest.class);
	
	private final UUID surveyId;
	private final Long time;
	private final String timezone;
	private final String annotationText;
	
	private final UUID annotationIdToReturn; 
	
	/**
	 * Creates a new survey annotation creation request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public SurveyResponseAnnotationCreationRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		LOGGER.info("Creating a survey annotation creation request.");
		
		UUID tSurveyId = null;
		Long tTime = Long.MIN_VALUE;
		String tTimezone = null;
		String tAnnotationText = null;
				
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
				
				// Validate the time
				t = parameters.get(InputKeys.TIME);
				
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.ANNOTATION_INVALID_TIME, "time is missing or there is more than one value");
					throw new ValidationException("time is missing or there is more than one value");
				} else {
					
					try {
						tTime = Long.valueOf(t[0]);
					}
					catch(NumberFormatException e) {
						setFailed(ErrorCode.ANNOTATION_INVALID_TIME, "The time is invalid.");
						throw new ValidationException("The time is invalid.");
					}	
				}
				
				// Validate the timezone
				t = parameters.get(InputKeys.TIMEZONE);
				
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.ANNOTATION_INVALID_TIMEZONE, "timezone is missing or there is more than one value");
					throw new ValidationException("timezone is missing or there is more than one value");
				} else {
					
					// FIXME This is too strict in that it will reject
					// JavaScript timezone offsets such as:
					//
					//  var d = new Date();
					//  d.getTimezoneOffset() / 60; 
					//
					// ... where the last line will return 8 and then the 
					// JS programmer has to create a String like "GMT-8".
					// This issue with JavaScript was surfaced by the MWF team.
					//
					// See also the SurveyResponse constructor which deals
					// with timezones inconsistent with the following code, but
					// is also flawed.
					//
					// There is another wrinkle here because MySQL can be 
					// configured to use the OSes timezone in
					// /usr/share/zoneinfo or it can be configured with
					// timezones that are supplied with its own distro.
					if(! Arrays.asList(TimeZone.getAvailableIDs()).contains(t[0])) {
						setFailed(ErrorCode.ANNOTATION_INVALID_TIMEZONE, "The timezone is unknown.");
						throw new ValidationException("The timezone is unknown.");
					}
					
					tTimezone = t[0];
				}
				
				// Validate the annotation text
				t = parameters.get(InputKeys.ANNOTATION_TEXT);
				
				if(t == null || t.length != 1) {
					setFailed(ErrorCode.ANNOTATION_INVALID_ANNOTATION, "annotation is missing or there is more than one value");
					throw new ValidationException("annotation is missing or there is more than one value");
				} else {
					
					if(StringUtils.isEmptyOrWhitespaceOnly(t[0])) {
						setFailed(ErrorCode.ANNOTATION_INVALID_ANNOTATION, "The annotation is empty.");
						throw new ValidationException("The annotation is empty.");
					}	
					
					tAnnotationText = t[0];
				}

				
			}
			catch(ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e.toString());
			}
		}
		
		this.surveyId = tSurveyId;
		this.time = tTime;
		this.timezone = tTimezone;
		this.annotationText = tAnnotationText;
		
		annotationIdToReturn = null;
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
		
//		try {
//			LOGGER.info("Verifying that the user is a participant in the campaign.");
//			UserCampaignServices.instance().verifyUserCanUploadSurveyResponses(getUser().getUsername(), campaignUrn);
//			
//			LOGGER.info("Verifying that the campaign is running.");
//			CampaignServices.instance().verifyCampaignIsRunning(campaignUrn);
//			
//			LOGGER.info("Verifying that the uploaded survey responses aren't out of date.");
//			CampaignServices.instance().verifyCampaignIsUpToDate(campaignUrn, campaignCreationTimestamp);
//			
//			LOGGER.info("Generating the campaign object.");
//			Campaign campaign = CampaignServices.instance().getCampaign(campaignUrn);
//			
//			LOGGER.info("Verifying the uploaded data against the campaign.");
//			List<SurveyResponse> surveyResponses = 
//				CampaignServices.instance().getSurveyResponses(
//						getUser().getUsername(), 
//						getClient(),
//						campaign, 
//						jsonData);
//			
//			surveyResponseIds = new ArrayList<UUID>(surveyResponses.size());
//			for(SurveyResponse surveyResponse : surveyResponses) {
//				surveyResponseIds.add(surveyResponse.getSurveyResponseId());
//			}
//
//			LOGGER.info("Validating that all photo prompt responses have their corresponding images attached.");
//			SurveyResponseServices.instance().verifyImagesExistForPhotoPromptResponses(surveyResponses, imageContentsMap);
//			
//			LOGGER.info("Inserting the data into the database.");
//			List<Integer> duplicateIndexList = SurveyResponseServices.instance().createSurveyResponses(getUser().getUsername(), getClient(), campaignUrn, surveyResponses, imageContentsMap);
//
//			LOGGER.info("Found " + duplicateIndexList.size() + " duplicate survey uploads");
//		}
//		catch(ServiceException e) {
//			e.failRequest(this);
//			e.logException(LOGGER);
//		}
	}

	/**
	 * Responds to the image upload request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey upload request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}
