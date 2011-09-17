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

/**
 * Singleton cache for the indices and String values for campaign roles.
 * 
 * @author John Jenkins
 */
public final class CampaignRoleCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String ROLE_COLUMN = "role";
	
	// The SQL used to get at the cache's values.
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = 
		"SELECT " + ID_COLUMN + ", " + ROLE_COLUMN + " " +
		"FROM user_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRoleCache";
	
	/**
	 * Known campaign roles.
	 * 
	 * @author John Jenkins
	 */
	public static enum Role {
		SUPERVISOR,
		AUTHOR,
		ANALYST,
		PARTICIPANT;
		
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
		 * Converts the role to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static CampaignRoleCache instance;
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRoleCache(DataSource dataSource, long updateFrequency) {
		super(dataSource, updateFrequency, SQL_GET_CAMPAIGN_ROLES_AND_IDS, ID_COLUMN, ROLE_COLUMN);
		
		instance = this;
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static CampaignRoleCache instance() {
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