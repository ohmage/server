package edu.ucla.cens.awserver.jee.servlet.writer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.PromptContext;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * Strategy for outputting the column-based JSON output for the new data point API.
 * 
 * @author selsky
 */
public class NewDataPointQueryJsonColumnOutputBuilder {

	public String createMultiResultOutput(int totalNumberOfResults,
			                              NewDataPointQueryAwRequest req,
			                              Map<String, PromptContext> promptContextMap,
			                              Map<String, List<Object>> columnMap) throws JSONException {
		
		Set<String> columnMapKeySet = columnMap.keySet();
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("campaign_name", req.getCampaignName());
		metadata.put("campaign_version", req.getCampaignVersion());
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
			
			if(key.startsWith("urn:awm:prompt:id:")) {
			
				String promptId = key.substring("urn:awm:prompt:id:".length());
				JSONObject column = new JSONObject();
				JSONObject context = new JSONObject();
				context.put("unit", 
						    promptContextMap.get(promptId).getUnit() == null ? "NA" : promptContextMap.get(promptId).getUnit());
				context.put("prompt_type", promptContextMap.get(promptId).getType());
				context.put("display_type", promptContextMap.get(promptId).getDisplayType());
				context.put("display_label", promptContextMap.get(promptId).getDisplayLabel());
				if(null != promptContextMap.get(promptId).getChoiceGlossary()) {
					context.put("choice_glossary", promptContextMap.get(promptId).getChoiceGlossary());
				} else {
					context.put("choice_glossary", "NA");
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
		
		return main.toString(4); // TODO need pretty print option on the query params
	}
	
	public String createZeroResultOutput(NewDataPointQueryAwRequest req,
                                         Map<String, List<Object>> columnMap) throws JSONException {
		
		// Create metadata section anyway
		
		JSONObject main = new JSONObject();
		main.put("result", "success");
		JSONObject metadata = new JSONObject();
		metadata.put("number_of_prompts", 0);
		metadata.put("number_of_surveys", 0);
		metadata.put("campaign_name", req.getCampaignName());
		metadata.put("campaign_version", req.getCampaignVersion());
		JSONArray items = new JSONArray();
		for(String key : columnMap.keySet()) {
			items.put(key);
		}
		metadata.put("items", items);
		main.put("metadata", metadata);
		JSONArray data = new JSONArray();
		main.put("data", data);
		
		return main.toString(4); // TODO need pretty print option on the query params
	}
}
