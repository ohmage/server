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
	protected DataSource dataSource;
	
	// The last time we refreshed our cache in milliseconds since epoch.
	protected long lastUpdateTimestamp;
	// The number of milliseconds between refreshes of the local cache.
	protected long updateFrequency;
	
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
	 * Sets the DataSource for this object. This can only be called once by
	 * Spring on startup and subsequent calls will cause an 
	 * IllegalArgumentException exception to be thrown.
	 * 
	 * @param dataSource The DataSource for its children to use when querying
	 * 					 the database.
	 * 
	 * @throws IllegalArgumentException Thrown if the DataSource has not yet
	 * 									been set and Spring has passed in a
	 * 									null value or if the DataSource has
	 * 									been set and it is attempting to be
	 * 									reset.
	 *
	public synchronized void setDataSource(DataSource dataSource) {
		if(this.dataSource != null) {
			throw new IllegalStateException("The DataSource may only be set once.");
		}
		if(dataSource == null) {
			throw new IllegalArgumentException("The DataSource may not be null.");
		}
		
		this.dataSource = dataSource;
	}*/
	
	/**
	 * Sets the initial update frequency for this object. This may be reset by
	 * anyone at a later date to dynamically increase cache fidelity or to
	 * lessen the load on the database.
	 * 
	 * @param frequencyInMilliseconds The frequency that updates should be
	 * 								  checked for in milliseconds. The system
	 * 								  will still only do updates when a 
	 * 								  request is being made and the cache has
	 * 								  expired to prevent unnecessary checks to
	 * 								  the database.
	 * 
	 * @throws IllegalArgumentException Thrown if the 
	 * 									'frequencyInMilliseconds' is less than
	 * 									the minimum allowed frequency, 
	 * 									{@value #MIN_CACHE_REFRESH_MILLIS}.
	 * 
	 * @see #MIN_CACHE_REFRESH_MILLIS
	 *
	public synchronized void setUpdateFrequency(long frequencyInMilliseconds) {
		if(frequencyInMilliseconds < MIN_CACHE_REFRESH_MILLIS) {
			throw new IllegalArgumentException("The update frequency must be a positive integer greater than or equal to " +
					MIN_CACHE_REFRESH_MILLIS + " milliseconds.");
		}
		
		updateFrequency = frequencyInMilliseconds;
	}*/
	
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
