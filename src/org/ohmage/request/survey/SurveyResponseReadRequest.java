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
import java.util.TimeZone;

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
import org.ohmage.domain.campaign.Prompt.LabelValuePair;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.Survey;
import org.ohmage.domain.campaign.SurveyItem;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.ColumnKey;
import org.ohmage.domain.campaign.SurveyResponse.OutputFormat;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.domain.campaign.prompt.ChoicePrompt;
import org.ohmage.domain.campaign.prompt.CustomChoicePrompt;
import org.ohmage.domain.campaign.response.MultiChoiceCustomPromptResponse;
import org.ohmage.domain.campaign.response.MultiChoicePromptResponse;
import org.ohmage.domain.campaign.response.SingleChoiceCustomPromptResponse;
import org.ohmage.domain.campaign.response.SingleChoicePromptResponse;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.DateUtils;
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
	
	private final long rowsToSkip;
	private final long rowsToAnalyze;
	
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
		
		long tRowsToSkip = SurveyResponse.DEFAULT_NUM_ROWS_TO_SKIP;
		long tRowsToAnalyze = SurveyResponse.DEFAULT_NUM_ROWS_TO_ANALYZE;
		
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
				
				// Rows to skip
				t = getParameterValues(InputKeys.SURVEY_ROWS_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(ErrorCode.SURVEY_INVALID_ROWS_TO_SKIP, "Multiple values were given for the number of rows to skip: " + InputKeys.SURVEY_ROWS_TO_SKIP);
				}
				else if(t.length == 1) {
					tRowsToSkip = SurveyResponseValidators.validateRowsToSkip(t[0]);
				}
				
				// Rows to analyze
				t = getParameterValues(InputKeys.SURVEY_ROWS_TO_ANALYZE);
				if(t.length > 1) {
					throw new ValidationException(ErrorCode.SURVEY_INVALID_ROWS_TO_ANALYZE, "Multiple values were given for the number of rows to analyze: " + InputKeys.SURVEY_ROWS_TO_ANALYZE);
				}
				else if(t.length == 1) {
					tRowsToAnalyze = SurveyResponseValidators.validateRowsToAnalyze(t[0]);
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
		
		rowsToSkip = tRowsToSkip;
		rowsToAnalyze = tRowsToAnalyze;
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
			surveyResponseList = 
					SurveyResponseServices.instance().readSurveyResponseInformation(
							campaign, 
							(URN_SPECIAL_ALL_LIST.equals(usernames) ? null : usernames), 
							null, 
							startDate, 
							endDate, 
							privacyState, 
							(URN_SPECIAL_ALL_LIST.equals(surveyIds)) ? null : surveyIds, 
							(URN_SPECIAL_ALL_LIST.equals(promptIds)) ? null : promptIds, 
							null,
							rowsToSkip,
							rowsToAnalyze
						);
			
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
		
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
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
								allColumns || columns.contains(ColumnKey.CONTEXT_EPOCH_MILLIS),
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
						
						if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
							currResult.put(
									"timestamp", 
									TimeUtils.getIso8601DateTimeString(
											new Date(surveyResponse.getTime())));
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_UTC_TIMESTAMP)) {
							currResult.put(
									"utc_timestamp",
									DateUtils.timestampStringToUtc(
											TimeUtils.getIso8601DateTimeString(
													new Date(surveyResponse.getTime())), 
											TimeZone.getDefault().getID()));
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_ACCURACY, JSONObject.NULL);
							}
							else {
								double accuracy = location.getAccuracy();
								
								if(Double.isInfinite(accuracy) || Double.isNaN(accuracy)) {
									currResult.put(Location.JSON_KEY_ACCURACY, JSONObject.NULL);
								}
								else {
									currResult.put(Location.JSON_KEY_ACCURACY, accuracy);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_LATITUDE, JSONObject.NULL);
							}
							else {
								double latitude = location.getLatitude();
								
								if(Double.isInfinite(latitude) || Double.isNaN(latitude)) {
									currResult.put(Location.JSON_KEY_LATITUDE, JSONObject.NULL);
								}
								else {
									currResult.put(Location.JSON_KEY_LATITUDE, latitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_LONGITUDE, JSONObject.NULL);
							}
							else {
								double longitude = location.getLongitude();
								
								if(Double.isInfinite(longitude) || Double.isNaN(longitude)) {
									currResult.put(Location.JSON_KEY_LONGITUDE, JSONObject.NULL);
								}
								else {
									currResult.put(Location.JSON_KEY_LONGITUDE, longitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_PROVIDER, JSONObject.NULL);
							}
							else {
								currResult.put(Location.JSON_KEY_PROVIDER, location.getProvider());
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_TIME, JSONObject.NULL);
							}
							else {
								currResult.put(Location.JSON_KEY_TIME, location.getTime());
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.JSON_KEY_TIME_ZONE, JSONObject.NULL);
							}
							else {
								currResult.put(Location.JSON_KEY_TIME_ZONE, location.getTimeZone().getID());
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
					JSONArray utcTimestamps = new JSONArray();
					JSONArray epochMillisTimestamps = new JSONArray();
					JSONArray timezones = new JSONArray();
					JSONArray locationStatuses = new JSONArray();
					JSONArray locationLongitude = new JSONArray();
					JSONArray locationLatitude = new JSONArray();
					JSONArray locationTimestamp = new JSONArray();
					JSONArray locationTimeZone = new JSONArray();
					JSONArray locationAccuracy = new JSONArray();
					JSONArray locationProvider = new JSONArray();
					JSONArray surveyIds = new JSONArray();
					JSONArray surveyTitles = new JSONArray();
					JSONArray surveyDescriptions = new JSONArray();
					JSONArray launchContexts = new JSONArray();
					Map<String, JSONObject> prompts = new HashMap<String, JSONObject>();
					
					// If the results are empty, then we still want to populate
					// the resulting JSON with information about all of the
					// prompts even though they don't have any responses 
					// associated with them.
					// FIXME: This shouldn't be conditional on the number of
					// responses found. We should create headers for all of the
					// requested prompt IDs (either via the list of survey IDs
					// or the list of prompt IDs) or none of the prompt IDs if
					// prompts aren't requested.
					if(surveyResponseList.isEmpty()) {
						// If the user-supplied list of survey IDs is present,
						if(this.surveyIds != null) {
							Map<String, Survey> campaignSurveys = campaign.getSurveys();
							// If the user asked for all surveys for this
							// campaign, then populate the prompt information
							// with all of the data about all of the prompts in
							// all of the surveys in this campaign.
							if(this.surveyIds.equals(URN_SPECIAL_ALL_LIST)) {
								for(Survey currSurvey : campaignSurveys.values()) {
									populatePrompts(currSurvey.getSurveyItems(), prompts);
								}
							}
							// Otherwise, populate the prompt information only
							// with the data about the requested surveys.
							else {
								for(String surveyId : this.surveyIds) {
									populatePrompts(campaignSurveys.get(surveyId).getSurveyItems(), prompts);
								}
							}
						}
						// If the user-supplied list of prompt IDs is present,
						else if(promptIds != null) {
							// If the user asked for all prompts for this
							// campaign, then populate the prompt information
							// with all of the data about all of the prompts in
							// this campaign.
							if(this.promptIds.equals(URN_SPECIAL_ALL_LIST)) {
								for(Survey currSurvey : campaign.getSurveys().values()) {
									populatePrompts(currSurvey.getSurveyItems(), prompts);
								}
							}
							// Otherwise, populate the prompt information with
							// the data about only the requested prompts.
							else {
								int currNumPrompts = 0;
								Map<Integer, SurveyItem> tempPromptMap = 
										new HashMap<Integer, SurveyItem>(promptIds.size());
								
								for(String promptId : promptIds) {
									tempPromptMap.put(currNumPrompts, campaign.getPrompt(campaign.getSurveyIdForPromptId(promptId), promptId));
									currNumPrompts++;
								}
								
								populatePrompts(tempPromptMap, prompts);
							}
						}
					}
					// If the results are non-empty, we need to populate the
					// prompt information only for those prompts to which the
					// user queried about.
					else {
						Set<String> unqiueSurveyIds = new HashSet<String>();
						Set<String> uniquePromptIds = new HashSet<String>();
						
						// For each of the survey responses in the result,
						for(SurveyResponse surveyResponse : surveyResponseList) {
							String surveyId = surveyResponse.getSurvey().getId();
							
							// If the survey with which this survey response is
							// associated has not yet been processed, process
							// it.
							if(! unqiueSurveyIds.contains(surveyId)) {
								// Add to the list of surveys that have been
								// processed so that no subsequent survey
								// responses that are associated with this 
								// survey cause it to be processed again.
								unqiueSurveyIds.add(surveyId);
								
								// Keep track of the unique number of prompts
								// that we process.
								int currNumPrompts = 0;
								// Get all of the unique prompt IDs for this
								// survey response.
								Set<String> promptIds = surveyResponse.getPromptIds();
								// Create a a temporary map of Integers to
								// Prompt objects.
								Map<Integer, SurveyItem> tempPrompts =
									new HashMap<Integer, SurveyItem>(promptIds.size());
								
								// Retrieve all of the unique prompts that have
								// not yet been processed and add them to the 
								// map.
								for(String promptId : promptIds) {
									if(! uniquePromptIds.contains(promptId)) {
										tempPrompts.put(currNumPrompts, campaign.getPrompt(surveyId, promptId));
										currNumPrompts++;
									}
								}
								
								// Populate the prompts JSON with the 
								// information about all of the distinct 
								// prompts from this survey response.
								populatePrompts(tempPrompts, prompts);
							}
						}
					}
					
					// Process each of the survey responses and keep track of
					// the number of prompt responses.
					int numSurveyResponses = surveyResponseList.size();
					int numPromptResponses = 0;
					for(SurveyResponse surveyResponse : surveyResponseList) {
						numPromptResponses += processResponses(allColumns, 
								surveyResponse, 
								surveyResponse.getResponses(), 
								prompts, 
								usernames, clients, privacyStates, 
								timestamps, utcTimestamps, 
								epochMillisTimestamps, timezones, 
								locationStatuses, locationLongitude, 
								locationLatitude, locationTimestamp, 
								locationTimeZone,
								locationAccuracy, locationProvider,
								surveyIds, surveyTitles, surveyDescriptions, 
								launchContexts
							);
					}
					
					// Add all of the applicable output stuff.
					JSONObject result = new JSONObject();
					JSONArray keysOrdered = new JSONArray();
					
					// For each of the requested columns, add their respective
					// data to the result in a specific order per Hongsuda's
					// request.
					if(allColumns || columns.contains(ColumnKey.SURVEY_ID)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyIds);
						result.put(ColumnKey.SURVEY_ID.toString(), values);
						keysOrdered.put(ColumnKey.SURVEY_ID.toString());
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_TITLE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyTitles);
						result.put(ColumnKey.SURVEY_TITLE.toString(), values);
						keysOrdered.put(ColumnKey.SURVEY_TITLE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_DESCRIPTION)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyDescriptions);
						result.put(ColumnKey.SURVEY_DESCRIPTION.toString(), values);
						keysOrdered.put(ColumnKey.SURVEY_DESCRIPTION.toString());
					}
					if(allColumns || columns.contains(ColumnKey.USER_ID)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, usernames);
						result.put(ColumnKey.USER_ID.toString(), values);
						keysOrdered.put(ColumnKey.USER_ID.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_CLIENT)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, clients);
						result.put(ColumnKey.CONTEXT_CLIENT.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_CLIENT.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_UTC_TIMESTAMP)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, utcTimestamps);
						result.put(ColumnKey.CONTEXT_UTC_TIMESTAMP.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_UTC_TIMESTAMP.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_EPOCH_MILLIS)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, epochMillisTimestamps);
						result.put(ColumnKey.CONTEXT_EPOCH_MILLIS.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_EPOCH_MILLIS.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, timestamps);
						result.put(ColumnKey.CONTEXT_TIMESTAMP.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_TIMESTAMP.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMEZONE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, timezones);
						result.put(ColumnKey.CONTEXT_TIMEZONE.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_TIMEZONE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
						List<String> unorderedList = new LinkedList<String>();
						for(String promptId : prompts.keySet()) {
							result.put(
									SurveyResponse.ColumnKey.URN_PROMPT_ID_PREFIX + promptId, 
									prompts.get(promptId));
							unorderedList.add(SurveyResponse.ColumnKey.URN_PROMPT_ID_PREFIX + promptId);
						}
						Collections.sort(unorderedList);
						
						for(String columnId : unorderedList) {
							keysOrdered.put(columnId);
						}
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_STATUS)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationStatuses);
						result.put(ColumnKey.CONTEXT_LOCATION_STATUS.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_STATUS.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationLatitude);
						result.put(ColumnKey.CONTEXT_LOCATION_LATITUDE.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_LATITUDE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationLongitude);
						result.put(ColumnKey.CONTEXT_LOCATION_LONGITUDE.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_LONGITUDE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationProvider);
						result.put(ColumnKey.CONTEXT_LOCATION_PROVIDER.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_PROVIDER.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
						JSONObject timeValues = new JSONObject();
						timeValues.put(JSON_KEY_VALUES, locationTimestamp);
						result.put(ColumnKey.CONTEXT_LOCATION_TIMESTAMP.toString(), timeValues);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_TIMESTAMP.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMEZONE)) {
						JSONObject timeZoneValues = new JSONObject();
						timeZoneValues.put(JSON_KEY_VALUES, locationTimeZone);
						result.put(ColumnKey.CONTEXT_LOCATION_TIMEZONE.toString(), timeZoneValues);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_TIMEZONE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, locationAccuracy);
						result.put(ColumnKey.CONTEXT_LOCATION_ACCURACY.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LOCATION_ACCURACY.toString());
					}
					if(allColumns || columns.contains(ColumnKey.SURVEY_PRIVACY_STATE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, privacyStates);
						result.put(ColumnKey.SURVEY_PRIVACY_STATE.toString(), values);
						keysOrdered.put(ColumnKey.SURVEY_PRIVACY_STATE.toString());
					}
					if(allColumns || columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, launchContexts);
						result.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_LONG.toString());
					}
					if(columns.contains(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, launchContexts);
						result.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_LAUNCH_CONTEXT_SHORT.toString());
					}
					
					// If the user requested to collapse the results, create a 
					// new column called count and set the initial value for
					// every existing row to 1. Then, create a map of string
					// representations of data to their original row. Cycle
					// through the indices creating a string by concatenating 
					// the value at the given index for each column and  
					// checking the map if the string already exists. If it  
					// does not exist, add the string value and have it point  
					// to the current index. If it does exist, go to that 
					// column, increase the count, and delete this row by
					// deleting whatever is at the current index in every  
					// column.
					if((collapse != null) && collapse) {
						// Get the key for each row.
						JSONArray keys = new JSONArray();
						Iterator<?> keysIter = result.keys();
						while(keysIter.hasNext()) {
							keys.put(keysIter.next());
						}
						int keyLength = keys.length();
						
						// Create a new "count" column and initialize every
						// count to 1.
						JSONArray counts = new JSONArray();
						for(int i = 0; i < numSurveyResponses; i++) {
							counts.put(1);
						}
						JSONObject countObject = new JSONObject();
						countObject.put(JSON_KEY_VALUES, counts);
						result.put("urn:ohmage:context:count", countObject);
						keys.put("urn:ohmage:context:count");
						
						// Cycle through the responses.
						Map<String, Integer> valueToIndexMap = new HashMap<String, Integer>();
						for(int i = 0; i < numSurveyResponses; i++) {
							// Build the string.
							StringBuilder currResultBuilder = new StringBuilder();
							
							for(int j = 0; j < keyLength; j++) {
								currResultBuilder.append(result.getJSONObject(keys.getString(j)).getJSONArray(JSON_KEY_VALUES).get(i));
								
								if((j + 1) != keyLength) {
									currResultBuilder.append(',');
								}
							}
							
							// Check if the string exists.
							String currResultString = currResultBuilder.toString();
							// If so, go to that index in every column 
							// including the count and delete it, effectively
							// deleting that row.
							if(valueToIndexMap.containsKey(currResultString)) {
								int count = result.getJSONObject("urn:ohmage:context:count").getJSONArray(JSON_KEY_VALUES).getInt(valueToIndexMap.get(currResultString)) + 1;
								result.getJSONObject("urn:ohmage:context:count").getJSONArray(JSON_KEY_VALUES).put(valueToIndexMap.get(currResultString).intValue(), count);
								
								for(int j = 0; j < keyLength + 1; j++) {
									result.getJSONObject(keys.getString(j)).getJSONArray(JSON_KEY_VALUES).remove(i);
								}

								i--;
								numSurveyResponses--;
							}
							// If not, add it to the map.
							else {
								valueToIndexMap.put(currResultString, i);
							}
						}
					}
					
					// If metadata is not suppressed, create it.
					JSONObject metadata = null;
					if((suppressMetadata == null) || (! suppressMetadata)) {
						metadata = new JSONObject();
						
						metadata.put(InputKeys.CAMPAIGN_URN, campaignId);
						metadata.put(JSON_KEY_NUM_SURVEYS, surveyResponseList.size());
						metadata.put(JSON_KEY_NUM_PROMPTS, numPromptResponses);
					}
					
					if(OutputFormat.JSON_COLUMNS.equals(outputFormat)) {
						httpResponse.setContentType("text/html");
						
						JSONObject resultJson = new JSONObject();
						resultJson.put(JSON_KEY_RESULT, RESULT_SUCCESS);
						
						int numHeaders = keysOrdered.length();
						for(int i = 0; i < numHeaders; i++) {
							String header = keysOrdered.getString(i);
							
							if(header.endsWith(":value") ||
								header.endsWith(":key")) {

								result.remove(header);
								keysOrdered.remove(i);
								i--;
								numHeaders--;
							}
							else if(header.endsWith(":label")) {
								String prunedHeader = header.substring(0, header.length() - 6);

								result.put(prunedHeader, result.get(header));
								result.remove(header);
								
								keysOrdered.put(i, prunedHeader);
							}
						}
						
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
					// For CSV output,
					else if(OutputFormat.CSV.equals(outputFormat)) {
						// Mark it as an attachment.
						httpResponse.setContentType("text/csv");
						httpResponse.setHeader(
								"Content-Disposition", 
								"attachment; filename=SurveyResponses.csv");

						StringBuilder resultBuilder = new StringBuilder();
						
						// If the metadata is not suppressed, add it to the
						// output builder.
						if((suppressMetadata == null) || (! suppressMetadata)) {
							metadata.put(JSON_KEY_RESULT, RESULT_SUCCESS);
							
							resultBuilder.append("## begin metadata\n");
							resultBuilder.append('#').append(metadata.toString().replace(',', ';')).append('\n');
							resultBuilder.append("## end metadata\n");
						}
						
						// Add the prompt contexts to the output builder if 
						// prompts were desired.
						if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
							resultBuilder.append("## begin prompt contexts\n");
							for(String promptId : prompts.keySet()) {
								JSONObject promptJson = new JSONObject();
								
								// Use the already-generated JSON from each of
								// the prompts.
								promptJson.put(
										promptId, 
										prompts
											.get(promptId)
											.get(JSON_KEY_CONTEXT));
								
								resultBuilder
									.append('#')
									.append(promptJson.toString())
									.append('\n');
							}
							resultBuilder.append("## end prompt contexts\n");
						}
						
						// Begin the data section of the CSV.
						resultBuilder.append("## begin data\n");

						// Get the number of keys.
						int keyLength = keysOrdered.length();
						
						// Create a comma-separated list of the header names.
						resultBuilder.append('#');
						for(int i = 0; i < keyLength; i++) {
							String header = keysOrdered.getString(i);
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
						
						// For each of the responses, 
						for(int i = 0; i < numSurveyResponses; i++) {
							for(int j = 0; j < keyLength; j++) {
								Object currResult = 
										result
											.getJSONObject(keysOrdered.getString(j))
											.getJSONArray(JSON_KEY_VALUES)
											.get(i);
								
								if(JSONObject.NULL.equals(currResult)) {
									resultBuilder.append("");
								}
								else {
									resultBuilder.append(currResult);
								}
								
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
			catch(IllegalStateException e) {
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
				if((surveyItem instanceof ChoicePrompt) && 
						(OutputFormat.CSV.equals(outputFormat))) {
					
					ChoicePrompt prompt = (ChoicePrompt) surveyItem;

					JSONObject promptJsonKey = new JSONObject();
					promptJsonKey.put(JSON_KEY_CONTEXT, prompt.toJson());
					promptJsonKey.put(JSON_KEY_VALUES, new JSONArray());
					
					JSONObject promptJsonLabel = new JSONObject();
					promptJsonLabel.put(JSON_KEY_CONTEXT, prompt.toJson());
					promptJsonLabel.put(JSON_KEY_VALUES, new JSONArray());
					
					JSONObject promptJsonValue = new JSONObject();
					promptJsonValue.put(JSON_KEY_CONTEXT, prompt.toJson());
					promptJsonValue.put(JSON_KEY_VALUES, new JSONArray());
				
					prompts.put(prompt.getId() + ":key", promptJsonKey);
					prompts.put(prompt.getId() + ":value", promptJsonLabel);
					prompts.put(prompt.getId() + ":label", promptJsonValue);
				}
				else {
					Prompt prompt = (Prompt) surveyItem;
					
					JSONObject promptJson = new JSONObject();
					promptJson.put(JSON_KEY_CONTEXT, prompt.toJson());
					promptJson.put(JSON_KEY_VALUES, new JSONArray());
					
					prompts.put(prompt.getId(), promptJson);
				}
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
	 * @param utcTimestamps The timestamps JSONArray where the timestamps are
	 * 						converted to UTC.
	 * 
	 * @param epochMillisTimestamps The epoch millis JSONArray.
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
			JSONArray timestamps, JSONArray utcTimestamps, 
			JSONArray epochMillisTimestamps, JSONArray timezones,
			JSONArray locationStatuses, JSONArray locationLongitude,
			JSONArray locationLatitude, JSONArray locationTime,
			JSONArray locationTimeZone,
			JSONArray locationAccuracy, JSONArray locationProvider,
			JSONArray surveyIds, JSONArray surveyTitles, 
			JSONArray surveyDescriptions, JSONArray launchContexts) 
			throws JSONException {

		// Add each of the survey response-wide pieces of information.
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
			timestamps.put(
					TimeUtils.getIso8601DateTimeString(
							new Date(
									surveyResponse.getTime())));
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_UTC_TIMESTAMP)) {
			utcTimestamps.put(
					DateUtils.timestampStringToUtc(
							TimeUtils.getIso8601DateTimeString(
									new Date(surveyResponse.getTime())), 
							TimeZone.getDefault().getID()));
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_EPOCH_MILLIS)) {
			epochMillisTimestamps.put(surveyResponse.getTime());
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
				locationLongitude.put(JSONObject.NULL);
			}
			else {
				locationLongitude.put(location.getLongitude());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationLatitude.put(JSONObject.NULL);
			}
			else {
				locationLatitude.put(location.getLatitude());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationTime.put(JSONObject.NULL);
			}
			else {
				locationTime.put(location.getTime());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationTimeZone.put(JSONObject.NULL);
			}
			else {
				locationTimeZone.put(location.getTimeZone().getID());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationAccuracy.put(JSONObject.NULL);
			}
			else {
				locationAccuracy.put(location.getAccuracy());
			}
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
			Location location = surveyResponse.getLocation();
			if(location == null) {
				locationProvider.put(JSONObject.NULL);
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
		
		// Create a map of which prompts were given a response in this 
		// iteration.
		Set<String> promptIdsWithResponse = new HashSet<String>();
		
		// Get the indices of each response in the list of responses and then
		// sort them to ensure that we process each response in the correct 
		// numeric order.
		List<Integer> indices = new ArrayList<Integer>(responses.keySet());
		Collections.sort(indices);
		
		// Now, add each of the responses.
		for(Integer index : indices) {
			Response response = responses.get(index);
			
			// If the response is a prompt response, add its appropriate 
			// columns.
			if(response instanceof PromptResponse) {
				numResponses++;
				
				if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
					PromptResponse promptResponse = (PromptResponse) response;
					
					Prompt prompt = promptResponse.getPrompt();
					String responseId = response.getId();
					
					// If it's a ChoicePrompt response, populate all three  
					// columns, <id>:key, <id>:label, and <id>:value.
					if((prompt instanceof ChoicePrompt) && 
							(OutputFormat.CSV.equals(outputFormat))) {
						
						// Get the response object.
						Object responseObject = response.getResponseValue();
						
						// If the response was not really a response, e.g.
						// skipped, not displayed, etc., put a JSONObject.NULL
						// object in for the key and value and put a quoted
						// string-representation of the non-response.
						if(responseObject instanceof NoResponse) {
							JSONArray keys =
									prompts.get(responseId + ":key")
										.getJSONArray(JSON_KEY_VALUES);
							keys.put(JSONObject.NULL);
							
							JSONArray labels =
									prompts.get(responseId + ":label")
										.getJSONArray(JSON_KEY_VALUES);
							labels.put("\"" + responseObject + "\"");
							
							JSONArray values =
									prompts.get(responseId + ":value")
										.getJSONArray(JSON_KEY_VALUES);
							values.put(JSONObject.NULL);
						}
						// Otherwise, get the key, label, and, potentially,
						// value and populate their corresponding columns.
						else {
							Object key;
							Object label;
							Object value;
							
							ChoicePrompt choicePrompt = 
									(ChoicePrompt) promptResponse.getPrompt();
							
							Map<Integer, LabelValuePair> choices;
							if(prompt instanceof CustomChoicePrompt) {
								choices = 
										((CustomChoicePrompt) choicePrompt)
											.getAllChoices();
							}
							else {
								choices = choicePrompt.getChoices();
							}
							
							if(response instanceof SingleChoicePromptResponse) {
								key = (Integer) responseObject;
								
								LabelValuePair lvp = choices.get(key);
								label = lvp.getLabel();
								value = lvp.getValue();
							}
							else if(response instanceof SingleChoiceCustomPromptResponse) {
								label = (String) responseObject;
								
								key = choicePrompt.getChoiceKey((String) label);
								value = choices.get(key).getValue();
							}
							else if(response instanceof MultiChoicePromptResponse) {
								@SuppressWarnings("unchecked")
								List<Integer> keys = (List<Integer>) responseObject;
								
								List<Object> labels = new ArrayList<Object>(keys.size());
								List<Object> values = new ArrayList<Object>(keys.size());
								for(Integer currKey : keys) {
									LabelValuePair lvp = choices.get(currKey);
									
									labels.add(lvp.getLabel());
									
									Number currValue = lvp.getValue();
									if(currValue == null) {
										values.add("");
									}
									else {
										values.add(currValue);
									}
								}
								
								key = new JSONArray(keys);
								label = labels;
								value = values;
							}
							else if(response instanceof MultiChoiceCustomPromptResponse) {
								@SuppressWarnings("unchecked")
								Collection<String> labels = (Collection<String>) responseObject;
								
								List<Object> keys = new ArrayList<Object>(labels.size());
								List<Object> values = new ArrayList<Object>(labels.size());
								for(String currLabel : labels) {
									Integer currKey = choicePrompt.getChoiceKey(currLabel);
									keys.add(currKey);
									
									Number currValue = choices.get(currKey).getValue();
									if(currValue == null) {
										values.add("");
									}
									else {
										values.add(currValue);
									}
								}
								
								key = keys;
								label = labels;
								value = values;
							}
							else {
								throw new IllegalStateException("There exists a choice prompt that is not a (single/multi) [custom] choice.");
							}
						
							promptIdsWithResponse.add(responseId + ":key");
							JSONArray keys =
									prompts.get(responseId + ":key")
										.getJSONArray(JSON_KEY_VALUES);
							keys.put("\"" + key + "\"");
							
							promptIdsWithResponse.add(responseId + ":label");
							JSONArray labels =
									prompts.get(responseId + ":label")
										.getJSONArray(JSON_KEY_VALUES);
							labels.put("\"" + label + "\"");
							
							promptIdsWithResponse.add(responseId + ":value");
							JSONArray values =
									prompts.get(responseId + ":value")
										.getJSONArray(JSON_KEY_VALUES);
							if(value == null) {
								values.put("");
							}
							else {
								values.put("\"" + value + "\"");
							}
						}

						// Finally, indicate that these prompts were given
						// values so that the others may be populated with
						// JSONObject.NULLs.
						promptIdsWithResponse.add(responseId + ":key");
						promptIdsWithResponse.add(responseId + ":label");
						promptIdsWithResponse.add(responseId + ":value");
					}
					// Otherwise, only populate the value.
					else {
						promptIdsWithResponse.add(responseId);
						
						JSONArray values =
								prompts.get(responseId).getJSONArray(JSON_KEY_VALUES);
						
						Object responseValue = response.getResponseValue();
						if(OutputFormat.CSV.equals(outputFormat)) {
							responseValue = "\"" + responseValue + "\"";
						}
					
						values.put(responseValue);
					}
				}
			}
			// FIXME
			// The problem is that we need to recurse, but if we have already
			// added all of the survey response stuff then we don't want to
			// add that again. If I remember correctly, we need to create new
			// columns with headers that are like, "prompt1name1", 
			// "prompt1name2", etc. where we append the iteration number after
			// the prompt name. The problem is that before this function is 
			// called we called a generic header creator for each prompt. This
			// prompt would have had a header that was created but only the 
			// one. We need to duplicate that header, JSONObject.NULL-out all 
			// of the previous responses, and give it a new header with the
			// iteration number.
			else if(response instanceof RepeatableSetResponse) {
				/*
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
							timestamps, utcTimestamps, 
							epochMillisTimestamps, timezones, 
							locationStatuses, locationLongitude,
							locationLatitude, locationTime, locationTimeZone,
							locationAccuracy, locationProvider,
							surveyIds, surveyTitles, surveyDescriptions, 
							launchContexts
						);
				}
				*/
			}
		}
		
		// Finally, get all of the prompts that didn't receive a value in this
		// iteration and give them a value of JSONObject.NULL.
		if(allColumns || columns.contains(ColumnKey.PROMPT_RESPONSE)) {
			Set<String> promptIdsWithoutResponse = 
				new HashSet<String>(prompts.keySet());
			promptIdsWithoutResponse.removeAll(promptIdsWithResponse);
			for(String currPromptId : promptIdsWithoutResponse) {
				prompts
					.get(currPromptId)
					.getJSONArray(JSON_KEY_VALUES)
					.put(JSONObject.NULL);
			}
		}
		
		return numResponses;
	}
}
