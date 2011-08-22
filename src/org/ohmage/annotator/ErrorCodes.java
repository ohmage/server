package org.ohmage.annotator;

/**
 * This class contains all of the possible error codes for responding to a 
 * request and are four digit Strings. When used in code, they should be paired
 * with a descriptive explanation of why the error is being thrown.
 * 
 * @author John Jenkins
 */
public final class ErrorCodes {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ErrorCodes() {}
	
	public static final String SYSTEM_GENERAL_ERROR = "0100";
	
	public static final String AUTHENTICATION_FAILED = "0200";
	public static final String AUTHENTICATION_ACCOUNT_DISABLED = "0201";
	public static final String AUTHENTICATION_NEW_ACCOUNT = "0202";
	
	public static final String SERVER_INVALID_JSON = "0300";
	public static final String SERVER_INVALID_CLIENT = "0301";
	public static final String SERVER_INVALID_DATE = "0302";
	public static final String SERVER_INVALID_TIMESTAMP = "0303";
	public static final String SERVER_INVALID_TIME = "0304";
	public static final String SERVER_INVALID_TIMEZONE = "0305";
	public static final String SERVER_INVALID_LOCATION_STATUS = "0306";
	public static final String SERVER_INVALID_LOCATION = "0307";
	
	public static final String MOBILITY_INVALID_SUBTYPE = "0500";
	public static final String MOBILITY_INVALID_MODE = "0501";
	public static final String MOBILITY_INVALID_ACCELEROMETER_DATA = "0502";
	public static final String MOBILITY_INVALID_SPEED = "0503";
	public static final String MOBILITY_INSUFFICIENT_PERMISSIONS = "0504";
	public static final String MOBILITY_INVALID_WIFI_DATA = "0505";
	public static final String MOBILITY_INVALID_DATA = "0506";
	
	public static final String SURVEY_INVALID_RESPONSES = "0600";
	public static final String SURVEY_INVALID_OUTPUT_FORMAT = "0601";
	public static final String SURVEY_INVALID_PRETTY_PRINT_VALUE = "0602";
	public static final String SURVEY_INVALID_SURVEY_ID = "0603";
	public static final String SURVEY_INVALID_PROMPT_ID = "0604";
	public static final String SURVEY_INVALID_COLUMN_ID = "0605";
	public static final String SURVEY_INVALID_PRIVACY_STATE = "0606";
	public static final String SURVEY_INVALID_SORT_ORDER = "0607";
	public static final String SURVEY_INVALID_SUPPRESS_METADATA_VALUE = "0608";
	public static final String SURVEY_INVALID_RETURN_ID = "0609";
	public static final String SURVEY_INVALID_COLLAPSE_VALUE = "0610";
	public static final String SURVEY_INVALID_SURVEY_KEY_VALUE = "0611";
	public static final String SURVEY_INVALID_SURVEY_FUNCTION_ID = "0612";
	public static final String SURVEY_TOO_MANY_USERS = "0613";
	public static final String SURVEY_NO_USERS = "0614";
	public static final String SURVEY_TOO_MANY_PROMPT_IDS = "0615";
	public static final String SURVEY_TOO_MANY_SURVEY_IDS = "0616";
	public static final String SURVEY_INSUFFICIENT_PERMISSIONS = "0617";
	public static final String SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY = "0618";
	public static final String SURVEY_INVALID_LAUNCH_CONTEXT = "0619";
	public static final String SURVEY_INVALID_LAUNCH_TIME = "0620";
	public static final String SURVEY_MALFORMED_USER_LIST = "0621";
	public static final String SURVEY_MALFORMED_PROMPT_ID_LIST = "0622";
	public static final String SURVEY_MALFORMED_SURVEY_ID_LIST = "0623";
	public static final String SURVEY_MALFORMED_COLUMN_LIST = "0624";
	
	public static final String CAMPAIGN_INVALID_ID = "0700";
	public static final String CAMPAIGN_INVALID_NAME = "0701";
	public static final String CAMPAIGN_INVALID_XML = "0702";
	public static final String CAMPAIGN_INVALID_RUNNING_STATE = "0703";
	public static final String CAMPAIGN_INVALID_PRIVACY_STATE = "0704";
	public static final String CAMPAIGN_INVALID_OUTPUT_FORMAT = "0705";
	public static final String CAMPAIGN_INVALID_ROLE = "0706";
	public static final String CAMPAIGN_INSUFFICIENT_PERMISSIONS = "0707";
	public static final String CAMPAIGN_XML_HEADER_CHANGED = "0708";
	public static final String CAMPAIGN_INVALID_DESCRIPTION = "0709";
	public static final String CAMPAIGN_OUT_OF_DATE = "0710";
	
	public static final String IMAGE_INVALID_ID = "0800";
	public static final String IMAGE_INVALID_SIZE = "0801";
	public static final String IMAGE_INSUFFICIENT_PERMISSIONS = "0802";
	public static final String IMAGE_INVALID_DATA = "0803";

	public static final String CLASS_INVALID_ID = "0900";
	public static final String CLASS_INVALID_NAME = "0901";
	public static final String CLASS_INVALID_DESCRIPTION = "0902";
	public static final String CLASS_INVALID_ROLE = "0903";
	public static final String CLASS_INVALID_ROSTER = "0904";
	public static final String CLASS_INSUFFICIENT_PERMISSIONS = "0905";

	public static final String USER_INVALID_USERNAME = "1000";
	public static final String USER_INVALID_PASSWORD = "1001";
	public static final String USER_INVALID_ADMIN_VALUE = "1002";
	public static final String USER_INVALID_ENABLED_VALUE = "1003";
	public static final String USER_INVALID_NEW_ACCOUNT_VALUE = "1004";
	public static final String USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE = "1005";
	public static final String USER_INVALID_FIRST_NAME_VALUE = "1006";
	public static final String USER_INVALID_LAST_NAME_VALUE = "1007";
	public static final String USER_INVALID_ORGANIZATION_VALUE = "1008";
	public static final String USER_INVALID_PERSONAL_ID_VALUE = "1009";
	public static final String USER_INVALID_EMAIL_ADDRESS = "1010";
	public static final String USER_INVALID_JSON_DATA = "1011";
	public static final String USER_INSUFFICIENT_PERMISSIONS = "1012";
	public static final String USER_NOT_IN_CAMPAIGN = "1013";
	
	public static final String DOCUMENT_INVALID_ID = "1100";
	public static final String DOCUMENT_INVALID_NAME = "1101";
	public static final String DOCUMENT_INVALID_CONTENTS = "1102";
	public static final String DOCUMENT_INVALID_PRIVACY_STATE = "1103";
	public static final String DOCUMENT_INVALID_DESCRIPTION = "1104";
	public static final String DOCUMENT_INVALID_ROLE = "1105";
	public static final String DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE = "1106";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS = "1107";
	public static final String DOCUMENT_MISSING_CAMPAIGN_AND_CLASS_ROLE_LISTS = "1108";
	
	public static final String VISUALIZATION_INVALID_WIDTH_VALUE = "1200";
	public static final String VISUALIZATION_INVALID_HEIGHT_VALUE = "1201";
	public static final String VISUALIZATION_GENERAL_ERROR = "1202";
	
	public static final String AUDIT_INSUFFICIENT_PERMISSIONS = "1300";
	public static final String AUDIT_INVALID_REQUEST_TYPE = "1301";
	public static final String AUDIT_INVALID_URI = "1302";
	public static final String AUDIT_INVALID_CLIENT = "1303";
	public static final String AUDIT_INVALID_DEVICE_ID = "1304";
	public static final String AUDIT_INVALID_RESPONSE_TYPE = "1305";
	public static final String AUDIT_INVALID_ERROR_CODE = "1306";
}