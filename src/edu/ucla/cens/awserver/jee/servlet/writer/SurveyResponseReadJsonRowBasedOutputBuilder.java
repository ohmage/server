package edu.ucla.cens.awserver.jee.servlet.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.PromptProperty;
import edu.ucla.cens.awserver.domain.SurveyResponseReadResult;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.util.DateUtils;
import edu.ucla.cens.awserver.util.JsonUtils;

/** 
 * For row-based output, the survey response results are treated like traditional records in a flat file where each result is
 * converted to a JSON object that represents a "record". 
 * 
 * @author joshua selsky
 */
public class SurveyResponseReadJsonRowBasedOutputBuilder {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadJsonRowBasedOutputBuilder.class);
	
	/**
	 * Converts the provided results to JSON using the provided rowItems List as a filter on what belongs in each
	 * row.
	 */
	public String buildOutput(SurveyResponseReadAwRequest req, List<SurveyResponseReadResult> results, List<String> rowItems) 
		throws JSONException {
		
		_logger.info("about to generate row-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		
		int numberOfSurveys = 0;
		String currentSurveyId = null;
		
		if(results.size() > 0) {
			
			JSONArray dataArray = new JSONArray();
			main.put("data", dataArray);
			
			for(SurveyResponseReadResult result : results) {
			
				if(! result.getSurveyId().equals(currentSurveyId)) {
					numberOfSurveys++;
					currentSurveyId = result.getSurveyId();
				}
				
				JSONObject record = new JSONObject();
				
				for(String rowItem : rowItems) {
				
					if("urn:ohmage:user:id".equals(rowItem)) {
						
						record.put("user", result.getLoginId());
						
					} else if("urn:ohmage:context:client".equals(rowItem)) {
						
						record.put("client", result.getClient());
					
					} else if("urn:ohmage:context:timestamp".equals(rowItem)){
						
						record.put("timestamp", result.getTimestamp());
					
					} else if("urn:ohmage:context:timezone".equals(rowItem)) {
						
						record.put("timezone", result.getTimezone());
						
					} else if("urn:ohmage:context:utc_timestamp".equals(rowItem)) {
					
						record.putOpt("utc_timestamp", generateUtcTimestamp(result));
					
				    } else if("urn:ohmage:context:launch_context_long".equals(rowItem)) {
				    	
				    	record.put("launch_context_long", result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
				    	
				    } else if("urn:ohmage:context:launch_context_short".equals(rowItem)) {
				    	
				    	record.put("launch_context_short", result.getLaunchContext() == null ? null : shortLaunchContext(result.getLaunchContext()));
				    	
				    } else if("urn:ohmage:context:location:status".equals(rowItem)) {
				    
				    	record.put("location_status", result.getLocationStatus());
				    
				    } else if("urn:ohmage:context:location:latitude".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		JSONObject location = new JSONObject(result.getLocation());
				    		
				    		if(! Double.isNaN(location.optDouble("latitude"))) {
				    			record.put("latitude", location.optDouble("latitude"));
							} else {
								record.put("latitude", JSONObject.NULL);
							}
				    		
				    	} else {
							record.put("latitude", JSONObject.NULL);
						}
				    
				    } else if("urn:ohmage:context:location:longitude".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		JSONObject location = new JSONObject(result.getLocation());
				    		
				    		if(! Double.isNaN(location.optDouble("longitude"))) {
				    			record.put("longitude", location.optDouble("longitude"));
							} else {
								record.put("longitude", JSONObject.NULL);
							}
				    	} else {
							record.put("longitude", JSONObject.NULL);
						}
				    	
				    } else if("urn:ohmage:context:location:timestamp".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) { 
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
					    	if(! "".equals(location.optString("timestamp")) ) { 
					    		record.put("location_timestamp", location.optString("timestamp"));
					    	} else {
					    		record.put("location_timestamp", JSONObject.NULL);
					    	}
				    	} else {
				    		record.put("location_timestamp", JSONObject.NULL);
				    	}
				    	
				    } else if("urn:ohmage:context:location:accuracy".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) {
				    		
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
				    		if(! Double.isNaN(location.optDouble("accuracy"))) {
				    			record.put("location_accuracy", location.optDouble("accuracy"));
							} else {
								record.put("location_accuracy", JSONObject.NULL);
							}
				    	} else {
							record.put("location_accuracy", JSONObject.NULL);
						}
				    	
				    } else if("urn:ohmage:context:location:provider".equals(rowItem)) {
				    	
				    	if(! "unavailable".equals(result.getLocationStatus())) { 
					    	JSONObject location = new JSONObject(result.getLocation());
					    	
					    	if(! "".equals(location.optString("provider")) ) { 
					    		record.put("location_provider", location.optString("provider"));
					    	} else {
					    		record.put("location_provider", JSONObject.NULL);
					    	}
				    	} else {
				    		record.put("location_provider", JSONObject.NULL);
				    	}
				    	
				    } else if ("urn:ohmage:survey:id".equals(rowItem)) {
				    	
				    	record.put("survey_id", result.getSurveyId());
				    	
				    } else if("urn:ohmage:survey:title".equals(rowItem)) {
				    	
				    	record.put("survey_title", result.getSurveyTitle());
				    
				    } else if("urn:ohmage:survey:description".equals(rowItem)) {
				    	
				    	record.put("survey_description", result.getSurveyDescription());
				    	
				    } else if("urn:ohmage:survey:privacy_state".equals(rowItem)) {
				    	
				    	record.put("privacy_state", result.getPrivacyState());
				    	
				    } else if("urn:ohmage:repeatable_set:id".equals(rowItem)) {
				    	
				    	record.put("repeatable_set_id", 
				    		result.getRepeatableSetId() == null ? JSONObject.NULL : result.getRepeatableSetId());
				    
				    } else if("urn:ohmage:repeatable_set:iteration".equals(rowItem)) {
				    	
				    	record.put("repeatable_set_iteration", 
				    		result.getRepeatableSetIteration() == null ? JSONObject.NULL : result.getRepeatableSetIteration());
				    
					} else if (rowItem.startsWith("urn:ohmage:prompt:id:")) {

						record.put("prompt_id", result.getPromptId());
						record.put("prompt_response", result.getDisplayValue());
						record.put("prompt_display_type", result.getDisplayType());
						record.put("prompt_unit", result.getUnit());
						record.put("prompt_type", result.getPromptType());
						record.put("prompt_text", result.getPromptText());
						if(null != result.getChoiceGlossary()) { // only output for single_choice and multi_choice prompt types
							record.put("prompt_choice_glossary", toJson(result.getChoiceGlossary()));
						}
					}
					
					if(req.performReturnId()) { // only allowed for json-rows output
						record.put("survey_key", result.getSurveyPrimaryKeyId());
					}
				}
				
				dataArray.put(record);
			}
		}
		
		// build the metadata section last after calculating the number of surveys above
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", results.size());
		metadata.put("number_of_surveys", numberOfSurveys);
		metadata.put("items", rowItems);
		main.put("metadata", metadata);
		
		return req.isPrettyPrint() ? main.toString(4) : main.toString();
	}
	
	// FIXME this method is copied directly from the SurveyResponseReadResponseWriter class
	private JSONObject shortLaunchContext(String launchContext) throws JSONException {
		JSONObject lc = new JSONObject(launchContext);
		JSONObject shortLc = new JSONObject();
		
		String launchTime = JsonUtils.getStringFromJsonObject(lc, "launch_time");
		if(null != launchTime) {
			shortLc.put("launch_time", launchTime);
		}
		
		JSONArray activeTriggers = JsonUtils.getJsonArrayFromJsonObject(lc, "active_triggers");
		if(null != activeTriggers) {
			JSONArray shortArray = new JSONArray();
			for(int i = 0; i < activeTriggers.length(); i++) {
				JSONObject shortArrayEntry = new JSONObject();
				JSONObject longArrayEntry = JsonUtils.getJsonObjectFromJsonArray(activeTriggers, i);
				if(null != longArrayEntry) {
					String triggerType = JsonUtils.getStringFromJsonObject(longArrayEntry, "trigger_type");
					if(null != triggerType) {
						shortArrayEntry.put("trigger_type", triggerType);
					}
					JSONObject runtimeDescription = JsonUtils.getJsonObjectFromJsonObject(longArrayEntry, "runtime_description");
					if(null != runtimeDescription) {
						String triggerTime = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timestamp");
						if(null != triggerTime) {
							shortArrayEntry.put("trigger_timestamp", triggerTime);
						}
						String triggerTimezone = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timezone");
						if(null != triggerTimezone) {
							shortArrayEntry.put("trigger_timezone", triggerTimezone);
						}
					}
				}
				shortArray.put(shortArrayEntry);
			}
		}
		return shortLc;
	}
	
	// FIXME this method is copied directly from the SurveyResponseReadResponseWriter class
	private String generateUtcTimestamp(SurveyResponseReadResult result) {
		return DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone());
	}	

	// FIXME this method is copied directly from the SurveyResponseReadCsvColumnOutputBuilder class
	private Object toJson(Map<String, PromptProperty> ppMap) throws JSONException {
		JSONObject main = new JSONObject();
		Iterator<String> it = ppMap.keySet().iterator();
		while(it.hasNext()) {
			PromptProperty pp = ppMap.get(it.next());
			JSONObject item = new JSONObject();
			item.put("value", pp.getValue());
			item.put("label", pp.getLabel());
			main.put(pp.getKey(), item);
		}
		return main;
	}
}
