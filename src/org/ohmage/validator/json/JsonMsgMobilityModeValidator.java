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

import java.util.List;

import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Validates the mode element from an mode_only or mode_features mobility message.
 * 
 * @author selsky
 */
public class JsonMsgMobilityModeValidator extends AbstractAnnotatingJsonObjectValidator {
//	private static Logger _logger = Logger.getLogger(JsonMsgMobilityModeValidator.class);
	protected String _key = "mode";
	protected List<String> _allowedValues;
		
	/**
     * @throws IllegalArgumentException if the provded list for allowed values is null or empty
	 */
	public JsonMsgMobilityModeValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedValues) {
		super(awRequestAnnotator);
		if(null == allowedValues || allowedValues.size() == 0) {
			throw new IllegalArgumentException("a non-null non-empty array of allowed values is required");
		}
		_allowedValues = allowedValues;
	}
	
	/**
	 * Validates the mode. If set up to doFeaturesValidation on construction, will attempt to retrieve the mode from the features
	 * object instead of the "root" object. Assumes the features object exists.  
	 * 
	 * @return true if the value returned from the AwRequest for the key "mode" exists and is a valid mode
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		String mode = JsonUtils.getStringFromJsonObject(jsonObject, _key);; 
				
		if(null == mode) {
			getAnnotator().annotate(awRequest, "mode in message is null");
			return false;
		}
		
		if(! _allowedValues.contains(mode)) {
			getAnnotator().annotate(awRequest, "invalid mode: " + mode);
			return false;
		}
		
		return true;
	}
}
