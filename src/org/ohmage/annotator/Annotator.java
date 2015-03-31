/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.annotator;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.util.StringUtils;


/**
 * This class handles the information regarding errors in the system. It
 * contains an error code and some human-readable description of the error.
 * 
 * @author John Jenkins
 */
public class Annotator {
	/**
	 * The JSON key to use to represent the error code.
	 */
	public static final String JSON_KEY_CODE = "code";
	/**
	 * The JSON key to use to represent the human-readable text for the error.
	 */
	public static final String JSON_KEY_TEXT = "text"; 
	
	/**
	 * The possible error codes.
	 * 
	 * @author John Jenkins
	 */
	public static enum ErrorCode {
		SYSTEM_GENERAL_ERROR ("0100"),
		SYSTEM_REQUEST_TOO_LARGE ("0101"),

		AUTHENTICATION_FAILED ("0200"),
		AUTHENTICATION_ACCOUNT_DISABLED ("0201"),
		AUTHENTICATION_NEW_ACCOUNT ("0202"),
		AUTHENTICATION_FAILED_BUT_NEW_ERROR_CODE_BECAUSE_WHY_NOT ("0203"),

		SERVER_INVALID_JSON ("0300"),
		SERVER_INVALID_CLIENT ("0301"),
		SERVER_INVALID_DATE ("0302"),
		SERVER_INVALID_TIMESTAMP ("0303"),
		SERVER_INVALID_TIME ("0304"),
		SERVER_INVALID_TIMEZONE ("0305"),
		SERVER_INVALID_LOCATION_STATUS ("0306"),
		SERVER_INVALID_LOCATION ("0307"),
		SERVER_INVALID_NUM_TO_SKIP ("0308"),
		SERVER_INVALID_NUM_TO_RETURN ("0309"),
		SERVER_INVALID_CAPTCHA ("0310"),
		SERVER_SELF_REGISTRATION_NOT_ALLOWED ("0311"),
		SERVER_INVALID_REDIRECT ("0312"),
		SERVER_INVALID_GZIP_DATA ("0313"),

		MOBILITY_INVALID_SUBTYPE ("0500"),
		MOBILITY_INVALID_MODE ("0501"),
		MOBILITY_INVALID_ACCELEROMETER_DATA ("0502"),
		MOBILITY_INVALID_SPEED ("0503"),
		MOBILITY_INSUFFICIENT_PERMISSIONS ("0504"),
		MOBILITY_INVALID_WIFI_DATA ("0505"),
		MOBILITY_INVALID_DATA ("0506"),
		MOBILITY_INVALID_ID ("0507"),
		MOBILITY_INVALID_CHUNK_DURATION ("0508"),
		MOBILITY_INVALID_INCLUDE_SENSOR_DATA_VALUE ("0509"),
		MOBILITY_INVALID_COLUMN_LIST ("0510"),
		MOBILITY_INVALID_PRIVACY_STATE ("0511"),
		MOBILITY_INVALID_AGGREGATE_DURATION ("0512"),

		SURVEY_INVALID_RESPONSES ("0600"),
		SURVEY_INVALID_OUTPUT_FORMAT ("0601"),
		SURVEY_INVALID_PRETTY_PRINT_VALUE ("0602"),
		SURVEY_INVALID_SURVEY_ID ("0603"),
		SURVEY_INVALID_PROMPT_ID ("0604"),
		SURVEY_INVALID_COLUMN_ID ("0605"),
		SURVEY_INVALID_PRIVACY_STATE ("0606"),
		SURVEY_INVALID_SORT_ORDER ("0607"),
		SURVEY_INVALID_SUPPRESS_METADATA_VALUE ("0608"),
		SURVEY_INVALID_RETURN_ID ("0609"),
		SURVEY_INVALID_COLLAPSE_VALUE ("0610"),
		SURVEY_INVALID_SURVEY_KEY_VALUE ("0611"),
		SURVEY_INVALID_SURVEY_FUNCTION_ID ("0612"),
		SURVEY_TOO_MANY_USERS ("0613"),
		SURVEY_NO_USERS ("0614"),
		SURVEY_TOO_MANY_PROMPT_IDS ("0615"),
		SURVEY_TOO_MANY_SURVEY_IDS ("0616"),
		SURVEY_INSUFFICIENT_PERMISSIONS ("0617"),
		SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY ("0618"),
		SURVEY_INVALID_LAUNCH_CONTEXT ("0619"),
		SURVEY_INVALID_LAUNCH_TIME ("0620"),
		SURVEY_MALFORMED_USER_LIST ("0621"),
		SURVEY_MALFORMED_PROMPT_ID_LIST ("0622"),
		SURVEY_MALFORMED_SURVEY_ID_LIST ("0623"),
		SURVEY_MALFORMED_COLUMN_LIST ("0624"),
		SURVEY_FUNCTION_INVALID_PRIVACY_STATE_GROUP_ITEM ("0625"),
		SURVEY_INVALID_REPEATABLE_SET_ID ("0626"),
		SURVEY_INVALID_REPEATABLE_SET_ITERATION ("0627"),
		SURVEY_INVALID_IMAGES_VALUE ("0628"),
		SURVEY_INVALID_PROMPT_RESPONSE_SEARCH ("0629"),
		SURVEY_INVALID_SURVEY_PROMPT_MAP ("0630"),
		SURVEY_DUPLICATE_RESOURCE_UUIDS ("0631"), // HT: when media or document uuids are duplicate

		CAMPAIGN_INVALID_ID ("0700"),
		CAMPAIGN_INVALID_NAME ("0701"),
		CAMPAIGN_INVALID_XML ("0702"),
		CAMPAIGN_INVALID_RUNNING_STATE ("0703"),
		CAMPAIGN_INVALID_PRIVACY_STATE ("0704"),
		CAMPAIGN_INVALID_OUTPUT_FORMAT ("0705"),
		CAMPAIGN_INVALID_ROLE ("0706"),
		CAMPAIGN_INSUFFICIENT_PERMISSIONS ("0707"),
		CAMPAIGN_XML_HEADER_CHANGED ("0708"),
		CAMPAIGN_INVALID_DESCRIPTION ("0709"),
		CAMPAIGN_OUT_OF_DATE ("0710"),
		CAMPAIGN_INVALID_AUTHORED_BY_VALUE ("0711"),
		CAMPAIGN_INVALID_SURVEY_ID ("O712"),
		CAMPAIGN_INVALID_PROMPT_ID ("0713"),

		IMAGE_INVALID_ID ("0800"),
		IMAGE_INVALID_SIZE ("0801"),
		IMAGE_INSUFFICIENT_PERMISSIONS ("0802"),
		IMAGE_INVALID_DATA ("0803"),

		CLASS_INVALID_ID ("0900"),
		CLASS_INVALID_NAME ("0901"),
		CLASS_INVALID_DESCRIPTION ("0902"),
		CLASS_INVALID_ROLE ("0903"),
		CLASS_INVALID_ROSTER ("0904"),
		CLASS_INSUFFICIENT_PERMISSIONS ("0905"),
		CLASS_INVALID_WITH_USER_LIST_VALUE ("0906"),

		USER_INVALID_USERNAME ("1000"),
		USER_INVALID_PASSWORD ("1001"),
		USER_INVALID_ADMIN_VALUE ("1002"),
		USER_INVALID_ENABLED_VALUE ("1003"),
		USER_INVALID_NEW_ACCOUNT_VALUE ("1004"),
		USER_INVALID_CAMPAIGN_CREATION_PRIVILEGE ("1005"),
		USER_INVALID_FIRST_NAME_VALUE ("1006"),
		USER_INVALID_LAST_NAME_VALUE ("1007"),
		USER_INVALID_ORGANIZATION_VALUE ("1008"),
		USER_INVALID_PERSONAL_ID_VALUE ("1009"),
		USER_INVALID_EMAIL_ADDRESS ("1010"),
		USER_INVALID_JSON_DATA ("1011"),
		USER_INSUFFICIENT_PERMISSIONS ("1012"),
		USER_NOT_IN_CAMPAIGN ("1013"),
		USER_INVALID_REGISTRATION_ID ("1014"),
		USER_INVALID_DELETE_PERSONAL_INFO ("1015"),
		USER_INVALID_CLASS_CREATION_PRIVILEGE ("1016"),
		USER_INVALID_USER_SETUP_PRIVILEGE ("1017"),

		DOCUMENT_INVALID_ID ("1100"),
		DOCUMENT_INVALID_NAME ("1101"),
		DOCUMENT_INVALID_CONTENTS ("1102"),
		DOCUMENT_INVALID_PRIVACY_STATE ("1103"),
		DOCUMENT_INVALID_DESCRIPTION ("1104"),
		DOCUMENT_INVALID_ROLE ("1105"),
		DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE ("1106"),
		DOCUMENT_INSUFFICIENT_PERMISSIONS ("1107"),
		DOCUMENT_MISSING_CAMPAIGN_AND_CLASS_ROLE_LISTS ("1108"),

		VISUALIZATION_INVALID_WIDTH_VALUE ("1200"),
		VISUALIZATION_INVALID_HEIGHT_VALUE ("1201"),
		VISUALIZATION_GENERAL_ERROR ("1202"),
		VISUALIZATION_INVALID_AGGREGATE_VALUE ("1203"),

		AUDIT_INSUFFICIENT_PERMISSIONS ("1300"),
		AUDIT_INVALID_REQUEST_TYPE ("1301"),
		AUDIT_INVALID_URI ("1302"),
		AUDIT_INVALID_CLIENT ("1303"),
		AUDIT_INVALID_DEVICE_ID ("1304"),
		AUDIT_INVALID_RESPONSE_TYPE ("1305"),
		AUDIT_INVALID_ERROR_CODE ("1306"),
		
		ANNOTATION_INVALID_TIME("1400"),
		ANNOTATION_INVALID_TIMEZONE("1401"),
		ANNOTATION_INVALID_ANNOTATION("1402"),
		ANNOTATION_INVALID_ID("1403"),
		ANNOTATION_INSUFFICIENT_PERMISSIONS("1404"),
		
		OBSERVER_INVALID_DEFINITION ("1500"),
		OBSERVER_INSUFFICIENT_PERMISSIONS ("1501"),
		OBSERVER_INVALID_ID ("1502"),
		OBSERVER_INVALID_VERSION ("1503"),
		OBSERVER_INVALID_NAME ("1504"),
		OBSERVER_INVALID_DESCRIPTION ("1505"),
		OBSERVER_INVALID_VERSION_STRING ("1506"),
		OBSERVER_INVALID_STREAM_ID ("1507"),
		OBSERVER_INVALID_STREAM_VERSION ("1508"),
		OBSERVER_INVALID_STREAM_NAME ("1509"),
		OBSERVER_INVALID_STREAM_DESCRIPTION ("1510"),
		OBSERVER_INVALID_STREAM_VERSION_STRING ("1511"),
		OBSERVER_INVALID_STREAM_DEFINITION ("1512"),
		OBSERVER_INVALID_STREAM_DATA ("1513"),
		OBSERVER_INVALID_COLUMN_LIST ("1514"),
		OBSERVER_INVALID_CHRONOLOGICAL_VALUE ("1515"),
		OBSERVER_INVALID_PRESERVE_INVALID_POINTS ("1516"),
		
		VIDEO_INVALID_ID("1600"),

		OMH_INVALID_PAYLOAD_ID ("1700"),
		OMH_INVALID_START_TIMESTAMP ("1701"),
		OMH_INVALID_END_TIMESTAMP ("1702"),
		OMH_INVALID_COLUMN_LIST ("1703"),
		OMH_INVALID_SUMMARIZE ("1704"),
		OMH_INVALID_NUM_TO_SKIP ("1705"),
		OMH_INVALID_NUM_TO_RETURN ("1706"),
		OMH_INVALID_REQUESTER ("1707"),
		OMH_INVALID_PAYLOAD_VERSION ("1708"),
		OMH_INVALID_OWNER ("1709"),
		OMH_INSUFFICIENT_PERMISSIONS ("1710"),
		OMH_ACCOUNT_NOT_LINKED ("1711"),
		OMH_INVALID_DATA ("1712"),

		AUDIO_INVALID_ID ("1800"),
		AUDIO_INVALID_DURATION ("1801"),
		AUDIO_INSUFFICIENT_PERMISSIONS ("1802"),
		AUDIO_INVALID_CONTENT ("1803"),
		
		DOCUMENTP_INVALID_ID ("1900");
		
		private final String value;
		
		/**
		 * Constructs a new error code enum with its error code value.
		 * 
		 * @param value The code's four character value.
		 */
		private ErrorCode(final String value) {
			this.value = value;
		}
		
		/**
		 * Converts an error code's four character value into an ErrorCode 
		 * object.
		 * 
		 * @param value The value is not valid.
		 * 
		 * @return An ErrorCode object that corresponds to the given error code
		 * 		   four digit value.
		 * 
		 * @throws IllegalArgumentException Thrown if no such error code 
		 * 									exists.
		 */
		public static ErrorCode getValue(final String value) {
			ErrorCode[] errorCodes = values();
			
			for(int i = 0; i < errorCodes.length; i++) {
				if(errorCodes[i].value.equals(value)) {
					return errorCodes[i];
				}
			}
			
			throw new IllegalArgumentException("Unknown value: " + value);
		}
		
		/**
		 * Returns the error code's four character value.
		 * 
		 * @return The code's four character value.
		 */
		@Override
		public String toString() {
			return value;
		}
	}
	private ErrorCode code;
	private String text;
	
	/**
	 * Default constructor. The default error response is a general server 
	 * error.
	 */
	public Annotator() {
		code = ErrorCode.SYSTEM_GENERAL_ERROR;
		text = "General server error.";
	}
	
	/**
	 * Sets the initial code and text with which to respond.
	 * 
	 * @param initialCode The initial code to use.
	 * 
	 * @param initialText The initial text with which to respond. This should
	 * 					  correlate with the 'initialCode'.
	 */
	public Annotator(final ErrorCode initialCode, final String initialText) {
		if(initialCode == null) {
			throw new IllegalArgumentException("The error code is null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(initialText)) {
			throw new IllegalArgumentException("The error text was empty or null.");
		}
		
		code = initialCode;
		text = initialText;
	}
	
	/**
	 * Updates the code and text for this annotator.
	 * 
	 * @param newCode The new code with which to respond.
	 * 
	 * @param newText The new text with which to respond. This should correlate
	 * 				  with the 'newCode'.
	 * 
	 * @throws IllegalArgumentException Thrown if the new code or text are 
	 * 									invalid.
	 */
	public void update(final ErrorCode newCode, final String newText) {
		if(newCode == null) {
			throw new IllegalArgumentException("The error code is null.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(newText)) {
			throw new IllegalArgumentException("The error text was empty or null.");
		}
		
		code = newCode;
		text = newText;
	}
	
	/**
	 * Returns the error code.
	 * 
	 * @return The error code.
	 */
	public ErrorCode getErrorCode() {
		return code;
	}
	
	/**
	 * Returns the error text.
	 * 
	 * @return The error text.
	 */
	public String getErrorText() {
		return text;
	}
	
	/**
	 * Creates a JSONObject that represents the error code and text.
	 * 
	 * @return A JSONObject that represents the error code and text. The key
	 * 		   for the code is {@value #JSON_KEY_CODE} and the key for the text
	 * 		   is {@value #JSON_KEY_TEXT}.
	 * 
	 * @throws JSONException Thrown if there is an error building the 
	 * 						 JSONObject.
	 */
	public JSONObject toJsonObject() throws JSONException {
		JSONObject result = new JSONObject();
		
		result.put(JSON_KEY_CODE, code.toString());
		result.put(JSON_KEY_TEXT, text);
		
		return result;
	}
}
