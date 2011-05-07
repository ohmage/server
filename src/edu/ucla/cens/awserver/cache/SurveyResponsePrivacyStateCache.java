package edu.ucla.cens.awserver.cache;

/**
 * Singleton lookup table for the indices and String values for survey 
 * response privacy states. It will only refresh the map when a lookup call is
 * being made and the lookup table has expired.
 * 
 * @author John Jenkins
 */
public class SurveyResponsePrivacyStateCache extends StringAndIdCache {
	// The SQL that will retrieve the desired values.
	private static final String SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES_AND_IDS = "SELECT id, privacy_state " +
																		  		 "FROM survey_response_privacy_state";
	
	// When we are requesting a cache in the Spring files, we use this
	// to reference which key we want.
	public static final String CACHE_KEY = "surveyResponsePrivacyStateCache";
	
	// Known survey response privacy states.
	public static final String PRIVACY_STATE_INVISIBLE = "invisible";
	public static final String PRIVACY_STATE_PRIVATE = "private";
	public static final String PRIVACY_STATE_SHARED = "shared";
	
	private static SurveyResponsePrivacyStateCache _self = new SurveyResponsePrivacyStateCache();
	
	/**
	 * Default constructor set private to make this a Singleton.
	 */
	private SurveyResponsePrivacyStateCache() {
		super(SQL_GET_SURVEY_RESPONSE_PRIVACY_STATES_AND_IDS);
	}
	
	public static SurveyResponsePrivacyStateCache instance() {
		return _self;
	}
}