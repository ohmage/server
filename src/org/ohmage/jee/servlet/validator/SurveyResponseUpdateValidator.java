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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Validates an incoming survey update HTTP request.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseUpdateValidator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseUpdateValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that the required parameters exist and represent sane values based on their lengths.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {		 
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String campaignUrn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		String surveyKey = httpRequest.getParameter(InputKeys.SURVEY_KEY);
		String privacyState = httpRequest.getParameter(InputKeys.PRIVACY_STATE);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			// Don't log this to avoid flooding the logs when an attack occurs.
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(campaignUrn)) {
			return false;
		} 
		else if(StringUtils.isEmptyOrWhitespaceOnly(surveyKey)) {
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(client)) {
			return false;
		}
		
		if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CAMPAIGN_URN, InputKeys.CAMPAIGN_URN, campaignUrn, 255)) {
			_logger.warn(InputKeys.CAMPAIGN_URN + " is too long.");
			return false;
		} 
		else if(greaterThanLength(InputKeys.SURVEY_KEY, InputKeys.SURVEY_KEY, surveyKey, 10)) {
			_logger.warn(InputKeys.SURVEY_KEY + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.PRIVACY_STATE, InputKeys.PRIVACY_STATE, privacyState, 9)) {
			_logger.warn(InputKeys.PRIVACY_STATE + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 250)) {
			_logger.warn(InputKeys.CLIENT + " is too long.");
			return false;
		}
		
		return true;
	}
}
