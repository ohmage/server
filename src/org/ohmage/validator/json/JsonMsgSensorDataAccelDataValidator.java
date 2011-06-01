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
package org.ohmage.validator.json;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates the accel_data element from a sensor_data mobility message.
 * 
 * @author selsky
 */
public class JsonMsgSensorDataAccelDataValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "accel_data";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSensorDataAccelDataValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "data");
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(object, _key);
		
		if(null == array || 0 == array.length()) {
			getAnnotator().annotate(awRequest, "invalid or non-existent accel_data array in sensor_data mobility message");
			return false;
		}
		
		return true;
	}
}
