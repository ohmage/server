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
import org.ohmage.util.DateUtils;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates the timezone element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgTimezoneValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "timezone";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgTimezoneValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the value returned from the JSONObject for the key "timezone" exists and is a valid timezone.
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		String tz = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == tz) {
			getAnnotator().annotate(awRequest, "timezone in message is null");
			return false;
		}
		
		if(! DateUtils.isValidTimezone(tz)) {
			getAnnotator().annotate(awRequest, "invalid timezone: " + tz);
			return false;
		}
		
		return true;
	}
}
