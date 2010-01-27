package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.PromptDataPacket.PromptResponseDataPacket;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Builder of DataPackets representing a prompt response upload.
 * 
 * @author selsky
 */
public class PromptDataPacketBuilder extends AbstractDataPacketBuilder {
//	private static Logger logger = Logger.getLogger(PromptDataPacketBuilder.class);
	
	/**
	 * Creates a DataPacket for a prompt response upload. The DataPacket will contain the JSON message metadata and each prompt
	 * response. Assumes that the prompt upload message is valid.
	 */
	public DataPacket createDataPacketFrom(JSONObject source) {
		PromptDataPacket promptDataPacket = new PromptDataPacket();
		createCommonFields(source, promptDataPacket);
		List<PromptResponseDataPacket> _responsePackets = new ArrayList<PromptResponseDataPacket>();
		 
		JSONArray responses = JsonUtils.getJsonArrayFromJsonObject(source, "responses");
		int arrayLength = responses.length();	
		
		for(int i = 0; i < arrayLength; i++) {
			PromptResponseDataPacket promptResponseDataPacket = promptDataPacket.new PromptResponseDataPacket();
			JSONObject fullResponseObject = JsonUtils.getJsonObjectFromJsonArray(responses, i);
			
			promptResponseDataPacket.setPromptConfigId(JsonUtils.getIntegerFromJsonObject(fullResponseObject, "prompt_id"));
			
			Object response = JsonUtils.getObjectFromJsonObject(fullResponseObject, "response");
			
			// Create a new JSON object containing only the response. The data type of the response is variable.
			Map<String, Object> responseOnlyMap = new HashMap<String, Object>();
			responseOnlyMap.put("response", response);
			JSONObject responseOnlyObject = new JSONObject(responseOnlyMap); 
			promptResponseDataPacket.setResponse(responseOnlyObject.toString());
			
			// logger.info(promptResponseDataPacket);
			
	    	_responsePackets.add(promptResponseDataPacket);
		}
		
		promptDataPacket.setResponses(_responsePackets);
		
		return promptDataPacket;
	}

}
