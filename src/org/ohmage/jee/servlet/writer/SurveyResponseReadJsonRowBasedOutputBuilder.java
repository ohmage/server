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
package org.ohmage.jee.servlet.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.PromptProperty;
import org.ohmage.domain.PromptResponseMetadata;
import org.ohmage.domain.SurveyResponseReadIndexedResult;
import org.ohmage.request.SurveyResponseReadAwRequest;


/** 
 * For row-based output, the survey response results are treated like traditional records in a flat file where each result is
 * converted to a JSON object that represents a record in the file. 
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadJsonRowBasedOutputBuilder {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadJsonRowBasedOutputBuilder.class);
	
	/**
	 * Converts the provided results to JSON using the provided rowItems List as a filter on what belongs in each
	 * row.
	 */
	public String buildOutput(int numberOfSurveys, int numberOfPrompts, SurveyResponseReadAwRequest req, List<SurveyResponseReadIndexedResult> results, List<String> rowItems) 
		throws JSONException {
		
		_logger.info("Generating row-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		
		if(results.size() > 0) {
			
			JSONArray dataArray = new JSONArray();
			main.put("data", dataArray);
			
			for(SurveyResponseReadIndexedResult result : results) {
				
				JSONObject record = new JSONObject();
				
				for(String rowItem : rowItems) {
				
					if("urn:ohmage:user:id".equals(rowItem)) {
						
						record.put("user", result.getUsername());
						
					} else if("urn:ohmage:context:client".equals(rowItem)) {
						
						record.put("client", result.getClient());
					
					} else if("urn:ohmage:context:timestamp".equals(rowItem)){
						
						record.put("timestamp", result.getTimestamp());
					
					} else if("urn:ohmage:context:timezone".equals(rowItem)) {
						
						record.put("timezone", result.getTimezone());
						
					} else if("urn:ohmage:context:utc_timestamp".equals(rowItem)) {
					
						record.putOpt("utc_timestamp", SurveyResponseReadWriterUtils.generateUtcTimestamp(result));
					
				    } else if("urn:ohmage:context:launch_context_long".equals(rowItem)) {
				    	
				    	record.put("launch_context_long", result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
				    	
				    } else if("urn:ohmage:context:launch_context_short".equals(rowItem)) {
				    	
				    	record.put("launch_context_short", result.getLaunchContext() == null ? null : SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext()));
				    	
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
						
						JSONObject promptObject = new JSONObject();
						
						Map<String, Map<String, PromptProperty>> choiceGlossaryMap = result.getChoiceGlossaryMap();
						Map<String, PromptResponseMetadata> promptResponseMetadataMap = result.getPromptResponseMetadataMap();
						Map<String, Object> promptResponseMap = result.getPromptResponseMap();
						Iterator<String> responseIterator = promptResponseMap.keySet().iterator();
						
						while(responseIterator.hasNext()) {
							String key = responseIterator.next();
							JSONObject response = new JSONObject();
							Map<String, PromptProperty> ppMap = choiceGlossaryMap.isEmpty() ? null : choiceGlossaryMap.get(key); 
							if(null != ppMap) {
								response.put("prompt_choice_glossary", SurveyResponseReadWriterUtils.choiceGlossaryToJson(ppMap));
							}
							response.put("prompt_response", promptResponseMap.get(key));
							response.put("prompt_display_type", promptResponseMetadataMap.get(key).getDisplayType());
							response.put("prompt_unit", promptResponseMetadataMap.get(key).getUnit());
							response.put("prompt_type", promptResponseMetadataMap.get(key).getPromptType());
							response.put("prompt_text", promptResponseMetadataMap.get(key).getPromptText());
							response.put("prompt_index", req.getConfiguration().getIndexForPrompt(result.getSurveyId(), key));
							promptObject.put(key, response); // the key here is the prompt_id from the XML config
						}

						record.put("responses", promptObject);
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
		metadata.put("number_of_prompts", numberOfPrompts);
		metadata.put("number_of_surveys", numberOfSurveys);
		metadata.put("items", rowItems);
		main.put("metadata", metadata);
		
		return req.isPrettyPrint() ? main.toString(4) : main.toString();
	}
}
