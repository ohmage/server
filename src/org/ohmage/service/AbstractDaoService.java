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

/**
 * Provides hooks for subclasses needing access to a DAO.
 * 
 * @author selsky
 */
public abstract class AbstractDaoService implements Service {
	private Dao _dao;
	
	/**
	 * Creates an instance of this class using the provided DAO.
	 * 
	 * @throws IllegalArgumentException if the provided DAO is null
	 */
	public AbstractDaoService(Dao dao) {
		if(null == dao) {
			throw new IllegalArgumentException("a DAO is required");
		}
		_dao = dao;
	}
	
	protected Dao getDao() {
		return _dao;
	}
}
