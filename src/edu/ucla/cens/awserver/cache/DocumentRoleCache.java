package edu.ucla.cens.awserver.cache;

public class DocumentRoleCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String ROLE_COLUMN = "role";
	
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_DOCUMENT_PRIVACY_STATES_AND_IDS = "SELECT " + ID_COLUMN + ", " + ROLE_COLUMN + " " +
																		  "FROM document_role";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which cache we want.
	public static final String CACHE_KEY = "documentRoleCache";
	
	// Known roles for those associated with documents.
	public static final String ROLE_READER = "reader";
	public static final String ROLE_WRITER = "writer";
	public static final String ROLE_OWNER = "owner";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static DocumentRoleCache _self = new DocumentRoleCache();
	
	/**
	 * Default constructor set to protected to make this a Singleton, but
	 * allow another cache to subclass it despite the likeliness of it.
	 */
	protected DocumentRoleCache() {
		super(SQL_GET_DOCUMENT_PRIVACY_STATES_AND_IDS, ID_COLUMN, ROLE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static DocumentRoleCache instance() {
		return _self;
	}
	
	/**
	 * Returns a human-readable name for this cache.
	 */
	@Override
	public String getName() {
		return CACHE_KEY;
	}
}