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
import org.ohmage.request.SurveyUploadAwRequest;


/**
 * Transformer for creating an AwRequest for the upload feature.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequestCreator implements AwRequestCreator {
  //private static Logger _logger = Logger.getLogger(SurveyUploadAwRequestCreator.class);
    
    /**
     * Default no-arg constructor. Simply creates an instance of this class.
     */
    public SurveyUploadAwRequestCreator() {
        
    }
    
    /**
     * Creates an AwRequest from the validatedParamterMap found in the HttpServletRequest.
     */
    public AwRequest createFrom(HttpServletRequest request) {
    	@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = (Map<String, String[]>) request.getAttribute("validatedParameterMap");
		
        String sessionId = request.getSession(false).getId(); // for upload logging to connect app logs to uploads
        
        String userName = parameterMap.get("user")[0];
        String campaignUrn = parameterMap.get("campaign_urn")[0];
        String password = parameterMap.get("password")[0];
        String client = parameterMap.get("client")[0];
        String jsonData = parameterMap.get("data")[0];
        String campaignCreationTimestamp = parameterMap.get("campaign_creation_timestamp")[0]; 
        
        User user = new User();
        user.setUserName(userName);
        user.setPassword(password);
                
        SurveyUploadAwRequest awRequest = new SurveyUploadAwRequest();

        awRequest.setStartTime(System.currentTimeMillis());
        awRequest.setSessionId(sessionId);
        awRequest.setUser(user);
        awRequest.setClient(client);
        awRequest.setJsonDataAsString(jsonData);
        awRequest.setCampaignUrn(campaignUrn);
        awRequest.setCampaignCreationTimestamp(campaignCreationTimestamp);

        String requestUrl = request.getRequestURL().toString();
        if(null != request.getQueryString()) {
            requestUrl += "?" + request.getQueryString(); 
        }
        
        awRequest.setRequestUrl(requestUrl); // output in reponse in case of error, logged to filesystem
        
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                      // it will be in every log message for the thread
        
        return awRequest;
    }
}

