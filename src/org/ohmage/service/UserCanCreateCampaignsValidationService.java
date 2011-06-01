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
import org.ohmage.request.AwRequest;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Checks that the user has sufficient permissions to create a new campaign.
 * 
 * @author John Jenkins
 */
public class UserCanCreateCampaignsValidationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserCanCreateCampaignsValidationService.class);
	
	/**
	 * Creates a validation service to check if the user has sufficient
	 * permissions to create a campaign.
	 * 
	 * @param annotator The annotator to respond with if the user doesn't have
	 * 					sufficient permissions.
	 * 
	 * @param dao The DAO to run our queries against.
	 */
	public UserCanCreateCampaignsValidationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Executes the DAO and throws a ServiceException if the user doesn't have
	 * sufficient permissions.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating whether or not the user is allowed to create campaigns.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "User doesn't have sufficient permissions to create a campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
