package org.ohmage.request.survey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSet;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.Survey;
import org.ohmage.domain.campaign.SurveyItem;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;
import org.ohmage.validator.DateValidators;
import org.ohmage.validator.SurveyResponseReadValidators;
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
 *         {@value #URN_CONTEXT_CLIENT},
 *         {@value #URN_CONTEXT_TIMESTAMP},
 *         {@value #URN_CONTEXT_TIMEZONE},
 *         {@value #URN_CONTEXT_UTC_TIMESTAMP},
 *         {@value #URN_CONTEXT_LAUNCH_CONTEXT_LONG},
 *         {@value #URN_CONTEXT_LAUNCH_CONTEXT_SHORT},
 *         {@value #URN_CONTEXT_LOCATION_STATUS},
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
	
	private final Date startDate;
	private final Date endDate;
	private final String campaignUrn;
	private final List<String> userList;
	private final List<String> promptIdList;
	private final List<String> surveyIdList;
	private final List<String> columnList;
	private final String outputFormat;
	private final Boolean prettyPrint;
	private final Boolean suppressMetadata;
	private final Boolean returnId;
	private final List<String> sortOrder;
	private final SurveyResponse.PrivacyState privacyState;
	private final Boolean collapse;
	
	private Campaign configuration;
	private List<SurveyResponse> surveyResponseList;
	
	private static final List<String> ALLOWED_COLUMN_URN_LIST;
	private static final List<String> ALLOWED_OUTPUT_FORMAT_LIST;
	private static final List<String> ALLOWED_SORT_ORDER_LIST;
	
	public static final String URN_SPECIAL_ALL = "urn:ohmage:special:all";
	public static final List<String> URN_SPECIAL_ALL_LIST;
	
	public static final String URN_CONTEXT_CLIENT = "urn:ohmage:context:client";
	public static final String URN_CONTEXT_TIMESTAMP = "urn:ohmage:context:timestamp";
	public static final String URN_CONTEXT_TIMEZONE = "urn:ohmage:context:timezone";
	public static final String URN_CONTEXT_UTC_TIMESTAMP = "urn:ohmage:context:utc_timestamp";
	public static final String URN_CONTEXT_LAUNCH_CONTEXT_LONG = "urn:ohmage:context:launch_context_long";
	public static final String URN_CONTEXT_LAUNCH_CONTEXT_SHORT = "urn:ohmage:context:launch_context_short";
	public static final String URN_CONTEXT_LOCATION_STATUS = "urn:ohmage:context:location:status";
	public static final String URN_CONTEXT_LOCATION_LATITUDE = "urn:ohmage:context:location:latitude";
	public static final String URN_CONTEXT_LOCATION_LONGITUDE = "urn:ohmage:context:location:longitude";
	public static final String URN_CONTEXT_LOCATION_TIMESTAMP = "urn:ohmage:context:location:timestamp";
	public static final String URN_CONTEXT_LOCATION_ACCURACY = "urn:ohmage:context:location:accuracy";
	public static final String URN_CONTEXT_LOCATION_PROVIDER = "urn:ohmage:context:location:provider";
	public static final String URN_USER_ID = "urn:ohmage:user:id";
	public static final String URN_SURVEY_ID = "urn:ohmage:survey:id";
	public static final String URN_SURVEY_TITLE = "urn:ohmage:survey:title";
	public static final String URN_SURVEY_DESCRIPTION = "urn:ohmage:survey:description";
	public static final String URN_SURVEY_PRIVACY_STATE = "urn:ohmage:survey:privacy_state";
	public static final String URN_REPEATABLE_SET_ID = "urn:ohmage:repeatable_set:id";
	public static final String URN_REPEATABLE_SET_ITERATION = "urn:ohmage:repeatable_set:iteration";
	public static final String URN_PROMPT_RESPONSE = "urn:ohmage:prompt:response";
	
	public static final String URN_PROMPT_ID_PREFIX = "urn:ohmage:prompt:id:";
	
	// output format constants - these are the output formats the requester
	// can select from
	public static final String OUTPUT_FORMAT_JSON_ROWS = "json-rows";
	public static final String OUTPUT_FORMAT_JSON_COLUMNS = "json-columns";
	public static final String OUTPUT_FORMAT_CSV = "csv";
	
	private static final String CONTEXT = "context";
	private static final String VALUES = "values";
	private static final String METADATA = "metadata";
	
	static {
		ALLOWED_COLUMN_URN_LIST = Arrays.asList(new String[] {
			URN_CONTEXT_CLIENT, URN_CONTEXT_TIMESTAMP, URN_CONTEXT_TIMEZONE, URN_CONTEXT_UTC_TIMESTAMP,
			URN_CONTEXT_LAUNCH_CONTEXT_LONG, URN_CONTEXT_LAUNCH_CONTEXT_SHORT, URN_CONTEXT_LOCATION_STATUS,
			URN_CONTEXT_LOCATION_LATITUDE, URN_CONTEXT_LOCATION_LONGITUDE, URN_CONTEXT_LOCATION_TIMESTAMP,
			URN_CONTEXT_LOCATION_ACCURACY, URN_CONTEXT_LOCATION_PROVIDER, URN_USER_ID, URN_SURVEY_ID,
			URN_SURVEY_TITLE, URN_SURVEY_DESCRIPTION, URN_SURVEY_PRIVACY_STATE, URN_REPEATABLE_SET_ID,
			URN_REPEATABLE_SET_ITERATION, URN_PROMPT_RESPONSE
		});
		
		ALLOWED_OUTPUT_FORMAT_LIST = Arrays.asList(new String[] {OUTPUT_FORMAT_JSON_ROWS, OUTPUT_FORMAT_JSON_COLUMNS, OUTPUT_FORMAT_CSV});
		
		ALLOWED_SORT_ORDER_LIST = Arrays.asList(new String[] {InputKeys.SORT_ORDER_SURVEY, InputKeys.SORT_ORDER_TIMESTAMP, InputKeys.SORT_ORDER_USER});
		
		URN_SPECIAL_ALL_LIST = Collections.unmodifiableList(Arrays.asList(new String[]{URN_SPECIAL_ALL}));
	}
	
	/**
	 * Creates a survey response read request.
	 * 
	 * @param httpRequest  The request to retrieve parameters from.
	 */
	public SurveyResponseReadRequest(HttpServletRequest httpRequest) {
		// Handle user-password or token-based authentication
		super(httpRequest, TokenLocation.EITHER, false);
		
		Date tStartDateAsDate = null;
		Date tEndDateAsDate = null;
		
		String tCampaignUrn = getParameter(InputKeys.CAMPAIGN_URN);
		String tOutputFormat = getParameter(InputKeys.OUTPUT_FORMAT);
		List<String> tSortOrder = null;
		SurveyResponse.PrivacyState tPrivacyState = null;
		
		List<String> tUserListAsList = null;
		List<String> tPromptIdListAsList = null;
		List<String> tSurveyIdListAsList = null;
		List<String> tColumnListAsList = null;
		
		Boolean tPrettyPrintAsBoolean = null;
		Boolean tSuppressMetadataAsBoolean = null;
		Boolean tReturnIdAsBoolean = null;
		Boolean tCollapseAsBoolean = null;
		
		if(! isFailed()) {
		
			LOGGER.info("Creating a survey response read request.");
			
			try {
				LOGGER.info("Making sure campaign_urn parameter is present.");
				
				if(tCampaignUrn == null) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The required campaign URN was not present.");
					throw new ValidationException("The required campaign URN was not present.");
				}
				
				LOGGER.info("Validating start_date and end_date parameters.");
					
				try {
					if(! StringUtils.isEmptyOrWhitespaceOnly(getParameter(InputKeys.START_DATE))) {
						tStartDateAsDate = DateValidators.validateISO8601Date(getParameter(InputKeys.START_DATE));
					}
					else {
						tStartDateAsDate = null;
					}
					if(! StringUtils.isEmptyOrWhitespaceOnly(getParameter(InputKeys.END_DATE))) {
						tEndDateAsDate = DateValidators.validateISO8601Date(getParameter(InputKeys.END_DATE));
					}
					else {
						tEndDateAsDate = null;
					}
					
					if((tStartDateAsDate != null && tEndDateAsDate == null) || (tStartDateAsDate == null && tEndDateAsDate != null)) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "Missing start_date or end_date");
					}
				} 
				catch (ValidationException e) {
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Invalid start_date or end_date");
					throw e;
				}

				LOGGER.info("Validating privacy_state parameter.");
				tPrivacyState = SurveyResponseValidators.validatePrivacyState(this, getParameter(InputKeys.PRIVACY_STATE));
				
				LOGGER.info("Validating user_list parameter.");
				tUserListAsList = SurveyResponseReadValidators.validateUserList(this, getParameter(InputKeys.USER_LIST));
				
				LOGGER.info("Validating prompt_id_list and survey_id_list parameters.");
				List<String> tList = SurveyResponseReadValidators.validatePromptIdSurveyIdLists(this, getParameter(InputKeys.PROMPT_ID_LIST), getParameter(InputKeys.SURVEY_ID_LIST));
				
				// Now check whether it's a prompt id list or a survey id list
				if(StringUtils.isEmptyOrWhitespaceOnly(getParameter(InputKeys.PROMPT_ID_LIST))) {
					LOGGER.info("Found " + tList.size() + " survey ids to query against.");
					tSurveyIdListAsList = tList;
					tPromptIdListAsList = Collections.emptyList();
				}
				else {
					LOGGER.info("Found " + tList.size() + " prompt ids to query against.");
					tSurveyIdListAsList = Collections.emptyList();
					tPromptIdListAsList = tList;
				}
				
				LOGGER.info("Validating column_list parameter.");
				tColumnListAsList = SurveyResponseReadValidators.validateColumnList(this, getParameter(InputKeys.COLUMN_LIST), ALLOWED_COLUMN_URN_LIST);
				
				LOGGER.info("Validating output_format parameter.");
				tOutputFormat = SurveyResponseReadValidators.validateOutputFormat(this, tOutputFormat, ALLOWED_OUTPUT_FORMAT_LIST);

				LOGGER.info("Validating sort_order parameter.");
				tSortOrder = SurveyResponseReadValidators.validateSortOrder(this, getParameter(InputKeys.SORT_ORDER), ALLOWED_SORT_ORDER_LIST); 
				
				LOGGER.info("Validating suppress_metadata parameter.");
				tSuppressMetadataAsBoolean = SurveyResponseReadValidators.validateSuppressMetadata(this, getParameter(InputKeys.SUPPRESS_METADATA));
				
				LOGGER.info("Validating pretty_print parameter.");
				tPrettyPrintAsBoolean = SurveyResponseReadValidators.validatePrettyPrint(this, getParameter(InputKeys.PRETTY_PRINT));

				LOGGER.info("Validating return_id parameter.");
				tReturnIdAsBoolean = SurveyResponseReadValidators.validateReturnId(this, getParameter(InputKeys.RETURN_ID));

				LOGGER.info("Validating collapse parameter.");
				tCollapseAsBoolean = SurveyResponseReadValidators.validateCollapse(this, getParameter(InputKeys.COLLAPSE));
			} 
			
			catch (ValidationException e) {
				
				LOGGER.info(e);
				
			}
		}
		
		this.campaignUrn = tCampaignUrn;
		this.collapse = tCollapseAsBoolean;
		this.columnList = tColumnListAsList;
		this.endDate = tEndDateAsDate;
		this.outputFormat = tOutputFormat;
		this.prettyPrint = tPrettyPrintAsBoolean;
		this.privacyState = tPrivacyState;
		this.promptIdList = tPromptIdListAsList;
		this.returnId = tReturnIdAsBoolean;
		this.sortOrder = tSortOrder;
		this.startDate = tStartDateAsDate;
		this.suppressMetadata = tSuppressMetadataAsBoolean;
		this.surveyIdList = tSurveyIdListAsList;
		this.userList = tUserListAsList;
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
			
			LOGGER.info("Verifying that requester belongs to the campaign specified by campaign ID.");
		    UserCampaignServices.campaignExistsAndUserBelongs(this, this.getUser(), this.campaignUrn);
		    
		    
		    // We have removed this ACL check because it causes participants
		    // to not be able to view their own data. The bigger problem
		    // is that for "Browse Data" the front end always passes 
		    // user_list=urn:ohmage:special:all
		    
//		    LOGGER.info("Verifying that the requester has a role that allows reading of survey responses for each of the users in the list.");
//	    	UserCampaignServices.requesterCanViewUsersSurveyResponses(this, this.campaignUrn, this.getUser().getUsername(), (String[]) userList.toArray());
			
		    if(! this.userList.equals(URN_SPECIAL_ALL_LIST)) {
		    	LOGGER.info("Checking the user list to make sure all of the users belong to the campaign ID.");
		    	UserCampaignServices.verifyUsersExistInCampaign(this, this.campaignUrn, this.userList);
		    }
		    
		    LOGGER.info("Retrieving campaign configuration.");
			this.configuration = CampaignServices.findCampaignConfiguration(this, this.campaignUrn);
			
			if(! this.promptIdList.isEmpty() && ! this.promptIdList.equals(URN_SPECIAL_ALL_LIST)) {
				LOGGER.info("Verifying that the prompt ids in the query belong to the campaign.");
				SurveyResponseReadServices.verifyPromptIdsBelongToConfiguration(this, this.promptIdList, this.configuration);
			}
			
			if(! this.surveyIdList.isEmpty() && ! this.surveyIdList.equals(URN_SPECIAL_ALL_LIST)) {
				LOGGER.info("Verifying that the survey ids in the query belong to the campaign.");
				SurveyResponseReadServices.verifySurveyIdsBelongToConfiguration(this, this.surveyIdList, this.configuration);
			}
		    
			LOGGER.info("Dispatching to the data layer.");
			surveyResponseList = new LinkedList<SurveyResponse>();
			for(String username : userList) {
				// This can never overlap unless the usernames are identical,
				// which should have already been taken care of.
				surveyResponseList.addAll(
						SurveyResponseServices.readSurveyResponseInformation(
								this, configuration, username, null, startDate, 
								endDate, privacyState, surveyIdList, 
								promptIdList, null
							)
					);
			}
			
			LOGGER.info("Found " + surveyResponseList.size() + " results");
			
			LOGGER.info("Filtering survey response results according to our privacy rules and the requester's role.");
			SurveyResponseReadServices.performPrivacyFilter(this.getUser(), this.campaignUrn, surveyResponseList, this.privacyState);
			
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
								
								for(String item : sortOrder) {
									if(InputKeys.SORT_ORDER_SURVEY.equals(item)) {
										if(! o1.getSurvey().equals(o2.getSurvey())) {
											return o1.getSurvey().getId().compareTo(o2.getSurvey().getId());
										}
									}
									else if(InputKeys.SORT_ORDER_TIMESTAMP.equals(item)) {
										if(o1.getTime() != o2.getTime()) {
											if((o1.getTime() - o2.getTime()) < 0) {
												return -1;
											}
											else {
												return 1;
											}
										}
									}
									else if(InputKeys.SORT_ORDER_USER.equals(item)) {
										if(! o1.getUsername().equals(o2.getUsername())) {
											return o1.getUsername().compareTo(o2.getUsername());
										}
									}
								}
								return 0;
							}
						}
					);
			}
		}
		
		catch(ServiceException e) {
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
				boolean allColumns = columnList.equals(URN_SPECIAL_ALL_LIST);
			
				if(OUTPUT_FORMAT_JSON_ROWS.equals(outputFormat)) {
					httpResponse.setContentType("text/html");
					
					JSONArray result = new JSONArray();
					
					for(SurveyResponse surveyResponse : surveyResponseList) {
						result.put(surveyResponse.toJson(
								allColumns || columnList.contains(URN_USER_ID),
								allColumns || columnList.contains(false),
								allColumns || columnList.contains(URN_CONTEXT_CLIENT),
								allColumns || columnList.contains(URN_SURVEY_PRIVACY_STATE),
								allColumns || columnList.contains(URN_CONTEXT_UTC_TIMESTAMP),
								allColumns || columnList.contains(false),
								allColumns || columnList.contains(URN_CONTEXT_TIMEZONE),
								allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS),
								allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS), // FIXME: This should break the location object down.
								allColumns || columnList.contains(URN_SURVEY_ID),
								allColumns || columnList.contains(URN_SURVEY_TITLE),
								allColumns || columnList.contains(URN_SURVEY_DESCRIPTION),
								allColumns || columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_LONG) || columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_SHORT),
								columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_LONG),
								allColumns || columnList.contains(URN_PROMPT_RESPONSE),
								returnId
							)
						);
					}
					
					if(collapse) {
						int count = result.length();
						Set<String> collapsedSet = new HashSet<String>(count);
						
						for(int i = 0; i < count; i++) {
							// This shouldn't work because JSONObject can 
							// output identical objects in different manners.
							if(! collapsedSet.add(result.getJSONObject(i).toString())) {
								result.remove(i);
								i--;
								count--;
							}
						}
					}
					
					if(prettyPrint) {
						resultString = result.toString(4);
					}
					else {
						resultString = result.toString();
					}
					
					// TODO: ADD METADATA
				}
				else if(OUTPUT_FORMAT_JSON_COLUMNS.equals(outputFormat) || 
						OUTPUT_FORMAT_CSV.equals(outputFormat)) {
					
					if(collapse) {
						Set<SurveyResponse> surveyResponseSet = 
							new HashSet<SurveyResponse>(surveyResponseList);
						
						for(SurveyResponse currResponse : surveyResponseList) {
							if(! surveyResponseSet.add(currResponse)) {
								surveyResponseList.remove(currResponse);
							}
						}
					}
					
					JSONArray usernames = new JSONArray();
					JSONArray clients = new JSONArray();
					JSONArray privacyStates = new JSONArray();
					JSONArray timestamps = new JSONArray();
					JSONArray timezones = new JSONArray();
					JSONArray locationStatuses = new JSONArray();
					JSONArray locations = new JSONArray();
					JSONArray surveyIds = new JSONArray();
					JSONArray surveyTitles = new JSONArray();
					JSONArray surveyDescriptions = new JSONArray();
					JSONArray launchContexts = new JSONArray();

					Map<String, Survey> surveys = new HashMap<String, Survey>();
					Map<String, JSONObject> prompts = new HashMap<String, JSONObject>();

					for(SurveyResponse surveyResponse : surveyResponseList) {
						Survey survey = surveyResponse.getSurvey();
						
						if(! surveys.containsKey(survey.getId())) {
							surveys.put(survey.getId(), survey);
							
							populatePrompts(survey.getSurveyItems(), prompts);
						}
					}
					
					for(SurveyResponse surveyResponse : surveyResponseList) {
						processResponses(allColumns, surveyResponse, 
								surveyResponse.getPromptResponses(), 
								surveys, prompts, 
								usernames, clients, privacyStates, 
								timestamps, timezones, 
								locationStatuses, locations, 
								surveyIds, surveyTitles, surveyDescriptions, 
								launchContexts
							);
					}
					
					// Add all of the applicable output stuff.
					JSONObject result = new JSONObject();
					
					if(allColumns || columnList.contains(URN_USER_ID)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, usernames);
						result.put(URN_USER_ID, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_CLIENT)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, clients);
						result.put(URN_CONTEXT_CLIENT, values);
					}
					if(allColumns || columnList.contains(URN_SURVEY_PRIVACY_STATE)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, privacyStates);
						result.put(URN_SURVEY_PRIVACY_STATE, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_UTC_TIMESTAMP)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, timestamps);
						result.put(URN_CONTEXT_UTC_TIMESTAMP, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_TIMEZONE)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, timezones);
						result.put(URN_CONTEXT_TIMEZONE, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, locationStatuses);
						result.put(URN_CONTEXT_LOCATION_STATUS, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, locations);
						result.put(URN_CONTEXT_LOCATION_STATUS, values);
					}
					if(allColumns || columnList.contains(URN_SURVEY_ID)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, surveyIds);
						result.put(URN_SURVEY_ID, values);
					}
					if(allColumns || columnList.contains(URN_SURVEY_TITLE)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, surveyTitles);
						result.put(URN_SURVEY_TITLE, values);
					}
					if(allColumns || columnList.contains(URN_SURVEY_DESCRIPTION)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, surveyDescriptions);
						result.put(URN_SURVEY_DESCRIPTION, values);
					}
					if(allColumns || columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_LONG)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, launchContexts);
						result.put(URN_CONTEXT_LAUNCH_CONTEXT_LONG, values);
					}
					if(columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_SHORT)) {
						JSONObject values = new JSONObject();
						values.put(VALUES, launchContexts);
						result.put(URN_CONTEXT_LAUNCH_CONTEXT_SHORT, values);
					}
					if(allColumns || columnList.contains(URN_PROMPT_RESPONSE)) {
						for(String promptId : prompts.keySet()) {
							result.put(
									URN_PROMPT_ID_PREFIX + promptId, 
									prompts.get(promptId));
						}
					}
					
					JSONObject metadata = null;
					if(! suppressMetadata) {
						metadata = new JSONObject();
						
						metadata.put("campaign_urn", campaignUrn);
						metadata.put("number_of_prompts", prompts.size());
						metadata.put("number_of_surveys", surveys.size());
					}
					
					if(OUTPUT_FORMAT_JSON_COLUMNS.equals(outputFormat)) {
						httpResponse.setContentType("text/html");
						
						JSONObject resultJson = new JSONObject();
						resultJson.put(JSON_KEY_RESULT, RESULT_SUCCESS);
						
						if(! suppressMetadata) {
							metadata.put("items", result.keys());
							resultJson.put(METADATA, metadata);
						}
						
						resultJson.put(JSON_KEY_DATA, result);
						
						if(prettyPrint) {
							resultString = resultJson.toString(4);
						}
						else {
							resultString = resultJson.toString();
						}
					}
					else if(OUTPUT_FORMAT_CSV.equals(outputFormat)) {
						httpResponse.setContentType("text/csv");
						httpResponse.setHeader("Content-Disposition", "attachment; filename=SurveyResponses.csv");

						StringBuilder resultBuilder = new StringBuilder();
						
						if(! suppressMetadata) {
							metadata.put(JSON_KEY_RESULT, RESULT_SUCCESS);
							
							resultBuilder.append("## begin metadata\n");
							resultBuilder.append('#').append(metadata.toString().replace(',', ';')).append('\n');
							resultBuilder.append("## end metadata\n");
						}
						
						// Prompt contexts.
						resultBuilder.append("## begin prompt contexts\n");
						for(String promptId : prompts.keySet()) {
							JSONObject promptJson = new JSONObject();
							
							promptJson.put(promptId, prompts.get(promptId).toString());
							
							resultBuilder.append('#').append(promptJson.toString()).append('\n');
						}
						resultBuilder.append("## end prompt contexts\n");
						
						// Data.
						resultBuilder.append("## begin data");

						JSONArray keys = new JSONArray(result.keys());
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
						
						for(int i = 0; i < keyLength; i++) {
							for(int j = 0; j < keyLength; j++) {
								resultBuilder.append(result.getJSONObject(keys.getString(j)).getJSONArray(VALUES).get(i));
								
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
				LOGGER.error(e);
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
	
	private void populatePrompts(
			final Map<Integer, SurveyItem> surveyItems,
			Map<String, JSONObject> prompts) 
			throws JSONException {
		
		for(SurveyItem surveyItem : 
			surveyItems.values()) {
	
			if(surveyItem instanceof Prompt) {
				Prompt prompt = (Prompt) surveyItem;
				
				JSONObject promptJson = new JSONObject();
				promptJson.put(CONTEXT, prompt.toJson());
				promptJson.put(VALUES, new JSONArray());
				
				prompts.put(prompt.getId(), promptJson);
			}
			else if(surveyItem instanceof RepeatableSet) {
				RepeatableSet repeatableSet = (RepeatableSet) surveyItem;
				populatePrompts(repeatableSet.getPrompts(), prompts);
			}
		}
	}
	
	private void processResponses(final boolean allColumns, 
			final SurveyResponse surveyResponse,
			final Map<Integer, Response> responses, 
			Map<String, Survey> surveys, Map<String, JSONObject> prompts,
			JSONArray usernames, JSONArray clients, JSONArray privacyStates,
			JSONArray timestamps, JSONArray timezones,
			JSONArray locationStatuses, JSONArray locations,
			JSONArray surveyIds, JSONArray surveyTitles, 
			JSONArray surveyDescriptions, JSONArray launchContexts) 
			throws JSONException {

		List<Integer> indices = new ArrayList<Integer>(responses.keySet());
		Collections.sort(indices);
		
		for(Integer index : indices) {
			Response response = responses.get(index);
			if(response instanceof PromptResponse) {				
				if(allColumns || columnList.contains(URN_USER_ID)) {
					usernames.put(surveyResponse.getUsername());
				}
				if(allColumns || columnList.contains(URN_CONTEXT_CLIENT)) {
					clients.put(surveyResponse.getClient());
				}
				if(allColumns || columnList.contains(URN_SURVEY_PRIVACY_STATE)) {
					privacyStates.put(surveyResponse.getPrivacyState().toString());
				}
				if(allColumns || columnList.contains(URN_CONTEXT_UTC_TIMESTAMP)) {
					timestamps.put(TimeUtils.getIso8601DateTimeString(surveyResponse.getDate()));
				}
				if(allColumns || columnList.contains(URN_CONTEXT_TIMEZONE)) {
					timezones.put(surveyResponse.getTimezone().getID());
				}
				if(allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS)) {
					locationStatuses.put(surveyResponse.getLocationStatus().toString());
				}
				if(allColumns || columnList.contains(URN_CONTEXT_LOCATION_STATUS)) { // FIXME: This should break down the location object.
					locations.put(surveyResponse.getLocation().toJson(false)); 
				}
				if(allColumns || columnList.contains(URN_SURVEY_ID)) {
					surveyIds.put(surveyResponse.getSurvey().getId());
				}
				if(allColumns || columnList.contains(URN_SURVEY_TITLE)) {
					surveyTitles.put(surveyResponse.getSurvey().getTitle());
				}
				if(allColumns || columnList.contains(URN_SURVEY_DESCRIPTION)) {
					surveyDescriptions.put(surveyResponse.getSurvey().getDescription());
				}
				if(allColumns || columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_LONG) || columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_SHORT)) {
					launchContexts.put(surveyResponse.getLaunchContext().toJson(columnList.contains(URN_CONTEXT_LAUNCH_CONTEXT_LONG)));
				}
				if(allColumns || columnList.contains(URN_PROMPT_RESPONSE)) {
					for(String promptId : prompts.keySet()) {
						JSONArray values = prompts.get(promptId).getJSONArray(VALUES);
						if(promptId.equals(response.getId())) {
							values.put(response.getResponseValue());
						}
						else {
							values.put("null");
						}
					}
				}
			}
			else if(response instanceof RepeatableSetResponse) {
				RepeatableSetResponse repeatableSetResponse =
					(RepeatableSetResponse) response;
				
				Map<Integer, Map<Integer, Response>> repeatableSetResponses =
					repeatableSetResponse.getResponseGroups();
				
				List<Integer> rsIndices = new ArrayList<Integer>(repeatableSetResponses.keySet());
				Collections.sort(rsIndices);
				
				for(Integer rsIndex : rsIndices) {
					this.processResponses(allColumns, surveyResponse,
							repeatableSetResponses.get(rsIndex), 
							surveys, prompts, 
							usernames, clients, privacyStates, 
							timestamps, timezones, 
							locationStatuses, locations, 
							surveyIds, surveyTitles, surveyDescriptions, 
							launchContexts
						);
				}
			}
		}
	}
}
