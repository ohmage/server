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

import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.AbstractAnnotatingJsonObjectValidator;


/**
 * Validator for the survey_launch_context element of a survey message. 
 * 
 * @author selsky
 */
public class JsonMsgSurveyLaunchContextValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "survey_launch_context";
		
	public JsonMsgSurveyLaunchContextValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if survey_launch_context does not exist or survey_launch_context exists and is a valid JSON object and contains
	 * a launch_time property 
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		JSONObject o = JsonUtils.getJsonObjectFromJsonObject(jsonObject, _key);
		
		if(null != o) {
			
			String launchTime = JsonUtils.getStringFromJsonObject(o, "launch_time");
			
			if(null == launchTime) {
				getAnnotator().annotate(awRequest, "launch_time from survey_launch_context is null");
				return false;
			}
			
		} else {
			
			getAnnotator().annotate(awRequest, "missing survey_launch_context");
			return false;
		}
		
		return true;
	}
}
