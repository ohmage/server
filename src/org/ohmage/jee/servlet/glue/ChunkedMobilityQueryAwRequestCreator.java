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
import org.ohmage.request.InputKeys;
import org.ohmage.request.MobilityQueryAwRequest;
import org.ohmage.util.CookieUtils;


/**
 * Builds an AwRequest for the chunked mobility data point API feature.
 * 
 * @author selsky
 */
public class ChunkedMobilityQueryAwRequestCreator implements AwRequestCreator {
	
	public ChunkedMobilityQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest httpRequest) {

		String startDate = httpRequest.getParameter("start_date");
		String endDate = httpRequest.getParameter("end_date");
		String userNameRequestParam = httpRequest.getParameter("user");
		String client = httpRequest.getParameter("client");
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		  
		MobilityQueryAwRequest awRequest = new MobilityQueryAwRequest();
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(token);
		awRequest.setClient(client);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}
