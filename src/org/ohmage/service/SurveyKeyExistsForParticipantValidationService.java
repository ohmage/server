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
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.User;
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Service that dispatches to a DAO to check whether the currently logged-in user owns the survey represented by the survey key.
 * 
 * @author Joshua Selsky
 */
public class SurveyKeyExistsForParticipantValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(SurveyKeyExistsForParticipantValidationService.class);

	/**
	 * Basic constructor.
	 * 
	 * @param dao the DAO to be used for querying.
	 */
    public SurveyKeyExistsForParticipantValidationService(AwRequestAnnotator annotator, Dao dao) {
    	super(dao, annotator);
    }
	
    /**
     * Dispatches to the DAO only if the logged-in user is a participant (and not also a supervisor).
     */
	public void execute(AwRequest awRequest) {
		User user = awRequest.getUser();
		String campaignUrn = awRequest.getCampaignUrn();
		
		if(user.isParticipantInCampaign(campaignUrn) && ! user.isSupervisorInCampaign(campaignUrn)) {
			
			_logger.info("Checking whether the participant owns the survey represented by the survey key in the request.");
			
			try {
				
				getDao().execute(awRequest);
				
				if(awRequest.isFailedRequest()) {
					getAnnotator().annotate(awRequest, "The logged-in participant user is not the " +
							"owner of the survey he or she is attempting to update.");
				}
				
			} catch (DataAccessException dae) {
				
				throw new ServiceException(dae);
				
			}
		}
	}
}
