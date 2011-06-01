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

import java.util.Set;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Post-authentication service for making sure that the logged-in user has access to the campaign specified in the request params.
 * 
 * @author selsky
 */
public class UserCampaignValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(UserCampaignValidationService.class);
	
	public UserCampaignValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	
	@Override
	public void execute(AwRequest awRequest) {
		// check whether the logged in user has access to the campaign in the query params
		
		Set<String> allowedCampaigns = awRequest.getUser().getCampaignUserRoleMap().keySet();
		
		if(! allowedCampaigns.contains(awRequest.getCampaignUrn())) {
			_logger.warn("user attempting to access a campaign they do not belong to. user: " + 
				awRequest.getUser().getUserName() + " campaign: " + awRequest.getCampaignUrn());
			getAnnotator().annotate(awRequest, "user attempt to access a campaign they do not belong to");
		}
	}
}
