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
import org.ohmage.domain.DataPointFunctionQueryMetadata;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointFunctionQueryAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * Builds an AwRequest for the data point function query API feature. This class is identical to DataPointQueryAwRequestCreator
 * except that it only allows one "i" parameter value.
 * 
 * @author selsky
 */
public class DataPointFunctionQueryAwRequestCreator implements AwRequestCreator {
	private DataPointFunctionQueryMetadata _metadata;
	
	public DataPointFunctionQueryAwRequestCreator(DataPointFunctionQueryMetadata metadata) {
		if(null == metadata || metadata.isEmpty()) {
			throw new IllegalArgumentException("metadata cannot be null or empty");
		}
		_metadata = metadata;
	}
	
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		
		String startDate = httpRequest.getParameter("start_date");
		String endDate = httpRequest.getParameter("end_date");
		String userNameRequestParam = httpRequest.getParameter("user");
		String client = httpRequest.getParameter("client");
		String campaignUrn = httpRequest.getParameter("campaign_urn");
		String functionName = httpRequest.getParameter("id");  
		
		DataPointFunctionQueryAwRequest awRequest = new DataPointFunctionQueryAwRequest();
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(token);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setFunctionName(functionName);
		awRequest.setMetadata(_metadata);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread

		return awRequest;
	}
}
