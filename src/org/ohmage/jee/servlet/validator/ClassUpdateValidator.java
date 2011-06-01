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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;


/**
 * Validates an incoming class update HTTP request.
 * 
 * @author John Jenkins
 */
public class ClassUpdateValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(ClassUpdateValidator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassUpdateValidator() {
		// Do nothing.
	}
	
	/**
	 * Validates that the required parameters exist and that at least one
	 * other parameter exists as well.
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) {		
		String token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String classUrn = httpRequest.getParameter(InputKeys.CLASS_URN);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(token)) {
			// Don't log this to avoid flooding the logs when an attack occurs.
			return false;
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(classUrn)) {
			return false;
		}
		else if(client == null) {
			return false;
		}
		
		if(greaterThanLength(InputKeys.AUTH_TOKEN, InputKeys.AUTH_TOKEN, token, 36)) {
			_logger.warn(InputKeys.AUTH_TOKEN + " is too long.");
			return false;
		}
		else if(greaterThanLength(InputKeys.CLASS_URN, InputKeys.CLASS_URN, classUrn, 255)) {
			_logger.warn(InputKeys.CLASS_URN + " is too long.");
			return false;
		}
		
		// Ensure that it contains at least one other valid parameter.
		Iterator<?> keys = httpRequest.getParameterMap().keySet().iterator();
		while(keys.hasNext()) {
			String currKey = (String) keys.next();
			if((! InputKeys.AUTH_TOKEN.equals(currKey)) &&
			   (! InputKeys.CLASS_URN.equals(currKey)) &&
			   (! InputKeys.CLASS_NAME.equals(currKey)) &&
			   (! InputKeys.DESCRIPTION.equals(currKey)) &&
			   (! InputKeys.USER_LIST_ADD.equals(currKey)) &&
			   (! InputKeys.USER_LIST_REMOVE.equals(currKey)) &&
			   (! InputKeys.PRIVILEGED_USER_LIST_ADD.equals(currKey))) {
				_logger.warn("Unknown parameter found in request: " + currKey);
				return false;
			}
		}
		
		return true;
	}
}
