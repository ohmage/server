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

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * An implementation of AwRequestAnnotator for validation failures which ultimately result in JSON output (initially used 
 * in response to phone/device data uploads).
 * 
 * @author selsky
 */
public class FailedJsonRequestAnnotator implements AwRequestAnnotator {
	private static Logger logger = Logger.getLogger(FailedJsonRequestAnnotator.class);
	private ErrorResponse _errorResponse;
	
	/**
	 * The provided error message will be used as JSON output and must be a syntactically valid JSON Object.
	 * 
	 * @throws IllegalArgumentException if jsonErrorMessage is null
	 * @throws IllegalArgumentException if jsonErrorMessage string cannot be parsed to syntactically correct JSON (it must be a 
	 * valid JSON array.)
	 */
	public FailedJsonRequestAnnotator(ErrorResponse errorResponse) {
		if(null == errorResponse) {
			throw new IllegalArgumentException("a null ErrorResponse is not allowed");
		}
		_errorResponse = errorResponse;
	}
	
	/**
     * Annotates the request with a failed error message using JSON syntax. The JSON message is of the form: 
     * 
     * <pre>
     *   {
     *     "response":"failure",
     *     "errors":[
     *       {
     *         "code":"0000",
     *         "text":"text"
     *       },
     *       ...
     *     ]
     *   }
     * </pre>
     * 
     * The message passed in is used for debug output only. For cases where the JSONObject representing the error output message 
     * must be passed into this method, @see FailedJsonSuppliedMessageRequestAnnotator.
	 */
	public void annotate(AwRequest awRequest, String message) {
		awRequest.setFailedRequest(true);
		
		try {
			
			JSONObject responseJsonObject = new JSONObject();
			JSONObject errorJsonObject = new JSONObject();
			JSONArray errorJsonArray = new JSONArray();
			
			responseJsonObject.put("result", "failure");
			
			errorJsonObject.put("code", _errorResponse.getCode());
			errorJsonObject.put("text", _errorResponse.getText());
			
			if(awRequest instanceof SurveyUploadAwRequest) { // hackeroo! // TODO fix this
			
				if(-1 != awRequest.getCurrentMessageIndex()) {
					errorJsonObject.put("at_record_number", awRequest.getCurrentMessageIndex());
				}
				
//				if(-1 != awRequest.getCurrentPromptId()) { // a prompt upload is being handled.
//					errorJsonObject.put("at_prompt_id", awRequest.getCurrentPromptId());
//				}
			}
			
			errorJsonArray.put(errorJsonObject);
			responseJsonObject.put("errors", errorJsonArray);
			awRequest.setFailedRequestErrorMessage(responseJsonObject.toString());
			
			if(logger.isDebugEnabled()) {
				logger.debug(message);
			}
			
		} catch(JSONException jsone) {  // invalid JSON at this point is a logical error
		
			throw new IllegalStateException(jsone);
		}
	}
}