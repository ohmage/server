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
 * A cache designed for String-ID relationships.
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
		
		public StringAndId(int id, String string) {
			_id = id;
			_string = string;
		}
	}
	
	// The lookup tables for translating states to IDs and visa versa.
	// At first this seemed like a waste, but no reversible map could be
	// found. Instead, we maintain two lists to decrease lookup time at the
	// cost of additional memory usage.
	protected static Map<String, Integer> _stateToIdMap;
	protected static Map<Integer, String> _idToStateMap;
	
	// The SQL to use to get the values which must return two String values as
	// dictated by the private class KeyAndValue.
	private String _sqlForRetrievingValues;
	
	/**
	 * Default constructor that calls its parent and is protected to maintain
	 * the Singleton-ness.
	 */
	protected StringAndIdCache(String sqlForRetrievingValues) {
		super();
		
		_stateToIdMap = new HashMap<String, Integer>();
		_idToStateMap = new HashMap<Integer, String>();
		
		_sqlForRetrievingValues = sqlForRetrievingValues;
	}
	
	/**
	 * Compares the current timestamp with the last time we did an update plus
	 * the amount of time between updates. If our cache has become stale, we
	 * attempt to update it and, if successful, we update the time of the last
	 * update.
	 * 
	 * Then, we check to see if such a state exists in our cache. If not, we
	 * throw an exception because, if someone is querying for a state that
	 * doesn't exist, we need to bring it to their immediate attention rather
	 * than return an incorrect value. Otherwise, the database ID is returned.
	 * 
	 * It is recommended but not required to use the PRIVACY_STATE_* constants
	 * defined in this class when possible.
	 * 
	 * @complexity O(n) if a refresh is required; otherwise, O(1) assuming the
	 * 			   map can lookup at that complexity on the average case.
	 * 
	 * @param state A String representation of the state whose database ID is 
	 * 				desired.
	 * 
	 * @return The database ID for the given state.
	 * 
	 * @throws CacheMissException Thrown if no such state exists.
	 */
	public int lookup(String string) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its ID.
		if(_stateToIdMap.containsKey(string)) {
			return _stateToIdMap.get(string);
		}
		// Otherwise, throw an exception that it is an unknown state.
		else {
			throw new CacheMissException("Unknown string for cache: " + string);
		}
	}
	
	/**
	 * Returns the String representation of the state in question based on the
	 * parameterized 'id'. If no such ID is known, it throws an exception
	 * because giving an incorrect ID should be brought to the system's 
	 * immediate attention.
	 * 
	 * @complexity O(n) if a refresh is required; otherwise, O(1) assuming the
	 * 			   map can lookup at that complexity on the average case.
	 * 
	 * @param id The ID of the state in question.
	 * 
	 * @return The String representation of the state based on the 
	 * 		   parameterized 'id'.
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
		// Otherwise, throw an exception that it is an unknown state.
		else {
			throw new CacheMissException("Unknown ID for cache: " + id);
		}
	}
	
	/**
	 * Returns all the known states.
	 * 
	 * @return All known states.
	 */
	public Set<String> getStates() {
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
	 * @complexity O(n) where n is the number of states in the database.
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
		
		// Get all the states and their corresponding IDs. If there is an
		// issue, just abort the whole update.
		List<?> stateAndId;
		try {
			stateAndId = jdbcTemplate.query(_sqlForRetrievingValues,
											new RowMapper() {
												@Override
												public Object mapRow(ResultSet rs, int row) throws SQLException {
													return new StringAndId(rs.getInt("id"), rs.getString("privacy_state"));
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