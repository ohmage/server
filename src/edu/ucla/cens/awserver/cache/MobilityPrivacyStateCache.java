package edu.ucla.cens.awserver.cache;

/**
 * Singleton cache for the indices and String values for Mobility privacy
 * states.
 * 
 * @author John Jenkins
 */
public class MobilityPrivacyStateCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String STATE_COLUMN = "privacy_state";
	
	private static final String SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS = "SELECT " + ID_COLUMN + ", " + STATE_COLUMN + " " +
																		  "FROM mobility_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "mobilityPrivacyStateCache";
	
	// Known Mobility privacy states.
	public static final String PRIVACY_STATE_PRIVATE = "private";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static MobilityPrivacyStateCache _self = new MobilityPrivacyStateCache();

	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private MobilityPrivacyStateCache() {
		super(SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS, ID_COLUMN, STATE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static MobilityPrivacyStateCache instance() {
		return _self;
	}
}