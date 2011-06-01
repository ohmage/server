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
package org.ohmage.validator.survey;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.AbstractAnnotatingJsonObjectValidator;


/**
 * Validator for the responses element of a prompt message.
 * 
 * @author selsky
 */
public class JsonMsgSurveyResponsesExistValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "responses";
		
	public JsonMsgSurveyResponsesExistValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the responses array exists and is not empty
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(jsonObject, _key);
		
		if(null == array || array.length() == 0) {
			getAnnotator().annotate(awRequest, "responses array from survey message is null or empty");
			return false;
		}
		
		return true;
	}
}
