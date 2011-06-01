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

import org.json.JSONArray;
import org.json.JSONException;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service-level validation of in-bound JSON messages.
 * 
 * @author selsky
 */
public class JsonMessageSyntaxValidationService implements Service {
	private AwRequestAnnotator _awRequestAnnotator;
	
	/**
	 * @throws IllegalArgumentException if the provided message is null, empty, or all whitespace 
	 */
	public JsonMessageSyntaxValidationService(AwRequestAnnotator awRequestAnnotator) {
		if(null == awRequestAnnotator) {
			throw new IllegalArgumentException("a non-null AwRequestAnnotator is required");
		}
		_awRequestAnnotator = awRequestAnnotator;
	}
	
	/**
	 * Checks the structure and contents of JSON messages. Extra validation is performed here instead of in the validation layer
	 * for because it is not the job of the first-level of validation to parse JSON and place the parsed results back into the 
	 * request. The idea is to keep the purpose/functionality of each app layer as segmented and specific as possible. 
	 * 
	 * If the message is syntactically correct JSON, it gets placed into the AwRequest as a JSONArray.
	 */
	public void execute(AwRequest awRequest) {
		JSONArray jsonArray =  null;
		
		try {
			String jsonDataString = awRequest.getJsonDataAsString();
			jsonArray = new JSONArray(jsonDataString);
			awRequest.setJsonDataAsString(null); // free the reference to the (potentially huge) string 
			awRequest.setJsonDataAsJsonArray(jsonArray);
		
		} catch(JSONException jsone) { // the message is not syntactically correct JSON (the new JSONArray() failed because of 
			                           // a JSON syntax error)
			
			_awRequestAnnotator.annotate(awRequest, "invalid JSON");
			
		}
	}
}
