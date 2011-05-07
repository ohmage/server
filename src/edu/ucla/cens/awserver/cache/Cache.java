package edu.ucla.cens.awserver.cache;

import javax.sql.DataSource;

/**
 * The superclass for all caches. It sets up the DataSource for all the caches
 * via the configuration files.
 * 
 * @author John Jenkins
 */
public class Cache {
	// The DataSource to use when querying the database.
	protected DataSource _dataSource;
	
	// The last time we refreshed our cache in milliseconds since epoch.
	protected long _lastUpdateTimestamp = -1;
	// The number of milliseconds between refreshes of the local cache.
	protected long _updateFrequency = -1;
	
	/**
	 * Default constructor made protected so children can call it, but no one
	 * else can.
	 */
	protected Cache() {
		// Do nothing.
	}
	
	/**
	 * Sets the DataSource for this object. This can only be called Spring on
	 * startup.
	 * 
	 * @complexity O(1)
	 * 
	 * @param dataSource The DataSource for this object to use when getting
	 * 					 the list of running states and their IDs. Cannot be
	 * 					 null.
	 * 
	 * @throws IllegalArgumentException Thrown if the dataSource has not yet
	 * 									been set and someone has passed in a
	 * 									null value.
	 */
	public synchronized void setDataSource(DataSource dataSource) {
		if(dataSource == null) {
			throw new IllegalArgumentException("A non-null DataSource is required.");
		} 
		
		_dataSource = dataSource;
	}
	
	/**
	 * Sets the initial update frequency for this object. This is only called
	 * by Spring as it is a non-static call and this object can never be
	 * instantiated in code. 
	 * 
	 * @complexity O(1)
	 * 
	 * @param frequencyInMilliseconds The frequency that updates should be
	 * 								  checked for in milliseconds. The system
	 * 								  will still only do updates when a 
	 * 								  request is being made and the cache has
	 * 								  expired to prevent unnecessary, 
	 * 								  premature checks to the database.
	 */
	public synchronized void setUpdateFrequency(long frequencyInMilliseconds) {
		if(frequencyInMilliseconds < 1000) {
			throw new IllegalArgumentException("The update frequency must be a positive integer greater than or equal to 1000 milliseconds.");
		}
		
		_updateFrequency = frequencyInMilliseconds;
	}
}
