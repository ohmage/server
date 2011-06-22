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
package org.ohmage.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.SurveyResponseDeleteAwRequest;
import org.ohmage.util.CookieUtils;


/**
 * Creates an internal request for deleting surveys.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseDeleteAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(SurveyResponseDeleteAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public SurveyResponseDeleteAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a request object based on the parameters from the HTTP request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating survey response delete request.");
		
		SurveyResponseDeleteAwRequest internalRequest = 
			new SurveyResponseDeleteAwRequest(httpRequest.getParameter(InputKeys.CAMPAIGN_URN),
					                          httpRequest.getParameter(InputKeys.SURVEY_KEY));
		
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		internalRequest.setUserToken(token);
		internalRequest.setCampaignUrn(httpRequest.getParameter(InputKeys.CAMPAIGN_URN));
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT)); // push the client string into the Log4J NDC for the currently  
                                                                      // executing thread _ this means that it will be in every log
		                                                              // message for the current thread
		
		return internalRequest;
	}
}
