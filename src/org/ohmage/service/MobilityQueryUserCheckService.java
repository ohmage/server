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
import org.ohmage.dao.Dao;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * @author selsky
 */
public class MobilityQueryUserCheckService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(MobilityQueryUserCheckService.class);
	private Dao _loggedInUserAdminCampaignsDao;
	private Dao _queryUserCampaignDao;
	
	public MobilityQueryUserCheckService(AwRequestAnnotator annotator, Dao loggedInUserAdminCampaignsDao, Dao queryUserCampaignDao) {
		super(annotator);
		if(null == loggedInUserAdminCampaignsDao) {
			throw new IllegalArgumentException("loggedInUserCampaignDao cannot be null");
		}
		if(null == queryUserCampaignDao) {
			throw new IllegalArgumentException("queryUserCampaignDao cannot be null");
		}
		
		_loggedInUserAdminCampaignsDao = loggedInUserAdminCampaignsDao;
		_queryUserCampaignDao = queryUserCampaignDao;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		
		if(! awRequest.getUser().getUserName().equals(awRequest.getUserNameRequestParam())) { // only check the access rule if the 
                                                                                              // logged-in user and the query user  
                                                                                              // are different
			
			// find all of the campaigns the logged-in user belongs to where the user is also an admin or a researcher
			_loggedInUserAdminCampaignsDao.execute(awRequest);
			
			@SuppressWarnings("unchecked")
			List<String> queryResults = (List<String>) awRequest.getResultList();
			
			// find all of the campaigns the user in the query parameters belongs to
			_queryUserCampaignDao.execute(awRequest);
			
			for(String campaignName : queryResults) {
				if(awRequest.getResultList().contains(campaignName)) {
					return;
				}
			}
			
			_logger.warn("logged-in user and user specified in the query do not share membership in any campaigns. logged-in user: "
				+ awRequest.getUser().getUserName() + " query user: " + awRequest.getUserNameRequestParam()
			);
			getAnnotator().annotate(awRequest, "user attempt to query a campaign they do not belong to");
		}
	}
}
