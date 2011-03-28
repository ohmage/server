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
 * Strategy for outputting CSV output for the new data point API.
 * 
 * @author selsky
 */
public class NewDataPointQueryCsvColumnOutputBuilder {
	private static String newLine = System.getProperty("line.separator");
	
	public String createMultiResultOutput(int totalNumberOfResults,
			                              NewDataPointQueryAwRequest req,
			                              Map<String, PromptContext> promptContextMap,
			                              Map<String, List<Object>> columnMap) throws JSONException {
		
		Set<String> columnMapKeySet = columnMap.keySet();
		Set<String> promptContextKeySet = promptContextMap.keySet();
		
		StringBuilder builder = new StringBuilder();
		builder.append("# begin metadata").append(newLine)
			   .append("success").append(newLine)
			   .append("campaign_name,").append(req.getCampaignName()).append(newLine)
		       .append("campaign_version,").append(req.getCampaignVersion()).append(newLine)
		       .append("number_of_prompts,").append(totalNumberOfResults).append(newLine)
		       .append("number_of_surveys,").append(columnMap.get(columnMapKeySet.toArray()[0]).size()).append(newLine)
		       .append("# end metadata").append(newLine).append("# begin prompt contexts").append(newLine);
		
		for(String key : promptContextKeySet) {
			JSONObject context = new JSONObject();
			context.put("prompt_id", key);
			context.put("unit", promptContextMap.get(key).getUnit() == null ? "NA" : promptContextMap.get(key).getUnit());
			context.put("prompt_type", promptContextMap.get(key).getType());
			context.put("display_type", promptContextMap.get(key).getDisplayType());
			context.put("display_label", promptContextMap.get(key).getDisplayLabel());
			if(null != promptContextMap.get(key).getChoiceGlossary()) {
				context.put("choice_glossary", promptContextMap.get(key).getChoiceGlossary());
			} else {
				context.put("choice_glossary", "NA");
			}
			
			builder.append(context.toString()).append(newLine);
		}
		builder.append("# end prompt contexts").append(newLine).append("# begin data").append(newLine);
		
		// Build the column headers
		int s = columnMapKeySet.size();
		int i = 0;
		for(String key : columnMapKeySet) {
			builder.append(key);
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
		builder.append("# end data").append(newLine);
		return builder.toString();
	}
	
	public String createZeroResultOutput(NewDataPointQueryAwRequest req, Map<String, List<Object>> columnMap) {
		StringBuilder builder = new StringBuilder();
		builder.append("# begin metadata").append(newLine)
		       .append("success").append(newLine)
			   .append("number_of_prompts,0").append(newLine)
			   .append("number_of_surveys,0").append(newLine)
		       .append("campaign_name,").append(req.getCampaignName()).append(newLine)
		       .append("campaign_version,").append(req.getCampaignVersion()).append(newLine)
		       .append("# end metadata").append(newLine).append("# begin data").append(newLine);
		int s = columnMap.keySet().size();
		int i = 0;
		for(String key : columnMap.keySet()) {
			builder.append(key);
			if(i < s - 1) {
				builder.append(",");
			}
			i++;
		}
		builder.append(newLine).append("# end data").append(newLine);
		return builder.toString();
	}
}
