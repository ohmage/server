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
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ohmage.domain.BidirectionalHashMap;
import org.ohmage.exception.CacheMissException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * A abstract cache designed for String-ID relationships.
 * 
 * @author John Jenkins
 */
public abstract class StringAndIdCache extends Cache {
	private static final Logger LOGGER = Logger.getLogger(StringAndIdCache.class);
	
	/**
	 * Inner class for handling the results of a query for the Strings and
	 * their respective IDs.
	 *  
	 * @author John Jenkins
	 */
	private final class StringAndId {
		private final int id;
		private final String string;
		
		/**
		 * Creates a new object with the specified ID and value. This is done
		 * instead of having a default constructor and directly setting the
		 * values as a convenience to make creating a new object a one-liner
		 * and to provide a thin veil of encapsulation.
		 * 
		 * @param Id The key for this key-value pair.
		 * 
		 * @param string The value for this key-value pair.
		 */
		private StringAndId(int id, String string) {
			this.id = id;
			this.string = string;
		}
	}
	
	// The lookup table for translating strings to IDs and visa versa.
	private BidirectionalHashMap<String, Integer> stringAndIdMap;
	
	// The SQL to use to get the values which must return a String value and
	// an integer value as dictated by the private class StringAndId.
	private final String sqlForRetrievingValues;
	
	// The names of the columns for which the data must be retrieved.
	private final String integerColumn;
	private final String stringColumn;
	
	/**
	 * Default constructor that calls its parent and is protected to maintain
	 * the Singleton-ness.
	 */
	protected StringAndIdCache(DataSource dataSource, long updateFrequency, String sqlForRetrievingValues, String integerColumn, String stringColumn) {
		super(dataSource, updateFrequency);
		
		stringAndIdMap = new BidirectionalHashMap<String, Integer>();
		
		this.sqlForRetrievingValues = sqlForRetrievingValues;
		this.integerColumn = integerColumn;
		this.stringColumn = stringColumn;
	}
	
	/**
	 * Compares the current timestamp with the last time we did an update plus
	 * the amount of time between updates. If our cache has become stale, we
	 * attempt to update it.
	 * 
	 * Then, we check to see if such a state exists in our cache. If not, we
	 * throw an exception because, if someone is querying for a state that
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
	 * @param string The String whose corresponding integer value is
	 * 				 being requested.
	 * 
	 * @return The corresponding integer value.
	 * 
	 * @throws CacheMissException Thrown if no such state exists.
	 */
	public int lookup(String string) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its integer 
		// representation.
		if(stringAndIdMap.containsKey(string)) {
			return stringAndIdMap.getValue(string);
		}
		// Otherwise, throw an exception that it is an unknown key.
		else {
			throw new CacheMissException("Unknown string for cache: " + string);
		}
	}
	
	/**
	 * Compares the current timestamp with the last time we did an update plus
	 * the amount of time between updates. If our cache has become stale, we
	 * attempt to update it.
	 * 
	 * Returns the String representation of the parameterized integer, 'Id'.
	 * If no such ID is known, an exception is thrown.
	 * 
	 * The complexity is O(n) if a refresh is required; otherwise, the 
	 * complexity of a Java Map object to lookup a key and return its value.
	 * 
	 * @param Id The ID whose String representation is desired.
	 * 
	 * @return The String representation of the parameterized 'Id'.
	 * 
	 * @throws CacheMissException Thrown if the parameterized 'Id' is unknown.
	 * 							  This is done because if we are querying on
	 * 							  unknown IDs it is probably indicative of a
	 * 							  larger problem.
	 */
	public String lookup(int id) throws CacheMissException {
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}

		// If the ID exists return the String-value representation.
		if(stringAndIdMap.containsValue(id)) {
			return stringAndIdMap.getKey(id);
		}
		// Otherwise, throw an exception that it is an unknown ID.
		else {
			throw new CacheMissException("Unknown ID for cache: " + id);
		}
	}
	
	/**
	 * Returns all the known strings.
	 * 
	 * @return All known strings.
	 */
	@Override
	public Set<String> getKeys() {
		// If the lookup table is out-of-date, refresh it.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		return stringAndIdMap.keySet();
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
	 * This is synchronized as any number of threads may realize that the
	 * cache is out-of-date and attempt to update it. The first one should
	 * succeed and the following ones will abort as the first thing a refresh
	 * does is, again, check if the cache is stale.
	 * 
	 * @complexity O(n) where n is the number of strings-values in the
	 * 			   database.
	 */
	private synchronized void refreshMap() {
		// Only one thread should be updating this information at a time. Once
		// other threads enter, they should check to see if an update was just
		// done and, if so, should abort a second update.
		if((getLastUpdateTimestamp() + getUpdateFrequency()) > System.currentTimeMillis()) {
			return;
		}
		
		// This is the JdbcTemplate we will use for our query.
		JdbcTemplate jdbcTemplate = new JdbcTemplate(getDataSource());
		
		// Get all of the strings and their corresponding IDs. If there is an
		// issue, report it and abort the update.
		List<StringAndId> stateAndId;
		try {
			stateAndId = jdbcTemplate.query(
					sqlForRetrievingValues,
					new RowMapper<StringAndId>() {
						@Override
						public StringAndId mapRow(ResultSet rs, int row) throws SQLException {
							return new StringAndId(rs.getInt(integerColumn), rs.getString(stringColumn));
						}
					}
				);
		}
		catch(org.springframework.dao.DataAccessException e) {
			LOGGER.error("Error executing SQL '" + sqlForRetrievingValues + "'. Aborting cache refresh.");
			return;
		}
		
		// Create a new map, populate it, and then completely replace the old
		// one. This allows for concurrent reads while a new map is being
		// generated.
		BidirectionalHashMap<String, Integer> stringAndIdMap = new BidirectionalHashMap<String, Integer>();
		for(StringAndId currStateAndId : stateAndId) {
			stringAndIdMap.putKey(currStateAndId.string, currStateAndId.id);
		}
		this.stringAndIdMap = stringAndIdMap;
		
		setLastUpdateTimestamp(System.currentTimeMillis());
	}
}
