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
 * Validator for inbound data to the image query API.
 * 
 * @author selsky
 */
public class ImageQueryValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(ImageQueryValidator.class);
	private List<String> _parameterList;
	
	public ImageQueryValidator() {
		_parameterList = new ArrayList<String>(Arrays.asList(new String[]{"user","campaign_urn","client","id","auth_token","size"}));
	}
	
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		String token;
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
			
			if(token == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		else {
			token = tokens.get(0);
		}
		
		Map<String, String[]> parameterMap = getParameterMap(httpRequest);
		
		if(parameterMap.size() != _parameterList.size() && parameterMap.size() != _parameterList.size() - 1) {
			_logger.info("incorrect number of parameters found");
			return false;
		}
		
		// Check for duplicate parameters
		if(containsDuplicateParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		// Check for parameters with unknown names
		if(containsUnknownParameter(parameterMap, _parameterList)) {
			return false;
		}
		
		String user = (String) httpRequest.getParameter("user");
		String campaignUrn = (String) httpRequest.getParameter("campaign_urn");
		String client = (String) httpRequest.getParameter("client");
		String id = (String) httpRequest.getParameter("id");
		String size = (String) httpRequest.getParameter("size");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("campaignUrn", "campaign_urn", campaignUrn, 250)
		   || greaterThanLength("client", "client",client, 250)		   
		   || greaterThanLength("authToken", "auth_token", token, 36)
		   || greaterThanLength("user", "user", user, 15)
		   || greaterThanLength("imageId", "id", id, 36)
		   || greaterThanLength("size", "size", size, 5)) { // the only currently allowed value is "small"
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
		return true;
	}
}