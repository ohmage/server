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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.domain.DataPacket;
import org.ohmage.domain.DataPacketBuilder;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;


/**
 * @author selsky
 */
public class SurveyDataPacketBuilderService implements Service {
	private static Logger _logger = Logger.getLogger(SurveyDataPacketBuilderService.class);
	private DataPacketBuilder _builder;
	
	/**
	 * @throws IllegalArgumentException if the provided Map is null or empty
	 */
	public SurveyDataPacketBuilderService(DataPacketBuilder builder) {
		if(null == builder) {
			throw new IllegalArgumentException("a builder is required");
		}
		_builder = builder;
	}
	
	/**
	 * Creates DataPackets for uploaded surveys using the builder set on construction. The DataPackets are then set on the 
	 * AwRequest.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to build data packets for db insertion using builder " + _builder.getClass());
		JSONArray jsonArray = awRequest.getJsonDataAsJsonArray();
		int length = jsonArray.length();
		List<DataPacket> dataPackets = new ArrayList<DataPacket>(length);
		 
		for(int i = 0; i < length; i++) {
			dataPackets.add(_builder.createDataPacketFrom(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i), awRequest));
		}
		
		awRequest.setDataPackets(dataPackets);
		_logger.info("finished building data packets using builder " + _builder.getClass());
	}
}
