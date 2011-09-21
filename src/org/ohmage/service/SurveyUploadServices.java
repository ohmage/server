package org.ohmage.service;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.configuration.Prompt;
import org.ohmage.domain.configuration.PromptTypeKeys;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.prompt.MultiChoiceCustomPromptValidator;
import org.ohmage.validator.prompt.MultiChoicePromptValidator;
import org.ohmage.validator.prompt.PromptValidator;
import org.ohmage.validator.prompt.RangeBoundNumberPromptValidator;
import org.ohmage.validator.prompt.RemoteActivityPromptValidator;
import org.ohmage.validator.prompt.SingleChoiceCustomPromptValidator;
import org.ohmage.validator.prompt.SingleChoicePromptValidator;
import org.ohmage.validator.prompt.TextWithinRangePromptValidator;
import org.ohmage.validator.prompt.TimestampPromptValidator;
import org.ohmage.validator.prompt.UuidPromptValidator;

/**
 * Contains methods for handling and validating survey uploads. 
 * 
 * @author Joshua Selsky
 */
public final class SurveyUploadServices {
	private static final Logger LOGGER = Logger.getLogger(SurveyUploadServices.class);

	// A  map of prompt validators for the supported prompt types
	private static final Map<String, PromptValidator> PROMPT_VALIDATOR_MAP = new TreeMap<String, PromptValidator>();
	
	static {
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_HOURS_BEFORE_NOW, new RangeBoundNumberPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_IMAGE, new UuidPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_MULTI_CHOICE, new MultiChoicePromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_MULTI_CHOICE_CUSTOM, new MultiChoiceCustomPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_NUMBER, new RangeBoundNumberPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_SINGLE_CHOICE, new SingleChoicePromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_SINGLE_CHOICE_CUSTOM, new SingleChoiceCustomPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_TEXT, new TextWithinRangePromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_TIMESTAMP, new TimestampPromptValidator());
		PROMPT_VALIDATOR_MAP.put(PromptTypeKeys.TYPE_REMOTE_ACTIVITY, new RemoteActivityPromptValidator());
	}
	
	/**
	 * Default constructor. Private to prevent instantiation.
	 */
	private SurveyUploadServices() { }
	
	/**
	 * Converts the incoming String into a JSONArray.
	 * 
	 * @param jsonArrayAsString The String to convert.
	 * @return JSONArray The JSONArray representation of the provided String.
	 * @throws ServiceException If the String contains invalid JSON.
	 */
	public static JSONArray stringToJsonArray(Request request, String jsonArrayAsString) throws ServiceException {
		try {
			
			return new JSONArray(jsonArrayAsString);
			
		} catch (JSONException e) {
	
			request.setFailed(ErrorCodes.SERVER_INVALID_JSON, SURVEY_INVALID_JSON);
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks each element in the provided JSONArray to make sure it 
	 * corresponds to our survey upload syntax. Each array element is a JSON 
	 * object that represents a survey. Main workflow for deep survey
	 * validation.
	 * 
	 * @param request The request to fail should validation fail.
	 * @param surveyResponses The array of responses to validate.
	 * @param configuration The campaign configuration used to aid in validating specific prompt responses.
	 * @return  Returns a List of Strings representing all of the image UUIDs
	 * found in the upload. If no image prompts were contained in the upload,
	 * an empty list is returned.
	 * @throws ServiceException If any part of the upload is syntactically or semantically invalid.
	 */
	public static List<String> validateSurveyUpload(Request request, JSONArray surveyResponses, Configuration configuration)
		throws ServiceException {

		List<String> imageIdList = new ArrayList<String>();
		int numberOfResponses = surveyResponses.length();
		
		LOGGER.info("Validating " + numberOfResponses + " survey responses");
		
		if(surveyResponses.length() < 1) {
			
			request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, SURVEY_EMPTY);
			throw new ServiceException(SURVEY_EMPTY);
			
		}
		
		for(int i = 0; i < numberOfResponses; i++) {
			
			JSONObject surveyObject = JsonUtils.getJsonObjectFromJsonArray(surveyResponses, i);
			
			// If an empty response is found in the array, the entire request fails because
			// it indicates a client-side logical error. This is ultra strict because all
			// clients depend on well-formed surveys for read, export, and visualization.
			if(surveyObject == null) {
				String msg = SURVEY_FOUND_EMPTY_PROMPT + i; 
				request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, msg);
				throw new ServiceException(msg);
			}
			
			UploadValidationServices.validateUploadMetadata(request, surveyObject);
			
			String surveyId = JsonUtils.getStringFromJsonObject(surveyObject, JsonInputKeys.SURVEY_ID);
			
			if(StringUtils.isEmptyOrWhitespaceOnly(surveyId)) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, SURVEY_ID_MISSING);
				throw new ServiceException(SURVEY_ID_MISSING);
			}
			if(! configuration.surveyIdExists(surveyId)) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_SURVEY_ID, SURVEY_ID_UNKNOWN);
				throw new ServiceException(SURVEY_ID_UNKNOWN);
			}

			JSONArray responseArray = JsonUtils.getJsonArrayFromJsonObject(surveyObject, JsonInputKeys.SURVEY_RESPONSES);
			
			if(responseArray == null || responseArray.length() == 0) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, SURVEY_MISSING_RESPONSES);
				throw new ServiceException(SURVEY_MISSING_RESPONSES);
			}
			
			JSONObject surveyLaunchContext = JsonUtils.getJsonObjectFromJsonObject(surveyObject, JsonInputKeys.SURVEY_LAUNCH_CONTEXT);
			
			if(surveyLaunchContext == null) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_LAUNCH_CONTEXT, SURVEY_LAUNCH_CONTEXT_MISSING);
				throw new ServiceException(SURVEY_LAUNCH_CONTEXT_MISSING);
			}
			
			String launchTime = JsonUtils.getStringFromJsonObject(surveyLaunchContext, JsonInputKeys.SURVEY_LAUNCH_TIME);
			
			if(StringUtils.isEmptyOrWhitespaceOnly(launchTime)) {
				request.setFailed(ErrorCodes.SURVEY_INVALID_LAUNCH_TIME, SURVEY_LAUNCH_TIME_MISSING);
				throw new ServiceException(SURVEY_LAUNCH_TIME_MISSING);
			}
			
			imageIdList.addAll(validatePromptResponses(request, configuration, responseArray, surveyId));
		}
		
		return imageIdList;
	}
	
	/**
	 * Validates the prompt responses in a survey upload according to each 
	 * response's prompt type and to the ohmage JSON survey schema.
	 *  
	 * @param request  The request to fail should any prompt response be
	 * invalid.
	 * @param configuration  The configuration used to look up prompt types
	 * for validation.
	 * @param responseArray The array of prompt responses to validate.
	 * @param surveyId  The id of the survey for the above responses.
	 * @return  Returns a List of Strings representing all of the image UUIDs
	 * found in the upload. If no image prompts were contained in the upload,
	 * an empty list is returned.   
	 * @throws ServiceException  If any prompt response is invalid.
	 */
	private static List<String> validatePromptResponses(Request request, Configuration configuration, JSONArray responseArray, String surveyId)
		throws ServiceException {
		
		List<String> imageIdList = new ArrayList<String>();
		int numberOfResponses = responseArray.length();
		
		for(int i = 0; i < numberOfResponses; i++) {
			JSONObject response = JsonUtils.getJsonObjectFromJsonArray(responseArray, i);
			
			// Determine whether it is a repeatable set or a prompt response
			String promptId = JsonUtils.getStringFromJsonObject(response, JsonInputKeys.SURVEY_PROMPT_ID);
			
			// Assume that no promptId equates to the existence of a repeatable
			// set.
			if(null == promptId) { 
				
				String repeatableSetId = JsonUtils.getStringFromJsonObject(response, JsonInputKeys.SURVEY_REPEATABLE_SET_ID);
				
				// The response is malformed.
				if(repeatableSetId == null) { 
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, PROMPT_MISSING_PROMPT_ID_AND_REPEATABLE_SET_ID);
					throw new ServiceException(PROMPT_MISSING_PROMPT_ID_AND_REPEATABLE_SET_ID);
				}
				
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("beginning to validate a repeatableSet: " + repeatableSetId);
				}
				
				// Validate the repeatable set
				
				// A repeatable set must have the properties: skipped,
				// not_displayed, repeatable_set_id, and a responses 
				// array. The repeatable_set must exist in the survey
				// configuration.
				
				if(! configuration.repeatableSetExists(surveyId, repeatableSetId)) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_DOES_NOT_EXIST_IN_CONFIGURATION + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_DOES_NOT_EXIST_IN_CONFIGURATION + repeatableSetId);
				}
				
				// skipped here does not mean that the repeatable_set itself
				// was skipped. it just means that the user clicked "skip" 
				// (or some variant on skip) when presented with the 
				// continuation screen for the repeatable_set.
				
				String skipped = JsonUtils.getStringFromJsonObject(response, JsonInputKeys.SURVEY_REPEATABLE_SET_SKIPPED);
				
				if(StringUtils.isEmptyOrWhitespaceOnly(skipped) || ! StringUtils.isValidBoolean(skipped)) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_INVALID_SKIPPED + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_INVALID_SKIPPED + repeatableSetId);
				}
				
				String notDisplayed = JsonUtils.getStringFromJsonObject(response, JsonInputKeys.SURVEY_REPEATABLE_SET_NOT_DISPLAYED);
				
				if(StringUtils.isEmptyOrWhitespaceOnly(notDisplayed) || ! StringUtils.isValidBoolean(notDisplayed)) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_INVALID_NOT_DISPLAYED + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_INVALID_NOT_DISPLAYED + repeatableSetId);
				}
				
				JSONArray rsResponseArray = JsonUtils.getJsonArrayFromJsonObject(response, "responses");
				
				if(rsResponseArray == null) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_MISSING_RESPONSES + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_MISSING_RESPONSES + repeatableSetId);
				}
				
				// A zero-length array of responses is allowed only if the
				// repeatable set was not displayed
				int numberOfResponsesInRepeatableSet = rsResponseArray.length();
				
				if(numberOfResponsesInRepeatableSet == 0 && ! Boolean.valueOf(notDisplayed)) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_CONTAINS_NO_RESPONSES_FOR_DISPLAYED + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_CONTAINS_NO_RESPONSES_FOR_DISPLAYED + repeatableSetId);
				}
				
				if(numberOfResponsesInRepeatableSet != 0 && Boolean.valueOf(notDisplayed)) {
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, REPEATABLE_SET_CONTAINS_RESPONSES_FOR_NOT_DISPLAYED + repeatableSetId);
					throw new ServiceException(REPEATABLE_SET_CONTAINS_RESPONSES_FOR_NOT_DISPLAYED + repeatableSetId);
				}

				// Only validate if the repeatable set was displayed
				// i.e., not not displayed ;)
				if(! Boolean.valueOf(notDisplayed)) { 
				
					// Validate each repeatable set iteration
					for(int j = 0; j < numberOfResponsesInRepeatableSet; j++) {
						
						// Each repeatable set iteration in the responses array
						// is grouped into its own anonymous array
						JSONArray repeatableSetResponseArray = JsonUtils.getJsonArrayFromJsonArray(rsResponseArray, j);
						int numberOfResponsesInRepeatableSetIteration = repeatableSetResponseArray.length();
						
						// Make sure that every prompt in the repeatable set is
						// accounted for
						int numberOfPromptsRequired = configuration.numberOfPromptsInRepeatableSet(surveyId, repeatableSetId);
						
						if(numberOfResponsesInRepeatableSetIteration != numberOfPromptsRequired) {

							String message = REPEATABLE_SET_INCORRECT_NUMBER_OF_RESPONSES + "expected "  + numberOfPromptsRequired + 
								", but found " + numberOfResponsesInRepeatableSetIteration + " repeatable set id  " + repeatableSetId 
								+ " iteration " + j; 
							
							request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
							throw new ServiceException(message);
						}
						
						// Now check each prompt in the repeatable set
						for(int k = 0; k < numberOfResponsesInRepeatableSetIteration; k++) { 
							
							// Each array entry is a JSONObject representing a 
							// prompt response
							JSONObject promptResponse = JsonUtils.getJsonObjectFromJsonArray(repeatableSetResponseArray, k);
							
							if(promptResponse == null) {
								String message = REPEATABLE_SET_NULL_PROMPT_RESPONSE + k + FOR_REPEATABLE_SET_ID + repeatableSetId;
								request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
								throw new ServiceException(message);
							}
							
							String repeatableSetPromptId = JsonUtils.getStringFromJsonObject(promptResponse, "prompt_id");
							
							if(repeatableSetPromptId == null) {
								String message = REPEATABLE_SET_MISSING_PROMPT_ID + k + FOR_REPEATABLE_SET_ID + repeatableSetId; 
								request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
								throw new ServiceException(message);
							}
							
							// Make sure the prompt exists in the configuration
							if(! configuration.promptExists(surveyId, repeatableSetId, repeatableSetPromptId)) {
								String message = REPEATABLE_SET_UNKNOWN_PROMPT_ID + k + FOR_REPEATABLE_SET_ID + repeatableSetId; 
								request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
								throw new ServiceException(message);
							}
							
							Prompt prompt = configuration.getPrompt(surveyId, repeatableSetId, repeatableSetPromptId);
							String promptType = configuration.getPromptType(surveyId, repeatableSetId, repeatableSetPromptId);
							PromptValidator promptValidator = PROMPT_VALIDATOR_MAP.get(promptType);
							
							// This is bad because it means the configuration
							// portion of the server app is out of sync with
							// the upload portion of the app. The likely cause
							// is that a new prompt type was added to the 
							// system, but the validator was not added here.
							if(promptValidator == null) {
								String message = PROMPT_UNKNOWN_TYPE + promptType;
								request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
								throw new ServiceException(message);
								
							}
							
							if(LOGGER.isDebugEnabled()) {
								LOGGER.debug("Validating prompt " + repeatableSetPromptId + IN_REPEATABLE_SET + repeatableSetId);
							}
							
							if(! promptValidator.validate(prompt, promptResponse)) {
								
								String message = PROMPT_INVALID_VALUE + repeatableSetPromptId + IN_REPEATABLE_SET + repeatableSetId;
								request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
								throw new ServiceException(message);
							}
							
							if(prompt.getType().equals(PromptTypeKeys.TYPE_IMAGE)) {
								String imageId = JsonUtils.getStringFromJsonObject(promptResponse, JsonInputKeys.PROMPT_VALUE); 
								
								if(! JsonInputKeys.PROMPT_NOT_DISPLAYED.equals(imageId) && ! JsonInputKeys.PROMPT_SKIPPED.equals(imageId)) {
									imageIdList.add(imageId);
								}
							}
						}
					}
				}
			} 
			
			else {
				// If the response is not a repeatable set, it can only be a 
				// single prompt response.
				
				if(! configuration.promptExists(surveyId, promptId)) {
					String message = PROMPT_INVALID_FOR_SURVEY + " survey id: " + surveyId + ", prompt id " + promptId;
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
					throw new ServiceException(message);
				}
				
				String promptType = configuration.getPromptType(surveyId, promptId);
				Prompt prompt = configuration.getPrompt(surveyId, promptId);
				PromptValidator promptValidator = PROMPT_VALIDATOR_MAP.get(promptType);
				
				// This is bad because it means the configuration
				// portion of the server app is out of sync with
				// the upload portion of the app. The likely cause
				// is that a new prompt type was added to the 
				// system, but the validator was not added here.
				if(promptValidator == null) {
					String message = PROMPT_UNKNOWN_TYPE + promptType;
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
					throw new ServiceException(message);
					
				}
				
				if(LOGGER.isDebugEnabled()) {
					LOGGER.debug("validating prompt " + promptId);
				}
				
				if(! promptValidator.validate(prompt, response)) {
					String message = PROMPT_INVALID_VALUE + promptId;
					request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
					throw new ServiceException(message);
				}
				
				if(prompt.getType().equals(PromptTypeKeys.TYPE_IMAGE)) {
					String imageId = JsonUtils.getStringFromJsonObject(response, JsonInputKeys.PROMPT_VALUE); 
					
					if(! JsonInputKeys.PROMPT_NOT_DISPLAYED.equals(imageId) && ! JsonInputKeys.PROMPT_SKIPPED.equals(imageId)) {
						imageIdList.add(imageId);
					}
				}
			}
		}
		
		return imageIdList;
	}
	
	/**
	 * Checks that image ids found in the survey payload match the image ids
	 * found in the multi-part binary section of the payload.
	 * 
	 * @param request  The request to fail should the validation indicate a problem.
	 * @param idsFoundInSurveyPayload  A List of Strings representing image ids
	 * from the "data" portion of a survey upload.
	 * @param imagePayloadMap  A Map of image payload data.
	 * @throws ServiceException
	 */
	public static void validateImageKeys(Request request, List<String> idsFoundInSurveyPayload, Map<String, BufferedImage> imagePayloadMap) 
		throws ServiceException {
		
		if(idsFoundInSurveyPayload.isEmpty() && imagePayloadMap == null) {
			// Nothing to do because there are no images.
			return;
		}
		
		if(imagePayloadMap == null && ! idsFoundInSurveyPayload.isEmpty()) {
			String message = "No images found, but image ids were present in the survey upload.";
			request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
			throw new ServiceException(message);
		}
		
		if(imagePayloadMap != null && idsFoundInSurveyPayload.isEmpty()) {
			String message = "Images were found, but no image ids were present in the survey upload.";
			request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
			throw new ServiceException(message);
		}
		
		Set<String> payloadKeys = imagePayloadMap.keySet(); 
		
		for(String payloadKey : payloadKeys) {
			if(! idsFoundInSurveyPayload.contains(payloadKey)) {
				String message = "An image key was found that was not present in the survey payload.";
				request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, message);
				throw new ServiceException(message);
			}
		}
	}
	
	// Error message constants
	private static final String SURVEY_INVALID_JSON = "survey upload contains unparseable JSON";
	private static final String SURVEY_EMPTY = "survey upload contains an empty responses array";
	private static final String SURVEY_FOUND_EMPTY_PROMPT = "found an empty prompt response at index ";
	private static final String SURVEY_ID_MISSING = "survey_id is null or empty";
	private static final String SURVEY_ID_UNKNOWN = "survey_id is unknown";
	private static final String SURVEY_MISSING_RESPONSES = "responses array is null or empty";
	private static final String SURVEY_LAUNCH_CONTEXT_MISSING = "survey_launch_context is missing";
	private static final String SURVEY_LAUNCH_TIME_MISSING = "survey_launch_context launch_time is missing";
	private static final String PROMPT_MISSING_PROMPT_ID_AND_REPEATABLE_SET_ID = "malformed prompt response: both prompt_id and repeatable_set_id are missing";
	private static final String REPEATABLE_SET_DOES_NOT_EXIST_IN_CONFIGURATION = "repeatable set does not exist, the provided id is: ";
	private static final String REPEATABLE_SET_INVALID_SKIPPED = "'skipped' is missing or non-boolean in repeatable set: ";
	private static final String REPEATABLE_SET_INVALID_NOT_DISPLAYED = "'not_displayed' is missing or non-boolean in repeatable set: ";
	private static final String REPEATABLE_SET_MISSING_RESPONSES = "missing responses array in repeatable set: ";
	private static final String REPEATABLE_SET_CONTAINS_NO_RESPONSES_FOR_DISPLAYED = "empty responses array in repeatable set that was displayed. The repeatable set id is: ";
	private static final String REPEATABLE_SET_CONTAINS_RESPONSES_FOR_NOT_DISPLAYED = "non-empty responses array in repeatable set that was not displayed. The repeatable set id is: ";
	private static final String REPEATABLE_SET_INCORRECT_NUMBER_OF_RESPONSES = "incorrect number of prompts returned in repeatable set. ";
	private static final String REPEATABLE_SET_NULL_PROMPT_RESPONSE = "null prompt response detected at repeatable set iteration ";
	private static final String FOR_REPEATABLE_SET_ID = " for repeatable set id ";
	private static final String IN_REPEATABLE_SET = " in repeatable set ";
	private static final String REPEATABLE_SET_MISSING_PROMPT_ID = "missing prompt_id detected at repeatable set iteration ";
	private static final String REPEATABLE_SET_UNKNOWN_PROMPT_ID = "unknown prompt_id detected at repeatable set iteration ";
	private static final String PROMPT_UNKNOWN_TYPE = "a prompt of an unknown type detected in a configuration: ";
	private static final String PROMPT_INVALID_VALUE = "found invalid value for prompt id ";
	private static final String PROMPT_INVALID_FOR_SURVEY = "survey does not contain prompt ";
}
