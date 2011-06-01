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
package org.ohmage.dao;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Provides base classes with access to a JDBC DataSource and Spring JdbcTemplate.
 * 
 * @author selsky
 */
public abstract class AbstractDao implements Dao {	
	private DataSource _dataSource;
	private JdbcTemplate _jdbcTemplate;
    
	/**
	 * Initializes a JdbcTempalte using the provided DataSource.
	 * 
	 * @throws IllegalArgumentException if the provided DataSource is null.
	 */
	public AbstractDao(DataSource dataSource) {
		if(null == dataSource) {
			throw new IllegalArgumentException("a non-null DataSource is required");
		}
		
		_dataSource = dataSource;
		_jdbcTemplate = new JdbcTemplate(_dataSource);
	}
	
	protected DataSource getDataSource() {
		return _dataSource;
	}
	
	protected JdbcTemplate getJdbcTemplate() {
		return _jdbcTemplate;
	}
}
