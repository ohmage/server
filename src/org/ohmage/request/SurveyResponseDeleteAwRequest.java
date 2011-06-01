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
package org.ohmage.request;


/**
 * State for /app/survey_response/delete.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteAwRequest extends ResultListAwRequest {
	
	/**
	 * All parameters are required and all parameters are added to the toValidate Map in the request.
	 * 
	 * @param campaignUrn a campaign URN that the user must belong to 
	 * @param surveyKey a survey id representing the survey to delete
	 */
	public SurveyResponseDeleteAwRequest(String campaignUrn, String surveyKey) {
		addToValidate(InputKeys.CAMPAIGN_URN, campaignUrn, true);
		addToValidate(InputKeys.SURVEY_KEY, surveyKey, true);
	}
}
