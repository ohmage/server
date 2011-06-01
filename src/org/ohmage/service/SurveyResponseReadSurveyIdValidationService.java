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
 * @author Joshua Selsky
 */
public class SurveyResponseReadSurveyIdValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadSurveyIdValidationService.class);
	
	public SurveyResponseReadSurveyIdValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating survey ids against a campaign config");
		
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		
		if((null != req.getSurveyIdListString()) && (0 != req.getSurveyIdList().size())) {
			
			if(! "urn:ohmage:special:all".equals(req.getSurveyIdListString())) {
				
				List<String> surveyIds = req.getSurveyIdList(); 
				
				Configuration config = req.getConfiguration();
				
				for(String surveyId : surveyIds) {
					
					if(! config.surveyIdExists(surveyId)) {
						
						getAnnotator().annotate(awRequest, "survey " + surveyId + " does not exist");
						return;
						
					}
				}
			}
		}
	}
}
