/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.ohmage.util.JsonUtils;


/**
 * @author Joshua Selsky
 */
public class SurveyDataPacketBuilder implements DataPacketBuilder {
	private static Logger _logger = Logger.getLogger(SurveyDataPacketBuilder.class);
	
	public SurveyDataPacketBuilder() {
		
	}
	
	/**
	 * Creates a SurveyDataPacket from a survey upload. Assumes that the upload message is valid.
	 */
	public DataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest) {
		
		// FIXME - drop cast
		Configuration configuration = ((SurveyUploadAwRequest) awRequest).getConfiguration();
		
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
				
				// now each element in the array is also an array
				for(int j = 0; j < outerArray.length(); j++) {
					JSONArray repeatableSetResponses =  JsonUtils.getJsonArrayFromJsonArray(outerArray, j);
					int numberOfRepeatableSetResponses = repeatableSetResponses.length();
					
					for(int k = 0; k < numberOfRepeatableSetResponses; k++) { 
						PromptResponseDataPacket promptResponseDataPacket = new PromptResponseDataPacket();
						
						JSONObject rsPromptResponse = JsonUtils.getJsonObjectFromJsonArray(repeatableSetResponses, k);
						String promptId = JsonUtils.getStringFromJsonObject(rsPromptResponse, "prompt_id");
						
						promptResponseDataPacket.setPromptId(promptId);
						promptResponseDataPacket.setRepeatableSetId(repeatableSetId);
						promptResponseDataPacket.setRepeatableSetIteration(j);

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
	 * Sets the value attribute on the dataPacket. The value will be a JSONObject if the promptType is one of the custom types.  
	 */
	private void handleDataPacketValue(PromptResponseDataPacket dataPacket, JSONObject response, String promptType) {
		JSONArray customChoicesArray = JsonUtils.getJsonArrayFromJsonObject(response, "custom_choices");
		
		if(null != customChoicesArray) {
			response.remove("prompt_id"); // the prompt id is stored in its own column so we don't need it in the response
			dataPacket.setValue(response.toString());
			
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
		if("multi_choice".equals(promptType)) {
			return string.replace("\"", "");
		}
		return string;
	}
}
