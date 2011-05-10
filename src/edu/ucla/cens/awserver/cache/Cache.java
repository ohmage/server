package edu.ucla.cens.awserver.cache;

import java.security.InvalidParameterException;

import javax.sql.DataSource;

/**
 * The superclass for all caches. No direct instance of this ever exists
 * because all subclasses should be either abstracted (but not abstract)
 * the same as this one, or should be concrete Singletons.
 * 
 * @author John Jenkins
 */
public class Cache {
	/**
	 * The minimum allowed frequency of cache refreshes with the database.
	 */
	public static final long MIN_CACHE_REFRESH_MILLIS = 1000;
	
	// The DataSource to use when querying the database.
	protected DataSource _dataSource;
	
	// The last time we refreshed our cache in milliseconds since epoch.
	protected long _lastUpdateTimestamp;
	// The number of milliseconds between refreshes of the local cache.
	protected long _updateFrequency;
	
	/**
	 * Default constructor made protected so children can call it, but it is
	 * still a Singleton.
	 * 
	 * @complexity O(1)
	 */
	protected Cache() {
		// This helps us guarantee that the DataSource starts off as null to
		// help assure that it will only be set once.
		_dataSource = null;
		
		// Initialize the refresh times to "invalid" values such that the
		// first run is guaranteed to refresh itself.
		_lastUpdateTimestamp = -1;
		_updateFrequency = -1;
	}
	
	/**
	 * Sets the DataSource for this object. This can only be called once by
	 * Spring on startup and subsequent calls will cause an 
	 * IllegalArgumentException exception to be thrown.
	 * 
	 * @complexity O(1)
	 * 
	 * @param dataSource The DataSource for its children to use when querying
	 * 					 the database.
	 * 
	 * @throws IllegalArgumentException Thrown if the DataSource has not yet
	 * 									been set and Spring has passed in a
	 * 									null value or if the DataSource has
	 * 									been set and it is attempting to be
	 * 									reset.
	 */
	public synchronized void setDataSource(DataSource dataSource) throws IllegalArgumentException {
		//if(_dataSource != null) {
		//	throw new IllegalStateException("The DataSource may only be set once.");
		//}
		if(dataSource == null) {
			throw new InvalidParameterException("The DataSource may not be null.");
		}
		
		_dataSource = dataSource;
	}
	
	/**
	 * Sets the initial update frequency for this object. This may be reset by
	 * anyone at a later date to dynamically increase cache fidelity or to
	 * lessen the load on the database.
	 * 
	 * @complexity O(1)
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
	 * @see {@link #MIN_CACHE_REFRESH_MILLIS}
	 */
	public synchronized void setUpdateFrequency(long frequencyInMilliseconds) throws IllegalArgumentException {
		if(frequencyInMilliseconds < MIN_CACHE_REFRESH_MILLIS) {
			throw new InvalidParameterException("The update frequency must be a positive integer greater than or equal to " +
					MIN_CACHE_REFRESH_MILLIS + " milliseconds.");
		}
		
		_updateFrequency = frequencyInMilliseconds;
	}
}
