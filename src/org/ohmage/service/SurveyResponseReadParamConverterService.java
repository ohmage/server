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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * TODO -- replace this class with data type conversions when the values are actually used? 
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadParamConverterService implements Service {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadParamConverterService.class);
	
	/**
	 * Converts the String representations of survey id, prompt id, user and column into Lists. Expects the awRequest
	 * to be a NewDataPointQueryAwRequest. Assumes the values have already been validated and that the string representations are
	 * comma-separated values.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Converting string parameters to lists or booleans where appropriate");
		
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest; 
		
		String surveyIdListString = req.getSurveyIdListString();
		String promptIdListString = req	.getPromptIdListString();
		String columnListString = req.getColumnListString();
		String userListString = req.getUserListString();
		
		req.setPrettyPrint(Boolean.valueOf(req.getPrettyPrintAsString()));
		req.setSuppressMetadata(Boolean.valueOf(req.getSuppressMetadataAsString()));
		req.setReturnId(Boolean.valueOf(req.getReturnIdAsString()));
		
		if(null == surveyIdListString) {
			
			String[] splitList = split(promptIdListString);
			List<String> ids = new ArrayList<String>();
			
			if(splitList.length == 1 && "urn:ohmage:special:all".equals(splitList[0])) {
				ids.add("urn:ohmage:special:all");
			} else {
				for(String entry : splitList) {
					ids.add(entry);
				}
			}
			
			req.setPromptIdList(ids);
			
		} else {
			
			String[] splitList = split(surveyIdListString);
			List<String> ids = new ArrayList<String>();
			
			if(splitList.length == 1 && "urn:ohmage:special:all".equals(splitList[0])) {
				ids.add("urn:ohmage:special:all");
			} else {
				for(String entry : splitList) {
					ids.add(entry);
				}
			}
			
			req.setSurveyIdList(ids);
		}
		
		req.setColumnList(Arrays.asList(split(columnListString)));
		req.setUserList(Arrays.asList(split(userListString)));
	}

	private String[] split(String string) {
		return string.split(",");
	}
}
