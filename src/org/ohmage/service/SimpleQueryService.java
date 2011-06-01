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
 * Service that dispatches directly to a DAO without performing any pre- or post-processing.
 * 
 * @author Joshua Selsky
 */
public class SimpleQueryService extends AbstractDaoService {
	
	/**
	 * Basic constructor.
	 * 
	 * @param dao the DAO to be used for querying.
	 */
    public SimpleQueryService(Dao dao) {
    	super(dao);
    }
	
    /**
     * Simply dispatches to the DAO.
     */
	public void execute(AwRequest awRequest) {
		try {
		
			getDao().execute(awRequest);
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
