package org.ohmage.request.survey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Location;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.Survey;
import org.ohmage.domain.campaign.SurveyItem;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.OutputFormat;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * <p>Allows a requester to read survey responses. Supervisors can read survey
 * responses anytime. Survey response owners (i.e., participants) can read
 * their own responses anytime. Authors can only read shared responses. 
 * Analysts can read shared responses only if the campaign is shared.</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#AUTH_TOKEN}</td>
 *     <td>The requesting user's authentication token.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaign URN to use when retrieving responses.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_LIST}</td>
 *     <td>A comma-separated list of usernames to retrieve responses for
 *         or the value {@value URN_SPECIAL_ALL}</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#COLUMN_LIST}</td>
 *     <td>A comma-separated list of the columns to retrieve responses for
 *         or the value {@value #_URN_SPECIAL_ALL}. If {@value #_URN_SPECIAL_ALL}
 *         is not used, the only allowed values are: 
 *         {@value org.ohmage.domain.campaign.SurveyResponse.ColumnKey#URN_CONTEXT_CLIENT},
 *         {@value #URN_CONTEXT_TIMESTAMP},
 *         {@value #URN_CONTEXT_TIMEZONE},
 *         {@value #URN_CONTEXT_UTC_TIMESTAMP},
 *         {@value #URN_CONTEXT_LAUNCH_CONTEXT_LONG},
 *         {@value #URN_CONTEXT_LAUNCH_CONTEXT_SHORT},
 *         {@value #URN_CONTEXT_LOCATION_STATUS},
 *         {@value #URN_CONTEXT_LOCATION_LONGITUDE},
 *         {@value #URN_CONTEXT_LOCATION_LATITUDE},
 *         {@value #URN_CONTEXT_LOCATION_TIMESTAMP},
 *         {@value #URN_CONTEXT_LOCATION_ACCURACY},
 *         {@value #URN_CONTEXT_LOCATION_PROVIDER},
 *         {@value #URN_USER_ID},
 *         {@value #URN_SURVEY_ID},
 *         {@value #URN_SURVEY_TITLE},
 *         {@value #URN_SURVEY_DESCRIPTION},
 *         {@value #URN_SURVEY_PRIVACY_STATE},
 *         {@value #URN_REPEATABLE_SET_ID},
 *         {@value #URN_REPEATABLE_SET_ITERATION},
 *         {@value #URN_PROMPT_RESPONSE}
 *         </td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OUTPUT_FORMAT}</td>
 *     <td>The desired output format of the results. Must be one of 
 *     {@value #_OUTPUT_FORMAT_JSON_ROWS}, {@value #_OUTPUT_FORMAT_JSON_COLUMNS},
 *     or, {@value #_OUTPUT_FORMAT_CSV}</td>
 *     <td>true</td>
 *   </tr>   
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT_ID_LIST}</td>
 *     <td>A comma-separated list of prompt ids to retrieve responses for
 *         or the value {@link #_URN_SPECIAL_ALL}. This key is only
 *         optional if {@value org.ohmage.request.InputKeys#SURVEY_ID_LIST}
 *         is not present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_ID_LIST}</td>
 *     <td>A comma-separated list of survey ids to retrieve responses for
 *         or the value {@link #_URN_SPECIAL_ALL}. This key is only
 *         optional if {@value org.ohmage.request.InputKeys#PROMPT_ID_LIST}
 *         is not present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The start date to use for results between dates.
 *         Required if end date is present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The end date to use for results between dates.
 *         Required if start date is present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SORT_ORDER}</td>
 *     <td>The sort order to use i.e., the SQL order by.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SUPPRESS_METADATA}</td>
 *     <td>For {@value #_OUTPUT_FORMAT_CSV} output, whether to suppress the
 *     metadata section from the output</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRETTY_PRINT}</td>
 *     <td>For JSON-based output, whether to pretty print the output</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#RETURN_ID}</td>
 *     <td>For {@value #_OUTPUT_FORMAT_JSON_ROWS} output, whether to return
 *     the id on each result. The web front-end uses the id value to perform
 *     updates.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>Filters the results by their associated privacy state.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#COLLAPSE}</td>
 *     <td>Filters the results by uniqueness.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseReadRequest.class);
	
	/**
	 * The JSON key for the metadata associated with a response read request.
	 */
	public static final String JSON_KEY_METADATA = "metadata";
	/**
	 * The, optional, additional JSON key associated with a prompt responses in
	 * the 
	 * {@link org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_COLUMNS JSON_COLUMNS} 
	 * format representing additional information about the prompt's responses.
	 * 
	 * @see org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_COLUMNS
	 */
	public static final String JSON_KEY_CONTEXT = "context";
	/**
	 * The JSON key associated with a prompt responses in the 
	 * {@link org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_COLUMNS JSON_COLUMNS} 
	 * format representing the values for the prompt response.
	 * 
	 * @see org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_COLUMNS
	 */
	public static final String JSON_KEY_VALUES = "values";
	/**
	 * The JSON key in the metadata for 
	 * {@link org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS JSON_ROWS}
	 * representing the number of unique surveys in the results. 
	 * 
	 * @see org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS
	 */
	public static final String JSON_KEY_NUM_SURVEYS = "number_of_surveys";
	/**
	 * The JSON key in the metadata for 
	 * {@link org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS JSON_ROWS}
	 * representing the number of unique prompts in the results. 
	 * 
	 * @see org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS
	 */
	public static final String JSON_KEY_NUM_PROMPTS = "number_of_prompts";
	/**
	 * The JSON key in the metadata for 
	 * {@link org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS JSON_ROWS}
	 * output representing the keys in the data portion of the response. 
	 * 
	 * @see org.ohmage.domain.campaign.SurveyResponse.OutputFormat#JSON_ROWS
	 */
	public static final String JSON_KEY_ITEMS = "items";
	/**
	 * The JSON key associated with every record if the input parameter
	 * {@link org.ohmage.request.InputKeys#COLLAPSE collapse} is true.
	 * 
	 * @see org.ohmage.request.InputKeys#COLLAPSE
	 */
	public static final String JSON_KEY_COUNT = "count";
	
	public static final String URN_SPECIAL_ALL = "urn:ohmage:special:all";
	public static final Collection<String> URN_SPECIAL_ALL_LIST;
	static {
		URN_SPECIAL_ALL_LIST = new HashSet<String>();
		URN_SPECIAL_ALL_LIST.add(URN_SPECIAL_ALL);
	}

	private static final int MAX_NUMBER_OF_USERS = 10;
	private static final int MAX_NUMBER_OF_SURVEYS = 10;
	private static final int MAX_NUMBER_OF_PROMPTS = 10;

	private final String campaignId;
	private final Collection<SurveyResponse.ColumnKey> columns;
	private final Collection<String> usernames;
	private final SurveyResponse.OutputFormat outputFormat;

	private final Collection<String> surveyIds;
	private final Collection<String> promptIds;

	private final Date startDate;
	private final Date endDate;

	private final List<SortParameter> sortOrder;

	private final SurveyResponse.PrivacyState privacyState;

	private final Boolean collapse;
	private final Boolean prettyPrint;
	private final Boolean returnId;
	private final Boolean suppressMetadata;
	
	private Campaign campaign;
	private List<SurveyResponse> surveyResponseList;
	
	/**
	 * Creates a survey response read request.
	 * 
	 * @param httpRequest  The request to retrieve parameters from.
	 */
	public SurveyResponseReadRequest(HttpServletRequest httpRequest) {
		// Handle user-password or token-based authentication
		super(httpRequest, TokenLocation.EITHER, false);

		String tCampaignId = null;
		Set<SurveyResponse.ColumnKey> tColumns = null;
		Set<String> tUsernames = null;
		SurveyResponse.OutputFormat tOutputFormat = null;

		Set<String> tSurveyIds = null;
		Set<String> tPromptIds = null;
		
		Date tStartDate = null;
		Date tEndDate = null;
		
		List<SortParameter> tSortOrder = null;
		
		SurveyResponse.PrivacyState tPrivacyState = null;

		Boolean tCollapse = null;
		Boolean tPrettyPrint = null;
		Boolean tReturnId = null;
		Boolean tSuppressMetadata = null;
		
		if(! isFailed()) {
		
			LOGGER.info("Creating a survey response read request.");
			String[] t;
			
			try {
				// Campaign ID
				t = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(t.length == 0) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The required campaign ID was not present: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("The required campaign ID was not present: " + InputKeys.CAMPAIGN_URN);
				}
				else if(t.length > 1) {
					setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
					throw new ValidationException("Multiple campaign IDs were found: " + InputKeys.CAMPAIGN_URN);
				}
				else {
					tCampaignId = CampaignValidators.validateCampaignId(t[0]);
					
					if(tCampaignId == null) {
						setFailed(ErrorCode.CAMPAIGN_INVALID_ID, "The required campaign ID was not present: " + InputKeys.CAMPAIGN_URN);
						throw new ValidationException("The required campaign ID was not present: " + InputKeys.CAMPAIGN_URN);
					}
				}
				
				// Column List
				t = getParameterValues(InputKeys.COLUMN_LIST);
				if(t.length == 0) {
					setFailed(ErrorCode.SURVEY_INVALID_COLUMN_ID, "The required column list was missing: " + InputKeys.COLUMN_LIST);
					throw new ValidationException("The required column list was missing: " + InputKeys.COLUMN_LIST);
				}
				else if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_COLUMN_ID, "Multiple column lists were given: " + InputKeys.COLUMN_LIST);
					throw new ValidationException("Multiple column lists were given: " + InputKeys.COLUMN_LIST);
				}
				else {
					tColumns = SurveyResponseValidators.validateColumnList(t[0]);
					
					if(tColumns == null) {
						setFailed(ErrorCode.SURVEY_INVALID_COLUMN_ID, "The required column list was missing: " + InputKeys.COLUMN_LIST);
						throw new ValidationException("The required column list was missing: " + InputKeys.COLUMN_LIST);
					}
				}
				
				// User List
				t = getParameterValues(InputKeys.USER_LIST);
				if(t.length == 0) {
					setFailed(ErrorCode.SURVEY_MALFORMED_USER_LIST, "The user list is missing: " + InputKeys.USER_LIST);
					throw new ValidationException("The user list is missing: " + InputKeys.USER_LIST);
				}
				else if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_MALFORMED_USER_LIST, "Mutliple user lists were given: " + InputKeys.USER_LIST);
					throw new ValidationException("Mutliple user lists were given: " + InputKeys.USER_LIST);
				}
				else {
					tUsernames = SurveyResponseValidators.validateUsernames(t[0]);
					
					if(tUsernames == null) {
						setFailed(ErrorCode.SURVEY_MALFORMED_USER_LIST, "The user list is missing: " + InputKeys.USER_LIST);
						throw new ValidationException("The user list is missing: " + InputKeys.USER_LIST);
					}
					else if(tUsernames.size() > MAX_NUMBER_OF_USERS) {
						setFailed(ErrorCode.SURVEY_TOO_MANY_USERS, "The user list contains more than " + MAX_NUMBER_OF_USERS + " users: " + tUsernames.size());
						throw new ValidationException("The user list contains more than " + MAX_NUMBER_OF_USERS + " users: " + tUsernames.size());
					}
				}
				
				// Output Format
				t = getParameterValues(InputKeys.OUTPUT_FORMAT);
				if(t.length == 0) {
					setFailed(ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, "The output format is missing: " + InputKeys.OUTPUT_FORMAT);
					throw new ValidationException("The output format is missing: " + InputKeys.OUTPUT_FORMAT);
				}
				else if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, "Multiple output formats were given: " + InputKeys.OUTPUT_FORMAT);
					throw new ValidationException("Multiple output formats were given: " + InputKeys.OUTPUT_FORMAT);
				}
				else {
					tOutputFormat = SurveyResponseValidators.validateOutputFormat(t[0]);
					
					if(tOutputFormat == null) {
						setFailed(ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, "The output format is missing: " + InputKeys.OUTPUT_FORMAT);
						throw new ValidationException("The output format is missing: " + InputKeys.OUTPUT_FORMAT);
					}
				}
				
				// Survey ID List
				t = getParameterValues(InputKeys.SURVEY_ID_LIST);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_MALFORMED_SURVEY_ID_LIST, "Multiple survey ID lists were given: " + InputKeys.SURVEY_ID_LIST);
					throw new ValidationException("Multiple survey ID lists were given: " + InputKeys.SURVEY_ID_LIST);
				}
				else if(t.length == 1) {
					tSurveyIds = SurveyResponseValidators.validateSurveyIds(t[0]);
					
					if((tSurveyIds != null) && (tSurveyIds.size() > MAX_NUMBER_OF_SURVEYS)) {
						setFailed(ErrorCode.SURVEY_TOO_MANY_SURVEY_IDS, "More than " + MAX_NUMBER_OF_SURVEYS + " survey IDs were given: " + tSurveyIds.size());
						throw new ValidationException("More than " + MAX_NUMBER_OF_SURVEYS + " survey IDs were given: " + tSurveyIds.size());
					}
				}
				
				// Prompt ID List
				t = getParameterValues(InputKeys.PROMPT_ID_LIST);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_MALFORMED_PROMPT_ID_LIST, "Multiple prompt ID lists were given: " + InputKeys.PROMPT_ID_LIST);
					throw new ValidationException("Multiple prompt ID lists were given: " + InputKeys.PROMPT_ID_LIST);
				}
				else if(t.length == 1) {
					tPromptIds = SurveyResponseValidators.validatePromptIds(t[0]);
					
					if((tPromptIds != null) && (tPromptIds.size() > MAX_NUMBER_OF_PROMPTS)) {
						setFailed(ErrorCode.SURVEY_TOO_MANY_PROMPT_IDS, "More than " + MAX_NUMBER_OF_PROMPTS + " prompt IDs were given: " + tPromptIds.size());
						throw new ValidationException("More than " + MAX_NUMBER_OF_PROMPTS + " prompt IDs were given: " + tPromptIds.size());
					}
				}
				
				// Survey ID List and Prompt ID List Presence Check
				if(((tSurveyIds == null) || (tSurveyIds.size() == 0)) && 
				   ((tPromptIds == null) || (tPromptIds.size() == 0))) {
					setFailed(ErrorCode.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, "A survey list (" + InputKeys.SURVEY_ID_LIST + ") or a prompt list (" + InputKeys.PROMPT_ID_LIST + ") must be given.");
					throw new ValidationException("A survey list (" + InputKeys.SURVEY_ID_LIST + ") or prompt list (" + InputKeys.PROMPT_ID_LIST + ") must be given.");
				}
				else if(((tSurveyIds != null) && (tSurveyIds.size() > 0)) && 
						((tPromptIds != null) && (tPromptIds.size() > 0))) {
					setFailed(ErrorCode.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, "Both a survey list (" + InputKeys.SURVEY_ID_LIST + ") and a prompt list (" + InputKeys.PROMPT_ID_LIST + ") must be given.");
					throw new ValidationException("Both a survey list (" + InputKeys.SURVEY_ID_LIST + ") and a prompt list (" + InputKeys.PROMPT_ID_LIST + ") must be given.");
				}
				
				// Start Date
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					setFailed(ErrorCode.SERVER_INVALID_DATE, "Multiple start dates were given: " + InputKeys.START_DATE);
					throw new ValidationException("Multiple start dates were given: " + InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = SurveyResponseValidators.validateStartDate(t[0]);
				}
				
				// End Date
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					setFailed(ErrorCode.SERVER_INVALID_DATE, "Multiple end dates were given: " + InputKeys.END_DATE);
					throw new ValidationException("Multiple end dates were given: " + InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = SurveyResponseValidators.validateEndDate(t[0]);
				}
				
				// Sort Order
				t = getParameterValues(InputKeys.SORT_ORDER);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_SORT_ORDER, "Multiple sort order lists were given: " + InputKeys.SORT_ORDER);
					throw new ValidationException("Multiple sort order lists were given: " + InputKeys.SORT_ORDER);
				}
				else if(t.length == 1) {
					tSortOrder = SurveyResponseValidators.validateSortOrder(t[0]);
				}
				
				// Privacy State
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_PRIVACY_STATE, "Multiple privacy state values were given: " + InputKeys.PRIVACY_STATE);
					throw new ValidationException("Multiple privacy state values were given: " + InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					tPrivacyState = SurveyResponseValidators.validatePrivacyState(t[0]);
				}
				
				// Collapse
				t = getParameterValues(InputKeys.COLLAPSE);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_COLLAPSE_VALUE, "Multiple collapse values were given: " + InputKeys.COLLAPSE);
					throw new ValidationException("Multiple collapse values were given: " + InputKeys.COLLAPSE);
				}
				else if(t.length == 1) {
					tCollapse = SurveyResponseValidators.validateCollapse(t[0]);
				}
				
				// Pretty print
				t = getParameterValues(InputKeys.PRETTY_PRINT);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_PRETTY_PRINT_VALUE, "Multiple pretty print values were given: " + InputKeys.PRETTY_PRINT);
					throw new ValidationException("Multiple pretty print values were given: " + InputKeys.PRETTY_PRINT);
				}
				else if(t.length == 1) {
					tPrettyPrint = SurveyResponseValidators.validatePrettyPrint(t[0]);
				}
				
				// Return ID
				t = getParameterValues(InputKeys.RETURN_ID);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_RETURN_ID, "Multiple return ID values were given: " + InputKeys.RETURN_ID);
					throw new ValidationException("Multiple return ID values were given: " + InputKeys.RETURN_ID);
				}
				else if(t.length == 1) {
					tReturnId = SurveyResponseValidators.validateReturnId(t[0]);
				}
				
				// Suppress metadata
				t = getParameterValues(InputKeys.SUPPRESS_METADATA);
				if(t.length > 1) {
					setFailed(ErrorCode.SURVEY_INVALID_SUPPRESS_METADATA_VALUE, "Multiple suppress metadata values were given: " + InputKeys.SUPPRESS_METADATA);
					throw new ValidationException("Multiple suppress metadata values were given: " + InputKeys.SUPPRESS_METADATA);
				}
				else if(t.length == 1) {
					tSuppressMetadata = SurveyResponseValidators.validateSuppressMetadata(t[0]);
				}
			}
			catch (ValidationException e) {
				e.failRequest(this);
				LOGGER.info(e);
			}
		}
		
		campaignId = tCampaignId;
		columns = tColumns;
		usernames = tUsernames;
		outputFormat = tOutputFormat;
		
		surveyIds = tSurveyIds;
		promptIds = tPromptIds;

		startDate = tStartDate;
		endDate = tEndDate;

		sortOrder = tSortOrder;

		privacyState = tPrivacyState;

		collapse = tCollapse;
		prettyPrint = tPrettyPrint;
		returnId = tReturnId;
		suppressMetadata = tSuppressMetadata;
	}
	
	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		
		LOGGER.info("Servicing a survey response read request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// This is not necessarily the case because the user may no longer
			// belong to the campaign but still want to see their data. This
			// should only check that the campaign exists.
			LOGGER.info("Verifying that requester belongs to the campaign specified by campaign ID.");
		    UserCampaignServices.instance().campaignExistsAndUserBelongs(campaignId, this.getUser().getUsername());
		    
		    
		    // We have removed this ACL check because it causes participants
		    // to not be able to view their own data. The bigger problem
		    // is that for "Browse Data" the front end always passes 
		    // user_list=urn:ohmage:special:all
		    
//		    LOGGER.info("Verifying that the requester has a role that allows reading of survey responses for each of the users in the list.");
//	    	UserCampaignServices.requesterCanViewUsersSurveyResponses(this, this.campaignUrn, this.getUser().getUsername(), (String[]) userList.toArray());
			
		    // The user may want to read survey responses from a user that no
		    // longer belongs to the campaign.
		    if(! usernames.equals(URN_SPECIAL_ALL_LIST)) {
		    	LOGGER.info("Checking the user list to make sure all of the users belong to the campaign ID.");
		    	UserCampaignServices.instance().verifyUsersExistInCampaign(campaignId, usernames);
		    }
		    
		    LOGGER.info("Retrieving campaign configuration.");
			campaign = CampaignServices.instance().getCampaign(campaignId);
			
			if((promptIds != null) && (! promptIds.isEmpty()) && (! URN_SPECIAL_ALL_LIST.equals(promptIds))) {
				LOGGER.info("Verifying that the prompt ids in the query belong to the campaign.");
				SurveyResponseReadServices.instance().verifyPromptIdsBelongToConfiguration(promptIds, campaign);
			}
			
			if((surveyIds != null) && (! surveyIds.isEmpty()) && (! URN_SPECIAL_ALL_LIST.equals(surveyIds))) {
				LOGGER.info("Verifying that the survey ids in the query belong to the campaign.");
				SurveyResponseReadServices.instance().verifySurveyIdsBelongToConfiguration(surveyIds, campaign);
			}
		    
			LOGGER.info("Dispatching to the data layer.");
			surveyResponseList = new LinkedList<SurveyResponse>();
			
			if(URN_SPECIAL_ALL_LIST.equals(usernames)) {
				surveyResponseList.addAll(
						SurveyResponseServices.instance().readSurveyResponseInformation(
								campaign, null, null, 
								startDate, endDate, privacyState, 
								(URN_SPECIAL_ALL_LIST.equals(surveyIds)) ? null : surveyIds, 
								(URN_SPECIAL_ALL_LIST.equals(promptIds)) ? null : promptIds, 
								null
							)
					);
			}
			else {
				for(String username : usernames) {
					surveyResponseList.addAll(
							SurveyResponseServices.instance().readSurveyResponseInformation(
									campaign, username, null, 
									startDate, endDate, privacyState, 
									(URN_SPECIAL_ALL_LIST.equals(surveyIds)) ? null : surveyIds, 
									(URN_SPECIAL_ALL_LIST.equals(promptIds)) ? null : promptIds, 
									null
								)
						);
				}
			}
			
			LOGGER.info("Found " + surveyResponseList.size() + " results");
						
			LOGGER.info("Filtering survey response results according to our privacy rules and the requester's role.");
			SurveyResponseReadServices.instance().performPrivacyFilter(this.getUser().getUsername(), campaignId, surveyResponseList);
			
			LOGGER.info("Found " + surveyResponseList.size() + " results after filtering.");
			
			if(sortOrder != null) {
				LOGGER.info("Sorting the results.");
				Collections.sort(
						surveyResponseList, 
						new Comparator<SurveyResponse>() {
							@Override
							public int compare(
									SurveyResponse o1,
									SurveyResponse o2) {
								
								for(SortParameter sortParameter : sortOrder) {
									switch(sortParameter) {
									
									case SURVEY:
										if(! o1.getSurvey().equals(o2.getSurvey())) {
											return o1.getSurvey().getId().compareTo(o2.getSurvey().getId());
										}
										break;
										
									case TIMESTAMP:
										if(o1.getTime() != o2.getTime()) {
											if((o1.getTime() - o2.getTime()) < 0) {
												return -1;
											}
											else {
												return 1;
											}
										}
										break;
										
									case USER:
										if(! o1.getUsername().equals(o2.getUsername())) {
											return o1.getUsername().compareTo(o2.getUsername());
										}
										break;
									}
								}
								return 0;
							}
						}
					);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Builds the output depending on the state of this request and whatever
	 * output format the requester selected.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the survey response read request.");
		
		// Create a writer for the HTTP response object.
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
		}
		catch(IOException e) {
			LOGGER.error("Unable to write response message. Aborting.", e);
			return;
		}
		
		// Sets the HTTP headers to disable caching.
		expireResponse(httpResponse);
		
		String resultString = "";
		
		if(! isFailed()) {
			try {
				boolean allColumns = columns.equals(URN_SPECIAL_ALL_LIST);
			
				// TODO: I am fairly confident that these two branches can be
				// merged further and subsequently cleaned up, but I don't have
				// the time to tackle it right now.
				if(OutputFormat.JSON_ROWS.equals(outputFormat)) {
					httpResponse.setContentType("text/html");
					
					JSONObject result = new JSONObject();
					result.put(JSON_KEY_RESULT, RESULT_SUCCESS);
					
					List<String> uniqueSurveyIds = new LinkedList<String>();
					List<String> uniquePromptIds = new LinkedList<String>();
					JSONArray results = new JSONArray();
					for(SurveyResponse surveyResponse : surveyResponseList) {
						uniqueSurveyIds.add(surveyResponse.getSurvey().getId());
						uniquePromptIds.addAll(surveyResponse.getPromptIds());
						
						JSONObject currResult = surveyResponse.toJson(
								allColumns || columns.contains(ColumnKey.USER_ID),
								allColumns || false,
								allColumns || columns.contains(ColumnKey.CONTEXT_CLIENT),
								allColumns || columns.contains(ColumnKey.SURVEY_PRIVACY_STATE),
								allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP),
								allColumns || false,
								allColumns || columns.contains(ColumnKey.CONTEXT_TIMEZONE),
								allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_STATUS),
								false,
								allColumns || columns.contains(ColumnKey.SURVEY_ID),
								allColumns || columns.contains(ColumnKey.SURVEY_TITLE),
								allColumns || columns.contains(ColumnKey.SURVEY_DESCRIPTION),
								allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT),
								allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG),
								allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE),
								false,
								((returnId == null) ? false : returnId)
							);
						
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_ACCURACY, "null");
							}
							else {
								double accuracy = location.getAccuracy();
								
								if(accuracy == Double.POSITIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_ACCURACY, "Infinity");
								}
								else if(accuracy == Double.NEGATIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_ACCURACY, "-Infinity");
								}
								else if(accuracy == Double.NaN) {
									currResult.put(Location.JSON_KEY_ACCURACY, "NaN");
								}
								else {
									currResult.put(Location.JSON_KEY_ACCURACY, accuracy);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_LATITUDE, "null");
							}
							else {
								double latitude = location.getLatitude();
								
								if(latitude == Double.POSITIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_LATITUDE, "Infinity");
								}
								else if(latitude == Double.NEGATIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_LATITUDE, "-Infinity");
								}
								else if(latitude == Double.NaN) {
									currResult.put(Location.JSON_KEY_LATITUDE, "NaN");
								}
								else {
									currResult.put(Location.JSON_KEY_LATITUDE, latitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_LONGITUDE, "null");
							}
							else {
								double longitude = location.getLongitude();
								
								if(longitude == Double.POSITIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_LONGITUDE, "Infinity");
								}
								else if(longitude == Double.NEGATIVE_INFINITY) {
									currResult.put(Location.JSON_KEY_LONGITUDE, "-Infinity");
								}
								else if(longitude == Double.NaN) {
									currResult.put(Location.JSON_KEY_LONGITUDE, "NaN");
								}
								else {
									currResult.put(Location.JSON_KEY_LONGITUDE, longitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_PROVIDER, "null");
							}
							else {
								currResult.put(Location.JSON_KEY_PROVIDER, location.getProvider());
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_OUTPUT_TIMESTAMP, "null");
							}
							else {
								currResult.put(Location.JSON_KEY_OUTPUT_TIMESTAMP, TimeUtils.getIso8601DateTimeString(location.getTimestamp()));
							}
						}
						
						results.put(currResult);
					}
					
					if((collapse != null) && collapse) {
						int numResults = results.length();
						Map<JSONObject, Integer> collapsedMap = 
							new HashMap<JSONObject, Integer>(numResults);
						
						for(int i = 0; i < numResults; i++) {
							JSONObject currResult = results.getJSONObject(i);
							Integer prevCount = collapsedMap.put(currResult, 1);
							if(prevCount != null) {
								collapsedMap.put(currResult, prevCount + 1);
								results.remove(i);
								i--;
								numResults--;
							}
						}
						
						for(int i = 0; i < numResults; i++) {
							JSONObject currResult = results.getJSONObject(i);
							currResult.put(JSON_KEY_COUNT, collapsedMap.get(currResult));
						}
					}
					result.put(JSON_KEY_DATA, results);
					
					// Metadata
					if((suppressMetadata == null) || (! suppressMetadata)) {
						JSONObject metadata = new JSONObject();
						
						metadata.put(JSON_KEY_NUM_SURVEYS, uniqueSurveyIds.size());
						metadata.put(JSON_KEY_NUM_PROMPTS, uniquePromptIds.size());
						
						Collection<String> columnsResult = 
							new HashSet<String>(columns.size());
						
						// If it contains the special 'all' value, add them 
						// all.
						if(columns.contains(URN_SPECIAL_ALL)) {
							ColumnKey[] values = SurveyResponse.ColumnKey.values();
							for(int i = 0; i < values.length; i++) {
								columnsResult.add(values[i].toString());
							}
						}
						// Otherwise, add cycle through them 
						else {
							for(ColumnKey columnKey : columns) {
								columnsResult.add(columnKey.toString());
							}
						}
						
						// Check if prompt responses were requested, and, if 
						// so, add them to the list of columns.
						if(columns.contains(SurveyResponse.ColumnKey.PROMPT_RESPONSE) ||
								columns.contains(URN_SPECIAL_ALL)) {
							
							for(String promptId : uniquePromptIds) {
								columnsResult.add(ColumnKey.URN_PROMPT_ID_PREFIX + promptId);
							}
						}
						
						// Add it to the metadata result.
						metadata.put(JSON_KEY_ITEMS, columnsResult);
						
						result.put(JSON_KEY_METADATA, metadata);
					}
					
					if((prettyPrint != null) && prettyPrint) {
						resultString = result.toString(4);
					}
					else {
						resultString = result.toString();
					}
				}
				else if(OutputFormat.JSON_COLUMNS.equals(outputFormat) || 
						OutputFormat.CSV.equals(outputFormat)) {
					
					JSONArray usernames = new JSONArray();
					JSONArray clients = new JSONArray();
					JSONArray privacyStates = new JSONArray();
					JSONArray timestamps = new JSONArray();
					JSONArray timezones = new JSONArray();
					JSONArray locationStatuses = new JSONArray();
					JSONArray locationLongitude = new JSONArray();
					JSONArray locationLatitude = new JSONArray();
					JSONArray locationTimestamp = new JSONArray();
					JSONArray locationAccuracy = new JSONArray();
					JSONArray locationProvider = new JSONArray();
					JSONArray surveyIds = new JSONArray();
					JSONArray surveyTitles = new JSONArray();
					JSONArray surveyDescriptions = new JSONArray();
					JSONArray launchContexts = new JSONArray();
					Map<String, JSONObject> prompts = new HashMap<String, JSONObject>();
					
					if(surveyResponseList.isEmpty()) {
						if(this.surveyIds != null) {
							Map<String, Survey> campaignSurveys = campaign.getSurveys();
							if(this.surveyIds.equals(URN_SPECIAL_ALL_LIST)) {
								for(Survey currSurvey : campaignSurveys.values()) {
									populatePrompts(currSurvey.getSurveyItems(), prompts);
								}
							}
							else {
								for(String surveyId : this.surveyIds) {
									populatePrompts(campaignSurveys.get(surveyId).getSurveyItems(), prompts);
								}
							}
						}
						else if(promptIds != null) {
							int currNumPrompts = 0;
							Map<Integer, SurveyItem> tempPromptMap = new HashMap<Integer, SurveyItem>(promptIds.size());
							for(String promptId : promptIds) {
								tempPromptMap.put(currNumPrompts, campaign.getPrompt(campaign.getSurveyIdForPromptId(promptId), promptId));
								currNumPrompts++;
							}
							
							populatePrompts(tempPromptMap, prompts);
						}
					}
					else {
						Set<String> unqiueSurveyIds = new HashSet<String>();
						Set<String> uniquePromptIds = new HashSet<String>();
						for(SurveyResponse surveyResponse : surveyResponseList) {
							String surveyId = surveyResponse.getSurvey().getId();
							
							if(! unqiueSurveyIds.contains(surveyId)) {
								unqiueSurveyIds.add(surveyId);
								
								int currNumPrompts = 0;
								Set<String> promptIds = surveyResponse.getPromptIds();
								Map<Integer, SurveyItem> tempPrompts =
									new HashMap<Integer, SurveyItem>(promptIds.size());
								for(String promptId : promptIds) {
									if(! uniquePromptIds.contains(promptId)) {
										tempPrompts.put(currNumPrompts, campaign.getPrompt(surveyId, promptId));
										currNumPrompts++;
									}
								}
								
								populatePrompts(tempPrompts, prompts);
							}
						}
					}
					
					int numResponses = 0;
					for(SurveyResponse surveyResponse : surveyResponseList) {
						numResponses += processResponses(allColumns, 
								surveyResponse, 
								surveyResponse.getResponses(), 
								prompts, 
								usernames, clients, privacyStates, 
								timestamps, timezones, 
								locationStatuses, locationLongitude, 
								locationLatitude, locationTimestamp, 
								locationAccuracy, locationProvider,
								surveyIds, surveyTitles, surveyDescriptions, 
								launchContexts
							);
					}
					int promptCount = numResponses;
					
					// Add all of the applicable output stuff.
					JSONObject result = new JSONObject();
					
					if(allColumns || columns.contains(ColumnKey.USER_ID)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, usernames);
						result.put(ColumnKey.USER_ID.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_CLIENT)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, clients);
						result.put(ColumnKey.CONTEXT_CLIENT.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_PRIVACY_STATE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, privacyStates);
						result.put(ColumnKey.SURVEY_PRIVACY_STATE.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, timestamps);
						result.put(ColumnKey.CONTEXT_TIMESTAMP.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMEZONE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, timezones);
						result.put(ColumnKey.CONTEXT_TIMEZONE.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_STATUS)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationStatuses);
						result.put(ColumnKey.CONTEXT_LOCATION_STATUS.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationLongitude);
						result.put(ColumnKey.CONTEXT_LOCATION_LONGITUDE.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationLatitude);
						result.put(ColumnKey.CONTEXT_LOCATION_LATITUDE.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationTimestamp);
						result.put(ColumnKey.CONTEXT_LOCATION_TIMESTAMP.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationAccuracy);
						result.put(ColumnKey.CONTEXT_LOCATION_ACCURACY.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationProvider);
						result.put(ColumnKey.CONTEXT_LOCATION_PROVIDER.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_ID)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyIds);
						result.put(ColumnKey.SURVEY_ID.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_TITLE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyTitles);
						result.put(ColumnKey.SURVEY_TITLE.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_DESCRIPTION)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyDescriptions);
						result.put(ColumnKey.SURVEY_DESCRIPTION.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, launchContexts);
						result.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG.toString(), values);
					}
					if(columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, launchContexts);
						result.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT.toString(), values);
					}
					if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
						for(String promptId : prompts.keySet()) {
							result.put(
									SurveyResponse.ColumnKey.URN_PROMPT_ID_PREFIX + promptId, 
									prompts.get(promptId));
						}
					}
					
					if((collapse != null) && collapse) {
						JSONArray keys = new JSONArray();
						Iterator<?> keysIter = result.keys();
						while(keysIter.hasNext()) {
							keys.put(keysIter.next());
						}
						int keyLength = keys.length();
						
						JSONArray counts = new JSONArray();
						for(int i = 0; i < numResponses; i++) {
							counts.put(1);
						}
						JSONObject countObject = new JSONObject();
						countObject.put(JSON_KEY_VALUES, counts);
						result.put("urn:ohmage:context:count", countObject);
						keys.put("urn:ohmage:context:count");
						
						Map<String, Integer> valueToIndexMap = new HashMap<String, Integer>();
						for(int i = 0; i < numResponses; i++) {
							StringBuilder currResultBuilder = new StringBuilder();
							
							for(int j = 0; j < keyLength; j++) {
								currResultBuilder.append(result.getJSONObject(keys.getString(j)).getJSONArray(JSON_KEY_VALUES).get(i));
								
								if((j + 1) != keyLength) {
									currResultBuilder.append(',');
								}
							}
							
							String currResultString = currResultBuilder.toString();
							if(valueToIndexMap.containsKey(currResultString)) {
								int count = result.getJSONObject("urn:ohmage:context:count").getJSONArray(JSON_KEY_VALUES).getInt(valueToIndexMap.get(currResultString)) + 1;
								result.getJSONObject("urn:ohmage:context:count").getJSONArray(JSON_KEY_VALUES).put(valueToIndexMap.get(currResultString).intValue(), count);
								
								for(int j = 0; j < keyLength + 1; j++) {
									result.getJSONObject(keys.getString(j)).getJSONArray(JSON_KEY_VALUES).remove(i);
								}

								i--;
								numResponses--;
							}
							else {
								valueToIndexMap.put(currResultString, i);
							}
						}
					}
					
					JSONObject metadata = null;
					if((suppressMetadata == null) || (! suppressMetadata)) {
						metadata = new JSONObject();
						
						metadata.put(InputKeys.CAMPAIGN_URN, campaignId);
						metadata.put(JSON_KEY_NUM_SURVEYS, surveyResponseList.size());
						metadata.put(JSON_KEY_NUM_PROMPTS, promptCount);
					}
					
					if(OutputFormat.JSON_COLUMNS.equals(outputFormat)) {
						httpResponse.setContentType("text/html");
						
						JSONObject resultJson = new JSONObject();
						resultJson.put(JSON_KEY_RESULT, RESULT_SUCCESS);
						
						if((suppressMetadata == null) || (! suppressMetadata)) {
							JSONArray itemsJson = new JSONArray();
							Iterator<?> keys = result.keys();
							while(keys.hasNext()) {
								itemsJson.put(keys.next());
							}
							metadata.put("items", itemsJson);
							resultJson.put(JSON_KEY_METADATA, metadata);
						}
						
						resultJson.put(JSON_KEY_DATA, result);
						
						if((prettyPrint != null) && prettyPrint) {
							resultString = resultJson.toString(4);
						}
						else {
							resultString = resultJson.toString();
						}
					}
					else if(OutputFormat.CSV.equals(outputFormat)) {
						httpResponse.setContentType("text/csv");
						httpResponse.setHeader("Content-Disposition", "attachment; filename=SurveyResponses.csv");

						StringBuilder resultBuilder = new StringBuilder();
						
						if((suppressMetadata == null) || (! suppressMetadata)) {
							metadata.put(JSON_KEY_RESULT, RESULT_SUCCESS);
							
							resultBuilder.append("## begin metadata\n");
							resultBuilder.append('#').append(metadata.toString().replace(',', ';')).append('\n');
							resultBuilder.append("## end metadata\n");
						}
						
						// Prompt contexts.
						if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
							resultBuilder.append("## begin prompt contexts\n");
							for(String promptId : prompts.keySet()) {
								JSONObject promptJson = new JSONObject();
								
								promptJson.put(promptId, prompts.get(promptId).get(JSON_KEY_CONTEXT).toString());
								
								resultBuilder.append('#').append(promptJson.toString()).append('\n');
							}
							resultBuilder.append("## end prompt contexts\n");
						}
						
						// Data.
						resultBuilder.append("## begin data\n");

						JSONArray keys = new JSONArray();
						Iterator<?> keysIter = result.keys();
						while(keysIter.hasNext()) {
							keys.put(keysIter.next());
						}
						int keyLength = keys.length();
						
						resultBuilder.append('#');
						for(int i = 0; i < keyLength; i++) {
							String header = keys.getString(i);
							if(header.startsWith("urn:ohmage:")) {
								header = header.substring(11);
								
								if(header.startsWith("prompt:id:")) {
									header = header.substring(10);
								}
							}
							resultBuilder.append(header);
							
							if((i + 1) != keyLength) {
								resultBuilder.append(',');
							}
						}
						resultBuilder.append('\n');
						
						for(int i = 0; i < numResponses; i++) {
							for(int j = 0; j < keyLength; j++) {
								resultBuilder.append(result.getJSONObject(keys.getString(j)).getJSONArray(JSON_KEY_VALUES).get(i));
								
								if((j + 1) != keyLength) {
									resultBuilder.append(',');
								}
							}

							resultBuilder.append('\n');
						}
						
						resultBuilder.append("## end data");
						
						resultString = resultBuilder.toString();
					}
				}
			}
			catch(JSONException e) {
				LOGGER.error(e.toString(), e);
				setFailed();
			}
		}
		
		if(isFailed()) {
			httpResponse.setContentType("text/html");
			resultString = this.getFailureMessage();
		}
		
		try {
			writer.write(resultString);
		}
		catch(IOException e) {
			LOGGER.error("Unable to write response message. Aborting.", e);
		}
		
		try {
			writer.flush();
			writer.close();
		}
		catch(IOException e) {
			LOGGER.error("Unable to flush or close the writer.", e);
		}
	}
	
	/**
	 * Populates the prompts map with all of the prompts from all of the survey
	 * items. 
	 * 
	 * @param surveyItems The map of survey item indices to the survey item.
	 * 
	 * @param prompts The prompts to be populated with all of the prompts in
	 * 				  the survey item including all of the sub-prompts of 
	 * 				  repeatable sets.
	 * 
	 * @throws JSONException Thrown if there is an error building the JSON.
	 */
	private void populatePrompts(
			final Map<Integer, SurveyItem> surveyItems,
			Map<String, JSONObject> prompts) 
			throws JSONException {
		
		for(SurveyItem surveyItem : surveyItems.values()) {
	
			if(surveyItem instanceof Prompt) {
				Prompt prompt = (Prompt) surveyItem;
				
				JSONObject promptJson = new JSONObject();
				promptJson.put(JSON_KEY_CONTEXT, prompt.toJson());
				promptJson.put(JSON_KEY_VALUES, new JSONArray());
				
				prompts.put(prompt.getId(), promptJson);
			}
			else if(surveyItem instanceof RepeatableSet) {
				RepeatableSet repeatableSet = (RepeatableSet) surveyItem;
				populatePrompts(repeatableSet.getSurveyItems(), prompts);
			}
		}
	}
	
	/**
	 * Processes each of the responses in map to populate the JSONObjects by
	 * placing the value from the response into its corresponding JSONObject.
	 * 
	 * @param allColumns Whether or not to populate all JSONObjects.
	 * 
	 * @param surveyResponse The current survey response.
	 * 
	 * @param responses The map of response index from the survey response to
	 * 					the actual response.
	 * 
	 * @param surveys The map of survey IDs to Survey objects.
	 * 
	 * @param prompts The map of prompt IDs to Prompt objects.
	 * 
	 * @param usernames The usernames JSONArray.
	 * 
	 * @param clients The clients JSONArray.
	 * 
	 * @param privacyStates The privacy states JSONArray.
	 * 
	 * @param timestamps The timestamps JSONArray.
	 * 
	 * @param timezones The timezones JSONArray.
	 * 
	 * @param locationStatuses The location statuses JSONArray.
	 * 
	 * @param locations The locations JSONArray.
	 * 
	 * @param surveyIds The survey IDs JSONArray.
	 * 
	 * @param surveyTitles The survey titles JSONArray.
	 * 
	 * @param surveyDescriptions The survey description JSONArray.
	 * 
	 * @param launchContexts The launch contexts JSONArray.
	 * 
	 * @return The total number of prompt responses that were processed.
	 * 
	 * @throws JSONException Thrown if there is an error populating any of the
	 * 						 JSONArrays.
	 */
	private int processResponses(final boolean allColumns, 
			final SurveyResponse surveyResponse,
			final Map<Integer, Response> responses, 
			Map<String, JSONObject> prompts,
			JSONArray usernames, JSONArray clients, JSONArray privacyStates,
			JSONArray timestamps, JSONArray timezones,
			JSONArray locationStatuses, JSONArray locationLongitude,
			JSONArray locationLatitude, JSONArray locationTimestamp,
			JSONArray locationAccuracy, JSONArray locationProvider,
			JSONArray surveyIds, JSONArray surveyTitles, 
			JSONArray surveyDescriptions, JSONArray launchContexts) 
			throws JSONException {

		List<Integer> indices = new ArrayList<Integer>(responses.keySet());
		Collections.sort(indices);
		
		if(allColumns || columns.contains(ColumnKey.USER_ID)) {
			usernames.put(surveyResponse.getUsername());
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_CLIENT)) {
			clients.put(surveyResponse.getClient());
		}
		if(allColumns || columns.contains(ColumnKey.SURVEY_PRIVACY_STATE)) {
			privacyStates.put(surveyResponse.getPrivacyState().toString());
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
			timestamps.put(TimeUtils.getIso8601DateTimeString(surveyResponse.getDate()));
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMEZONE)) {
			timezones.put(surveyResponse.getTimezone().getID());
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_STATUS)) {
			locationStatuses.put(surveyResponse.getLocationStatus().toString());
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationLongitude.put("");
			}
			else {
				locationLongitude.put(location.getLongitude());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationLatitude.put("");
			}
			else {
				locationLatitude.put(location.getLatitude());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationTimestamp.put("");
			}
			else {
				locationTimestamp.put(TimeUtils.getIso8601DateTimeString(location.getTimestamp()));
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationAccuracy.put("");
			}
			else {
				locationAccuracy.put(location.getAccuracy());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationProvider.put("");
			}
			else {
				locationProvider.put(location.getProvider());
			}
		}
		if(allColumns || columns.contains(ColumnKey.SURVEY_ID)) {
			surveyIds.put(surveyResponse.getSurvey().getId());
		}
		if(allColumns || columns.contains(ColumnKey.SURVEY_TITLE)) {
			surveyTitles.put(surveyResponse.getSurvey().getTitle());
		}
		if(allColumns || columns.contains(ColumnKey.SURVEY_DESCRIPTION)) {
			surveyDescriptions.put(surveyResponse.getSurvey().getDescription());
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG) || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT)) {
			launchContexts.put(surveyResponse.getLaunchContext().toJson(allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG)));
		}
		
		int numResponses = 0;
		for(Integer index : indices) {
			if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
				Response response = responses.get(index);
				if(response instanceof PromptResponse) {
					numResponses++;
					JSONArray values =
						prompts.get(response.getId()).getJSONArray(JSON_KEY_VALUES);
					
					Object responseValue = response.getResponseValue();
					if(OutputFormat.CSV.equals(outputFormat)) {
						responseValue = "\"" + responseValue + "\"";
					}
					
					values.put(responseValue);
				}
				else if(response instanceof RepeatableSetResponse) {
					RepeatableSetResponse repeatableSetResponse =
						(RepeatableSetResponse) response;
					
					Map<Integer, Map<Integer, Response>> repeatableSetResponses =
						repeatableSetResponse.getResponseGroups();
					
					List<Integer> rsIterations = 
						new ArrayList<Integer>(repeatableSetResponses.keySet());
					
					for(Integer rsIndex : rsIterations) {
						numResponses += processResponses(allColumns, 
								surveyResponse,
								repeatableSetResponses.get(rsIndex), 
								prompts, 
								usernames, clients, privacyStates, 
								timestamps, timezones, 
								locationStatuses, locationLongitude,
								locationLatitude, locationTimestamp,
								locationAccuracy, locationProvider,
								surveyIds, surveyTitles, surveyDescriptions, 
								launchContexts
							);
					}
				}
			}
		}
		
		return numResponses;
	}
}
