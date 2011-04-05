package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.cache.CacheService;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.service.ServiceException;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public class SurveyDataPacketBuilder implements DataPacketBuilder {
	private static Logger _logger = Logger.getLogger(SurveyDataPacketBuilder.class);
	private CacheService _configurationCacheService;
	
	public SurveyDataPacketBuilder(CacheService configurationCacheService) {
		if(null == configurationCacheService) {
			throw new IllegalArgumentException("a configuration cache service is required");
		}
		_configurationCacheService = configurationCacheService;
	}
	
	/**
	 * Creates a SurveyDataPacket from a survey upload. Assumes that the upload message is valid.
	 */
	public DataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest) {
		SurveyDataPacket surveyDataPacket = new SurveyDataPacket();
		List<PromptResponseDataPacket> promptResponseDataPackets  = new ArrayList<PromptResponseDataPacket>();
		
		surveyDataPacket.setLocationStatus(JsonUtils.getStringFromJsonObject(source, "location_status"));
		JSONObject location = JsonUtils.getJsonObjectFromJsonObject(source, "location");
		surveyDataPacket.setLocation(location == null ? null : location.toString());
		surveyDataPacket.setDate(JsonUtils.getStringFromJsonObject(source, "date"));
		surveyDataPacket.setEpochTime(JsonUtils.getLongFromJsonObject(source, "time"));
		surveyDataPacket.setTimezone(JsonUtils.getStringFromJsonObject(source, "timezone"));
		String surveyId = JsonUtils.getStringFromJsonObject(source, "survey_id");
		surveyDataPacket.setSurveyId(surveyId);
		surveyDataPacket.setLaunchContext(JsonUtils.getJsonObjectFromJsonObject(source, "survey_launch_context").toString());
		
		surveyDataPacket.setSurvey(source.toString()); // the whole JSONObject is stored in order to avoid having to recreate
		                                               // it after the fact
		
		JSONArray responses = JsonUtils.getJsonArrayFromJsonObject(source, "responses");

		int arrayLength = responses.length();	
		
		for(int i = 0; i < arrayLength; i++) {
			JSONObject responseObject = JsonUtils.getJsonObjectFromJsonArray(responses, i);
			
			// Check to see if its a repeatable set
			String repeatableSetId = JsonUtils.getStringFromJsonObject(responseObject, "repeatable_set_id");
			
			if(null != repeatableSetId) {
				
				// ok, grab the inner responses - repeatable sets are anonymous objects in an array of arrays
				// get the outer array
				JSONArray outerArray = JsonUtils.getJsonArrayFromJsonObject(responseObject, "responses");
				//_logger.info("outerArray.length()=" + outerArray.length());
				
				// now each element in the array is also an array
				for(int j = 0; j < outerArray.length(); j++) {
					JSONArray repeatableSetResponses =  JsonUtils.getJsonArrayFromJsonArray(outerArray, j);
					int numberOfRepeatableSetResponses = repeatableSetResponses.length();
					
					// _logger.info("numberOfRepeatableSetResponses=" + numberOfRepeatableSetResponses);
					
					for(int k = 0; k < numberOfRepeatableSetResponses; k++) { 
						PromptResponseDataPacket promptResponseDataPacket = new PromptResponseDataPacket();
						
						JSONObject rsPromptResponse = JsonUtils.getJsonObjectFromJsonArray(repeatableSetResponses, k);
						String promptId = JsonUtils.getStringFromJsonObject(rsPromptResponse, "prompt_id");
						
						promptResponseDataPacket.setPromptId(promptId);
						promptResponseDataPacket.setRepeatableSetId(repeatableSetId);
						promptResponseDataPacket.setRepeatableSetIteration(j);
						
						Configuration configuration = (Configuration) _configurationCacheService.lookup(awRequest.getCampaignUrn());
						String promptType = configuration.getPromptType(surveyId, repeatableSetId, promptId);
						promptResponseDataPacket.setType(promptType);
						
						handleDataPacketValue(promptResponseDataPacket, rsPromptResponse, promptType);
						
						promptResponseDataPackets.add(promptResponseDataPacket);
					}
				}
				
			} else {
				
				PromptResponseDataPacket promptResponseDataPacket = new PromptResponseDataPacket();
				String promptId = JsonUtils.getStringFromJsonObject(responseObject, "prompt_id");
				promptResponseDataPacket.setPromptId(promptId);
				promptResponseDataPacket.setRepeatableSetId(null);
				Configuration configuration = (Configuration) _configurationCacheService.lookup(awRequest.getCampaignUrn());
				String promptType = configuration.getPromptType(surveyId, promptId); 
				promptResponseDataPacket.setType(promptType);
				handleDataPacketValue(promptResponseDataPacket, responseObject, promptType);
				promptResponseDataPackets.add(promptResponseDataPacket);
			}
		}

		surveyDataPacket.setResponses(promptResponseDataPackets);
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(surveyDataPacket);
		}
		
		return surveyDataPacket;
	}
	
	/**
	 * Creates a JSON object for insertion into the db for the custom prompt types.
	 */
	private String customChoicesJsonString(JSONObject responseObject, String promptType) {
		JSONObject copyObject = new JSONObject();
		
		try {
			
			copyObject.put("custom_choices", JsonUtils.getJsonArrayFromJsonObject(responseObject, "custom_choices"));
			
			Object value = null;
			
			// handle number types explicitly so they aren't quoted like strings
			if(isNumberType(promptType)) {
				
				copyObject.put("value", JsonUtils.getIntegerFromJsonObject(responseObject, "value"));
				
			} else if(isArrayType(promptType)){
				
				copyObject.put("value", JsonUtils.getJsonArrayFromJsonObject(responseObject, "value"));
				
			} else {
		
				copyObject.put("value", stripQuotes((String) value, promptType)); 
			}
			
		} catch (JSONException jsone) {
			
			_logger.error("caught JSONException when attempting to build custom prompt response for db insertion", jsone);
			throw new ServiceException(jsone);
			
		}
		
		return copyObject.toString();
	}
	
	private boolean isNumberType(String promptType) {
		return "number".equals(promptType) 
			|| "hours_before_now".equals(promptType)
			|| "single_choice".equals(promptType)
			|| "single_choice_custom".equals(promptType);
	}
	
	
	private boolean isArrayType(String promptType) {
		return "multi_choice".equals(promptType) 
			|| "multi_choice_custom".equals(promptType);
	}
	
	/**
	 * Sets the value attribute on the dataPacket. The value will be a JSONObject if the promptType is one of the custom types.  
	 */
	private void handleDataPacketValue(PromptResponseDataPacket dataPacket, JSONObject response, String promptType) {
		JSONArray customChoicesArray = JsonUtils.getJsonArrayFromJsonObject(response, "custom_choices");
		
		if(null != customChoicesArray) {
			
			dataPacket.setValue(customChoicesJsonString(response, promptType));
			
		} else {
			String value = JsonUtils.getStringFromJsonObject(response, "value");
			// _logger.info("single value =" + value);
			
			dataPacket.setValue(stripQuotes(value, promptType));
		}
	}
	
	/**
	 * Strip quotes from String-ified JSONArrays. The JSON library will auto-quote arrays if you ask for them as strings. 
	 */
	private String stripQuotes(String string, String promptType) {
		if("multi_choice".equals(promptType) || "multi_choice_custom".equals(promptType)) {
			return string.replace("\"", "");
		}
		return string;
	}
}
