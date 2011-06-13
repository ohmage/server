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
import org.ohmage.request.ClassReadAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;

/**
 * Creates an internal request for reading classes.
 * 
 * @author John Jenkins
 */
public class ClassReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ClassReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassReadAwRequestCreator() {
		// Do nothing.
	}

	/**
	 * Creates a request object based on the parameters from the HTTP request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating class read request.");
		
		ClassReadAwRequest internalRequest = new ClassReadAwRequest(httpRequest.getParameter(InputKeys.CLASS_URN_LIST));
		internalRequest.setUserToken(CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0));
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return internalRequest;
	}
}