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
package org.ohmage.domain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;

import edu.ucla.cens.mobilityclassifier.Sample;

/**
 * @author selsky
 */
public class MobilityModeSensorDataPacketBuilder extends AbstractDataPacketBuilder {

	public MobilityModeSensorDataPacketBuilder() {
		
	}
	
	public MetadataDataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest) {
		MobilitySensorDataPacket packet = new MobilitySensorDataPacket();
		createCommonFields(source, packet);
		JSONObject dataObject = JsonUtils.getJsonObjectFromJsonObject(source, "data");
		packet.setSensorDataString(dataObject.toString());
		packet.setSpeed(JsonUtils.getDoubleFromJsonObject(dataObject, "speed"));
		packet.setMode(JsonUtils.getStringFromJsonObject(dataObject, "mode"));
		
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(dataObject, "accel_data");
		List<Sample> sampleList = new ArrayList<Sample>();
		int arraySize = array.length();
		for(int i = 0; i < arraySize; i++) {
			JSONObject sample = JsonUtils.getJsonObjectFromJsonArray(array, i);
			Double x = JsonUtils.getDoubleFromJsonObject(sample, "x");
			Double y = JsonUtils.getDoubleFromJsonObject(sample, "y");
			Double z = JsonUtils.getDoubleFromJsonObject(sample, "z");
			Sample s = new Sample();
			s.setX(x);
			s.setY(y);
			s.setZ(z);
			sampleList.add(s);
		}
		packet.setSamples(sampleList);
		
		return packet;
	}
}
