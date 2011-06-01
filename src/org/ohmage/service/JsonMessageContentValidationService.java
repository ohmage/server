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

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.JsonObjectValidator;


/**
 * A service for validating the contents of JSON messages. The messages are sent to AW as a JSON Array where each element in the 
 * array corresponds to a type defined by the original request URL /app/u/survey or /app/u/mobility. 
 *
 * The JSON spec can be found here: http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Upload-API
 * 
 * @author selsky
 */
public class JsonMessageContentValidationService implements Service {
	private static Logger _logger = Logger.getLogger(JsonMessageContentValidationService.class);
	private List<JsonObjectValidator> _validators;
	private AwRequestAnnotator _noDataAnnotator;
	private AwRequestAnnotator _incorrectEntryAnnotator;;
	
	/**
	 * @throws IllegalArgumentException if the provided validatorMap is null or empty
	 * @throws IllegalArgumentException if the provided noDataAnnotator is null
	 * @throws IllegalArgumentException if the provided incorrectEntryAnnotator is null
	 */
	public JsonMessageContentValidationService(List<JsonObjectValidator> validators, 
		AwRequestAnnotator noDataAnnotator, AwRequestAnnotator incorrectEntryAnnotator) {
		
		if(null == validators || validators.size() == 0) {
			throw new IllegalArgumentException("the provided validator List cannot be null or empty");
		}
		
		if(null == noDataAnnotator) {
			throw new IllegalArgumentException("the provided AwRequestAnnotator (noDataAnnotator) cannot be null");
		}
		
		if(null == incorrectEntryAnnotator) {
			throw new IllegalArgumentException("the provided AwRequestAnnotator (incorrectEntryAnnotator) cannot be null");
		}
		
		_validators = validators;
		_noDataAnnotator = noDataAnnotator;
		_incorrectEntryAnnotator = incorrectEntryAnnotator;
	}
	
	/**
	 * Validates the content of our sensor data upload JSON messages. The messages come in three main types: mobility mode only,
	 * mobility features, and prompt. Each prompt type is defined by a type in the data store (currently, the prompt_type table).
	 * 
	 * The messages are sent in a JSON array where each array element represents a message. The type of the messages is 
	 * found in the AwRequest attribute <code>requestType</code>. The array must contain only messages that belong to the 
	 * request type. 
	 * 
	 * This method is fail-fast in that it will mark an entire array of messages as invalid if a single entry in the array fails
	 * validation. The reason this is done is that incorrect data in the messages implies a logical error on the device uploading
	 * the data (or that the server validation rule is too strict) and also that it will be simpler to fix one error at a time 
	 * rather than having to go through many error messages in a server response.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning JSON message content validation");
		
		JSONArray jsonArray = awRequest.getJsonDataAsJsonArray();
		int length = jsonArray.length();
		
		if(0 == length) {
			
			_noDataAnnotator.annotate(awRequest, "no elements in the data array");
			return;
			
		}
		
		for(int i = 0; i < length; i++) {
			
			JSONObject jsonObject = JsonUtils.getJsonObjectFromJsonArray(jsonArray, i); // each array entry is a JSON Object
			                                                                            // representing an upload data packet
			
			if(null == jsonObject) {
				_incorrectEntryAnnotator.annotate(awRequest, "missing data packet - empty JSON object");
				return;
			}
			
			awRequest.setCurrentMessageIndex(i); // keep track of the current index for error logging
			
			for(JsonObjectValidator validator : _validators) {
				
				if(! validator.validate(awRequest, jsonObject)) {
					
					return; // Bail out because somewhere within a message there is a validation failure. 
					        // The failed validator handles annotating the request with the specific error.
					
				}
			}
		}	
		
		_logger.info("JSON message successfully validated");
	}
}
