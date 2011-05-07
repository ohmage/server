package edu.ucla.cens.awserver.cache;

/**
 * Cache for the preferences in the database.
 * 
 * @author John Jenkins
 */
public class PreferenceCache extends KeyValueCache {
	private static final String SQL_GET_KEYS_AND_VALUES = "SELECT p_key, p_value " +
														  "FROM preference";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "preferenceCache";
	
	// Known campaign privacy states.
	public static final String KEY_DEFAULT_CAN_CREATE_PRIVILIEGE = "default_can_create_privilege";
	public static final String KEY_DEFAULT_SURVEY_RESPONSE_SHARING_STATE = "default_survey_response_sharing_state";
	
	// The reference to one's self to return to requesters.
	private static PreferenceCache _self = new PreferenceCache();
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	protected PreferenceCache() {
		super(SQL_GET_KEYS_AND_VALUES);
	}
	
	/**
	 * A reference to the one instance of this object.
	 * 
	 * @return The one instance of this object through which call calls to
	 * 		   this cache should be made.
	 */
	public static PreferenceCache instance() {
		return _self;
	}
}