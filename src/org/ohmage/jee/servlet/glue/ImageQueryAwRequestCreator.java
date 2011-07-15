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
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.MediaQueryAwRequest;
import org.ohmage.util.CookieUtils;


/**
 * Builds an AwRequest for the image query API feature.
 * 
 * @author selsky
 */
public class ImageQueryAwRequestCreator implements AwRequestCreator {
	
	public ImageQueryAwRequestCreator() {
		
	}
	
	public AwRequest createFrom(HttpServletRequest httpRequest) {
		String token;
		try {
			token = CookieUtils.getCookieValue(httpRequest.getCookies(), InputKeys.AUTH_TOKEN).get(0);
		}
		catch(IndexOutOfBoundsException e) {
			token = httpRequest.getParameter(InputKeys.AUTH_TOKEN);
		}
		String userNameRequestParam = httpRequest.getParameter("owner");
		String client = httpRequest.getParameter("client");
		String campaignUrn = httpRequest.getParameter("campaign_urn");
		String imageId = httpRequest.getParameter("id");
		String size = httpRequest.getParameter("size");
		String username = httpRequest.getParameter("user");
		String password = httpRequest.getParameter("password");
		
		MediaQueryAwRequest awRequest = new MediaQueryAwRequest();

		awRequest.setUserNameRequestParam(userNameRequestParam);
		awRequest.setUserToken(token);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setMediaId(imageId);
		
		User user = new User();
		user.setUserName(username);
		user.setPassword(password);
		awRequest.setUser(user);
		
		awRequest.addToValidate(InputKeys.IMAGE_QUERY_ID, imageId, true);
		awRequest.addToValidate(InputKeys.IMAGE_QUERY_SIZE, size, true);
		awRequest.addToValidate(InputKeys.USER, userNameRequestParam, true);
		
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                      // it will be in every log message for the thread
		
        awRequest.addToValidate(InputKeys.USER, userNameRequestParam, true);
        
		return awRequest;
	}
}
