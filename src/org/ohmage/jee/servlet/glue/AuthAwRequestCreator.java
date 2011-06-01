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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;
import org.ohmage.domain.User;
import org.ohmage.request.AuthRequest;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * @author selsky
 */
public class AuthAwRequestCreator implements AwRequestCreator {

	public AuthAwRequestCreator() {
		
	}
	
	/**
	 * Pushes the client HTTP param into the Log4J NDC and creates an AwRequest with the login information.
	 */
	public AwRequest createFrom(HttpServletRequest request) {
		
		String userName = request.getParameter(InputKeys.USER);
		String password = null; 
		try {
			
			password = URLDecoder.decode(request.getParameter(InputKeys.PASSWORD), "UTF-8");
		
		} catch(UnsupportedEncodingException uee) { // if UTF-8 is not recognized we have big problems
		
			throw new IllegalStateException(uee);
		}
		
		User user = new User();
		user.setUserName(userName);
		user.setPassword(password);
		
		String client = request.getParameter(InputKeys.CLIENT); 
		NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - 
		                              // this means that it will be in every log message for the thread
		
		AwRequest awRequest = new AuthRequest();
		awRequest.setUser(user);
		
		return awRequest;
	}
}
