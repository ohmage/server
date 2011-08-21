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

import java.util.Set;

import javax.sql.DataSource;

/**
 * The superclass for all caches. No direct instance of this ever exists
 * because all subclasses should be either abstracted (but not abstract)
 * the same as this one, or should be concrete Singletons.
 * 
 * @author John Jenkins
 */
public abstract class Cache {
	/**
	 * The minimum allowed frequency of cache refreshes with the database.
	 */
	public static final long MIN_CACHE_REFRESH_MILLIS = 1000;
	
	// The DataSource to use when querying the database.
	private DataSource dataSource;
	
	// The last time we refreshed our cache in milliseconds since epoch.
	private long lastUpdateTimestamp;
	// The number of milliseconds between refreshes of the local cache.
	private long updateFrequency;
	
	/**
	 * Default constructor made protected so children can call it, but it is
	 * still a Singleton.
	 * 
	 * @complexity O(1)
	 */
	protected Cache(DataSource dataSource, long updateFrequency) {
		// This helps us guarantee that the DataSource starts off as null to
		// help assure that it will only be set once.
		this.dataSource = dataSource;
		
		// Initialize the refresh times to "invalid" values such that the
		// first run is guaranteed to refresh itself.
		lastUpdateTimestamp = -1;
		this.updateFrequency = updateFrequency;
	}
	
	/**
	 * Returns the DataSource that is used to query the database for each 
	 * cache's specific values.
	 * 
	 * @return The DataSource that is used to query the database for each
	 * 		   cache's specific values.
	 */
	protected DataSource getDataSource() {
		return dataSource;
	}
	
	/**
	 * Returns the milliseconds since epoch of the last update.
	 * 
	 * @return The milliseconds since epoch of the last update.
	 */
	protected long getLastUpdateTimestamp() {
		return lastUpdateTimestamp;
	}
	
	/**
	 * Updates the milliseconds since epoch of the most recent update unless 
	 * this value is less than the current value in which case this call is
	 * ignored.
	 * 
	 * @param timestamp The number of milliseconds since epoch when the most
	 * 					recent update took place.
	 */
	protected void setLastUpdateTimestamp(long timestamp) {
		if(timestamp > lastUpdateTimestamp) {
			lastUpdateTimestamp = timestamp;
		}
	}
	
	/**
	 * Returns the minimum number of milliseconds between each cache update.
	 * 
	 * @return The minimum number of milliseconds between each cache update.
	 */
	protected long getUpdateFrequency() {
		return updateFrequency;
	}
	
	/**
	 * Gets the known keys for the cache.
	 * 
	 * @return Returns a Set of all their known keys and, if no keys are
	 * 		   known, should return an empty Set.
	 */
	public abstract Set<String> getKeys();
	
	/**
	 * Returns a human-readable name for this cache.
	 * 
	 * @return Returns a human-readable name for this cache.
	 */
	public abstract String getName();
}
