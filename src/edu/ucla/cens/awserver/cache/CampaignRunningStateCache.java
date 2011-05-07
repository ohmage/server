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
 * Singleton lookup table for the indices and String values for campaign
 * running states. It will only refresh the map when a call is being made and
 * the lookup table has expired.
 * 
 * @author John Jenkins
 */
public class CampaignRunningStateCache extends StringAndIdCache{
	private static Logger _logger = Logger.getLogger(CampaignRunningStateCache.class);
	
	private static final String SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS = "SELECT id, running_state " +
																		  "FROM campaign_running_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRunningStateCache";
	
	// Known campaign running states.
	public static final String RUNNING_STATE_RUNNING = "running";
	public static final String RUNNING_STATE_STOPPED = "stopped";
	
	// The lookup tables for translating states to IDs and visa versa.
	// At first this seemed like a waste, but no reversible map could be
	// found. Instead, we maintain two lists to decrease lookup time at the
	// cost of additional memory usage.
	private static Map<String, Integer> _stateToIdMap = new HashMap<String, Integer>();
	private static Map<Integer, String> _idToStateMap = new HashMap<Integer, String>();
	
	// The last time we refreshed our cache in milliseconds since epoch.
	private static long _lastUpdateTimestamp = -1;
	// The number of milliseconds between refreshes of the local cache.
	private static long _updateFrequency = -1;
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRunningStateCache() {
		// Does nothing.
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
	 * It is recommended but not required to use the RUNNING_STATE_* constants
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
	public static int lookup(String state) throws CacheMissException {		
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its ID.
		if(_stateToIdMap.containsKey(state)) {
			return _stateToIdMap.get(state);
		}
		// Otherwise, throw an exception that it is an unknown state.
		else {
			throw new CacheMissException("Unknown state: " + state);
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
	public static String lookup(int id) throws CacheMissException {
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
			throw new CacheMissException("Unknown ID: " + id);
		}
	}
	
	/**
	 * Returns all the known states.
	 * 
	 * @return All known states.
	 */
	public static Set<String> getStates() {
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
	private static synchronized void refreshMap() {
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
			stateAndId = jdbcTemplate.query(SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS,
											new RowMapper() {
												@Override
												public Object mapRow(ResultSet rs, int row) throws SQLException {
													return new StringAndId(rs.getInt("id"), rs.getString("running_state"));
												}
											});
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS + "'. Aborting cache refresh.");
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