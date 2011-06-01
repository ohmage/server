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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.request.UploadAwRequest;


/**
 * Transformer for creating an AwRequest for the upload feature.
 * 
 * @author selsky
 */
public class MobilityUploadAwRequestCreator implements AwRequestCreator {
	// private static Logger _logger = Logger.getLogger(MobilityUploadAwRequestCreator.class);
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public MobilityUploadAwRequestCreator() {
		
	}
	
	/**
	 *  Pulls the user, password, client, and data parameters out of the HttpServletRequest
	 *  and places them in a new AwRequest. Assumes the parameters in the HttpServletRequest exist.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = (Map<String, String[]>) request.getAttribute("validatedParameterMap");
		
		String sessionId = request.getSession(false).getId(); // for connecting app logs to upload logs
		
		String userName = parameterMap.get("user")[0];
		String password = parameterMap.get("password")[0]; 
		String client = parameterMap.get("client")[0];
		String jsonData = parameterMap.get("data")[0];
		
		User user = new User();
		user.setUserName(userName);
		user.setPassword(password);
		
		AwRequest awRequest = new UploadAwRequest();

		awRequest.setStartTime(System.currentTimeMillis());
		awRequest.setSessionId(sessionId);
		awRequest.setUser(user);
		awRequest.setClient(client);
		awRequest.setJsonDataAsString(jsonData);
		
		String requestUrl = request.getRequestURL().toString();
		if(null != request.getQueryString()) {
			requestUrl += "?" + request.getQueryString(); 
		}
		
		awRequest.setRequestUrl(requestUrl); // placed in the request for use in logging messages
		
		NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that it 
                                      // will be in every log message for the thread
		
		return awRequest;
	}
}

