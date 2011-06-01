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
 * Does basic validation on an incoming user read request.
 * 
 * @author John Jenkins
 */
public class UserReadValidator extends AbstractHttpServletRequestValidator {
	public static Logger _logger = Logger.getLogger(UserReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public UserReadValidator() {
		super();
	}

	/**
	 * Checks that the required parameters exist.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(authToken)) {
			return false;
		}
		else if(authToken.length() != 36) {
			return false;
		}
		else if(client == null) {
			return false;
		}
		
		return true;
	}

}
