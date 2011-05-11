package edu.ucla.cens.awserver.cache;

/**
 * Singleton cache for the indices and String values for class roles.
 * 
 * @author John Jenkins
 */
public class ClassRoleCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String ROLE_COLUMN = "role";
	
	private static final String SQL_GET_CAMPAIGN_ROLES_AND_IDS = "SELECT " + ID_COLUMN + ", " + ROLE_COLUMN + " " +
																 "FROM user_class_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "classRoleCache";
	
	// Known class role constants.
	public static final String ROLE_PRIVILEGED = "privileged";
	public static final String ROLE_RESTRICTED = "restricted";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static ClassRoleCache _self = new ClassRoleCache();

	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private ClassRoleCache() {
		super(SQL_GET_CAMPAIGN_ROLES_AND_IDS, ID_COLUMN, ROLE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static ClassRoleCache instance() {
		return _self;
	}
}
