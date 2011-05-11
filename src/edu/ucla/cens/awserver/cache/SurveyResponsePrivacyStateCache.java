package edu.ucla.cens.awserver.cache;

/**
 * Singleton cache for the indices and String values for survey response
 * privacy states.
 * 
 * @author John Jenkins
 */
public class SurveyResponsePrivacyStateCache extends StringAndIdCache {
	// The column IDs for the query.
	private static final String ID_COLUMN = "id";
	private static final String STATE_COLUMN = "privacy_state";
	
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES_AND_IDS = "SELECT " + ID_COLUMN + ", " + STATE_COLUMN + " " +
																		  		 "FROM survey_response_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "surveyResponsePrivacyStateCache";
	
	// Known survey response privacy states.
	public static final String PRIVACY_STATE_INVISIBLE = "invisible";
	public static final String PRIVACY_STATE_PRIVATE = "private";
	public static final String PRIVACY_STATE_SHARED = "shared";
	
	// A reference to the only instance of this class for the Singleton
	// pattern.
	private static SurveyResponsePrivacyStateCache _self = new SurveyResponsePrivacyStateCache();
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private SurveyResponsePrivacyStateCache() {
		super(SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES_AND_IDS, ID_COLUMN, STATE_COLUMN);
	}
	
	/**
	 * Returns the instance of this class. This should be used to get at all
	 * the cache's methods.
	 * 
	 * @return The only instance of this class.
	 */
	public static SurveyResponsePrivacyStateCache instance() {
		return _self;
	}
}