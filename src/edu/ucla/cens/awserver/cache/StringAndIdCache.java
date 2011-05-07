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
 * A cache designed for String-ID relationships. No direct instance of this
 * ever exists because all subclasses should be either abstracted (but not
 * abstract) the same as this one, or should be concrete Singletons.
 * 
 * @author John Jenkins
 */
public class StringAndIdCache extends Cache {
	private static Logger _logger = Logger.getLogger(StringAndIdCache.class);
	
	/**
	 * Inner class for handling the results of a query for the Strings and
	 * their respective IDs.
	 *  
	 * @author John Jenkins
	 */
	protected static class StringAndId {
		public int _id;
		public String _string;
		
		/**
		 * Creates a new object with the specified id and value. This is done
		 * instead of having a default constructor and directly setting the
		 * values as a convenience to make creating a new object a one-liner
		 * and to provide a thin veil of encapsulation.
		 * 
		 * @param id The key for this key-value pair.
		 * 
		 * @param string The value for this key-value pair.
		 */
		public StringAndId(int id, String string) {
			_id = id;
			_string = string;
		}
	}
	
	// The lookup tables for translating strings to IDs and visa versa.
	// At first this seemed like a waste, but no reversible map could be
	// found. Instead, we maintain two lists to decrease lookup time at the
	// cost of additional memory usage.
	protected static Map<String, Integer> _stateToIdMap;
	protected static Map<Integer, String> _idToStateMap;
	
	// The SQL to use to get the values which must return a String value and
	// an integer value as dictated by the private class StringAndId.
	private String _sqlForRetrievingValues;
	
	// The names of the columns for which the data must be retrieved.
	private String _integerColumn;
	private String _stringColumn;
	
	/**
	 * Default constructor that calls its parent and is protected to maintain
	 * the Singleton-ness.
	 */
	protected StringAndIdCache(String sqlForRetrievingValues, String integerColumn, String stringColumn) {
		super();
		
		_stateToIdMap = new HashMap<String, Integer>();
		_idToStateMap = new HashMap<Integer, String>();
		
		_sqlForRetrievingValues = sqlForRetrievingValues;
		_integerColumn = integerColumn;
		_stringColumn = stringColumn;
		
		// Boot-time check that everything is working correctly. Given that
		// the SQL and its parameters shouldn't change while the system is
		// running, if this initial refresh succeeds then all subsequent
		// refreshes should succeed.
		refreshMap();
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
	 * @complexity O(n) if a refresh is required; otherwise, the complexity of
	 * 			   a Java Map object to lookup a key and return its value.
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
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its integer 
		// representation.
		if(_stateToIdMap.containsKey(string)) {
			return _stateToIdMap.get(string);
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
	 * Returns the String representation of the parameterized integer, 'id'.
	 * If no such ID is known, an exception is thrown.
	 * 
	 * @complexity O(n) if a refresh is required; otherwise, the complexity of
	 * 			   a Java Map object to lookup a key and return its value.
	 * 
	 * @param id The ID whose String representation is desired.
	 * 
	 * @return The String representation of the parameterized 'id'.
	 * 
	 * @throws CacheMissException Thrown if the parameterized 'id' is unknown.
	 * 							  This is done because if we are querying on
	 * 							  unknown IDs it is probably indicative of a
	 * 							  larger problem.
	 */
	public String lookup(int id) throws CacheMissException {
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}

		// If the ID exists return the String-value representation.
		if(_idToStateMap.containsValue(id)) {
			return _idToStateMap.get(id);
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
	public Set<String> getStrings() {
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		return _stateToIdMap.keySet();
	}
	
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
		if((_lastUpdateTimestamp + _updateFrequency) > System.currentTimeMillis()) {
			return;
		}
		
		// This is the JdbcTemplate we will use for our query.
		JdbcTemplate jdbcTemplate = new JdbcTemplate(_dataSource);
		
		// Get all of the strings and their corresponding IDs. If there is an
		// issue, report it and abort the update.
		List<?> stateAndId;
		try {
			stateAndId = jdbcTemplate.query(_sqlForRetrievingValues,
											new RowMapper() {
												@Override
												public Object mapRow(ResultSet rs, int row) throws SQLException {
													return new StringAndId(rs.getInt(_integerColumn), rs.getString(_stringColumn));
												}
											});
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + _sqlForRetrievingValues + "'. Aborting cache refresh.");
			return;
		}
		
		// Clear the list and begin populating it with the new information.
		_stateToIdMap.clear();
		_idToStateMap.clear();
		ListIterator<?> stateAndIdIter = stateAndId.listIterator();
		while(stateAndIdIter.hasNext()) {
			StringAndId currStateAndId = (StringAndId) stateAndIdIter.next();
			_stateToIdMap.put(currStateAndId._string, currStateAndId._id);
			_idToStateMap.put(currStateAndId._id, currStateAndId._string);
		}
		
		_lastUpdateTimestamp = System.currentTimeMillis();
	}
}