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
package org.ohmage.jee.servlet.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class SurveyUploadValidator extends AbstractGzipHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyUploadValidator.class);
	private List<String> _parameterList;
	
	/**
	 * 
	 */
	public SurveyUploadValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"user",
				                                                          "client",
				                                                          "data",
				                                                          "password",
				                                                          "campaign_urn",
				                                                          "campaign_creation_timestamp"}));
	}
	
	
	@Override
	public boolean validate(HttpServletRequest httpServletRequest) {
		Map<String, String[]> parameterMap = requestToMap(httpServletRequest);
		
		if(! basicValidation(parameterMap, _parameterList)) {
			return false;
		}
				
		String user = parameterMap.get("user")[0];
		String password = parameterMap.get("password")[0];
		String client = parameterMap.get("client")[0];
		String campaignUrn = parameterMap.get("campaign_urn")[0];
		String data = parameterMap.get("data")[0];
		String campaignCreationTimestamp = parameterMap.get("campaign_creation_timestamp")[0];
		
		// Check for abnormal lengths (buffer overflow attack)
		// The max lengths are based on the column widths in the db  
		
		if(greaterThanLength("user", "user", user, 15)
		   || greaterThanLength("client", "client", client, 255)
		   || greaterThanLength("campaign URN", "campaign_urn", campaignUrn, 250)
		   || greaterThanLength("password", "password", password, 100)
		   || greaterThanLength("campaignCreationTimestamp", "campaign_creation_timestamp", campaignCreationTimestamp, 19) 
		   || greaterThanLength("survey data payload", "data", data, 65535)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		httpServletRequest.setAttribute("validatedParameterMap", parameterMap);
		
		return true;
	}
}
