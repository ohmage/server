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
import org.ohmage.domain.Configuration;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * @author selsky
 */
public class SurveyResponseReadPromptIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadPromptIdValidationService.class);
	
	public SurveyResponseReadPromptIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks the prompt ids from the query (if any prompt ids exist) to make sure that they belong to the campaign name-version
	 * in the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating prompt ids against a campaign config");
		
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		
		if((null != req.getPromptIdListString()) && (0 != req.getPromptIdList().size())) {
			
			if(! "urn:ohmage:special:all".equals(req.getPromptIdListString())) {
				
				List<String> promptIds = req.getPromptIdList();
				
				Configuration config = req.getConfiguration();
				
				for(String promptId : promptIds) {
					
					if(null == config.getSurveyIdForPromptId(promptId)) {
						
						getAnnotator().annotate(awRequest, "prompt id " + promptId + " does not exist for campaign");
						return;
						
					}
				}
			}
		}
	}
}
