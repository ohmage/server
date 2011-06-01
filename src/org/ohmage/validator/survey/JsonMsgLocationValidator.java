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

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.AbstractAnnotatingJsonObjectValidator;


/** 
 * @author selsky
 */
public class JsonMsgLocationValidator extends AbstractAnnotatingJsonObjectValidator {
	private static Logger _logger = Logger.getLogger(JsonMsgLocationValidator.class);
	// TODO should be using a List of interfaces, not abstract classes
	// and should also refactor the whole Annotator idea. Could use an interface with annotate() instead of 
	// having to use getAnnotator().annotate(). 
	private List<AbstractAnnotatingJsonObjectValidator>  _validators;
		
	public JsonMsgLocationValidator(AwRequestAnnotator annotator, List<AbstractAnnotatingJsonObjectValidator> validators) {
		super(annotator);
		
		if(null == validators || validators.isEmpty()) {
			throw new IllegalStateException("a list of validators is required");
		}
		
		_validators = validators;
	}
	
	/**
	 * Validates the location object depending on the value of location_status.
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {		
		String locationStatus = JsonUtils.getStringFromJsonObject(jsonObject, "location_status");
		
		if("unavailable".equals(locationStatus)) {
			
			if(null != JsonUtils.getObjectFromJsonObject(jsonObject, "location")) {
				_logger.warn("location_status is unavailable, but a location object was included " + JsonUtils.getObjectFromJsonObject(jsonObject, "location"));
				getAnnotator().annotate(awRequest, "location object exists even though location_status is unavailable");
				return false;
			}
			
		} else {
			
			if(null == JsonUtils.getJsonObjectFromJsonObject(jsonObject, "location")) {
				_logger.warn("location object is missing from message: " + jsonObject);
				getAnnotator().annotate(awRequest, "location object is missing from message");
				return false;
			}
			
			
			// lat, long, accuracy, provider, timestamp
			for(AbstractAnnotatingJsonObjectValidator validator : _validators) {
				if(! validator.validate(awRequest, jsonObject)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
