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

import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;


/**
 * Service a data upload; dispatch to a DAO for persistence.
 * 
 * This class is completely redundant with SimpleQueryService.
 * 
 * @author selsky
 */
public class DaoService implements Service {
	private Dao _dao;
	
	/**
	 * @throws IllegalArgumentException if the provided Dao is null
	 */
	public DaoService(Dao dao) {
		if(null == dao) {
			throw new IllegalArgumentException("a non-null DAO is required");			
		}
		_dao = dao;
	}
	
	/**
	 * Simply dispatches to a DAO for request processing.
	 */
	public void execute(AwRequest awRequest) {
		try {
		
			_dao.execute(awRequest);
			
		} catch(DataAccessException dae) {
			
			throw new ServiceException(dae);
		}
	}
}
