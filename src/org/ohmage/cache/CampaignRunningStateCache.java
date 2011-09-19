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
 * Singleton cache for the indices and String values for campaign running
 * states.
 * 
 * @author John Jenkins
 */
public final class CampaignRunningStateCache extends StringAndIdCache{
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String STATE_COLUMN = "running_state";
	
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS = 
		"SELECT " + ID_COLUMN + ", " + STATE_COLUMN + " " +
		"FROM campaign_running_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRunningStateCache";
	
	/**
	 * Known campaign running states.
	 * 
	 * @author John Jenkins
	 */
	public static enum RunningState {
		RUNNING,
		STOPPED;
		
		/**
		 * Converts a String value into a RunningState or throws an exception 
		 * if there is no comparable running state.
		 * 
		 * @param runningState The running state to be converted into a 
		 * 					   RunningState enum.
		 * 
		 * @return A comparable RunningState enum.
		 * 
		 * @throws IllegalArgumentException Thrown if there is no comparable
		 * 									RunningState enum.
		 */
		public static RunningState getValue(String runningState) {
			return valueOf(runningState.toUpperCase());
		}
		
		/**
		 * Converts the running state to a nice, human-readable format.
		 */
		@Override
		public String toString() {
			return name().toLowerCase();
		}
	}

	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static CampaignRunningStateCache instance;
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRunningStateCache(DataSource dataSource, long updateFrequency) {
		super(dataSource, updateFrequency, SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS, ID_COLUMN, STATE_COLUMN);
		
		instance = this;
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static CampaignRunningStateCache instance() {
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
