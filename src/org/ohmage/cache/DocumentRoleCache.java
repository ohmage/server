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
	
	/**
	 * Known document roles.
	 * 
	 * @author John Jenkins
	 */
	public static enum Role {
		READER,
		WRITER,
		OWNER;
		
		/**
		 * Converts a String value into a Role or throws an exception if there
		 * is no comparable role.
		 * 
		 * @param role The role to be converted into a Role enum.
		 * 
		 * @return A comparable Role enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									Role enum.
		 */
		public static Role getValue(String role) {
			return valueOf(role.toUpperCase());
		}
		
		/**
		 * Compares this Role with another Role that may be null. The order is
		 * OWNER > WRITER > READER > null. If 'role' is greater than this Role,
		 * 1 is returned. If they are the same, 0 is returned. Otherwise, -1 is
		 * returned.
		 * 
		 * @param role The Role to compare against this Role.
		 * 
		 * @return 1 if 'role' is greater than this role, 0 if they are the 
		 * 		   same, or -1 otherwise.
		 */
		public int compare(Role role) {
			if(this == OWNER) {
				if(role == OWNER) {
					return 0;
				}
			}
			else if(this == WRITER) {
				if(role == OWNER) {
					return 1;
				}
				else if(role == WRITER) {
					return 0;
				}
			}
			else if(this == READER) {
				if((role == OWNER) || (role == WRITER)) {
					return 1;
				}
				else if(role == READER) {
					return 0;
				}
			}
			
			return -1;
		}
		
		/**
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
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
