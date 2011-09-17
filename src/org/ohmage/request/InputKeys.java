/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.request;

/**
 * A true singleton that contains all the constants for the system.
 * 
 * @author John Jenkins
 */
public final class InputKeys {
	/**
	 * Only constructor. Can never be instantiated.
	 */
	private InputKeys() {
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
	public static final String CAMPAIGN_ROLE_LIST_ADD = "campaign_role_list_add";
	public static final String CAMPAIGN_LIST_REMOVE = "campaign_list_remove";
	public static final String XML = "xml";
	public static final String CAMPAIGN_CREATION_TIMESTAMP = "campaign_creation_timestamp";
	
	// Class Constants
	public static final String CLASS_URN = "class_urn";
	public static final String CLASS_URN_LIST = "class_urn_list";
	public static final String CLASS_ROLE_LIST_ADD = "class_role_list_add";
	public static final String CLASS_LIST_REMOVE = "class_list_remove";
	public static final String CLASS_NAME = "class_name";
	public static final String ROSTER = "roster";
	
	// Survey Constants
	public static final String SURVEY_ID = "survey_id";
	public static final String SURVEY_ID_LIST = "survey_id_list";
	public static final String SURVEY_FUNCTION_ID = "id"; // This should probably be more descriptive.
	public static final String SURVEY_KEY = "survey_key";
	public static final String SURVEY_RESPONSE_OWNER = "owner";
	public static final String SURVEYS = "surveys";
	
	// Prompt Constants
	public static final String PROMPT_ID = "prompt_id";
	public static final String PROMPT2_ID = "prompt2_id";
	public static final String PROMPT_ID_LIST = "prompt_id_list";
	public static final String IMAGE_ID = "id";
	
	// Image Constants
	public static final String IMAGE_OWNER = "owner";
	public static final String IMAGE_SIZE = "size";
	
	// User Constants
	public static final String USER = "user";
	public static final String NEW_USERNAME = "new_username";
	public static final String NEW_PASSWORD = "new_password";
	public static final String USER_LIST = "user_list";
	public static final String USER_LIST_ADD = "user_list_add";
	public static final String USER_LIST_REMOVE = "user_list_remove";
	public static final String PRIVILEGED_USER_LIST_ADD = "privileged_user_list_add";
	public static final String USER_ROLE = "user_role";
	public static final String USER_ROLE_LIST_ADD = "user_role_list_add";
	public static final String USER_ROLE_LIST_REMOVE = "user_role_list_remove";
	public static final String USER_ENABLED = "enabled";
	public static final String USER_ADMIN = "admin";
	public static final String NEW_ACCOUNT = "new_account";
	public static final String CAMPAIGN_CREATION_PRIVILEGE = "campaign_creation_privilege";
	public static final String FIRST_NAME = "first_name";
	public static final String LAST_NAME = "last_name";
	public static final String ORGANIZATION = "organization";
	public static final String PERSONAL_ID = "personal_id";
	public static final String EMAIL_ADDRESS = "email_address";
	public static final String USER_JSON_DATA = "json_data"; 
	
	// State Constants
	public static final String PRIVACY_STATE = "privacy_state";
	public static final String RUNNING_STATE = "running_state";
	
	// Output Constraints
	public static final String OUTPUT_FORMAT = "output_format";
	public static final String PRETTY_PRINT = "pretty_print";
	public static final String SORT_ORDER = "sort_order";
	public static final String SORT_ORDER_USER = "user";
	public static final String SORT_ORDER_TIMESTAMP = "timestamp";
	public static final String SORT_ORDER_SURVEY = "survey";
	public static final String SUPPRESS_METADATA = "suppress_metadata";
	public static final String COLUMN_LIST = "column_list";
	public static final String RETURN_ID = "return_id";
	public static final String COLLAPSE = "collapse";
	
	// Shared Constants
	public static final String DESCRIPTION = "description";
	
	// Document Constants
	public static final String DOCUMENT = "document";
	public static final String DOCUMENT_ID = "document_id";
	public static final String DOCUMENT_ID_LIST = "document_id_list";
	public static final String DOCUMENT_NAME = "document_name";
	public static final String DOCUMENT_CAMPAIGN_ROLE_LIST = "document_campaign_role_list";
	public static final String DOCUMENT_CLASS_ROLE_LIST = "document_class_role_list";
	public static final String DOCUMENT_USER_ROLE_LIST = "document_user_role_list";
	public static final String DOCUMENT_PERSONAL_DOCUMENTS = "personal_documents";

	// Visualization Constants
	public static final String VISUALIZATION_WIDTH = "width";
	public static final String VISUALIZATION_HEIGHT = "height";

	// Audit Constants
	public static final String AUDIT_REQUEST_TYPE = "request_type";
	public static final String AUDIT_URI = "uri";
	public static final String AUDIT_CLIENT = "client_value";
	public static final String AUDIT_DEVICE_ID = "device_id_value";
	public static final String AUDIT_RESPONSE_TYPE = "response_type";
	public static final String AUDIT_ERROR_CODE = "error_code";
	public static final String AUDIT_START_DATE = "start_date";
	public static final String AUDIT_END_DATE = "end_date";
	
	// Separator Constants
	public static final String LIST_ITEM_SEPARATOR = ",";
	public static final String ENTITY_ROLE_SEPARATOR = ";";
}
