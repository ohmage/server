package org.ohmage.service;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.JsonUtils;

/**
 * 
 * @author Joshua Selsky
 */
public final class SurveyUploadJsonServices {
	private static Logger logger = Logger.getLogger(SurveyUploadJsonServices.class);
	
	private static List<String> locationStatuses = Arrays.asList(new String[] {
		JsonInputKeys.JSON_SURVEY_LOCATION_STATUS_UNAVAILABLE,
		JsonInputKeys.JSON_SURVEY_LOCATION_STATUS_INACCURATE,
		JsonInputKeys.JSON_SURVEY_LOCATION_STATUS_VALID,
		JsonInputKeys.JSON_SURVEY_LOCATION_STATUS_STALE
	});
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private SurveyUploadJsonServices() {}
	
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
	
			request.setFailed(ErrorCodes.SERVER_INVALID_JSON, "could not parse JSON message");
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks each element in the provided JSONArray to make sure it 
	 * corresponds to our survey upload syntax. Each array element is a JSON 
	 * object that represents a survey.
	 * 
	 * @param request The request to fail should validation fail.
	 * @param surveyResponses The array of responses to validate.
	 * @param configuration The campaign configuration used to aid in validating specific prompt responses.
	 * @throws ServiceException If any part of the upload is syntactically or semantically invalid.
	 */
	public static void validateSurveyUpload(Request request, JSONArray surveyResponses, Configuration configuration)
		throws ServiceException {
//	        <ref bean="jsonMsgLocationValidator" />
//	        <ref bean="jsonMsgSurveyIdValidator" />
//	        <ref bean="jsonMsgSurveyResponsesExistValidator" />
//	        <ref bean="jsonMsgSurveyLaunchContextValidator" />
//	        <ref bean="jsonMsgSurveyResponsesValidator" />
		
		int numberOfResponses = surveyResponses.length();
		
		logger.info("Validating " + numberOfResponses + " survey responses");
		
		if(surveyResponses.length() < 1) {
			
			request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, "responses array is empty");
			throw new ServiceException("responses array is empty");
			
		}
		
		for(int i = 0; i < numberOfResponses; i++) {
			
			JSONObject jsonObject = JsonUtils.getJsonObjectFromJsonArray(surveyResponses, i);
			
			// If an empty response is found in the array, the entire request fails because
			// it indicates a client-side logical error. This is ultra-strict and in the 
			// future it might be nice to be more lenient following the maxim: be lenient in
			// what you accept and strict in what you output. Perhaps not for JSON content, 
			// but it may make sense to implement for query parameters.
			if(jsonObject == null) {
				
				String msg = "found an empty response at index " + i; 
				request.setFailed(ErrorCodes.SURVEY_INVALID_RESPONSES, msg);
				throw new ServiceException(msg);
				
			}
			
			// FIXME this is actually a timestamp
			String metadataDate = JsonUtils.getStringFromJsonObject(jsonObject, JsonInputKeys.JSON_METADATA_DATE);
			Long epochTime = JsonUtils.getLongFromJsonObject(jsonObject, JsonInputKeys.JSON_METADATA_TIME);
			String timezone = JsonUtils.getStringFromJsonObject(jsonObject, JsonInputKeys.JSON_METADATA_TIMEZONE);
			String locationStatus = JsonUtils.getStringFromJsonObject(jsonObject, JsonInputKeys.JSON_SURVEY_LOCATION_STATUS);
			JSONObject location = JsonUtils.getJsonObjectFromJsonObject(jsonObject, JsonInputKeys.JSON_SURVEY_LOCATION);
			
			
			UploadValidationServices.validateIso8601Timestamp(request, metadataDate);
			UploadValidationServices.validateEpochTime(request, epochTime);
			UploadValidationServices.validateTimezone(request, timezone);
			validateLocationStatus(request, locationStatus);
			validateLocation(request, location, locationStatus);

			// survey id
			// responses exist
			// launch context
			// each prompt in the responses for the survey
			
			
		}
	}
	
	/**
	 * Validates that the provided location status is not null and an accepted
	 * location status value.
	 * 
	 * @param request  The request to fail should the location status be invalid.
	 * @param locationStatus  The location status to validate.
	 * @throws ServiceException  If the location status is null or invalid.
	 */
	private static void validateLocationStatus(Request request, String locationStatus) throws ServiceException {
		if(locationStatus == null) {
			String msg = "location_status in upload message is null";
			request.setFailed(ErrorCodes.SURVEY_INVALID_LOCATION_STATUS, msg);
			throw new ServiceException(msg);
		}
		
		if(! locationStatuses.contains(locationStatus)) {
			String msg = "location_status in upload message is invalid: " + locationStatus;
			request.setFailed(ErrorCodes.SURVEY_INVALID_LOCATION_STATUS, msg);
			throw new ServiceException(msg);
		}
	}
	
	/**
	 * Validates the provided location object. If the location status is 
	 * unavailable, it is an error to send a location object. Otherwise,
	 * the location object must contain values for longitude, latitude,
	 * accuracy, provider, and timestamp.
	 *  
	 * @param request  The request to fail should the location object be
	 * invalid.
	 * @param location  A JSON object containing location properties to be
	 * validated.
	 * @throws ServiceException  If the location is null and the location 
	 * status is unavailable or if the location status is not unavailable
	 * and the location is invalid in structure.
	 */
	private static void validateLocation(Request request, JSONObject location, String locationStatus) throws ServiceException {
		if(locationStatus.equals(JsonInputKeys.JSON_SURVEY_LOCATION_STATUS_UNAVAILABLE) && location != null) {
			String msg = "location object in upload message exists, but location status is unavailable";
			request.setFailed(ErrorCodes.SURVEY_INVALID_LOCATION, msg);
			throw new ServiceException(msg);
		}
		
		if(location == null) {
			String msg = "missing location object in upload message";
			request.setFailed(ErrorCodes.SURVEY_INVALID_LOCATION, msg);
			throw new ServiceException(msg);
		}
		
		
		// TODO
		
		
		
	}
}
