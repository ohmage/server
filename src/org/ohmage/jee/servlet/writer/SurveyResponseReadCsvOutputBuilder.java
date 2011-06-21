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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.Configuration;
import org.ohmage.domain.PromptProperty;
import org.ohmage.domain.PromptResponseMetadata;
import org.ohmage.domain.SurveyResponseReadIndexedResult;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * Converts survey response read results into CSV format using a canonicalized column order: 
 * [user, timestamp, survey id, repeatable set id, repeatable set iteration, promptId[1] ... promptId[n], metadata columns].
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadCsvOutputBuilder  {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadCsvOutputBuilder.class);
	
	private static String newLine = System.getProperty("line.separator");
	
	public String createMultiResultOutput(int numberOfSurveys, 
										  int numberOfPrompts,
			                              SurveyResponseReadAwRequest req,
			                              List<SurveyResponseReadIndexedResult> indexedResultList,
			                              List<String> outputColumns) throws JSONException {
		
		_logger.info("Generating multi-result CSV output");
		
		Map<String, Map<String, PromptProperty>> choiceGlossaryMap = new HashMap<String, Map<String, PromptProperty>>();
		Map<String, PromptResponseMetadata> promptResponseMetadataMap = new HashMap<String, PromptResponseMetadata>();
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
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
		
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("## begin metadata").append(newLine);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			jsonObject.put("number_of_prompts", numberOfPrompts);
			jsonObject.put("number_of_surveys", numberOfSurveys);
			
			builder.append("#" + jsonObject.toString().replace(",", ";")).append(newLine)	
			       .append("## end metadata").append(newLine).append("# begin prompt contexts").append(newLine);
			
			Iterator<String> promptResponseMetadataIterator = promptResponseMetadataMap.keySet().iterator();
			
			while(promptResponseMetadataIterator.hasNext()) {
				String key = promptResponseMetadataIterator.next();
				
				JSONObject prompt = new JSONObject();
				JSONObject context = new JSONObject();
				
				context.put("unit", promptResponseMetadataMap.get(key).getUnit() == null ?  JSONObject.NULL : promptResponseMetadataMap.get(key).getUnit());
				context.put("prompt_type", promptResponseMetadataMap.get(key).getPromptType());
				context.put("display_type", promptResponseMetadataMap.get(key).getDisplayType());
				context.put("display_label", promptResponseMetadataMap.get(key).getDisplayLabel());
				context.put("text", promptResponseMetadataMap.get(key).getPromptText());
				if(null != choiceGlossaryMap.get(key)) {	
					context.put("choice_glossary", SurveyResponseReadWriterUtils.choiceGlossaryToJson(choiceGlossaryMap.get(key)));
				} else {
					context.put("choice_glossary", JSONObject.NULL);
				}
				prompt.put(key, context); 
				builder.append("#" + prompt.toString().replace(",", ";")).append(newLine);
			}
			builder.append("## end prompt contexts").append(newLine).append("## begin data").append(newLine);
		}
		
		
		// Sort the column map key set for predictable columnar output.
		// In the future, there can be a column_order parameter to the API 
		// For now, canonicalize into 'ohmage order': user, datetime, survey id, repeatable set id, repeatable set iteration,
		// prompt id, and then 'context' columns and survey title and description
		
		// Note that not all columns are necessarily present. This is based on the column_list parameter passed to the API.
	
		TreeSet<String> sortedColumnMapKeySet = new TreeSet<String>(outputColumns);
		ArrayList<String> canonicalColumnList = new ArrayList<String>();
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:user:id")) {
			canonicalColumnList.add("urn:ohmage:user:id");
			sortedColumnMapKeySet.remove("urn:ohmage:user:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:context:timestamp")) {
			canonicalColumnList.add("urn:ohmage:context:timestamp");
			sortedColumnMapKeySet.remove("urn:ohmage:context:timestamp");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:context:utc_timestamp")) {
			canonicalColumnList.add("urn:ohmage:context:utc_timestamp");
			sortedColumnMapKeySet.remove("urn:ohmage:context:utc_timestamp");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:survey:id")) {
			canonicalColumnList.add("urn:ohmage:survey:id");
			sortedColumnMapKeySet.remove("urn:ohmage:survey:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:repeatable_set:id")) {
			canonicalColumnList.add("urn:ohmage:repeatable_set:id");
			sortedColumnMapKeySet.remove("urn:ohmage:repeatable_set:id");
		}
		
		if(sortedColumnMapKeySet.contains("urn:ohmage:repeatable_set:iteration")) {
			canonicalColumnList.add("urn:ohmage:repeatable_set:iteration");
			sortedColumnMapKeySet.remove("urn:ohmage:repeatable_set:iteration");
		}
		
		// Need to group all of the prompts by survey
		Configuration configuration = req.getConfiguration();
		Map<String, List<Object>> surveyIdToPromptIdListMap = new HashMap<String, List<Object>>();
		
		Iterator<String> sortedSetIterator = sortedColumnMapKeySet.iterator();
		while(sortedSetIterator.hasNext()) {
			String columnName = sortedSetIterator.next();
			if(columnName.contains("prompt:id")) {
				String promptId = columnName.substring(columnName.lastIndexOf(":") + 1);
				String surveyId = configuration.getSurveyIdForPromptId(promptId);
				
				if(! surveyIdToPromptIdListMap.containsKey(surveyId)) {
					
					List<Object> promptIdList = new ArrayList<Object>(); // _logger.info(configuration.getNumberOfPromptsInSurvey(surveyId));
					promptIdList.addAll(Collections.nCopies(configuration.getNumberOfPromptsInSurvey(surveyId), null));
					promptIdList.set(configuration.getIndexForPrompt(surveyId, promptId), columnName);
					surveyIdToPromptIdListMap.put(surveyId, promptIdList);
					
				} else {
					
					surveyIdToPromptIdListMap.get(surveyId).set(configuration.getIndexForPrompt(surveyId, promptId), columnName);
				}
				
				sortedSetIterator.remove();
			}
		}
		
		_logger.info(surveyIdToPromptIdListMap);
		
		Iterator<String> surveyIdToPromptIdListMapIterator = surveyIdToPromptIdListMap.keySet().iterator();
		while(surveyIdToPromptIdListMapIterator.hasNext()) {
			List<Object> promptIdList = surveyIdToPromptIdListMap.get(surveyIdToPromptIdListMapIterator.next());
			for(Object promptId : promptIdList) {
				canonicalColumnList.add((String) promptId);
			}
		}
		
		canonicalColumnList.addAll(sortedColumnMapKeySet);
		sortedColumnMapKeySet.clear();
		
		_logger.info(canonicalColumnList);
		
		// Build the column headers
		// For the CSV output, user advocates have requested that the column names be made shorter
		List<String> copyOfCanonicalColumnList = new ArrayList<String> (canonicalColumnList); 
		
		int s = copyOfCanonicalColumnList.size();
		int i = 0;
		for(String key : copyOfCanonicalColumnList) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				shortHeader = key.replace("urn:ohmage:prompt:id:", "");
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		for(SurveyResponseReadIndexedResult result : indexedResultList) {
			
			for(String canonicalColumnId : canonicalColumnList) {
				
				if("urn:ohmage:user:id".equals(canonicalColumnId)) {
					builder.append(result.getUsername()).append(",");
				}
				else if("urn:ohmage:context:client".equals(canonicalColumnId)) {
					builder.append(result.getClient()).append(",");
				}
				else if("urn:ohmage:context:timestamp".equals(canonicalColumnId)) {
					builder.append(result.getTimestamp()).append(",");
				}
				else if("urn:ohmage:context:timezone".equals(canonicalColumnId)) {
					builder.append(result.getTimezone()).append(",");
				}
				else if("urn:ohmage:context:utc_timestamp".equals(canonicalColumnId)) {
					builder.append(result.getUtcTimestamp()).append(",");
				}
				else if("urn:ohmage:context:launch_context_short".equals(canonicalColumnId)) {
					if(null != result.getLaunchContext()) {
						builder.append(SurveyResponseReadWriterUtils.shortLaunchContext(result.getLaunchContext().replace(",", ";"))).append(",");
					} else {
						builder.append("null,");
					}
				}
				else if("urn:ohmage:context:launch_context_long".equals(canonicalColumnId)) {
					if(null != result.getLaunchContext()) {
						builder.append(result.getLaunchContext().replace(",", ";")).append(",");
					} else {
						builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:status".equals(canonicalColumnId)) {
					builder.append(result.getLocationStatus()).append(",");
				}
				else if("urn:ohmage:context:location:latitude".equals(canonicalColumnId)) {
					
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("latitude"))) {
			    			builder.append(location.optDouble("latitude")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:longitude".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("longitude"))) {
			    			builder.append(location.optDouble("longitude")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:timestamp".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("timestamp"))) {
			    			builder.append(location.optDouble("timestamp")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:accuracy".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("accuracy"))) {
			    			builder.append(location.optDouble("accuracy")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:context:location:provider".equals(canonicalColumnId)) {
					if(! "unavailable".equals(result.getLocationStatus())) {
			    		JSONObject location = new JSONObject(result.getLocation());
			    		
			    		if(! Double.isNaN(location.optDouble("provider"))) {
			    			builder.append(location.optDouble("provider")).append(",");
						} else {
							builder.append("null,");
						}
			    	} else {
			    		builder.append("null,");
					}
				}
				else if("urn:ohmage:repeatable_set:id".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetId()).append(",");
				}
				else if("urn:ohmage:repeatable_set:iteration".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetIteration()).append(",");
				}
				else if("urn:ohmage:repeatable_set:iteration".equals(canonicalColumnId)) {
					builder.append(result.getRepeatableSetIteration()).append(",");
				}
				else if("urn:ohmage:survey:id".equals(canonicalColumnId)) {
					builder.append(result.getSurveyId()).append(",");
				}
				else if("urn:ohmage:survey:title".equals(canonicalColumnId)) {
					builder.append(cleanAndQuoteString(result.getSurveyTitle())).append(",");
				}
				else if("urn:ohmage:survey:description".equals(canonicalColumnId)) {
					builder.append(cleanAndQuoteString(result.getSurveyDescription())).append(",");
				}
				else if("urn:ohmage:survey:privacy_state".equals(canonicalColumnId)) {
					builder.append(result.getPrivacyState()).append(",");
				}
				else if(canonicalColumnId.contains("prompt:id")) {

					Object value = result.getPromptResponseMap().get(canonicalColumnId.substring("urn:ohmage:prompt:id:".length()));
					
					if(value instanceof JSONObject) { // single_choice_custom, multi_choice_custom
						builder.append(((JSONObject) value).toString().replace(",", ";"));
					} 
					
					else if(value instanceof JSONArray) { // multi_choice
						builder.append(((JSONArray) value).toString().replace(",", ";"));
					}
					
					else if (value instanceof String) { // clean up text for easier input into spreadsheets 
						String string = (String) value;
						
						if("text".equals(promptResponseMetadataMap.get(canonicalColumnId.substring("urn:ohmage:prompt:id:".length())).getPromptType())) {
							builder.append(cleanAndQuoteString(string));
						} else {
							builder.append(string);
						}
					}					
					else {
						builder.append(value);
					}
					
					builder.append(",");
				}
				
			}
			int lastCommaIndex = builder.lastIndexOf(",");
			builder.replace(lastCommaIndex, lastCommaIndex + 1, "");
			builder.append(newLine);
		}
		
		if(! req.isSuppressMetadata()) {
			builder.append("## end data").append(newLine);
		}
		
		return builder.toString();
	}
	
	public String createZeroResultOutput(SurveyResponseReadAwRequest req, List<String> outputColumns) 
		throws JSONException  {
		
		_logger.info("Generating zero-result CSV output");
		
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("## begin metadata").append(newLine);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("number_of_prompts", 0);
			jsonObject.put("number_of_surveys", 0);
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			builder.append(jsonObject.toString().replace(",", ";")).append(newLine)
			       .append("## end metadata").append(newLine)
			       .append("## begin prompt contexts").append(newLine).append("# end prompt contexts").append(newLine)
			       .append("## begin data").append(newLine);
		}
		
		int s = outputColumns.size();
		
		// Logic that is completely redundant with the method above
		int i = 0;
		for(String key : outputColumns) {
			String shortHeader = null;
			if(key.startsWith("urn:ohmage:context")) {
				shortHeader = key.replace("urn:ohmage:context", "sys");				
			} else if(key.startsWith("urn:ohmage:prompt:id")) {
				shortHeader = key.replace("urn:ohmage:prompt:id:", "");
			} else if(key.startsWith("urn:ohmage")) {
				shortHeader = key.replace("urn:ohmage:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		if(! req.isSuppressMetadata()) {
			builder.append("## end data").append(newLine);
		}
		
		return builder.toString();
	}
		
	private String cleanAndQuoteString(String string) {
		return "\"" + string.replaceAll("\\s", " ").replaceAll("\"", "'") + "\"";
	}
}
