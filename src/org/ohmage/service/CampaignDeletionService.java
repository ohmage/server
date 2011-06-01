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
 * Service for deleting a campaign.
 * 
 * @author John Jenkins
 */
public class CampaignDeletionService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignDeletionService.class);
	
	/**
	 * Builds a service to delete a campaign.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public CampaignDeletionService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Attempts to delete the campaign.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to delete the campaign.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "This user cannot delete this campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
