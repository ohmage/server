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
import org.ohmage.util.StringUtils;


/**
 * Basic validation for a document read request.
 * 
 * @author John Jenkins
 */
public class DocumentReadValidator extends AbstractHttpServletRequestValidator {
	private static Logger _logger = Logger.getLogger(DocumentReadValidator.class);
	
	/**
	 * Default constructor.
	 */
	public DocumentReadValidator() {
		// Do nothing.
	}

	/**
	 * Validates that the required parameters are present and that their sizes
	 * are rational.
	 * 
	 * @throws MissingAuthTokenException Thrown if the authentication / session
	 * 									 token is missing or invalid. 
	 */
	@Override
	public boolean validate(HttpServletRequest httpRequest) throws MissingAuthTokenException {
		String personalDocuments = httpRequest.getParameter(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS);
		String client = httpRequest.getParameter(InputKeys.CLIENT);
		
		if((personalDocuments == null) || StringUtils.isEmptyOrWhitespaceOnly(personalDocuments)) {
			_logger.warn("The personal documents boolean is missing or empty.");
			return false;
		}
		else if((client == null) || (greaterThanLength(InputKeys.CLIENT, InputKeys.CLIENT, client, 255))) {
			_logger.warn("The client is missing or too long.");
			return false;
		}
		
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
		
		return true;
	}
}