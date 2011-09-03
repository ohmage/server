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
package org.ohmage.domain.survey.read;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.util.JsonUtils;


/**
 * For survey response read, merges values from a campaign configuration with
 * the query results. Also performs display value formatting depending on the
 * prompt type contained in the SurveyResponseReadResult.
 * 
 * @author Joshua Selsky
 */
public class ConfigurationValueMerger {
	private static Logger _logger = Logger.getLogger(ConfigurationValueMerger.class);
	
	/**
	 * Private to prevent instantiation.
	 */
	private ConfigurationValueMerger() { }
	
	/**
	 * For the given result, includes static values from the configuration that
	 * are used for writing output
	 * 
	 * @param result  The result to merge configuration values into.
	 * @param configuration  The configuration to retrieve values from.
	 */
	public static void merge(SurveyResponseReadResult result, Configuration configuration) {
		
		result.setSurveyTitle(configuration.getSurveyTitleFor(result.getSurveyId()));
		result.setSurveyDescription(configuration.getSurveyDescriptionFor(result.getSurveyId()));
		
		if(result.isRepeatableSetResult()) {
			
			result.setUnit(configuration.getUnitFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
			result.setDisplayLabel(
				configuration.getDisplayLabelFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
			);
			result.setDisplayType(configuration.getDisplayTypeFor(
				result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
			);
			result.setPromptText(configuration.getPromptTextFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
			
			if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
				result.setSingleChoiceOrdinalValue(convertToNumber(configuration.getValueForChoiceKey(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId(), String.valueOf(result.getResponse()))));
				result.setSingleChoiceLabel(configuration.getLabelForChoiceKey(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId(), String.valueOf(result.getResponse())));
				setDisplayValueFromSingleChoice(result);	
				
			}
			else if(PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
				setDisplayValueFromMultiChoice(result);
				
			} 
			else if(PromptTypeUtils.isJsonObject(result.getPromptType())) {
			
				result.setDisplayValue((String) result.getResponse());
				
			}
			else {

				result.setDisplayValue(PromptTypeUtils.isNumberPromptType(result.getPromptType()) ? convertToNumber(result.getResponse()) : result.getResponse());
			}
			
		} else {
			
			result.setUnit(configuration.getUnitFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayLabel(configuration.getDisplayLabelFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayType(configuration.getDisplayTypeFor(result.getSurveyId(), result.getPromptId()));
			result.setPromptText(configuration.getPromptTextFor(result.getSurveyId(), result.getPromptId()));
			
			if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getPromptId()));
				result.setSingleChoiceOrdinalValue(convertToNumber(configuration.getValueForChoiceKey(result.getSurveyId(), result.getPromptId(), String.valueOf(result.getResponse()))));
				result.setSingleChoiceLabel(configuration.getLabelForChoiceKey(result.getSurveyId(), result.getPromptId(), String.valueOf(result.getResponse())));
				setDisplayValueFromSingleChoice(result);
			
			}
			else if (PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getPromptId()));
				setDisplayValueFromMultiChoice(result);
				
			} 
			else if (PromptTypeUtils.isRemoteActivityType(result.getPromptType())) {
				
				setDisplayValueFromRemoteActivity(result);
			}
			else if(PromptTypeUtils.isJsonObject(result.getPromptType())) {
				try {
					result.setDisplayValue(new JSONObject(String.valueOf(result.getResponse())));
				}
				catch(JSONException e) {
					_logger.warn("could not convert custom choice prompt response to a JSON object");
					result.setDisplayValue(new JSONObject());
				}
				
			} else {
				
				result.setDisplayValue(PromptTypeUtils.isNumberPromptType(result.getPromptType()) ? convertToNumber(result.getResponse()) : result.getResponse());
			}
		}
		
//		if(_logger.isDebugEnabled()) {
//			_logger.debug(result);
//		}
		
	}
	
	/**
	 * Sets the display value on the result the for single_choice prompt type.
	 * The display value is converted to a number if it is indeed a number. If
	 * the result cannot be coerced into a number, the display value will be
	 * treated as a String. It would be quite unusual for the key not to be a 
	 * number, but perhaps in the future non-numeric keys will be allowed.
	 *  
	 * @param result The result to set the display value on.
	 */
	private static void setDisplayValueFromSingleChoice(SurveyResponseReadResult result) {
		// The response here is the *index* (key) of the single_choice response
		result.setDisplayValue(convertToNumber(result.getResponse())); 
	}
	
	/**
	 * Sets the display value for the multi_choice prompt type. The display
	 * value is a JSON array. If the value cannot be converted into an 
	 * array, a warning message is printed and an empty array is set as the
	 * display value.
	 * 
	 * @param result The result to set the display value on.
	 */
	private static void setDisplayValueFromMultiChoice(SurveyResponseReadResult result) {
		try {
			result.setDisplayValue(new JSONArray(String.valueOf(result.getResponse())));
		} catch (JSONException je) {
			// Check for SKIPPED or NOT_DISPLAYED
			String str = String.valueOf(result.getResponse());
			if("SKIPPED".equals(str) || "NOT_DISPLAYED".equals(str)) {
				result.setDisplayValue(str);
			} 
			else {
				_logger.warn("multi_choice response is not a string or a JSONArray: " + result.getResponse());
				result.setDisplayValue(new JSONArray());
			}
		}
	}
	
	/**
	 * Formats and sets the display value in the result for the remote activity
	 * prompt type.
	 * 
	 * @param result The result where the display value will be set.
	 */
	private static void setDisplayValueFromRemoteActivity(SurveyResponseReadResult result) {
		JSONArray responseArray = JsonUtils.getJsonArrayFromString(String.valueOf(result.getResponse()));
		
		if(responseArray != null) {
			double total = 0.0f;
			
			for(int i = 0; i < responseArray.length(); i++) {
				JSONObject currRun;
				try {
					currRun = responseArray.getJSONObject(i);
				}
				catch(JSONException e) {
					_logger.warn("Could not convert an individual run of a RemoteActivity response into JSONObjects.", e);
					continue;
				}
				
				try {
					total += Double.valueOf(currRun.get("score").toString());
				}
				catch(JSONException e) {
					_logger.warn("Missing necessary key in RemoteActivity run.", e);
					continue;
				}
				catch(NullPointerException e) {
					_logger.warn("Missing necessary key in RemoteActivity run.", e);
					continue;
				}
				catch(ClassCastException e) {
					_logger.warn("Cannot cast the score value into a double.");
					continue;
				}
			}
			
			if(responseArray.length() == 0) {
				result.setDisplayValue(0.0);
			}
			else {
				result.setDisplayValue(total / responseArray.length());
			}
		}
		else {
			result.setDisplayValue(String.valueOf(result.getResponse()));
		}
	}
	
	/**
	 * Lazy number conversion so the JSON lib serializes output properly. 
	 * Individual prompt responses are stored in a schema-less db column
	 * and retrieved as Objects. Here type coercion is attempted in order to
	 * avoid quoting numbers that may have a String datatype given to them via
	 * JDBC. 
	 */
	private static Object convertToNumber(Object value) {
		try {
			return Integer.parseInt(String.valueOf(value));
		} catch (NumberFormatException e) { 
			try {
				return Double.parseDouble(String.valueOf(value));
					
			} catch (NumberFormatException e2) {
				// Ignore because the value must be a string or some other
				// number representation that can be treated as a JSON string
			} 
		}
		return value;
	}
}
