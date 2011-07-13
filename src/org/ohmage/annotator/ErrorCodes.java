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
	
	public static final String SYSTEM_JSON_SYNTAX_ERROR = "0101";
	public static final String SYSTEM_NO_DATA_IN_MESSAGE = "0102";
	public static final String SYSTEM_SERVER_ERROR = "0103";
	
	public static final String AUTHENTICATION_FAILED = "0200";
	public static final String AUTHENTICATION_ACCOUNT_DISABLED = "0201";
	public static final String AUTHENTICATION_NEW_ACCOUNT = "0202";
	public static final String AUTHENTICATION_LOOKING_UP_INFO_ABOUT_ANOTHER_USER = "0203";
	public static final String AUTHENTICATION_INSUFFICIENT_PERMISSIONS_TO_CREATE_CAMPAIGN = "0204";
	public static final String AUTHENTICATION_USER_DOES_NOT_BELONG_TO_ANY_CAMPAIGNS = "0205";
	public static final String AUTHENTICATION_USER_DOES_NOT_HAVE_PERMISSIONS_TO_MODIFY_CLASS = "0206";
	public static final String AUTHENTICATION_INVALID_NEW_PASSWORD = "0207";
	
	public static final String POST_MISSING_JSON_DATA = "0300";
	public static final String POST_UNKNOWN_REQUEST_TYPE = "0301";
	public static final String POST_MISSING_CLIENT = "0302";
	public static final String POST_INVALID_CAMPAIGN_ID = "0303";
	
	public static final String JSON_MISSING_OR_INVALID_DATE = "0400";
	public static final String JSON_MISSING_OR_INVALID_TIME = "0401";
	public static final String JSON_MISSING_OR_INVALID_TIMEZONE = "0402";
	public static final String JSON_MISSING_OR_INVALID_LOCATION = "0403";
	public static final String JSON_INVALID_LATITUDE = "0404";
	public static final String JSON_INVALID_LONGITUDE = "0405";
	public static final String JSON_INVALID_ACCURACY = "0406";
	public static final String JSON_INVALID_PROVIDER = "0407";
	public static final String JSON_INVALID_LOCATION_STATUS = "0408";
	public static final String JSON_LOCATION_EXISTENCE_PROBLEM = "0409";
	public static final String JSON_INVALID_LOCATION_TIMESTAMP = "0410";
	
	public static final String MOBILITY_MISSING_OR_INVALID_SUBTYPE = "0500";
	public static final String MOBILITY_MISSING_OR_INVALID_MODE = "0501";
	public static final String MOBILITY_MISSING_SENSOR_DATA = "0502";
	public static final String MOBILITY_MISSING_ACCEL_DATA = "0503";
	public static final String MOBILITY_MISSING_OR_INVALID_SPEED = "0504";
	
	public static final String SURVEY_UPDLOAD_MISSING_RESPONSES = "0602";
	public static final String SURVEY_UPDLOAD_INVALID_RESPONSES = "0603";
	public static final String SURVEY_UPDLOAD_INVALID_CAMPAIGN_ID = "0604";
	public static final String SURVEY_UPDLOAD_MISSING_OR_INVALID_SURVEY_ID = "0605";
	public static final String SURVEY_UPDLOAD_MISSING_OR_INVALID_SURVEY_LAUNCH_CONTEXT = "0606";
	public static final String SURVEY_UPDLOAD_INVALID_USER_ROLE = "0607";
	public static final String SURVEY_UPDLOAD_CAMPAIGN_STOPPED_REJECT_SURVEY = "0608";
	public static final String SURVEY_UPDLOAD_CAMPAIGN_OUT_OF_DATE = "0609";
	
	public static final String DATA_POINT_QUERY_MISSING_OR_INVALID_DATE = "0700";
	public static final String DATA_POINT_QUERY_INVALID_USER = "0701";
	public static final String DATA_POINT_QUERY_INVALID_CAMPAIGN = "0702";
	public static final String DATA_POINT_QUERY_INVALID_PROMPT_ID = "0703";
	
	public static final String SURVEY_RESPONSE_INVALID_USER_LIST = "0704";
	public static final String SURVEY_RESPONSE_PROMPT_OR_SURVEY_ID_LIST_ERROR = "0705";
	public static final String SURVEY_RESPONSE_INVALID_SURVEY_ID = "0707";
	public static final String SURVEY_RESPONSE_INVALID_COLUMN_ID = "0708";
	public static final String SURVEY_RESPONSE_USER_DOES_NOT_BELONG_TO_CAMPAIGN = "0709";
	public static final String SURVEY_RESPONSE_INVALID_OUTPUT_FORMAT = "0710";
	public static final String SURVEY_RESPONSE_MISSING_START_OR_END_DATE = "0711";
	public static final String SURVEY_RESPONSE_INVALID_PRETTY_PRINT_VALUE = "0712";
	public static final String SURVEY_RESPONSE_INVALID_RETURN_ID_VALUE = "0713";
	public static final String SURVEY_RESPONSE_INVALID_SUPPRESS_METADATA_VALUE = "0714";
	public static final String SURVEY_RESPONSE_INVALID_SORT_ORDER_VALUE = "0715";
	public static final String SURVEY_RESPONSE_CAMPAIGN_RUNNING_STATE_STOPPED = "0716";
	public static final String SURVEY_RESPONSE_PRIVACY_STATE_PRIVATE = "0717";
	public static final String SURVEY_RESPONSE_PARTICIPANT_QUERYING_OTHER_USERS_DATA = "0718";
	public static final String SURVEY_RESPONSE_INSUFFICIENT_PERMISSIONS_TO_CHANGE_PRIVACY_STATE = "0719";
	public static final String SURVEY_RESPONSE_INVALID_SUVEY_KEY_VALUE = "0720";
	public static final String SURVEY_RESPONSE_INVALID_COLLAPSE_VALUE = "0721";
	public static final String SURVEY_RESPONSE_INVALID_PROMPT_ID_VALUE = "0722";
	
	public static final String CAMPAIGN_NO_CAMPAIGNS_FOUND = "0800";
	public static final String CAMPAIGN_GENERAL_CREATION_FAILURE = "0801";
	public static final String CAMPAIGN_INVALID_INITIAL_RUNNING_STATE = "0802";
	public static final String CAMPAIGN_INVALID_INITIAL_PRIVACY_STATE = "0803";
	public static final String CAMPAIGN_INVALID_CAMPAIGN_XML = "0804";
	public static final String CAMPAIGN_UNABLE_TO_ADD_CLASS_TO_CAMPAIGN = "0805";
	public static final String CAMPAIGN_GENERAL_DELETION_FAILURE = "0806";
	public static final String CAMPAIGN_INVALID_URN = "0807";
	public static final String CAMPAIGN_NOT_FOUND = "0808";
	public static final String CAMPAIGN_NOT_ALLOWED_TO_DELETE = "0809";
	public static final String CAMPAIGN_INVALID_OUTPUT_FORMAT_FOR_READ = "0810";
	public static final String CAMPAIGN_INVALID_PRIVACY_STATE_FOR_READ = "0811";
	public static final String CAMPAIGN_INVALID_RUNNING_STATE_FOR_READ = "0812";
	public static final String CAMPAIGN_INVALID_USER_ROLE_FOR_READ = "0813";
	public static final String CAMPAIGN_INVALID_DESCRIPTION = "0814";
	public static final String CAMPAIGN_UPDATE_FAILED = "0815";
	public static final String CAMPAIGN_INSUFFICIENT_PERMISSIONS = "0816";
	public static final String CAMPAIGN_UNKNOWN_PROMPT_ID = "0817";
	public static final String CAMPAIGN_XML_HEADER_CHANGED = "0818";
	public static final String CAMPAIGN_USER_DOES_NOT_BELONG = "0819";
	public static final String CAMPAIGN_MISSING_XML = "0820";
	public static final String CAMPAIGN_MISSING_RUNNING_STATE = "0821";
	public static final String CAMPAIGN_MISSING_PRIVACY_STATE = "0822";
	public static final String CAMPAIGN_MISSING_CLASS_ID_LIST = "0823";
	
	public static final String IMAGE_NOT_FOUND = "0900";
	public static final String IMAGE_NOT_AN_IMAGE = "0901";
	public static final String IMAGE_INVALID_SIZE_PARAMETER = "0902";
	public static final String IMAGE_INVALID_UUID = "0903";
	
	public static final String DATA_POINT_FUNCTION_UNKNOWN_FUNCTION_NAME = "1100";;
	
	public static final String CLASS_GENERAL_ERROR = "1200";
	public static final String CLASS_INVALID_URN = "1201";
	public static final String CLASS_INVALID_NAME = "1202";
	public static final String CLASS_READ_ERROR = "1203";
	public static final String CLASS_INVALID_LIST = "1204";
	public static final String CLASS_UNKNOWN = "1205";
	public static final String CLASS_DOES_NOT_EXIST = "1206";
	public static final String CLASS_INSUFFICIENT_PERMISSIONS = "1207";
	public static final String CLASS_MISSING_CLASS_LIST = "1208";
	public static final String CLASS_ALREADY_EXISTS = "1209";
	public static final String CLASS_INVALID_ROSTER = "1210";
	public static final String CLASS_UNKNOWN_ROLE = "1211";
	public static final String CLASS_MISSING_ID = "1212";
	public static final String CLASS_MISSING_NAME_PARAMETER = "1213";
	public static final String CLASS_USER_DOES_NOT_BELONG = "1214";
	public static final String CLASS_INVALID_USER_CLASS_ROLE_LIST = "1215";

	public static final String USER_INVALID_USERNAME = "1300";
	public static final String USER_IN_LIST_DOES_NOT_EXIST = "1301";
	public static final String USER_DUPLICATE_IN_LISTS = "1302";
	public static final String USER_INSUFFICIENT_PERMISSIONS_TO_MODIFY_OTHER_USER_WITH_CAMPAIGN_ROLE = "1303";
	public static final String USER_ALREADY_HAS_ROLE = "1304";
	public static final String USER_PASSWORD_CHANGE_FAILURE = "1305";
	public static final String USER_MANIPULATION_ERROR = "1306";
	public static final String USER_DOES_NOT_EXIST = "1308";
	public static final String USER_INVALID_ENABLED_VALUE = "1310";
	public static final String USER_INVALID_ADMIN_VALUE = "1311";
	public static final String USER_INVALID_NEW_ACCOUNT_VALUE = "1312";
	public static final String USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE_VALUE = "1313";
	public static final String USER_NOT_ADMIN = "1314";
	public static final String USER_USERNAME_ALREADY_EXISTS = "1315";
	public static final String USER_CREATION_FAILED = "1316";
	public static final String USER_DELETION_FAILED = "1317";
	public static final String USER_INSUFFICIENT_PERMISSIONS_TO_MODIFY_USERS_IN_LIST = "1318";
	public static final String USER_UPDATE_FAILED = "1319";
	public static final String USER_INVALID_FIRST_NAME = "1320";
	public static final String USER_INVALID_LAST_NAME = "1321";
	public static final String USER_INVALID_ORGANIZATION = "1322";
	public static final String USER_INVALID_PERSONAL_IDENTIFIER = "1323";
	public static final String USER_INVALID_EMAIL_ADDRESS = "1324";
	public static final String USER_INVALID_USER_JSON_DATA = "1325";
	public static final String USER_MISSING_PERSONAL_INFORMATION = "1326";
	public static final String USER_INVALID_PASSWORD = "1327";
	public static final String USER_INVALID_TOKEN = "1328";
	
	public static final String ROLE_INVALID = "1400";
	
	public static final String DOCUMENT_GENERAL_ERROR = "1500";
	public static final String DOCUMENT_INVALID_NAME = "1502";
	public static final String DOCUMENT_INVALID_CONTENTS = "1503";
	public static final String DOCUMENT_INVALID_PRIVACY_STATE = "1504";
	public static final String DOCUMENT_INVALID_CAMPAIGN_ROLE_LIST = "1506";
	public static final String DOCUMENT_INVALID_CLASS_ROLE_LIST = "1507";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_ASSOCIATE_CAMPAIGN = "1508";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_ASSOCIATE_CLASS = "1509";
	public static final String DOCUMENT_DOES_NOT_EXIST = "1510";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_DELETE = "1511";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_READ = "1512";
	public static final String DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE = "1513";
	public static final String DOCUMENT_INSUFFICIENT_PERMISSIONS_TO_MODIFY = "1514";
	public static final String DOCUMENT_DOES_NOT_BELONG_TO_ENTITY = "1515";
	public static final String DOCUMENT_REQUESTER_GRANTING_MORE_PERMISSIONS_THAN_THEY_HAVE = "1516";
	public static final String DOCUMENT_INVALID_ROLE = "1517";
	public static final String DOCUMENT_MISSING_CAMPAIGN_AND_CLASS_ROLE_LISTS = "1518";
	public static final String DOCUMENT_MISSING_ID = "1519";
	public static final String DOCUMENT_INVALID_USER_ROLE_LIST = "1520";
	
	public static final String MALFORMED_UUID = "1600";
	public static final String MALFORMED_URN_ROLE_LIST = "1601";
	public static final String INVALID_BOOLEAN_VALUE = "1602";
	
	public static final String VISUALIZATION_GENERAL_ERROR = "1700";
	public static final String VISUALIZATION_INVALID_WIDTH_VALUE = "1701";
	public static final String VISUALIZATION_INVALID_HEIGHT_VALUE = "1702";
}