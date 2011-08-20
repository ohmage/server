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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.exception.CacheMissException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * The abstract cache class for key-Value pairs.
 * 
 * @author John Jenkins
 */
public abstract class KeyValueCache extends Cache {
	private static final Logger LOGGER = Logger.getLogger(KeyValueCache.class);
	
	/**
	 * Inner class for handling the results of a query for the String keys and
	 * their respective String values.
	 *  
	 * @author John Jenkins
	 */
	private final class KeyAndValue {
		private final String key;
		private final String value;
		
		/**
		 * Creates a new object with the specified key and value. This is done
		 * instead of having a default constructor and directly setting the
		 * values as a convenience to make creating a new object a one-liner
		 * and to provide a thin veil of encapsulation.
		 * 
		 * @param key The key for this key-value pair.
		 * 
		 * @param value The value for this key-value pair.
		 */
		private KeyAndValue(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}
	
	// The map of all the keys to their values.
	private Map<String, String> keyValueMap;
	
	// The SQL to use to get the values which must return two String values as
	// dictated by the private class KeyAndValue.
	private final String sqlForRetrievingValues;
	
	// The column names for the key and value columns to be used with the SQL.
	private final String keyColumn;
	private final String valueColumn;
	
	/**
	 * Default constructor that calls its parent and is protected to maintain
	 * the Singleton-ness.
	 */
	protected KeyValueCache(DataSource dataSource, long updateFrequency, String sqlForRetrievingValues, String keyKey, String valueKey) {
		super(dataSource, updateFrequency);
		
		keyValueMap = new HashMap<String, String>();
		this.sqlForRetrievingValues = sqlForRetrievingValues;
		
		keyColumn = keyKey;
		valueColumn = valueKey;
	}
	
	/**
	 * Compares the current timestamp with the last time we did an update plus
	 * the amount of time between updates. If our cache has become stale, we
	 * attempt to update it.
	 * 
	 * Then, we check to see if such a key exists in our cache. If not, we
	 * throw an exception because, if someone is querying for a key that
	 * doesn't exist, we need to bring it to their immediate attention rather
	 * than returning an "error" value. Otherwise, the corresponding integer
	 * value is returned.
	 * 
	 * It is recommended, but not required, to use the constants declared in
	 * the concrete cache class as the parameter.
	 * 
	 * The complexity is O(n) if a refresh is required; otherwise, the 
	 * complexity of a Java Map object to lookup a key and return its value.
	 * 
	 * @param key The key whose corresponding value is being requested.
	 * 
	 * @return The corresponding value.
	 * 
	 * @throws CacheMissException Thrown if no such key exists.
	 */
	public String lookup(String key) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its value.
		if(keyValueMap.containsKey(key)) {
			return keyValueMap.get(key);
		}
		// Otherwise, throw an exception that it is an unknown state.
		else {
			throw new CacheMissException("Unknown key: " + key);
		}
	}
	
	/**
	 * Returns all the known keys.
	 * 
	 * @return All known keys.
	 */
	@Override
	public Set<String> getKeys() {
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		return keyValueMap.keySet();
	}
	
	/**
	 * Gets a human-readable name for this cache.
	 * 
	 * @return Returns a human-readable name for their cache.
	 */
	@Override
	public abstract String getName();
	
	/**
	 * Reads the database for the information in the lookup table and
	 * populates its map with the gathered information. If there is an issue
	 * reading the database, it will just remain with the current lookup table
	 * it has.
	 * 
	 * @complexity O(n) where n is the number of keys in the database.
	 */
	protected synchronized void refreshMap() {
		// Only one thread should be updating this information at a time. Once
		// other threads enter, they should check to see if an update was just
		// done and, if so, should abort a second update.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) > System.currentTimeMillis()) {
			return;
		}
		
		// This is the JdbcTemplate we will use for our query. If there is an
		// issue report it and abort the update.
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		
		// Get all the keys and their corresponding values.
		List<KeyAndValue> keyAndValue;
		try {
			keyAndValue = jdbcTemplate.query(
					sqlForRetrievingValues, 
					new RowMapper<KeyAndValue>() {
						@Override
						public KeyAndValue mapRow(ResultSet rs, int row) throws SQLException {
							return new KeyAndValue(rs.getString(keyColumn), rs.getString(valueColumn));
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			LOGGER.error("Error executing SQL '" + sqlForRetrievingValues + "'. Aborting cache refresh.");
			return;
		}
		
		// Create a new Map, populate it, and replace the old one. This allows
		// for concurrent readying while the new Map is being created.
		Map<String, String> keyValueMap = new HashMap<String, String>();
		for(KeyAndValue currStateAndId : keyAndValue) {
			keyValueMap.put(currStateAndId.key, currStateAndId.value);
		}
		this.keyValueMap = keyValueMap;
		
		setLastUpdateTimestamp(System.currentTimeMillis());
	}
}
