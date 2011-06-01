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
 * Checks that all the users in a list exist. This is done in the DAO and all
 * this class does is call the DAO it was built with.
 * 
 * @author John Jenkins
 */
public class UsersInListExistService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UsersInListExistService.class);
	
	/**
	 * Builds the service with the annotator and DAO given.
	 * 
	 * @param annotator What to annotate back to the user if a user in the
	 * 					list does not exist.
	 * 
	 * @param dao The DAO that will be run.
	 */
	public UsersInListExistService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Runs the DAO and reports back if there is an issue.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checks that all the users in a list exist.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "A user in the list does not exist.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
