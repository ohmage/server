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
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.PromptContext;
import org.ohmage.domain.PromptProperty;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * Strategy for outputting the column-based JSON output for the new data point API.
 * 
 * @author selsky
 */
public class SurveyResponseReadJsonColumnOutputBuilder implements SurveyResponseReadColumnOutputBuilder {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadJsonColumnOutputBuilder.class);

	public String createMultiResultOutput(int totalNumberOfResults,
			                              SurveyResponseReadAwRequest req,
			                              Map<String, PromptContext> promptContextMap,
			                              Map<String, List<Object>> columnMap) throws JSONException {
		
		_logger.info("Generating multi-result column-based JSON output");
		
		Set<String> columnMapKeySet = columnMap.keySet();
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("campaign_urn", req.getCampaignUrn());
		metadata.put("number_of_prompts", totalNumberOfResults);
		// hacky way to do this, but any list will do because they are all the same size
		metadata.put("number_of_surveys", columnMap.get(columnMapKeySet.toArray()[0]).size());
		JSONArray items = new JSONArray();
		for(String key : columnMapKeySet) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		
		JSONArray data = new JSONArray();
		main.put("data", data);
		
		for(String key : columnMapKeySet) {
			
			if(key.startsWith("urn:ohmage:prompt:id:")) {
			
				String promptId = key.substring("urn:ohmage:prompt:id:".length());
				JSONObject column = new JSONObject();
				JSONObject context = new JSONObject();
				context.put("unit", 
						    promptContextMap.get(promptId).getUnit() == null ? JSONObject.NULL : promptContextMap.get(promptId).getUnit());
				context.put("prompt_type", promptContextMap.get(promptId).getType());
				context.put("display_type", promptContextMap.get(promptId).getDisplayType());
				context.put("display_label", promptContextMap.get(promptId).getDisplayLabel());
				context.put("text", promptContextMap.get(promptId).getText());
				if(null != promptContextMap.get(promptId).getChoiceGlossary()) {
					JSONObject choiceGlossaryObject = new JSONObject();
					Map<String, PromptProperty> choiceGlossary = promptContextMap.get(promptId).getChoiceGlossary();
					Iterator<String> iterator = choiceGlossary.keySet().iterator();
					while(iterator.hasNext()) {
						PromptProperty pp = choiceGlossary.get(iterator.next());
						JSONObject choice = new JSONObject();
						choice.put("value", pp.getValue());
						choice.put("label", pp.getLabel());
						choiceGlossaryObject.put(pp.getKey(), choice);
					}
					context.put("choice_glossary", choiceGlossaryObject);
				} else {
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
                                         Map<String, List<Object>> columnMap) throws JSONException {
		
		_logger.info("Generating zero-result column-based JSON output");
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", 0);
		metadata.put("number_of_surveys", 0);
		metadata.put("campaign_urn", req.getCampaignUrn());
		JSONArray items = new JSONArray();
		for(String key : columnMap.keySet()) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		JSONArray data = new JSONArray();
		main.put("data", data);
		
		return req.isPrettyPrint() ? main.toString(4) : main.toString();
	}
}
