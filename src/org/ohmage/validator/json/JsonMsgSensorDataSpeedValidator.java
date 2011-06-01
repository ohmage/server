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

import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates the speed element from a sensor_data mobility message.
 * 
 * This class is nearly identical to JsonMsgLocationAccuracyValidator.
 * 
 * @author selsky
 */
public class JsonMsgSensorDataSpeedValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "speed";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSensorDataSpeedValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, "data");
		String speed = JsonUtils.getStringFromJsonObject(object, _key); // annoyingly, the JSON lib does not have a getFloat(..)
		
		if(StringUtils.isEmptyOrWhitespaceOnly(speed)) {
			getAnnotator().annotate(awRequest, "missing speed");
			return false;
		}
		
		try {
		
			Float.parseFloat(speed);
			
		} catch (NumberFormatException nfe) {
			
			getAnnotator().annotate(awRequest, "unparseable float. " + nfe.getMessage() + " value: " + speed);
			return false;
			
		}
		
		return true;
	}
}
