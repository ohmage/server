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
package org.ohmage.cache;

import javax.sql.DataSource;

public final class DocumentRoleCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String ROLE_COLUMN = "role";
	
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_DOCUMENT_PRIVACY_STATES_AND_IDS = 
		"SELECT " + ID_COLUMN + ", " + ROLE_COLUMN + " " +
		"FROM document_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which cache we want.
	public static final String CACHE_KEY = "documentRoleCache";
	
	// Known roles for those associated with documents.
	public static final String ROLE_READER = "reader";
	public static final String ROLE_WRITER = "writer";
	public static final String ROLE_OWNER = "owner";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static DocumentRoleCache instance;
	
	/**
	 * Default constructor set to protected to make this a Singleton, but
	 * allow another cache to subclass it despite the likeliness of it.
	 */
	private DocumentRoleCache(DataSource dataSource, long updateFrequency) {
		super(dataSource, updateFrequency, SQL_GET_DOCUMENT_PRIVACY_STATES_AND_IDS, ID_COLUMN, ROLE_COLUMN);
		
		instance = this;
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static DocumentRoleCache instance() {
		return instance;
	}
	
	/**
	 * Returns a human-readable name for this cache.
	 */
	@Override
	public String getName() {
		return CACHE_KEY;
	}
}
