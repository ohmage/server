/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
package org.ohmage.query.impl;

import javax.sql.DataSource;

import org.ohmage.exception.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The superclass for all Queries, this class contains the DataSource with which 
 * it was built as well as a JDBC template which is the most common field that
 * subclasses will use to access the database.
 * 
 * All subclasses must be Singletons as subsequent invocations of a constructor
 * will throw an IllegalStateException.
 * 
 * @author John Jenkins
 */
public abstract class Query {
	private boolean initialized = false;
	
	private final DataSource dataSource;
	private final JdbcTemplate jdbcTemplate;
	
	/**
	 * Builds this query object by keeping track of the DataSource that was 
	 * used to create it and creates a JDBC template for subclasses to use when 
	 * querying the database.
	 * 
	 * All subclasses must be Singletons in that all subsequent invocations of
	 * this constructor will throw an IllegalStateException. 
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 * 
	 * @throws IllegalArgumentException Thrown if the 'dataSource' is null.
	 * 
	 * @throws IllegalStateException Thrown if someone attempts to instantiate
	 * 								 this class again. All subclasses must be
	 * 								 Singletons.
	 */
	protected Query(DataSource dataSource) {
		if(dataSource == null) {
			throw new IllegalArgumentException("The data source cannot be null.");
		}
		else if(initialized) {
			throw new IllegalStateException("A Query should be built exactly once by Spring when the server is initialized.");
		}
		
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
		
		initialized = true;
	}
	
	/**
	 * Throws a new org.ohmage.dao.DataAccessException. This is what all query 
	 * objects should use when a generic 
	 * org.springframework.dao.DataAccessException is thrown.
	 * 
	 * @param sql The SQL that caused the 
	 * 			  org.springframework.dao.DataAccessException.
	 * 
	 * @param e The org.springframework.dao.DataAccessException that was 
	 * 			thrown.
	 * 
	 * @param params The parameters that were added to the SQL.
	 * 
	 * @throws DataAccessException The new 
	 * 							   org.springframework.dao.DataAccessException
	 * 							   that encapsulates the SQL, its arguments,
	 * 							   and the 
	 * 							   org.springframework.dao.DataAccessException.
	 */
	protected static void errorExecutingSql(String sql, org.springframework.dao.DataAccessException e, String... params) throws DataAccessException {
		throw new DataAccessException(
				"Error executing SQL '" + sql + "' " +
				"with parameter(s): " + params, e);
	}
	
	/**
	 * Returns the DataSource that was used to create this object.
	 * 
	 * @return The DataSource that was used to create this object.
	 */
	protected DataSource getDataSource() {
		return dataSource;
	}
	
	/**
	 * Returns the JdbcTemplate that should be used by this object to perform 
	 * its queries, updates, and deletes.
	 * 
	 * @return The JdbcTemplate that should be used by this object to perform 
	 *         its queries, updates, and deletes.
	 */
	protected JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
}
