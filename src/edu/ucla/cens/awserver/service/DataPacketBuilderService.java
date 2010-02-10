package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.DataPacketBuilder;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * Service for building MetadataDataPacket domain objects from JSON.
 * 
 * @author selsky
 */
public class DataPacketBuilderService implements Service {
	private static Logger _logger = Logger.getLogger(DataPacketBuilderService.class);
	private Map<String, DataPacketBuilder> _builderMap;
	
	/**
	 * @throws IllegalArgumentException if the provided Map is null or empty
	 */
	public DataPacketBuilderService(Map<String, DataPacketBuilder> builderMap) {
		if(null == builderMap || builderMap.isEmpty()) {
			throw new IllegalArgumentException("a builderMap is required");
		}
		_builderMap = builderMap;
	}
	
	/**
	 * 
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to build data packets for db insertion");
		
        // Special Rules (implement in builders)
		// convert dates to UTC 
		// convert Double.NaN for lat-long values to null for db - perform this action in the DAO

		JSONArray jsonArray = (JSONArray) awRequest.getAttribute("jsonData");
		int length = jsonArray.length();
		List<DataPacket> dataPackets = new ArrayList<DataPacket>(length);
		String requestType = (String) awRequest.getAttribute("requestType");
		
		// TODO eventually this could be made smarter (more generic) - it should just be able to loop through the map without
		// needing to know actual key values. 
		for(int i = 0; i < length; i++) {
			
			String builderName = "prompt";
			 
			if("mobility".equals(requestType)) {
				
				String subtype = JsonUtils.getStringFromJsonObject(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i), "subtype");
				
				if("mode_features".equals(subtype)) {
					
					builderName = "mobility-mode_features";
					
				} else if("mode_only".equals(subtype)) {
					
					builderName = "mobility-mode_only";
					
				} else { // this means there is a logical error in the validation because it did not detect an unknown subtype
					
					throw new IllegalStateException("illegal subtype found in mobility message");
				}
			} 
			
			DataPacket dataPacket = 
				_builderMap.get(builderName).createDataPacketFrom(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i));
			
			
//			if(_logger.isDebugEnabled()) {
//				_logger.debug(dataPacket.toString());
//			}
			
			dataPackets.add(dataPacket);
			
		}
		
		awRequest.setAttribute("dataPackets", dataPackets);
		_logger.info("finished building data packets");
	}
}
