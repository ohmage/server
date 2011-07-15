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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * Validator for inbound data to the data point function API. This class is exactly the same as DataPointQueryValidator except that
 * it doesn't allow multiple "i" parameters.
 * 
 * @author selsky
 */
public class DataPointFunctionQueryValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DataPointFunctionQueryValidator.class);
	
	/**
	 */
	public DataPointFunctionQueryValidator() {
		// Do nothing.
	}
	
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		Map<String,String[]> parameterMap = getParameterMap(httpRequest); 
		
		Iterator<?> iterator = parameterMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			String[] valuesForKey = (String[]) parameterMap.get(key);
			
			if(valuesForKey.length != 1) {
				_logger.warn("an incorrect number of values (" + valuesForKey.length + ") was found for parameter " + key);
				return false;
			}
		}
		
		String startDate = (String) httpRequest.getParameter("start_date");
		String endDate = (String) httpRequest.getParameter("end_date");
		String owner = (String) httpRequest.getParameter("owner");
		String campaignUrn = (String) httpRequest.getParameter("campaign_urn");
		String client = (String) httpRequest.getParameter("client");
		String id = (String) httpRequest.getParameter("id");
		String user = httpRequest.getParameter("user");
		String password = httpRequest.getParameter("password");
		
		// Check for abnormal lengths (buffer overflow attack)
		
		if(greaterThanLength("startDate", "start_date", startDate, 10) 
		   || greaterThanLength("endDate", "end_date", endDate, 10)
		   || greaterThanLength("campaignUrn", "campaign_urn", campaignUrn, 250)
		   || greaterThanLength("client", "client",client, 250)
		   || greaterThanLength("owner", "owner", owner, 15)
		   || greaterThanLength("user", "user", user, 15)
		   || greaterThanLength("password", "password", password, 100)
		   || greaterThanLength("id", "id", id, 250)) {
			
			_logger.warn("found an input parameter that exceeds its allowed length");
			return false;
		}
		
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
		
		return true;
	}
}
