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
import org.ohmage.request.MobilityQueryAwRequest;


/**
 * Builds an AwRequest for the mobility data point API feature.
 * 
 * @author selsky
 */
public class MobilityQueryAwRequestCreator implements AwRequestCreator {
	
	public MobilityQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest request) {

		String date = request.getParameter("date");
		String userNameRequestParam = request.getParameter("user");
		String client = request.getParameter("client");
		String authToken = request.getParameter("auth_token");
		  
		
		MobilityQueryAwRequest awRequest = new MobilityQueryAwRequest();
		awRequest.setStartDate(date);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}