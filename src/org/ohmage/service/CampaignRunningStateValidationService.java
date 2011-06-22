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

import org.ohmage.dao.Dao;
import org.ohmage.request.AwRequest;
import org.ohmage.request.MediaUploadAwRequest;
import org.ohmage.request.SurveyUploadAwRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Configurable campaign state validation to be used in various service workflows. Any flow where the campaign state needs to be 
 * checked against a specific state will find this class useful. The AwRequest will be marked as failed if the running_state of the 
 * campaign_urn does not match the running state provided upon construction. 
 * 
 * @author Joshua Selsky
 */
public class CampaignRunningStateValidationService extends AbstractAnnotatingDaoService {
	//private static Logger _logger = Logger.getLogger(CampaignRunningStateValidationService.class);
	private String _allowedState;
	
	public CampaignRunningStateValidationService(AwRequestAnnotator annotator, Dao dao, String allowedState) {
		super(dao, annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(allowedState)) {
			throw new IllegalArgumentException("an allowedState is required");
		}
		_allowedState = allowedState;
	}
	
	@Override
	// TODO -- fix this to avoid having to use the DAO - can it be made to just look at the CampaignUserRoleMap in the User object
	public void execute(AwRequest awRequest) {
		getDao().execute(awRequest);
		
		// hack for now until toProcess is utilized
		
		if(awRequest instanceof SurveyUploadAwRequest) {
			SurveyUploadAwRequest req = (SurveyUploadAwRequest) awRequest;
			if(! req.getCampaignRunningState().equals(_allowedState)) {
				getAnnotator().annotate(awRequest, "campaign " + awRequest.getCampaignUrn() + " is not " + _allowedState);
			}
		} 
		else if (awRequest instanceof MediaUploadAwRequest) {
			MediaUploadAwRequest req = (MediaUploadAwRequest) awRequest;
			if(! req.getCampaignRunningState().equals(_allowedState)) {
				getAnnotator().annotate(awRequest, "campaign " + awRequest.getCampaignUrn() + " is not " + _allowedState);
			}
		}
		else {
			throw new IllegalStateException("This request type unsupported: " + awRequest.getClass().getName());
			
		}
	}
}
