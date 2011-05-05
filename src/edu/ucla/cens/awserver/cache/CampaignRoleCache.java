package edu.ucla.cens.awserver.cache;

import java.security.InvalidParameterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * Keeps a cache of the campaign roles and their respective IDs.
 * 
 * @author John Jenkins
 */
public class CampaignRoleCache extends StringAndIdCache {
	private static Logger _logger = Logger.getLogger(CampaignRoleCache.class);
	
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = "SELECT id, role " +
																 "FROM user_role";
	
	// Known campaign role constants.
	public static final String ROLE_ANALYST = "analyst";
	public static final String ROLE_AUTHOR = "author";
	public static final String ROLE_PARTICIPANT = "participant";
	public static final String ROLE_SUPERVISOR = "supervisor";
	
	// The lookup tables for translating roles to IDs and visa versa.
	// At first this seemed like a waste, but no reversible map could be
	// found. Instead, we maintain two lists to decrease lookup time at the
	// cost of additional memory usage.
	protected static Map<String, Integer> _stringToIdMap = new HashMap<String, Integer>();
	protected static Map<Integer, String> _idToStringMap = new HashMap<Integer, String>();
	
	// The last time we refreshed our cache in milliseconds since epoch.
	protected static long _lastUpdateTimestamp = -1;
	// The number of milliseconds between refreshes of the local cache.
	protected static long _updateFrequency = -1;
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRoleCache() {
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
	 * Then, we check to see if such a role exists in our cache. If not, we
	 * throw an InvalidParameterException because, if someone is querying for
	 * a role that doesn't exist, we need to bring it to their immediate
	 * attention rather than return an incorrect value. Otherwise, the
	 * database ID is returned.
	 * 
	 * It is recommended but not required that you use the ROLE_* constants 
	 * defined in this class if possible.
	 * 
	 * @complexity O(n) if a refresh is required; otherwise, O(1) assuming the
	 * 			   map can lookup at that complexity on the average case.
	 * 
	 * @param role A String representation of the role whose database ID is 
	 * 				desired.
	 * 
	 * @return The database ID for the given role.
	 * 
	 * @throws InvalidParameterException Thrown if no such role exists.
	 */
	public static int lookup(String role) throws InvalidParameterException {		
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}
		
		// If the key exists in the lookup table, return its ID.
		if(_stringToIdMap.containsKey(role)) {
			return _stringToIdMap.get(role);
		}
		// Otherwise, throw an exception that it is an unknown role.
		else {
			throw new InvalidParameterException("Unknown role: " + role);
		}
	}
	
	/**
	 * Returns the String representation of the role in question based on the
	 * parameterized 'id'. If no such ID is known, it throws an 
	 * InvalidParameterException exception because giving an incorrect ID
	 * should be brought to the system's immediate attention.
	 * 
	 * @complexity O(n) if a refresh is required; otherwise, O(1) assuming the
	 * 			   map can lookup at that complexity on the average case.
	 * 
	 * @param id The ID of the role in question.
	 * 
	 * @return The String representation of the role based on the 
	 * 		   parameterized 'id'.
	 * 
	 * @throws InvalidParameterException Thrown if the parameterized 'id' is
	 * 									 unknown. This is done because if we
	 * 									 are querying on unknown IDs it is
	 * 									 probably indicative of a larger
	 * 									 problem.
	 */
	public static String lookup(int id) throws InvalidParameterException {
		// If the lookup table is out-of-date, refresh it.
		if((_lastUpdateTimestamp + _updateFrequency) <= System.currentTimeMillis()) {
			refreshMap();
		}

		// If the ID exists return the String-value representation.
		if(_idToStringMap.containsValue(id)) {
			return _idToStringMap.get(id);
		}
		// Otherwise, throw an exception that it is an unknown role.
		else {
			throw new InvalidParameterException("Unknown ID: " + id);
		}
	}
	
	/**
	 * Reads the database for the information in the lookup table and
	 * populates its map with the gathered information. If there is an issue
	 * reading the database, it will just remain with the current lookup table
	 * it has.
	 * 
	 * @complexity O(n) where n is the number of roles in the database.
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
		
		// Get all the roles and their corresponding IDs. If there is an
		// issue, just abort the whole update.
		List<?> roleAndId;
		try {
			roleAndId = jdbcTemplate.query(SQL_GET_CAMPAIGN_ROLES_AND_IDS,
											new RowMapper() {
												@Override
												public Object mapRow(ResultSet rs, int row) throws SQLException {
													return new StringAndId(rs.getInt("id"), rs.getString("role"));
												}
											});
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("Error executing SQL '" + SQL_GET_CAMPAIGN_ROLES_AND_IDS + "'. Aborting cache refresh.");
			return;
		}
		
		// Clear the list and begin populating it with the new information.
		_stringToIdMap.clear();
		_idToStringMap.clear();
		ListIterator<?> roleAndIdIter = roleAndId.listIterator();
		while(roleAndIdIter.hasNext()) {
			StringAndId currRoleAndId = (StringAndId) roleAndIdIter.next();
			_stringToIdMap.put(currRoleAndId._string, currRoleAndId._id);
			_idToStringMap.put(currRoleAndId._id, currRoleAndId._string);
		}
		
		_lastUpdateTimestamp = System.currentTimeMillis();
	}
}
