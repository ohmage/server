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
import org.ohmage.request.UserReadRequest;


/**
 * Creates a user read request.
 * 
 * @author John Jenkins
 */
public class UserReadAwRequestCreator implements AwRequestCreator {
	private static Logger _logger = Logger.getLogger(UserReadAwRequestCreator.class);
	
	/**
	 * Default constructor.
	 */
	public UserReadAwRequestCreator() {
		super();
	}

	/**
	 * Creates a request object for processing a user read request.
	 */
	@Override
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		_logger.info("Creating request for reading user information.");
		
		String authToken = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		String campaignUrnList = httpRequest.getParameter(InputKeys.CAMPAIGN_URN_LIST);
		String classUrnList = httpRequest.getParameter(InputKeys.CLASS_URN_LIST);
		
		UserReadRequest request = new UserReadRequest(campaignUrnList, classUrnList);
		request.setUserToken(authToken);
		
		NDC.push("client=" + httpRequest.getParameter(InputKeys.CLIENT));
		
		return request;
	}
}
