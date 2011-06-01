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

import org.json.JSONObject;
import org.ohmage.util.JsonUtils;


/**
 * Abstract helper class for handling fields common to all packets.
 * 
 * @author selsky
 */
public abstract class AbstractDataPacketBuilder implements DataPacketBuilder {
	
	public void createCommonFields(JSONObject source, MetadataDataPacket packet) {
		String date = JsonUtils.getStringFromJsonObject(source, "date");
		long time = JsonUtils.getLongFromJsonObject(source, "time");
		String timezone = JsonUtils.getStringFromJsonObject(source, "timezone");
		String locationStatus = JsonUtils.getStringFromJsonObject(source, "location_status");
		JSONObject o = JsonUtils.getJsonObjectFromJsonObject(source, "location");
		String location = null;
		if(null != o) {
			location = o.toString();
		}
		packet.setDate(date);
		packet.setEpochTime(time);
		packet.setTimezone(timezone);
		packet.setLocationStatus(locationStatus);
		packet.setLocation(location);
	}
}
