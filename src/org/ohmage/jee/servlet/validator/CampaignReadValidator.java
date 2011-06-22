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
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * @author selsky
 */
public class CampaignReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(CampaignReadValidator.class);
	private List<String> _parameterList;
	
	public CampaignReadValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"client",
		 		                                                          "output_format", 
		 		                                                          "campaign_urn_list",
		 		                                                          "start_date",
		 		                                                          "end_date",
		 		                                                          "privacy_state",
		 		                                                          "running_state",
		 		                                                          "user_role",
		 		                                                          "class_urn_list",
		 		                                                          "user",
		 		                                                          "password"}
		));
	}
	
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		Map<String, String[]> parameterMap = getParameterMap(httpRequest);
		
		// check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		// check for parameters with duplicate values
		if(containsDuplicateParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		// Make sure required parameters exist
		String client = httpRequest.getParameter("client");
		if(null == client) {
			_logger.info("missing client parameter in request");
			return false;
		}
		String outputFormat = httpRequest.getParameter("output_format");
		if(null == outputFormat) {
			_logger.info("missing output_format parameter in request");
			return false;
		}
		
		// perform sanity check on the optional params anyway
		String user = httpRequest.getParameter("user");
		String password = httpRequest.getParameter("password");
		String campaignUrnList = httpRequest.getParameter("campaign_urn_list");
		String startDate = httpRequest.getParameter("start_date");
		String endDate = httpRequest.getParameter("end_date");
		String privacyState = httpRequest.getParameter("privacy_state");
		String runningState = httpRequest.getParameter("running_state");
		String userRole = httpRequest.getParameter("user_role");
		String classUrnList = httpRequest.getParameter("class_urn_list");
		
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			// Possible if user and password aren't null.
			if((user == null) || (password == null)) {
				if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
					throw new MissingAuthTokenException("The required authentication / session token is missing.");
				}
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
 		
		if(greaterThanLength("client", "client", client, 250)
		   || greaterThanLength("output format", "output_format", outputFormat, 6)
		   || greaterThanLength("campaign URN list", "cammpaign_urn_list", campaignUrnList, 2550) // max of 10 URNs (our db column 
		                                                                                          // restriction is 255 chars)
		   || greaterThanLength("start date", "start_date", startDate, 10)
		   || greaterThanLength("end date", "end_date", endDate, 10)
		   || greaterThanLength("privacy state", "privacy_state", privacyState, 7)
		   || greaterThanLength("user", "user", user, 15)
		   || greaterThanLength("password", "password", password, 100)
		   || greaterThanLength("running state", "running_state", runningState, 7)
		   || greaterThanLength("user role", "user_role", userRole, 11)
		   || greaterThanLength("class URN list", "class_urn_list", classUrnList, 2550) // max of 10 URNs as above for campaignUrnList
		) {
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}
