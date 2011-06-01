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
package org.ohmage.validator;

import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;


/**
 * Validates the survey id list for a new data point query. 
 * 
 * @see PromptIdListValidator
 * @author selsky
 */
public class SurveyIdListValidator extends AbstractAnnotatingRegexpValidator {
	
	public SurveyIdListValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String surveyIdListString = ((SurveyResponseReadAwRequest) awRequest).getSurveyIdListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "empty user survey id list found");
			return false;
		
		}
		
		// first check for the special "all users" value
		if("urn:ohmage:special:all".equals(surveyIdListString)) {
			
			return true;
			
		} else {
			
			String[] surveyIds = surveyIdListString.split(",");
			
			if(surveyIds.length > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 survey ids in query: " + surveyIdListString);
				return false;
				
			} else {
				
				for(int i = 0; i < 10; i++) {
					if(! _regexpPattern.matcher(surveyIds[i]).matches()) {
						getAnnotator().annotate(awRequest, "incorrect survey id: " + surveyIds[i]);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
