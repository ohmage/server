package edu.ucla.cens.awserver.domain;

import org.apache.log4j.Logger;
import org.json.JSONArray;

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
	
	public void merge(NewDataPointQueryResult result, Configuration configuration) {
		
		if(result.isRepeatableSetResult()) {
			
			result.setUnit(configuration.getUnitFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
			result.setDisplayLabel(
				configuration.getDisplayLabelFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
			);
			result.setDisplayType(configuration.getDisplayTypeFor(
				result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
			);
			
			if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
				
				setDisplayValueFromSingleChoice(result, configuration, true);	
				
			} else if(PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				setDisplayValueFromMultiChoice(result, configuration, true);
				
			} else { 
					
				result.setDisplayValue(result.getResponse());
			}
			
		} else {
			
			result.setUnit(configuration.getUnitFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayLabel(configuration.getDisplayLabelFor(result.getSurveyId(), result.getPromptId()));
			result.setDisplayType(configuration.getDisplayTypeFor(result.getSurveyId(), result.getPromptId()));
			
			if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
			
				setDisplayValueFromSingleChoice(result, configuration, false);
			
			} else if (PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
				
				setDisplayValueFromMultiChoice(result, configuration, false);
									
			} else {
				
				result.setDisplayValue(result.getResponse());
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(result);
		}
		
	}
	
	private void setDisplayValueFromSingleChoice(NewDataPointQueryResult result, Configuration config, boolean isRepeatableSetItem) {
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
			result.setDisplayValue(value);
		} else {
			result.setDisplayValue(result.getResponse());
		}
	}
	
	private void setDisplayValueFromMultiChoice(NewDataPointQueryResult result, Configuration config, boolean isRepeatableSetItem) {
	
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
					valueArray.put(value);
				}
			}
			
			if(valueArray.length() == length) {
				result.setDisplayValue(valueArray);
			} else {				
				result.setDisplayValue(result.getResponse());
			}
		
		} else {
			result.setDisplayValue(result.getResponse());
		}
	}
}
