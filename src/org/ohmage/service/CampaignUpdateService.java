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
 * Checks that the user has sufficient roles to modify the campaign and then
 * makes all the changes to the campaign.
 * 
 * @author John Jenkins
 */
public class CampaignUpdateService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(CampaignUpdateService.class);
	
	/**
	 * Default constructor.
	 * 
	 * @param annotator What to be returned to the user if the service fails.
	 * 
	 * @param dao The DAO to call to run the service.
	 */
	public CampaignUpdateService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Calls the DAO and annotates if there are any errors.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to update the campaign.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The user has insufficient permissions to modify this campaign.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
