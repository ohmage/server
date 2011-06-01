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


/**
 * Validates the list users and roles in the list in this request.
 * 
 * @author John Jenkins
 */
public class UserRoleListValidationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleListValidationService.class);
	
	/**
	 * Creates this service with a DAO.
	 * 
	 * @param dao The DataAccessObject to use to run this service.
	 */
	public UserRoleListValidationService(Dao dao) {
		super(dao);
	}

	/**
	 * Executes the DAO.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating the usernames and roles in the request.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}

}
