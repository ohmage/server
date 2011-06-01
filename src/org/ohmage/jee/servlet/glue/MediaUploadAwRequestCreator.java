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

import org.apache.log4j.NDC;
import org.ohmage.request.AwRequest;


/** 
 * @author selsky
 */
public class MediaUploadAwRequestCreator implements AwRequestCreator {
//	private static Logger _logger = Logger.getLogger(MediaUploadAwRequestCreator.class);
	
	/**
	 * Default no-arg constructor. Simply creates an instance of this class.
	 */
	public MediaUploadAwRequestCreator() {
		
	}
	
	/**
	 * For media upload, the AwRequest object is created during the initial validation in order to avoid multiple parses of the 
	 * inbound data out of the HttpServletRequest. Here the AwRequest is simply retrieved out of the HttpServletRequest.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		AwRequest awRequest = (AwRequest) request.getAttribute("awRequest");
		
		if(null == awRequest) {
			throw new IllegalStateException("missing AwRequest in HttpServletRequest - did the validation process not run?");
		}
		
		String sessionId = request.getSession(false).getId(); // for upload logging to connect app logs to upload logs
		String client = awRequest.getClient();
		
		awRequest.setSessionId(sessionId);
		
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

