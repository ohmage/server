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

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.Campaign;
import org.ohmage.domain.UserRoleCampaignResult;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service populating User objects with Campaign and User Role information.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class UserRoleCampaignPopulationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleCampaignPopulationService.class);
	
	public UserRoleCampaignPopulationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Adding campaign roles to user object.");
		
		try {
			
			getDao().execute(awRequest);
			
			ListIterator<?> iter = awRequest.getResultList().listIterator();
			
			while(iter.hasNext()) {
				UserRoleCampaignResult currResult = (UserRoleCampaignResult) iter.next();
				Campaign campaign = new Campaign();
				campaign.setCampaignCreationTimestamp(currResult.getCampaignCreationTimestamp());
				campaign.setDescription(currResult.getCampaignDescription());
				campaign.setName(currResult.getCampaignName());
				campaign.setPrivacyState(currResult.getCampaignPrivacyState());
				campaign.setRunningState(currResult.getCampaignRunningState());
				campaign.setUrn(currResult.getCampaignUrn());
				
				awRequest.getUser().addCampaignRole(campaign, currResult.getUserRole());
			}
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
