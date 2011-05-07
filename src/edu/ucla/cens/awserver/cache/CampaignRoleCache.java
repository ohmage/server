package edu.ucla.cens.awserver.cache;

/**
 * Keeps a cache of the campaign roles and their respective IDs.
 * 
 * @author John Jenkins
 */
public class CampaignRoleCache extends StringAndIdCache {
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = "SELECT id, role " +
																 "FROM user_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "campaignRoleCache";
	
	// Known campaign role constants.
	public static final String ROLE_ANALYST = "analyst";
	public static final String ROLE_AUTHOR = "author";
	public static final String ROLE_PARTICIPANT = "participant";
	public static final String ROLE_SUPERVISOR = "supervisor";
	
	private static CampaignRoleCache _self = new CampaignRoleCache();
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private CampaignRoleCache() {
		super(SQL_GET_CAMPAIGN_ROLES_AND_IDS);
	}
	
	public static CampaignRoleCache instance() {
		return _self;
	}
}
