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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;


/**
 * Validates the prompt id list for a new data point query.
 * 
 * @author selsky
 */
public class PromptIdListSurveyIdListValidator extends AbstractAnnotatingRegexpValidator {
	private static Logger _logger = Logger.getLogger(PromptIdListSurveyIdListValidator.class);
	
	public PromptIdListSurveyIdListValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(regexp, awRequestAnnotator);
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating prompt id list or survey id list");
		
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String promptIdListString = ((SurveyResponseReadAwRequest) awRequest).getPromptIdListString();
		String surveyIdListString = ((SurveyResponseReadAwRequest) awRequest).getSurveyIdListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptIdListString) && StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "empty prompt id list and empty survey id list found");
			return false;	
			
		} else if (! StringUtils.isEmptyOrWhitespaceOnly(promptIdListString) 
						&& ! StringUtils.isEmptyOrWhitespaceOnly(surveyIdListString)) {
			
			getAnnotator().annotate(awRequest, "both prompt id list and survey id list found - only one is allowed");
			return false;
			
		}
		
		return checkList(awRequest, surveyIdListString == null ? promptIdListString : surveyIdListString, surveyIdListString == null);
		
	}
	
	private boolean checkList(AwRequest awRequest, String listAsString, boolean isPrompt) {
		// first check for the special value for retrieving all items
		if("urn:ohmage:special:all".equals(listAsString)) {
			
			return true;
			
		} else {
			
			List<String> ids = Arrays.asList(listAsString.split(InputKeys.LIST_ITEM_SEPARATOR));
			
			if(ids.size() > 10) {
				
				getAnnotator().annotate(awRequest, "more than 10 ids in query: " + listAsString);
				return false;
			
			} else {
				
				Set<String> idSet = new HashSet<String>(ids);
				
				if(idSet.size() != ids.size()) {
					
					getAnnotator().annotate(awRequest, "found duplicate id in list: " + ids);
					return false;
					
				}
				
				for(String id : ids) {
					if(! _regexpPattern.matcher(id).matches()) {
						getAnnotator().annotate(awRequest, "malformed id: " + id);
						return false;
					}
				}
			}
		}
		
		return true;
	}
}
