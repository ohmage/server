package edu.ucla.cens.awserver.cache;

/**
 * Keeps a cache of the class roles and their respective IDs.
 * 
 * @author John Jenkins
 */
public class ClassRoleCache extends StringAndIdCache {
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = "SELECT id, role " +
																 "FROM user_class_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "classRoleCache";
	
	// Known class role constants.
	public static final String ROLE_PRIVILEGED = "privileged";
	public static final String ROLE_RESTRICTED = "restricted";
	
	private static ClassRoleCache _self = new ClassRoleCache();

	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private ClassRoleCache() {
		super(SQL_GET_CAMPAIGN_ROLES_AND_IDS);
	}
	
	public static ClassRoleCache instance() {
		return _self;
	}
}
