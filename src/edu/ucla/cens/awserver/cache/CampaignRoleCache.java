package edu.ucla.cens.awserver.cache;

/**
 * Singleton cache for the indices and String values for campaign roles.
 * 
 * @author John Jenkins
 */
public class CampaignRoleCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String ROLE_COLUMN = "role";
	
	// The SQL used to get at the cache's values.
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = "SELECT " + ID_COLUMN + ", " + ROLE_COLUMN + " " +
																 "FROM user_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRoleCache";
	
	// Known campaign role constants.
	public static final String ROLE_ANALYST = "analyst";
	public static final String ROLE_AUTHOR = "author";
	public static final String ROLE_PARTICIPANT = "participant";
	public static final String ROLE_SUPERVISOR = "supervisor";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static CampaignRoleCache _self = new CampaignRoleCache();
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRoleCache() {
		super(SQL_GET_CAMPAIGN_ROLES_AND_IDS, ID_COLUMN, ROLE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static CampaignRoleCache instance() {
		return _self;
	}
}
