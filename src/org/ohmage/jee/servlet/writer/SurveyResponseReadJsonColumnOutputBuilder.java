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

import java.util.ArrayList;
import java.util.HashMap;
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
 * Strategy for outputting column-based JSON output for /app/survey_response/read.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadJsonColumnOutputBuilder  {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadJsonColumnOutputBuilder.class);

	public String createMultiResultOutput(int numberOfSurveys, 
			                              int numberOfPrompts,
			                              SurveyResponseReadAwRequest req,
			                              List<SurveyResponseReadIndexedResult> results,
			                              List<String> outputColumns) throws JSONException {
		
		_logger.info("Generating multi-result column-based JSON output");
	
		// Each column id in the output gets mapped to its associated list of values 
		Map<String, List<Object>> columnMap = new HashMap<String, List<Object>> ();
		
		// For prompt responses, the choice glossary and prompt metadata only needs to be set in the "column" once.
		// Choice glossaries will only be present for single_choice and multi_choice prompt types.
		
		Map<String, Map<String, PromptProperty>> choiceGlossaryMap = new HashMap<String, Map<String, PromptProperty>>();
		Map<String, PromptResponseMetadata> promptResponseMetadataMap = new HashMap<String, PromptResponseMetadata>();
		
		for(SurveyResponseReadIndexedResult result : results) {
			if(result.getChoiceGlossaryMap() != null) {
				Iterator<String> iterator = result.getChoiceGlossaryMap().keySet().iterator();
				while(iterator.hasNext()) {
					String key = iterator.next();
					if(! choiceGlossaryMap.containsKey(key)) {
						choiceGlossaryMap.put(key, result.getChoiceGlossaryMap().get(key));
					}
				}
			}
			Iterator<String> iterator = result.getPromptResponseMetadataMap().keySet().iterator();
			while(iterator.hasNext()) {
				String key = iterator.next();
				if(! promptResponseMetadataMap.containsKey(key)) {
					promptResponseMetadataMap.put(key, result.getPromptResponseMetadataMap().get(key));
				}
			}
		}
		
		for(SurveyResponseReadIndexedResult result : results) {
		
			for(String outputColumnKey : outputColumns) {
			
				if(! columnMap.containsKey(outputColumnKey)) {
					List<Object> columnList = new ArrayList<Object>();
					columnMap.put(outputColumnKey, columnList);
				}	
				
				if("urn:ohmage:user:id".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getUsername());
					
				} else if("urn:ohmage:context:client".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getClient());
				
				} else if("urn:ohmage:context:timestamp".equals(outputColumnKey)){
					
					columnMap.get(outputColumnKey).add(result.getTimestamp());
				
				} else if("urn:ohmage:context:timezone".equals(outputColumnKey)) {
					
					columnMap.get(outputColumnKey).add(result.getTimezone());
					
				} else if("urn:ohmage:context:utc_timestamp".equals(outputColumnKey)) {
				
					columnMap.get(outputColumnKey).add(SurveyResponseReadWriterUtils.generateUtcTimestamp(result));
				
			    } else if("urn:ohmage:context:launch_context_long".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
			    	
			    } else if("urn:ohmage:context:launch_context_short".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getLaunchContext() == null ? null : SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext()));
			    	
			    } else if("urn:ohmage:context:location:status".equals(outputColumnKey)) {
			    
			    	columnMap.get(outputColumnKey).add(result.getLocationStatus());
			    
			    } else if("urn:ohmage:context:location:latitude".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("latitude"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("latitude"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    		
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    
			    } else if("urn:ohmage:context:location:longitude".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("longitude"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("longitude"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    	
			    } else if("urn:ohmage:context:location:timestamp".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) { 
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
				    	if(! "".equals(location.optString("timestamp")) ) { 
				    		columnMap.get(outputColumnKey).add(location.optString("timestamp"));
				    	} else {
				    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
				    	}
			    	} else {
			    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
			    	}
			    	
			    } else if("urn:ohmage:context:location:accuracy".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) {
			    		
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
			    		if(! Double.isNaN(location.optDouble("accuracy"))) {
			    			columnMap.get(outputColumnKey).add(location.optDouble("accuracy"));
						} else {
							columnMap.get(outputColumnKey).add(JSONObject.NULL);
						}
			    	} else {
						columnMap.get(outputColumnKey).add(JSONObject.NULL);
					}
			    	
			    } else if("urn:ohmage:context:location:provider".equals(outputColumnKey)) {
			    	
			    	if(! "unavailable".equals(result.getLocationStatus())) { 
				    	JSONObject location = new JSONObject(result.getLocation());
				    	
				    	if(! "".equals(location.optString("provider")) ) { 
				    		columnMap.get(outputColumnKey).add(location.optString("provider"));
				    	} else {
				    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
				    	}
			    	} else {
			    		columnMap.get(outputColumnKey).add(JSONObject.NULL);
			    	}
			    	
			    } else if ("urn:ohmage:survey:id".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyId());
			    	
			    } else if("urn:ohmage:survey:title".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyTitle());
			    
			    } else if("urn:ohmage:survey:description".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getSurveyDescription());
			    	
			    } else if("urn:ohmage:survey:privacy_state".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getPrivacyState());
			    	
			    } else if("urn:ohmage:repeatable_set:id".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getRepeatableSetId() == null ? JSONObject.NULL : result.getRepeatableSetId());
			    
			    } else if("urn:ohmage:repeatable_set:iteration".equals(outputColumnKey)) {
			    	
			    	columnMap.get(outputColumnKey).add(result.getRepeatableSetIteration() == null ? JSONObject.NULL : result.getRepeatableSetIteration());
			    
				} else if (outputColumnKey.startsWith("urn:ohmage:prompt:id:")) {
					
					columnMap.get(outputColumnKey).add(result.getPromptResponseMap().get(outputColumnKey.substring("urn:ohmage:prompt:id:".length())));
				}
			}
			
		}
				
		// Build the output
		
		// Build metadata section
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("campaign_urn", req.getCampaignUrn());
		metadata.put("number_of_prompts", numberOfPrompts);
		metadata.put("number_of_surveys", numberOfSurveys);
		JSONArray items = new JSONArray();
		for(String key : outputColumns) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		
		JSONArray data = new JSONArray();
		main.put("data", data);

		
		Iterator<String> columnMapIterator = columnMap.keySet().iterator();
		while(columnMapIterator.hasNext()) {
			String key = columnMapIterator.next();
			
			if(key.contains("prompt:id")) {
				
				Map<String, PromptProperty> choiceGlossary = choiceGlossaryMap.get(key.substring("urn:ohmage:prompt:id:".length()));
				PromptResponseMetadata promptResponseMetadata = promptResponseMetadataMap.get(key.substring("urn:ohmage:prompt:id:".length()));
				
				JSONObject column = new JSONObject();
				JSONObject context = new JSONObject();
				
				context.put("unit", null == promptResponseMetadata.getUnit() ? JSONObject.NULL : promptResponseMetadata.getUnit());
				context.put("prompt_type", promptResponseMetadata.getPromptType());
				context.put("display_type", promptResponseMetadata.getDisplayType());
				context.put("display_label", promptResponseMetadata.getDisplayLabel());
				context.put("text", promptResponseMetadata.getPromptText());
				
				if(null != choiceGlossary) {
					JSONObject choiceGlossaryObject = new JSONObject();
					Iterator<String> choiceGlossaryIterator = choiceGlossary.keySet().iterator();
					while(choiceGlossaryIterator.hasNext()) {
						PromptProperty pp = choiceGlossary.get(choiceGlossaryIterator.next());
						JSONObject choice = new JSONObject();
						choice.put("value", pp.getValue());
						choice.put("label", pp.getLabel());
						choiceGlossaryObject.put(pp.getKey(), choice);
					}
					context.put("choice_glossary", choiceGlossaryObject);
				}
				else {
					context.put("choice_glossary", JSONObject.NULL);
				}
				
				column.put("context", context);
				column.put("values", columnMap.get(key));
				JSONObject labelledColumn = new JSONObject();
				labelledColumn.put(key, column);
				data.put(labelledColumn);
				
			} else {
				
				JSONObject column = new JSONObject();
				column.put("values", columnMap.get(key));
				JSONObject labelledColumn = new JSONObject();
				labelledColumn.put(key, column);
				data.put(labelledColumn);
			}
		}
				
		return req.isPrettyPrint() ? main.toString(4) : main.toString();
	}
	
	public String createZeroResultOutput(SurveyResponseReadAwRequest req,
                                         List<String> outputColumns) throws JSONException {
		
		_logger.info("Generating zero-result column-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", 0);
		metadata.put("number_of_surveys", 0);
		metadata.put("campaign_urn", req.getCampaignUrn());
		JSONArray items = new JSONArray();
		for(String key : outputColumns) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		JSONArray data = new JSONArray();
		main.put("data", data);
		
		return req.isPrettyPrint() ? main.toString(4) : main.toString();
	}
}
