package edu.ucla.cens.awserver.cache;

/**
 * Singleton lookup table for the indices and String values for campaign
 * privacy states. It will only refresh the map when a call is being made and
 * the lookup table has expired.
 * 
 * @author John Jenkins
 */
public class CampaignPrivacyStateCache extends StringAndIdCache {
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_CAMPAIGN_PRIVACY_STATES_AND_IDS = "SELECT id, privacy_state " +
																		  "FROM campaign_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignPrivacyStateCache";
	
	// Known campaign privacy states.
	public static final String PRIVACY_STATE_PRIVATE = "private";
	public static final String PRIVACY_STATE_SHARED = "shared";
	
	private static CampaignPrivacyStateCache _self = new CampaignPrivacyStateCache();
	
	/**
	 * Default constructor set to protected to make this a Singleton.
	 */
	protected CampaignPrivacyStateCache() {
		super(SQL_GET_CAMPAIGN_PRIVACY_STATES_AND_IDS);
	}
	
	public static CampaignPrivacyStateCache instance() {
		return _self;
	}
}