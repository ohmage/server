package edu.ucla.cens.awserver.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * The abstract cache class for key-value pairs.
 * 
 * @author John Jenkins
 */
public abstract class KeyValueCache extends Cache {
	private static Logger _logger = Logger.getLogger(KeyValueCache.class);
	
	/**
	 * Inner class for handling the results of a query for the String keys and
	 * their respective String values.
	 *  
	 * @author John Jenkins
	 */
	private class KeyAndValue {
		public String _key;
		public String _value;
		
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
		public KeyAndValue(String key, String value) {
			_key = key;
			_value = value;
		}
	}
	
	// The map of all the keys to their values.
	private Map<String, String> _keyValueMap;
	
	// The SQL to use to get the values which must return two String values as
	// dictated by the private class KeyAndValue.
	private String _sqlForRetrievingValues;
	
	// The column names for the key and value columns to be used with the SQL.
	private String _keyColumn;
	private String _valueColumn;
	
	/**
	 * Default constructor that calls its parent and is protected to maintain
	 * the Singleton-ness.
	 */
	protected KeyValueCache(String sqlForRetrievingValues, String keyKey, String valueKey) {
		super();
		
		_keyValueMap = new HashMap<String, String>();
		_sqlForRetrievingValues = sqlForRetrievingValues;
		
		_keyColumn = keyKey;
		_valueColumn = valueKey;
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
	 * @complexity O(n) if a refresh is required; otherwise, the complexity of
	 * 			   a Java Map object to lookup a key and return its value.
	 * 
	 * @param key The key whose corresponding value is being requested.
	 * 
	 * @return The corresponding value.
	 * 
	 * @throws CacheMissException Thrown if no such key exists.
	 */
	public String lookup(String key) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its value.
		if(_keyValueMap.containsKey(key)) {
			return _keyValueMap.get(key);
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
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		return _keyValueMap.keySet();
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
	private synchronized void refreshMap() {
		// Only one thread should be updating this information at a time. Once
		// other threads enter, they should check to see if an update was just
		// done and, if so, should abort a second update.
		if((_lastUpdateTimestamp + _updateFrequency) > System.currentTimeMillis()) {
			return;
		}
		
		// This is the JdbcTemplate we will use for our query. If there is an
		// issue report it and abort the update.
		JdbcTemplate jdbcTemplate = new JdbcTemplate(_dataSource);
		
		// Get all the keys and their corresponding values.
		List<?> keyAndValue;
		try {
			keyAndValue = jdbcTemplate.query(_sqlForRetrievingValues,
											new RowMapper() {
												@Override
												public Object mapRow(ResultSet rs, int row) throws SQLException {
													return new KeyAndValue(rs.getString(_keyColumn), rs.getString(_valueColumn));
												}
											});
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + _sqlForRetrievingValues + "'. Aborting cache refresh.");
			return;
		}
		
		// Create a new Map, populate it, and replace the old one. This allows
		// for concurrent readying while the new Map is being created.
		Map<String, String> keyValueMap = new HashMap<String, String>();
		ListIterator<?> keyAndValueIter = keyAndValue.listIterator();
		while(keyAndValueIter.hasNext()) {
			KeyAndValue currStateAndId = (KeyAndValue) keyAndValueIter.next();
			keyValueMap.put(currStateAndId._key, currStateAndId._value);
		}
		_keyValueMap = keyValueMap;
		
		_lastUpdateTimestamp = System.currentTimeMillis();
	}
}
