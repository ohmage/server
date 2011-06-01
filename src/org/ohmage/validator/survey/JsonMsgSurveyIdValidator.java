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
import org.ohmage.domain.Configuration;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.ohmage.util.JsonUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.AbstractAnnotatingJsonObjectValidator;


/**
 * Validates the survey_id element from an AW JSON message.
 * 
 * @author selsky
 */
public class JsonMsgSurveyIdValidator extends AbstractAnnotatingJsonObjectValidator {
	private String _key = "survey_id";
		
	/**
     * @throws IllegalArgumentException if the provded AwRequestAnnotator is null
	 */
	public JsonMsgSurveyIdValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * @return true if the survey_id from the JSONObject is found in a configuration bound to the campaign name-version pair found 
	 * in the AwRequest
	 * @return false otherwise
	 */
	public boolean validate(AwRequest awRequest, JSONObject jsonObject) {
		String surveyId = JsonUtils.getStringFromJsonObject(jsonObject, _key);
		
		if(null == surveyId) {
			getAnnotator().annotate(awRequest, "survey_id in message is null");
			return false;
		}
		
		// FIXME - drop cast
		Configuration configuration = ((SurveyUploadAwRequest) awRequest).getConfiguration();
		
		if(null == configuration) { // this is bad because it means that previous validation failed or didn't run
			throw new IllegalStateException("missing configuration for campaign URN: " + awRequest.getCampaignUrn());
		}
		
		if(! configuration.surveyIdExists(surveyId)) {
			getAnnotator().annotate(awRequest, "survey_id in message does not exist for configuration");
			return false;
		}
		
		return true;
	}
}
