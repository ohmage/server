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
 * Checks if the currently logged in user is a privileged user in the only
 * class in a request.
 * 
 * @author John Jenkins
 */
public class UserIsPrivilegedInClassService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserIsPrivilegedInClassService.class);
	
	/**
	 * Sets up the annotator and DAO for this service.
	 * 
	 * @param annotator The verbage to annotate back to the user if they are
	 * 					not a privileged user in this class.
	 * 
	 * @param dao The DataAccessObject that will be called to run the query
	 * 			  against the database.
	 */
	public UserIsPrivilegedInClassService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	/**
	 * Calls the DAO to execute the query.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Checking if the user is a privileged user in the class.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "The user is not a privileged user in the class.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
