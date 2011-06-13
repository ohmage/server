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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.JsonUtils;


/**
 * For the "new" data point API, merges values from a conifguration into DB result object.
 * 
 * TODO convert to interface?
 * 
 * @author selsky
 */
public class ConfigurationValueMerger {
	private static Logger _logger = Logger.getLogger(ConfigurationValueMerger.class);
	
	public void merge(SurveyResponseReadResult result, Configuration configuration) {
		
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
				setDisplayValueFromSingleChoice(result, configuration, true);	
				
			} else if(PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
				setDisplayValueFromMultiChoice(result, configuration, true);
				
			} else { 

				result.setDisplayValue(PromptTypeUtils.isNumberPromptType(result.getPromptType()) ? convertToNumber(result.getResponse()) : result.getResponse());
			}
			
		} else {
			
			result.setUnit(configuration.getUnitFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayLabel(configuration.getDisplayLabelFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayType(configuration.getDisplayTypeFor(result.getSurveyId(), result.getPromptId()));
			result.setPromptText(configuration.getPromptTextFor(result.getSurveyId(), result.getPromptId()));
			
			if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getPromptId()));
				setDisplayValueFromSingleChoice(result, configuration, false);
			
			} else if (PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				result.setChoiceGlossary(configuration.getChoiceGlossaryFor(result.getSurveyId(), result.getPromptId()));
				setDisplayValueFromMultiChoice(result, configuration, false);
				
			} else if (PromptTypeUtils.isRemoteActivityType(result.getPromptType())) {
				
				setDisplayValueFromRemoteActivity(result, configuration);
				
			} else {
				
				result.setDisplayValue(PromptTypeUtils.isNumberPromptType(result.getPromptType()) ? convertToNumber(result.getResponse()) : result.getResponse());
			}
		}
		
//		if(_logger.isDebugEnabled()) {
//			_logger.debug(result);
//		}
		
	}
	
	private void setDisplayValueFromSingleChoice(SurveyResponseReadResult result, Configuration config, boolean isRepeatableSetItem) {
		String value = null;
		
		if(isRepeatableSetItem) {
			value = config.getValueForChoiceKey(
				result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId(), String.valueOf(result.getResponse())
			);
		} else {
			value = config.getValueForChoiceKey(
				result.getSurveyId(), result.getPromptId(), String.valueOf(result.getResponse())
			);
		}
		
		if(null != value) {
			result.setDisplayValue(convertToNumber(value));
		} else {
			result.setDisplayValue(convertToNumber(result.getResponse()));
		}
	}
	
	private void setDisplayValueFromMultiChoice(SurveyResponseReadResult result, Configuration config, boolean isRepeatableSetItem) {
	
		JSONArray responseArray = JsonUtils.getJsonArrayFromString(String.valueOf(result.getResponse()));
		
		if(null != responseArray) {
			JSONArray valueArray = new JSONArray();
			int length = responseArray.length();
			
			for(int j = 0; j < length; j++) {
				
				Object value = null;
				
				if(isRepeatableSetItem) {
					value = config.getValueForChoiceKey(result.getSurveyId(), result.getRepeatableSetId(), 
						result.getPromptId(), JsonUtils.getStringFromJsonArray(responseArray, j)
					);
				} else {
					value = config.getValueForChoiceKey(
						result.getSurveyId(), result.getPromptId(), JsonUtils.getStringFromJsonArray(responseArray, j)
					);
				}
				
				if(null == value) {
					break;
				} else {
					valueArray.put(convertToNumber(value));
				}
			}
			
			if(valueArray.length() == length) {
				result.setDisplayValue(valueArray);
			} else {
				try {
					result.setDisplayValue(new JSONArray(String.valueOf(result.getResponse())));
				} catch (JSONException je) {
					_logger.warn("cannot convert multi-choice response value to JSON Array: " + result.getResponse());
				}
			}
		
		} else {
			try {
				result.setDisplayValue(new JSONArray(String.valueOf(result.getResponse())));
			} catch (JSONException je) {
				_logger.warn("cannot convert multi-choice response value to JSON Array: " + result.getResponse());
			}
		}
	}
	
	private void setDisplayValueFromRemoteActivity(SurveyResponseReadResult result, Configuration config) {
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
	 */
	private Object convertToNumber(Object value) {
		try {
			
			return Integer.parseInt(String.valueOf(value));
			
		} catch (NumberFormatException a) { 
			
			try {
				
				return Double.parseDouble(String.valueOf(value));
			
			} catch (NumberFormatException b) {} // ignore because the value must be a string or some other number representation
			                                     // that can be treated as a JSON string
		}
		
		return value;
	}
}
