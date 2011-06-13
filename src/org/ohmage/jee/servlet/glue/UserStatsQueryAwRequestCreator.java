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

import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserStatsQueryAwRequest;
import org.ohmage.util.CookieUtils;


/**
 * Builds an AwRequest for the user stats API feature.
 * 
 * @author selsky
 */
public class UserStatsQueryAwRequestCreator implements AwRequestCreator {
	
	public UserStatsQueryAwRequestCreator() {
		
	}
	
	/**
	 * 
	 */
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		String userNameRequestParam = httpRequest.getParameter("user");
		String client = httpRequest.getParameter("client");
		String campaignName = httpRequest.getParameter("campaign_urn");
		String token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		
		UserStatsQueryAwRequest awRequest = new UserStatsQueryAwRequest();
		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(token);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignName);
		
		return awRequest;
	}
}
