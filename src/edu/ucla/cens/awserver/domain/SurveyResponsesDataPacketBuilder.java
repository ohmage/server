//package edu.ucla.cens.awserver.domain;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.log4j.Logger;
//import org.json.JSONArray;
//import org.json.JSONObject;
//
//import edu.ucla.cens.awserver.util.JsonUtils;
//
///**
// * @author selsky
// */
//public class SurveyResponsesDataPacketBuilder extends AbstractDataPacketBuilder {
//	private static Logger _logger = Logger.getLogger(SurveyResponsesDataPacketBuilder.class);
//	
//	/**
//	 * Creates a SurveyResponsesDataPacket from a survey upload. Assumes that the upload message is valid. Each prompt response
//	 * from a survey is converted to its storable form. The storable form is dependent on the data type of the prompt. The 
//	 * following prompt types are stored formatted as strings: timestamp, number, hours_before_now, text, single_choice, photo. 
//	 * multi_choice values are stored as a JSON array. multi_choice_custom and single_choice_custom are stored as JSON objects. 
//	 */
//	public DataPacket createDataPacketFrom(JSONObject source) {
//		List<PromptResponseDataPacket> promptResponseDataPackets  = new ArrayList<PromptResponseDataPacket>();
//		SurveyResponsesDataPacket surveyResponsesDataPacket = new SurveyResponsesDataPacket();
//		
//		// Get the main array of prompt responses from the survey
//		JSONArray responses = JsonUtils.getJsonArrayFromJsonObject(source, "responses");
//		int arrayLength = responses.length();	
//		
//		for(int i = 0; i < arrayLength; i++) {
//			JSONObject responseObject = JsonUtils.getJsonObjectFromJsonArray(responses, i);
//			
//			// Check to see if its a repeatable set
//			String repeatableSetId = JsonUtils.getStringFromJsonObject(responseObject, "repeatable_set_id");
//			
//			if(null != repeatableSetId) {
//				
//				// ok, grab the inner responses - repeatable sets are anonymous objects in an array of arrays
//				// get the outer array
//				JSONArray outerArray = JsonUtils.getJsonArrayFromJsonObject(responseObject, "responses");
//				_logger.info("outerArray.length()=" + outerArray.length());
//				
//				// now each element in the array is also an array
//				for(int j = 0; j < outerArray.length(); j++) {
//					JSONArray repeatableSetResponses =  JsonUtils.getJsonArrayFromJsonArray(outerArray, j);
//					int numberOfRepeatableSetResponses = repeatableSetResponses.length(); _logger.info("numberOfRepeatableSetResponses=" + numberOfRepeatableSetResponses);
//					for(int k = 0; k < numberOfRepeatableSetResponses; k++) { 
//						PromptResponseDataPacket promptResponseDataPacket = new PromptResponseDataPacket();
//						JSONObject rsPromptResponse = JsonUtils.getJsonObjectFromJsonArray(repeatableSetResponses, k);
//						promptResponseDataPacket.setPromptId(JsonUtils.getStringFromJsonObject(rsPromptResponse, "prompt_id"));
//						promptResponseDataPacket.setRepeatableSetId(repeatableSetId);
//						// TODO will this autoconvert JSON objects to strings?
//						// TODO if it's a custom type, the custom_choices must be persisted with the value
//						promptResponseDataPacket.setValue(JsonUtils.getStringFromJsonObject(rsPromptResponse, "value")); 
//						promptResponseDataPackets.add(promptResponseDataPacket);
//					}
//				}
//				
//			} else {
//				
//				PromptResponseDataPacket promptResponseDataPacket = new PromptResponseDataPacket();
//				promptResponseDataPacket.setPromptId(JsonUtils.getStringFromJsonObject(responseObject, "prompt_id"));
//				// TODO will this autoconvert JSON objects to strings?
//				// TODO if it's a custom type, the custom_choices must be persisted with the value 
//				promptResponseDataPacket.setValue(JsonUtils.getStringFromJsonObject(responseObject, "value"));
//				promptResponseDataPacket.setRepeatableSetId(null);
//				promptResponseDataPackets.add(promptResponseDataPacket);
//			}
//		}
//
//		surveyResponsesDataPacket.setResponses(promptResponseDataPackets);
//		
//		if(_logger.isDebugEnabled()) {
//			_logger.debug(surveyResponsesDataPacket);
//		}
//		
//		return surveyResponsesDataPacket;
//	}
//}
