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

import org.apache.log4j.Logger;
import org.ohmage.domain.Campaign;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MediaUploadAwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service for validating the campaign_creation_timestamp sent with certain upload requests. In rare cases, a campaign may be
 * removed from the system and replaced with a newer version that uses the same URN. (The campaign may only be removed if it
 * has zero uploads attached to it.) This validation makes sure that the client attempting to upload is using the campaign with
 * the same creation timestamp as the one stored in the db.
 * 
 * @author Joshua Selsky
 */
public class CampaignCreationTimeValidationService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignCreationTimeValidationService.class);
	
	public CampaignCreationTimeValidationService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("checking the campaign_creation_time in the AwRequest against the campaign's creation time");
		
		Campaign campaign = awRequest.getUser().getCampaignUserRoleMap().get(awRequest.getCampaignUrn()).getCampaign();
		
		// FIXME
		
		if(awRequest instanceof SurveyUploadAwRequest) {
			
			if(! campaign.getCampaignCreationTimestamp().equals(((SurveyUploadAwRequest) awRequest).getCampaignCreationTimestamp())) {
				getAnnotator().annotate(awRequest, "campaign " + ((SurveyUploadAwRequest) awRequest).getCampaignUrn() + " was sent with a campaign " +
					"creation timestamp that does not match what is stored on the server");
			}
			
		} else if (awRequest instanceof MediaUploadAwRequest) {
			
			if(! campaign.getCampaignCreationTimestamp().equals(((MediaUploadAwRequest) awRequest).getCampaignCreationTimestamp())) {
				getAnnotator().annotate(awRequest, "campaign " + ((MediaUploadAwRequest) awRequest).getCampaignUrn() + " was sent with a campaign " +
					"creation timestamp that does not match what is stored on the server");
			}
			
		} else {
			
			throw new ServiceException("found an unsupported AwRequest subclass: " + awRequest.getClass().getName());
		}
	}
}
