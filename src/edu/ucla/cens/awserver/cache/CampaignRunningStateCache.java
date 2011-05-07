package edu.ucla.cens.awserver.cache;

/**
 * Singleton lookup table for the indices and String values for campaign
 * running states. It will only refresh the map when a call is being made and
 * the lookup table has expired.
 * 
 * @author John Jenkins
 */
public class CampaignRunningStateCache extends StringAndIdCache{
	private static final String SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS = "SELECT id, running_state " +
																		  "FROM campaign_running_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRunningStateCache";
	
	// Known campaign running states.
	public static final String RUNNING_STATE_RUNNING = "running";
	public static final String RUNNING_STATE_STOPPED = "stopped";

	private static CampaignRunningStateCache _self = new CampaignRunningStateCache();
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRunningStateCache() {
		super(SQL_GET_CAMPAIGN_RUNNING_STATES_AND_IDS);
	}
	
	public static CampaignRunningStateCache instance() {
		return _self;
	}
}