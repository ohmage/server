package org.ohmage.domain.upload;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.request.JsonInputKeys;
import org.ohmage.util.JsonUtils;

/**
 * Builder class to convert JSON survey uploads into POJOs.
 * 
 * @author Joshua Selsky
 */
public final class SurveyUploadBuilder {
	private static Logger logger = Logger.getLogger(SurveyUploadBuilder.class);
	
	/**
	 * Private to prevent instantiation.
	 */
	private SurveyUploadBuilder() { } 
	
	/**
	 * Converts the JSON survey upload into a SurveyUpload in order to 
	 * decouple the data layer from JSON. Assumes the data in the survey
	 * JSONObject has already been validated.
	 * 
	 * @param configuration  The configuration to use for lookups from the
	 * associated campaign. 
	 * @param survey  The survey to convert.
	 * @return  The JSON survey converted to a POJO.
	 */
	public static SurveyResponse createSurveyUploadFrom(Configuration configuration, JSONObject survey) {
		
		List<PromptResponse> convertedPromptResponses = new ArrayList<PromptResponse>();
		
		String locationStatus = JsonUtils.getStringFromJsonObject(survey, JsonInputKeys.METADATA_LOCATION_STATUS);
		JSONObject location = JsonUtils.getJsonObjectFromJsonObject(survey, JsonInputKeys.METADATA_LOCATION);
		
		// The location is allowed to be null if the location_status is
		// unavailable.
		String locationString = null;
		if(location != null) {
			locationString = location.toString();
		}
		
		String date = JsonUtils.getStringFromJsonObject(survey, JsonInputKeys.METADATA_DATE);
		Long time = JsonUtils.getLongFromJsonObject(survey, JsonInputKeys.METADATA_TIME);
		String timezone = JsonUtils.getStringFromJsonObject(survey, JsonInputKeys.METADATA_TIMEZONE);
		String surveyId = JsonUtils.getStringFromJsonObject(survey, JsonInputKeys.SURVEY_ID);
		String launchContext = JsonUtils.getJsonObjectFromJsonObject(survey, JsonInputKeys.SURVEY_LAUNCH_CONTEXT).toString();
		String surveyString = survey.toString(); 
		
		JSONArray promptResponses = JsonUtils.getJsonArrayFromJsonObject(survey, JsonInputKeys.SURVEY_RESPONSES);
		
		int arrayLength = promptResponses.length();	
		
		for(int i = 0; i < arrayLength; i++) {
			JSONObject responseObject = JsonUtils.getJsonObjectFromJsonArray(promptResponses, i);
			
			// Check to see if its a repeatable set
			String repeatableSetId = JsonUtils.getStringFromJsonObject(responseObject, JsonInputKeys.SURVEY_REPEATABLE_SET_ID);
			
			if(repeatableSetId != null) {
				
				// ok, grab the inner responses - repeatable sets are anonymous
				// objects in an array of arrays
				JSONArray outerArray = JsonUtils.getJsonArrayFromJsonObject(responseObject, JsonInputKeys.SURVEY_RESPONSES);
				
				// Now each element in the outer array is also an array
				for(int j = 0; j < outerArray.length(); j++) {
					JSONArray repeatableSetResponses =  JsonUtils.getJsonArrayFromJsonArray(outerArray, j);
					int numberOfRepeatableSetResponses = repeatableSetResponses.length();
					
					for(int k = 0; k < numberOfRepeatableSetResponses; k++) { 
						
						JSONObject rsPromptResponse = JsonUtils.getJsonObjectFromJsonArray(repeatableSetResponses, k);
						String promptId = JsonUtils.getStringFromJsonObject(rsPromptResponse, JsonInputKeys.SURVEY_PROMPT_ID);
						String repeatableSetIteration = String.valueOf(j);
						String promptType = configuration.getPromptType(surveyId, repeatableSetId, promptId);
						String value = handleDataPacketValue(rsPromptResponse, promptType);
						
						convertedPromptResponses.add(new PromptResponse(promptId, repeatableSetId, repeatableSetIteration, value, promptType));
					}
				}
				
			} else {
				
				String promptId = JsonUtils.getStringFromJsonObject(responseObject, "prompt_id");
				String promptType = configuration.getPromptType(surveyId, promptId); 
				String value = handleDataPacketValue(responseObject, promptType);
				
				convertedPromptResponses.add(new PromptResponse(promptId, null, null, value, promptType));
			}
		}

		SurveyResponse surveyUpload = new SurveyResponse(date, time, timezone, locationStatus, locationString, surveyString, surveyId, launchContext, convertedPromptResponses);
		
		if(logger.isDebugEnabled()) {
			logger.debug(surveyUpload);
		}
		
		return surveyUpload;
	}
	
	
	/**
	 * Handles special formatting cases for custom types and JSON arrays. For
	 * custom types, the prompt id is removed from the JSONObject 
	 * (custom_choices) because the prompt id is stored in its own db column.
	 * For multi_choice respsonses, the json.org JSON lib escapes JSON arrays
	 * using quotes when asked for arrays to be returned as Strings, so the 
	 * quotes have to be stripped in order for the value to be a 'clean'
	 * JSON array when it is eventually stored in the db.
	 * 
	 * @param response  The response containing a value to convert.
	 * @param promptType  The prompt type that indicates whether any special
	 * handling needs to occur.
	 * @return  The (possibly cleaned up) response value for any prompt type as
	 * a String.
	 */
	private static String handleDataPacketValue(JSONObject response, String promptType) {
		JSONArray customChoicesArray = JsonUtils.getJsonArrayFromJsonObject(response, JsonInputKeys.PROMPT_CUSTOM_CHOICES);
		
		if(null != customChoicesArray) {
			
			// Remove the prompt id because it is stored in its own column in 
			// the db.
			response.remove(JsonInputKeys.SURVEY_PROMPT_ID);
			return response.toString(); 
			
		} else {
			
			return stripQuotes(JsonUtils.getStringFromJsonObject(response, JsonInputKeys.PROMPT_VALUE), promptType);
		}
	}
	
	/**
	 * Strip quotes from String-ified JSONArrays. The JSON library will auto-quote arrays if you ask for them as strings.
	 * 
	 * TODO Move to StringUtils?
	 */
	private static String stripQuotes(String string, String promptType) {
		if("multi_choice".equals(promptType)) {
			return string.replace("\"", "");
		}
		return string;
	}
}
