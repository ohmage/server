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
package org.ohmage.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.SurveyResponseReadResult;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * Filters survey responses based on the privacy_state of the response and the currently logged-in user's role(s). 
 * 
 * Authors or analysts cannot view private survey responses.
 * Participants cannot view invisible data (the special case for the CHIPTS study).
 * 
 * @author Joshua Selsky
 */
public class SurveyResponsePrivacyFilterService implements Service {
	private static Logger _logger = Logger.getLogger(SurveyResponsePrivacyFilterService.class);
	
	/**
	 * Retrieves the result list from the awRequest and removes results if they are not viewable by the currently logged-in 
	 * user.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("Filtering survey response results according to our privacy rules.");
		
		List<?> results = awRequest.getResultList();
		
		if(! results.isEmpty()) {
			
			int numberOfResults = results.size();
			
			User user = awRequest.getUser();
			String campaignUrn = awRequest.getCampaignUrn();
			// Now filter based on the privacy_state the logged-in user selected 
			SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
			
			if(! user.isSupervisorInCampaign(campaignUrn)) { // supervisors can read all data, all the time
					
				for(int i = 0; i < numberOfResults; i++) {
					
					SurveyResponseReadResult currentResult = (SurveyResponseReadResult) results.get(i);
					
					_logger.info(currentResult.getPrivacyState());
					
					if( (resultIsUnshared(currentResult) && ! currentResult.getUsername().equals(req.getUser().getUserName())) 
						|| 
						(user.getCampaignUserRoleMap().get(campaignUrn).getCampaign().getPrivacyState().equals(CampaignPrivacyStateCache.PRIVACY_STATE_PRIVATE) 
							&& currentResult.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_SHARED) 
							&& ! user.isAuthorInCampaign(campaignUrn) 
							&& ! currentResult.getUsername().equals(req.getUser().getUserName()))) {
						
						results.remove(i);
						i--;
						numberOfResults--;
					}
				}
			}
			
			if(null != req.getPrivacyState()) {
				for(int i = 0; i < numberOfResults; i++) {
					if(! ((SurveyResponseReadResult) results.get(i)).getPrivacyState().equals(req.getPrivacyState())) { 
						results.remove(i);
						i--;
						numberOfResults--;
					}
				}
			}
		}
		
		_logger.info(results.size() + " results after filtering");
	}
	
	private boolean resultIsUnshared(SurveyResponseReadResult result) {
		return result.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_PRIVATE) 
			|| result.getPrivacyState().equals(SurveyResponsePrivacyStateCache.PRIVACY_STATE_INVISIBLE); 
	}
}
