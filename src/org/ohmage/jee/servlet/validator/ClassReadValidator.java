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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Validates that the basic, required parameters exist for this request.
 * 
 * @author John Jenkins
 */
public class ClassReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(ClassReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassReadValidator() {
		// Does nothing.
	}
	
	/**
	 * Validates that all required parameters exist and that their size is not
	 * excessive.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is not in the HTTP header.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		// Get the authentication / session token from the header.
		List<String> tokens = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN);
		if(tokens.size() == 0) {
			if(httpRequest.getParameter(InputKeys.AUTH_TOKEN) == null) {
				throw new MissingAuthTokenException("The required authentication / session token is missing.");
			}
		}
		else if(tokens.size() > 1) {
			throw new MissingAuthTokenException("More than one authentication / session token was found in the request.");
		}
		
		String classList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(classList == null) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("The class list is missing in the request.");
			}
			
			return false;
		}
		else if(client == null) {
			if(_logger.isDebugEnabled()) {
				_logger.debug("The client parameter is missing in the request.");
			}
			
			return false;
		}
		
		return true;
	}
}