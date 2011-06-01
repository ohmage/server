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
import org.ohmage.request.DataPointFunctionQueryAwRequest;
import org.ohmage.request.MediaQueryAwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.request.UserStatsQueryAwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service that verifies that the each user in the user list ("new" data point API) belongs to the campaign in the query. 
 * 
 * @author selsky
 */
public class CampaignUserCheckService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignUserCheckService.class);
	
	/**
	 * The provided DAO must push a list of campaign names into AwRequest.setResultList. 
	 */
	public CampaignUserCheckService(Dao dao, AwRequestAnnotator annotator) {
		super(dao, annotator);
	}
	
	/**
	 * Verifies that the query campaign name is present in each query user's campaign list.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating that each user in the user list belongs to the campaign specified in the query");
		
		// FIXME: Hackalicious!
		String userListString;
		List<String> users;
		if(awRequest instanceof SurveyResponseReadAwRequest) {
			SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
			userListString = req.getUserListString();
			users = req.getUserList();
			
			if(! "urn:ohmage:special:all".equals(userListString)) {
				
				for(String user : users) {
					
					req.setCurrentUser(user);
					
					getDao().execute(awRequest);
					
					List<?> results = awRequest.getResultList();
					
					if(! results.contains(awRequest.getCampaignUrn())) {
						_logger.warn("invalid campaign name in request: the query user does not belong to the campaign in the query.");
						getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
					}
				}
			}
		}
		else if(awRequest instanceof UserStatsQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("Invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else if(awRequest instanceof DataPointFunctionQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("Invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else if(awRequest instanceof MediaQueryAwRequest) {
			getDao().execute(awRequest);
			
			List<?> results = awRequest.getResultList();
			
			if(! results.contains(awRequest.getCampaignUrn())) {
				_logger.warn("Invalid campaign name in request: the query user does not belong to the campaign in the query.");
				getAnnotator().annotate(awRequest, "the query user does not belong to the campaign in the query");
			}
		}
		else {
			awRequest.setFailedRequest(true);
			throw new ServiceException("Invalid request for CampaignUserCheckService.");
		}
	}
}
