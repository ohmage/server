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

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.ClassDescription;
import org.ohmage.domain.UserRoleClassResult;
import org.ohmage.request.AwRequest;


/**
 * Populates the user object of the currently logged in user with all of the
 * classes the belong to and their roles in those classes.
 * 
 * @author John Jenkins
 */
public class UserRoleClassPopulationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(UserRoleClassPopulationService.class);
	
	/**
	 * Creates this service with a DAO to get the information from the
	 * database.
	 * 
	 * @param dao The DAO to use to retrieve the information from the database.
	 */
	public UserRoleClassPopulationService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Runs the DAO which returns a list of classes and the roles that the
	 * currently logged in user has in those classes.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Adding classes and roles to the user object.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.getResultList().isEmpty()) {
				_logger.info("The user doesn't belong to any classes.");
				return;
			}
			
			ListIterator<?> iter = awRequest.getResultList().listIterator();
			
			while(iter.hasNext()) {
				UserRoleClassResult currResult = (UserRoleClassResult) iter.next();
				
				ClassDescription currClass = new ClassDescription(currResult.getUrn(), currResult.getName(), currResult.getDescription());
				awRequest.getUser().addClassRole(currClass, currResult.getRole());
			}
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}

}
