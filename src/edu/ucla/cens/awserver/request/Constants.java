package edu.ucla.cens.awserver.request;

/**
 * A true singleton that contains all the constants for the system.
 * 
 * @author John Jenkins
 */
public class Constants {
	/**
	 * Only constructor. Can never be instantiated.
	 */
	private Constants() {
		// Do nothing.
	}
	
	// Authentication Constants
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String AUTH_TOKEN = "auth_token";
	
	// Supplemental Information
	public static final String CLIENT = "client";
	
	// Upload Data
	public static final String DATA = "data";
	
	// Mobility Extras
	public static final String SUBTYPE = "subtype";
	
	// Temporal Constants
	public static final String DATE = "date";
	public static final String START_DATE = "start_date";
	public static final String END_DATE = "end_date";
	
	// Campaign Constants
	public static final String CAMPAIGN_URN = "campaign_urn";
	public static final String CAMPAIGN_URN_LIST = "campaign_urn_list";
	
	// Class Constants
	public static final String CLASS_URN = "class_urn";
	public static final String CLASS_URN_LIST = "class_urn_list";
	public static final String CLASSES = "classes"; // Should be removed in favor of CAMPAIGN_URN_LIST
	
	// Survey Constants
	public static final String SURVEY_ID = "survey_id";
	public static final String SURVEY_ID_LIST = "survey_id_list";
	public static final String SURVEY_FUNCTION_ID = "id"; // This should probably be more descriptive.
	
	// Prompt Constants
	public static final String PROMPT_ID = "prompt_id";
	public static final String PROMPT_ID_LIST = "prompt_id_list";
	public static final String IMAGE_ID = "image_id";
	
	// User Constants
	public static final String USER = "user";
	public static final String USER_LIST = "user_list";
	public static final String USER_ROLE = "user_role";
	
	// State Constants
	public static final String PRIVACY_STATE = "privacy_state";
	public static final String RUNNING_STATE = "running_state";
	
	// Output Constraints
	public static final String OUTPUT_FORMAT = "output_format";
	public static final String PRETTY_PRINT = "pretty_print";
	public static final String SORT_ORDER = "sort_order";
	public static final String SUPPRESS_METADATA = "suppress_metadata";
	public static final String COLUMN_LIST = "column_list";
}
