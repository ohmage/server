package edu.ucla.cens.awserver.jee.servlet.writer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.PromptContext;
import edu.ucla.cens.awserver.domain.PromptProperty;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * Strategy for outputting CSV output for the new data point API.
 * 
 * @author selsky
 */
public class NewDataPointQueryCsvColumnOutputBuilder implements NewDataPointQueryOutputBuilder {
	private static String newLine = System.getProperty("line.separator");
	
	public String createMultiResultOutput(int totalNumberOfResults,
			                              NewDataPointQueryAwRequest req,
			                              Map<String, PromptContext> promptContextMap,
			                              Map<String, List<Object>> columnMap) throws JSONException {
		
		Set<String> columnMapKeySet = columnMap.keySet();
		Set<String> promptContextKeySet = promptContextMap.keySet();
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("# begin metadata").append(newLine);
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			jsonObject.put("number_of_prompts", totalNumberOfResults);
			jsonObject.put("number_of_surveys", columnMap.get(columnMapKeySet.toArray()[0]).size());
			
			builder.append(jsonObject.toString().replace(",", ";")).append(newLine)	
			       .append("# end metadata").append(newLine).append("# begin prompt contexts").append(newLine);
			
			for(String key : promptContextKeySet) {
				JSONObject prompt = new JSONObject();
				JSONObject context = new JSONObject();
				context.put("unit", promptContextMap.get(key).getUnit() == null ? "NA" : promptContextMap.get(key).getUnit());
				context.put("prompt_type", promptContextMap.get(key).getType());
				context.put("display_type", promptContextMap.get(key).getDisplayType());
				context.put("display_label", promptContextMap.get(key).getDisplayLabel());
				if(null != promptContextMap.get(key).getChoiceGlossary()) {	
					context.put("choice_glossary", toJson(promptContextMap.get(key).getChoiceGlossary()));
				} else {
					context.put("choice_glossary", "NA");
				}
				prompt.put(key, context); 
				builder.append(prompt.toString().replace(",", ";")).append(newLine);
			}
			builder.append("# end prompt contexts").append(newLine).append("# begin data").append(newLine);
		}
		
		// Build the column headers
		// For the CSV output, user advocates have requested that the column names be made shorter 
		int s = columnMapKeySet.size();
		int i = 0;
		for(String key : columnMapKeySet) {
			String shortHeader = null;
			if(key.startsWith("urn:awm:context")) {
				shortHeader = key.replace("urn:awm:context", "sys");				
			} else if(key.startsWith("urn:awm:prompt:id")) {
				shortHeader = key.replace("urn:awm:prompt:id:", "");
			} else if(key.startsWith("urn:awm")) {
				shortHeader = key.replace("urn:awm:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		// Build data output row by row
		int listSize = columnMap.get(columnMapKeySet.toArray()[0]).size();
		for(i = 0; i < listSize; i++) {
			int j = 0;
			for(String key : columnMapKeySet) {
				Object value = columnMap.get(key).get(i);
				if(null == value) {
					builder.append("NA");
				} else {
					if(value instanceof JSONObject) { //single_choice_custom, multi_choice_custom, launch_context 
						builder.append(((JSONObject) value).toString().replace(",", ";"));
					} else if(value instanceof JSONArray) { // multi_choice
						builder.append(((JSONArray) value).toString().replace(",", ";"));
					} else {
						builder.append(value);
					}
				}
				if(j < columnMapKeySet.size() - 1) {
					builder.append(",");
				}
				j++;
			}
			builder.append(newLine);
		}
		
		if(! req.isSuppressMetadata()) {
			builder.append("# end data").append(newLine);
		}
		
		return builder.toString();
	}
	
	public String createZeroResultOutput(NewDataPointQueryAwRequest req, Map<String, List<Object>> columnMap) 
		throws JSONException  {
		
		StringBuilder builder = new StringBuilder();
		
		if(! req.isSuppressMetadata()) {
			builder.append("# begin metadata").append(newLine);
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("result", "success");
			jsonObject.put("number_of_prompts", 0);
			jsonObject.put("number_of_surveys", 0);
			jsonObject.put("campaign_urn", req.getCampaignUrn());
			builder.append(jsonObject.toString().replace(",", ";")).append(newLine)
			       .append("# end metadata").append(newLine)
			       .append("# begin prompt contexts").append(newLine).append("# end prompt contexts").append(newLine)
			       .append("# begin data").append(newLine);
		}
		
		int s = columnMap.keySet().size();
		
		// Logic that is completely redundant with the method above
		int i = 0;
		for(String key : columnMap.keySet()) {
			String shortHeader = null;
			if(key.startsWith("urn:awm:context")) {
				shortHeader = key.replace("urn:awm:context", "sys");				
			} else if(key.startsWith("urn:awm:prompt:id")) {
				shortHeader = key.replace("urn:awm:prompt:id:", "");
			} else if(key.startsWith("urn:awm")) {
				shortHeader = key.replace("urn:awm:", "");
			}
			builder.append(shortHeader);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine);
		
		if(! req.isSuppressMetadata()) {
			builder.append("# end data").append(newLine);
		}
		
		return builder.toString();
	}
	
	private Object toJson(Map<String, PromptProperty> ppMap) throws JSONException {
		JSONArray main = new JSONArray();
		Iterator<String> it = ppMap.keySet().iterator();
		while(it.hasNext()) {
			PromptProperty pp = ppMap.get(it.next());
			JSONObject outer = new JSONObject();
			JSONObject inner = new JSONObject();
			inner.put("value", pp.getValue());
			inner.put("label", pp.getLabel());
			outer.put(pp.getKey(), inner);
			main.put(outer);
		}
		return main;
	}
}
