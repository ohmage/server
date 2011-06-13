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
import org.ohmage.request.ClassUpdateAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Creates the internal request used to update a class.
 * 
 * @author John Jenkins
 */
public class ClassUpdateAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(ClassUpdateAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public ClassUpdateAwRequestCreator() {
		// Does nothing.
	}
	
	/**
	 * 
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating a new internal request object for updating a class.");
		
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		String classUrn = httpRequest.getParameter(InputKeys.CLASS_URN);
		String name = httpRequest.getParameter(InputKeys.CLASS_NAME);
		String description = httpRequest.getParameter(InputKeys.DESCRIPTION);
		String userListAdd = httpRequest.getParameter(InputKeys.USER_LIST_ADD);
		String userListRemove = httpRequest.getParameter(InputKeys.USER_LIST_REMOVE);
		String privilegedUserListAdd = httpRequest.getParameter(InputKeys.PRIVILEGED_USER_LIST_ADD);
		
		ClassUpdateAwRequest awRequest = new ClassUpdateAwRequest(classUrn, name, description, userListAdd, userListRemove, privilegedUserListAdd);
		awRequest.setUserToken(token);
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return awRequest;
	}

}
