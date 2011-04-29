package edu.ucla.cens.awserver.domain;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.service.DataPointQueryService;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * For the "new" data point API, merges values from a conifguration into DB result object.
 * 
 * TODO convert to interface?
 * 
 * @author selsky
 * @see DataPointQueryService
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
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(result);
		}
		
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
			JSONArray valueArray = new JSONArray();
			
			for(int i = 0; i < valueArray.length(); i++) {
				JSONObject currRun;
				try {
					currRun = valueArray.getJSONObject(i);
				}
				catch(JSONException e) {
					_logger.warn("Could not convert an individual run of a RemoteActivity response into JSONObjects.", e);
					continue;
				}
				
				double runResult;
				try {
					runResult = (Double) currRun.get("score");
				}
				catch(JSONException e) {
					_logger.warn("Missing necessary key in RemoteActivity run.", e);
					continue;
				}
				catch(NullPointerException e) {
					_logger.warn("Missing necessary key in RemoteActivity run.", e);
					continue;
				}
				
				try {
					valueArray.put(runResult);
				}
				catch(JSONException e) {
					_logger.warn("Error constructing CSV response in RemoteActivity response merge.");
					continue;
				}
			}
			
			result.setDisplayValue(valueArray);
		}
		else {
			try {
				result.setDisplayValue(new JSONArray(String.valueOf(result.getResponse())));
			}
			catch(JSONException e) {
				_logger.warn("cannot convert RemoteActivity response value to JSONArray: " + result.getResponse(), e);
			}
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
