package edu.ucla.cens.awserver.cache;

/**
 * Singleton cache for the indices and String values for campaign privacy
 * states.
 * 
 * @author John Jenkins
 */
public class CampaignPrivacyStateCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String STATE_COLUMN = "privacy_state";
	
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_CAMPAIGN_PRIVACY_STATES_AND_IDS = "SELECT " + ID_COLUMN + ", " + STATE_COLUMN + " " +
																		  "FROM campaign_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which cache we want.
	public static final String CACHE_KEY = "campaignPrivacyStateCache";
	
	// Known campaign privacy states.
	public static final String PRIVACY_STATE_PRIVATE = "private";
	public static final String PRIVACY_STATE_SHARED = "shared";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static CampaignPrivacyStateCache _self = new CampaignPrivacyStateCache();
	
	/**
	 * Default constructor set to protected to make this a Singleton.
	 */
	protected CampaignPrivacyStateCache() {
		super(SQL_GET_CAMPAIGN_PRIVACY_STATES_AND_IDS, ID_COLUMN, STATE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static CampaignPrivacyStateCache instance() {
		return _self;
	}
}