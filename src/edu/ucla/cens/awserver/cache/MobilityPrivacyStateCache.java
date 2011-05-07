package edu.ucla.cens.awserver.cache;

/**
 * Singleton lookup table for the indices and String values for mobility 
 * privacy states. It will only refresh the map when a lookup call is being
 * made and the lookup table has expired.
 * 
 * @author John Jenkins
 */
public class MobilityPrivacyStateCache extends StringAndIdCache {
	private static final String SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS = "SELECT id, privacy_state " +
																		  "FROM mobility_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "mobilityPrivacyStateCache";
	
	// Known Mobility privacy states.
	public static final String PRIVACY_STATE_PRIVATE = "private";
	
	private static MobilityPrivacyStateCache _self = new MobilityPrivacyStateCache();

	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private MobilityPrivacyStateCache() {
		super(SQL_GET_MOBILITY_PRIVACY_STATES_AND_IDS);
	}
	
	public static MobilityPrivacyStateCache instance() {
		return _self;
	}
}