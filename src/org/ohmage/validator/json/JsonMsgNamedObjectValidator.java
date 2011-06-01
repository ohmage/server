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
 * Validator for named JSON Objects in AW JSON messages.
 * 
 * @author selsky
 */
public class JsonMsgNamedObjectValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key;
		
	/**
     * @throws IllegalArgumentException if the provded String key is empty, null, or all whitespace
	 */
	public JsonMsgNamedObjectValidator(AwRequestAnnotator awRequestAnnotator, String key) {
		super(awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("a non-null, non-empty, non-all-whitespace key is required");
		}
		_key = key;
	}
	
	/**
	 * @return true if the value returned from the AwRequest for the key set on construction returns a value that is a JSON Object
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		 
		JSONObject object = JsonUtils.getJsonObjectFromJsonObject(jsonObject, _key);
		
		if(null == object) {
			getAnnotator().annotate(awRequest, _key + " object in message is null");
			return false;
		}
		
		return true;
	}
}
