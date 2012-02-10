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

import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.Function;
import org.ohmage.domain.campaign.SurveyResponse.FunctionPrivacyStateItem;
import org.ohmage.domain.campaign.SurveyResponse.PrivacyState;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Gathers information about survey responses based on the given function
 * ID.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token. This may be a parameter
 *       or may be a cookie in the HTTP request.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaigns's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_FUNCTION_ID}</td>
 *     <td>A survey function ID that dictates the type of information that will
 *       be returned. Must be one of 
 *       {@link org.ohmage.domain.campaign.SurveyResponse.Function}.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_FUNCTION_PRIVACY_STATE_GROUP_ITEM_LIST}</td>
 *     <td>The list of items to use to further split the results. This should
 *       be a list of 
 *       {@link org.ohmage.domain.campaign.SurveyResponse.FunctionPrivacyStateItem} 
 *       values divided by
 *       {@link org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR LIST_ITEM_SEPARATOR}s.</td>
 *     <td>true</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class SurveyResponseFunctionReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseFunctionReadRequest.class);
	
	private final String campaignId;
	private final Function functionId;
	private final Collection<FunctionPrivacyStateItem> privacyStateGroupItems;
	
	private Collection<SurveyResponse> surveyResponses;
	
	/**
	 * Creates a new survey response function read request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for the
	 * 					  request.
	 */
	public SurveyResponseFunctionReadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.EITHER, false);
		
		LOGGER.info("Creating a survey response function read request.");
		
		String tCampaignId = null;
		Function tFunctionId = null;
		Collection<FunctionPrivacyStateItem> tPrivacyStateGroupItems = null;
		
		if(! isFailed()) {
			try {
				String[] campaignIds = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(campaignIds.length == 0) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
				}
				else if(campaignIds.length == 1) {
					tCampaignId = CampaignValidators.validateCampaignId(campaignIds[0]);
					
					if(tCampaignId == null) {
						setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
						throw new ValidationException("The required campaign ID is missing: " + InputKeys.CAMPAIGN_URN);
					}
				}
				else {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
				}
				
				String[] functionIds = getParameterValues(InputKeys.SURVEY_FUNCTION_ID);
				if(functionIds.length == 0) {
					setFailed(ErrorCode.SURVEY_INVALID_SURVEY_FUNCTION_ID, "The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
					throw new ValidationException("The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
				}
				else if(functionIds.length == 1) {
					tFunctionId = SurveyResponseValidators.validateFunction(functionIds[0]);
					
					if(tFunctionId == null) {
						setFailed(ErrorCode.SURVEY_INVALID_SURVEY_FUNCTION_ID, "The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
						throw new ValidationException("The survey function ID is missing: " + InputKeys.SURVEY_FUNCTION_ID);
					}
				}
				else {
					setFailed(ErrorCode.SURVEY_INVALID_SURVEY_FUNCTION_ID, "Multiple survey function IDs were given: " + InputKeys.SURVEY_FUNCTION_ID);
					throw new ValidationException("Multiple survey function IDs were given: " + InputKeys.SURVEY_FUNCTION_ID);
				}
				
				String[] t;
				t = getParameterValues(InputKeys.SURVEY_FUNCTION_PRIVACY_STATE_GROUP_ITEM_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_FUNCTION_INVALID_PRIVACY_STATE_GROUP_ITEM,
							"Multiple privacy state group item lists were given: " +
								InputKeys.SURVEY_FUNCTION_PRIVACY_STATE_GROUP_ITEM_LIST);
				}
				else if(t.length == 1) {
					tPrivacyStateGroupItems = 
						SurveyResponseValidators.validatePrivacyStateGroupList(
								t[0]);
				}
				if(tPrivacyStateGroupItems == null) {
					tPrivacyStateGroupItems = Collections.emptyList();
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		campaignId = tCampaignId;
		functionId = tFunctionId;
		privacyStateGroupItems = tPrivacyStateGroupItems;
		
		surveyResponses = Collections.emptyList();
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing survey response function read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			LOGGER.info("Verifying that the campaign exists.");
			CampaignServices.instance().checkCampaignExistence(campaignId, true);
			
			LOGGER.info("Verifying that the user is allowed to view the requested data.");
			UserCampaignServices.instance().requesterCanViewUsersSurveyResponses(campaignId, getUser().getUsername());
			
			LOGGER.info("Gathering the campaign.");
			Campaign campaign = CampaignServices.instance().getCampaign(campaignId);
			
			LOGGER.info("Gathering the survey response information.");
			surveyResponses = SurveyResponseServices.instance().readSurveyResponseInformation(
					campaign, 
					getUser().getUsername(),
					null, 
					null, 
					null, 
					null, 
					null, 
					null, 
					null,
					null,
					SurveyResponse.DEFAULT_NUM_SURVEY_RESPONSES_TO_SKIP,
					SurveyResponse.DEFAULT_NUM_SURVEY_RESPONSES_TO_PROCESS);
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/**
	 * Respond to the request based on the function ID.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey response function read request.");
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}

		switch(functionId) {
		case PRIVACY_STATE:
			functionResponsePrivacyState(httpRequest, httpResponse);
			break;
		}
	}
	
	/**
	 * This responds to a "privacy state" function request.
	 * 
	 * @param httpRequest The HttpServletRequest
	 * 
	 * @param httpResponse The HttpServletResponse
	 */
	private void functionResponsePrivacyState(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse) {

		// This maps each of the privacy states to a collection of collections
		// of survey responses. The inner collection is all of the survey 
		// responses that have the same parameters as specified by the 
		// 'privacyStateGroupItems'. The outer collection is the aggregation of
		// all of those inner collections associated with this privacy state.
		// For example, if the 'privacyStateGroupItems' is empty, then the 
		// outer collection would contain exactly one inner collection which
		// would be all survey responses with the privacy state for the key.
		// If the 'privacyStateGroupItems' contains a 'date' item, then for 
		// each key the outer collection is a collection of all of the survey
		// responses with the same privacy state and each inner collection is 
		// all of the survey responses with the same date. If a 'survey ID' 
		// item was also given, then each of the inner collections would be
		// those survey responses that share the same date and survey ID.
		Map<PrivacyState, Collection<Collection<SurveyResponse>>> privacyStateBucket =
				new HashMap<PrivacyState, Collection<Collection<SurveyResponse>>>(
						PrivacyState.values().length);
		
		// Cycle through all of the survey responses and populate their initial
		// buckets.
		for(SurveyResponse surveyResponse : surveyResponses) {
			// Get this response's privacy state.
			PrivacyState privacyState = 
					surveyResponse.getPrivacyState();
			
			// Get the outer collection if it exists.
			Collection<Collection<SurveyResponse>> outerCollection =
					privacyStateBucket.get(privacyState);
			
			// If it doesn't exist, create and add it.
			if(outerCollection == null) {
				outerCollection = new LinkedList<Collection<SurveyResponse>>();
				privacyStateBucket.put(privacyState, outerCollection);
			}
			
			// The outer collection should have exactly zero or one inner 
			// collections. If it has zero, create the one.
			Collection<SurveyResponse> innerCollection;
			if(outerCollection.size() == 0) {
				innerCollection = new LinkedList<SurveyResponse>();
				outerCollection.add(innerCollection);
			}
			// If it already has one then it should have exactly one and we
			// retrieve that one.
			else {
				innerCollection = outerCollection.iterator().next();
			}
			
			// Add the survey response to the inner collection.
			innerCollection.add(surveyResponse);
		}
		
		if(privacyStateGroupItems.contains(FunctionPrivacyStateItem.DATE)) {
			LOGGER.info("Subdividing buckets by date.");
			subdivideDate(privacyStateBucket);
		}

		if(privacyStateGroupItems.contains(FunctionPrivacyStateItem.SURVEY)) {
			LOGGER.info("Subdividing buckets by survey ID.");
			subdivideSurveyId(privacyStateBucket);
		}
		
		try {
			// Create the resulting JSONObject and populate it.
			JSONObject result = new JSONObject();
			for(PrivacyState privacyState : privacyStateBucket.keySet()) {
				Collection<Collection<SurveyResponse>> currPrivacyStateBucket =
						privacyStateBucket.get(privacyState);
				
				// Create the array of buckets for this privacy state.
				JSONArray jsonBuckets = new JSONArray();
				
				// Cycle through each of the buckets of collections, create a 
				// JSONObject representing that bucket, and add it to the array for
				// this privacy state.
				for(Collection<SurveyResponse> currBucket : currPrivacyStateBucket) {
					JSONObject jsonBucket = new JSONObject();
					
					jsonBucket.put("count", currBucket.size());
					
					if(privacyStateGroupItems.contains(FunctionPrivacyStateItem.DATE)) {
						// Get a representative survey response.
						SurveyResponse surveyResponse = 
								currBucket.iterator().next();
						
						// Create a calendar with the timezone from the 
						// response and set its time based on the epoch 
						// milliseconds.
						Calendar timezoneAdjustedCalendar =
								Calendar.getInstance(surveyResponse.getTimezone());
						timezoneAdjustedCalendar.setTimeInMillis(
								surveyResponse.getTime());

						// Create the date string to represent this date based on
						// the creator's timezone at the time they took it.
						String dateString = 
								timezoneAdjustedCalendar.get(Calendar.YEAR) + 
								"-" +
								(timezoneAdjustedCalendar.get(Calendar.MONTH) + 1) +
								"-" +
								timezoneAdjustedCalendar.get(Calendar.DAY_OF_MONTH);
						
						jsonBucket.put("date", dateString);
					}
					
					if(privacyStateGroupItems.contains(FunctionPrivacyStateItem.SURVEY)) {
						jsonBucket.put(
								"survey_id", 
								currBucket.iterator().next().getSurvey().getId());
					}
					
					jsonBuckets.put(jsonBucket);
				}
				
				result.put(privacyState.toString(), jsonBuckets);
			}
			
			super.respond(httpRequest, httpResponse, result);
		}
		catch(JSONException e) {
			LOGGER.error("There was a problem creating the response.", e);
			setFailed();
			super.respond(httpRequest, httpResponse, null);
		}
	}
	
	/**
	 * Subdivides the privacy state buckets into those that occurred on the 
	 * same date.
	 * 
	 * @param privacyStateBucket The map of privacy state to buckets of
	 * 							 collections of survey responses.
	 */
	private void subdivideDate(
			Map<PrivacyState, Collection<Collection<SurveyResponse>>> privacyStateBucket) {
		
		// For each of the privacy state buckets,
		for(PrivacyState privacyState : privacyStateBucket.keySet()) {
			// Get the current privacy state bucket.
			Collection<Collection<SurveyResponse>> currPrivacyStateBucket =
					privacyStateBucket.get(privacyState);
			
			// Create a new, empty privacy state bucket.
			Collection<Collection<SurveyResponse>> newPrivacyStateBucket =
					new LinkedList<Collection<SurveyResponse>>();
			
			// Subdivide each of the outer buckets by removing their inner 
			// buckets and replacing them with their subdivided counterparts.
			for(Collection<SurveyResponse> currBucket : currPrivacyStateBucket) {
				// Subdivide the inner collections.
				Map<String, Collection<SurveyResponse>> newInnerBuckets =
						new HashMap<String, Collection<SurveyResponse>>();
				
				// For all of the survey responses in this inner bucket, get
				// their survey ID and re-bucket them.
				for(SurveyResponse surveyResponse : currBucket) {
					// Create a calendar with the timezone from the response 
					// and set its time based on the epoch milliseconds.
					Calendar timezoneAdjustedCalendar =
							Calendar.getInstance(surveyResponse.getTimezone());
					timezoneAdjustedCalendar.setTimeInMillis(
							surveyResponse.getTime());
					
					// Create the date string to represent this date based on
					// the creator's timezone at the time they took it.
					String dateString = 
							timezoneAdjustedCalendar.get(Calendar.YEAR) + 
							"-" +
							timezoneAdjustedCalendar.get(Calendar.MONTH) +
							"-" +
							timezoneAdjustedCalendar.get(Calendar.DAY_OF_MONTH);
					
					// Get the other responses on this date.
					Collection<SurveyResponse> currResponses = 
							newInnerBuckets.get(dateString);
					
					// If no other responses exist for this date, create
					// the list.
					if(currResponses == null) {
						currResponses = new LinkedList<SurveyResponse>();
						newInnerBuckets.put(dateString, currResponses);
					}
					
					// Add this response to the given date.
					currResponses.add(surveyResponse);
				}
				
				// Add the inner buckets to the collection of outer buckets.
				for(String date : newInnerBuckets.keySet()) {
					newPrivacyStateBucket.add(newInnerBuckets.get(date));
				}
			}
			
			privacyStateBucket.put(privacyState, newPrivacyStateBucket);
		}
	}
	
	/**
	 * Subdivides the privacy state buckets into those survey responses whose
	 * corresponding survey has the same ID.
	 * 
	 * @param privacyStateBucket The map of privacy state to buckets of
	 * 							 collections of survey responses.
	 */
	private void subdivideSurveyId(
			Map<PrivacyState, Collection<Collection<SurveyResponse>>> privacyStateBucket) {
		
		// For each of the privacy state buckets,
		for(PrivacyState privacyState : privacyStateBucket.keySet()) {
			// Get the current privacy state bucket.
			Collection<Collection<SurveyResponse>> currPrivacyStateBucket =
					privacyStateBucket.get(privacyState);
			
			// Create a new, empty privacy state bucket.
			Collection<Collection<SurveyResponse>> newPrivacyStateBucket =
					new LinkedList<Collection<SurveyResponse>>();
			
			// Subdivide each of the outer buckets by removing their inner 
			// buckets and replacing them with their subdivided counterparts.
			for(Collection<SurveyResponse> currBucket : currPrivacyStateBucket) {
				// Subdivide the inner collections.
				Map<String, Collection<SurveyResponse>> newInnerBuckets =
						new HashMap<String, Collection<SurveyResponse>>();
				
				// For all of the survey responses in this inner bucket, get
				// their survey ID and re-bucket them.
				for(SurveyResponse surveyResponse : currBucket) {
					// Get the date that this record was created.
					String surveyId = surveyResponse.getSurvey().getId();
					
					// Get the other responses on this date.
					Collection<SurveyResponse> currResponses = 
							newInnerBuckets.get(surveyId);
					
					// If no other responses exist for this date, create
					// the list.
					if(currResponses == null) {
						currResponses = new LinkedList<SurveyResponse>();
						newInnerBuckets.put(surveyId, currResponses);
					}
					
					// Add this response to the given date.
					currResponses.add(surveyResponse);
				}
				
				// Add the inner buckets to the collection of outer buckets.
				for(String surveyId : newInnerBuckets.keySet()) {
					newPrivacyStateBucket.add(newInnerBuckets.get(surveyId));
				}
			}
			
			privacyStateBucket.put(privacyState, newPrivacyStateBucket);
		}
	}
}
