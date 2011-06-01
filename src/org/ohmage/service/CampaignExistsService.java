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
 * Checks to ensure that the campaign in the request exists.
 * 
 * @author John Jenkins
 */
public class CampaignExistsService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignExistsService.class);
	
	/**
	 * Builds a validation service to check if a campaign exists.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public CampaignExistsService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Checks that the campaign in question actually exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking whether the campaign exists.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The campaign in question does not exist.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
