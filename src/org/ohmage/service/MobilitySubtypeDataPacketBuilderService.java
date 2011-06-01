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
package org.ohmage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.domain.DataPacket;
import org.ohmage.domain.DataPacketBuilder;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;


/**
 * Service for building MetadataDataPacket domain objects from JSON.
 * 
 * @author selsky
 */
public class MobilitySubtypeDataPacketBuilderService implements Service {
	private static Logger _logger = Logger.getLogger(MobilitySubtypeDataPacketBuilderService.class);
	private Map<String, DataPacketBuilder> _builderMap;
	
	/**
	 * @throws IllegalArgumentException if the provided Map is null or empty
	 */
	public MobilitySubtypeDataPacketBuilderService(Map<String, DataPacketBuilder> builderMap) {
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

		JSONArray jsonArray = awRequest.getJsonDataAsJsonArray();
		int length = jsonArray.length();
		List<DataPacket> dataPackets = new ArrayList<DataPacket>(length);
		String builderName = null;
		
		// TODO mobility is hardcoded for now
//		String requestType = "mobility"; // awRequest.getRequestType();
//		
//		// TODO eventually this could be made smarter (more generic) - it should just be able to loop through the map without
//		// needing to know actual key values i.e., the subtype values should be the keys in the map 
		for(int i = 0; i < length; i++) {
//			
//			String builderName = "prompt";
//				
				String subtype = JsonUtils.getStringFromJsonObject(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i), "subtype");
				
				if("sensor_data".equals(subtype)) {
					
					builderName = "mobility-sensor_data";
					
				} else if("mode_only".equals(subtype)) {
					
					builderName = "mobility-mode_only";
					
				} else { // this means there is a logical error in the validation because it did not detect an unknown subtype
					
					throw new IllegalStateException("illegal subtype found in mobility message");
				}
//			} 
			
			DataPacket dataPacket = 
				_builderMap.get(builderName).createDataPacketFrom(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i), awRequest);
			
			
//			if(_logger.isDebugEnabled()) {
//				_logger.debug(dataPacket.toString());
//			}
			
			dataPackets.add(dataPacket);
			
		}
		
		awRequest.setDataPackets(dataPackets);
		_logger.info("finished building data packets");
	}
}
