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

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * @author selsky
 */
public class JsonMsgValueInListValidator extends AbstractAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgValueInListValidator.class);
	private String _key;
	private List<String> _allowedValues;
	 
	public JsonMsgValueInListValidator(AwRequestAnnotator annotator, String key, List<String> allowedValues) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a key is required");
		}
		
		if(null == allowedValues || allowedValues.isEmpty()) {
			throw new IllegalArgumentException("a non-empty list of values is required");
		}
		
		_key = key;
		_allowedValues = allowedValues;
	}
	
	
	@Override
	public boolean validate(AwRequest awRequest, JSONObject object) {
		String value = JsonUtils.getStringFromJsonObject(object, _key);
		
		if(null == value) {
			_logger.warn("could not retrieve " + _key + " from JSON Object " + object);
			getAnnotator().annotate(awRequest, "could not retrieve " + _key + "from JSON object");
			return false;
		}
		
		
		if(! _allowedValues.contains(value)) {
			_logger.warn("invalid value in JSON Object for " + _key + ". Object: " + object);
			getAnnotator().annotate(awRequest, "invalid value in JSON Object for " + _key);
			return false;
		}
		
		return true;
	}
}
