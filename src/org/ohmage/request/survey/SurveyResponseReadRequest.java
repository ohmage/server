package org.ohmage.request.survey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.cache.UserBin;
import org.ohmage.dao.SurveyResponseReadDao;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.survey.read.CustomChoiceItem;
import org.ohmage.domain.survey.read.PromptResponseMetadata;
import org.ohmage.domain.survey.read.SurveyResponseReadIndexedResult;
import org.ohmage.domain.survey.read.SurveyResponseReadResult;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.JsonUtils;
import org.ohmage.util.StringUtils;
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
 *         {@value #_URN_CONTEXT_CLIENT},
 *         {@value #_URN_CONTEXT_TIMESTAMP},
 *         {@value #_URN_CONTEXT_URN_CONTEXT_TIMEZONE},
 *         {@value #_URN_CONTEXT_UTC_TIMESTAMP},
 *         {@value #_URN_CONTEXT_LAUNCH_CONTEXT_LONG},
 *         {@value #_URN_CONTEXT_LAUNCH_CONTEXT_SHORT},
 *         {@value #_URN_CONTEXT_LOCATION_STATUS},
 *         {@value #_URN_CONTEXT_LOCATION_LATITUDE},
 *         {@value #_URN_CONTEXT_LOCATION_TIMESTAMP},
 *         {@value #_URN_CONTEXT_LOCATION_ACCURACY},
 *         {@value #_URN_CONTEXT_LOCATION_PROVIDER},
 *         {@value #_URN_USER_ID},
 *         {@value #_URN_SURVEY_ID},
 *         {@value #_URN_SURVEY_TITLE},
 *         {@value #_URN_SURVEY_DESCRIPTION},
 *         {@value #_URN_SURVEY_PRIVACY_STATE},
 *         {@value #_URN_REPEATABLE_SET_ID},
 *         {@value #_URN_REPEATABLE_SET_ITERATION},
 *         {@value #_URN_PROMPT_RESPONSE}
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
	private final String sortOrder;
	private final SurveyResponsePrivacyStateCache.PrivacyState privacyState;
	private final Boolean collapse;
	
	private Configuration configuration;
	private List<SurveyResponseReadResult> surveyResponseList;
	
	private static final int MAGIC_CUSTOM_CHOICE_INDEX = 100;
	
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
	
	// FIXME: some of these constants belong elsewhere with the survey response
	// upload prompt type hierarchy where there all still a bunch of hard-coded
	// strings
//	private static final String SKIPPED = "SKIPPED";
//	private static final String NOT_DISPLAYED = "NOT_DISPLAYED";
	private static final String VALUE = "value";
	private static final String SINGLE_CHOICE_CUSTOM = "single_choice_custom";
	private static final String MULTI_CHOICE_CUSTOM = "multi_choice_custom";
	private static final String CUSTOM_CHOICES = "custom_choices";
	private static final String CHOICE_ID = "choice_id";
	private static final String CHOICE_VALUE = "choice_value";
	private static final String GLOBAL = "global";
	private static final String CUSTOM = "custom";
	
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
		String tSortOrder = getParameter(InputKeys.SORT_ORDER);
		SurveyResponsePrivacyStateCache.PrivacyState tPrivacyState = null;
		
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
				// FIXME constant-ify the hard-coded strings, maybe even for
				// logging messages (possibly AOP-ify logging)
				// Also, move validation to the SurveyResponseReadValidators
				// And check for multiple copies of params
				
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
				catch (ValidationException e) { // FIXME the DateValidators methods should take a Request parameter to fail
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
				tSortOrder = SurveyResponseReadValidators.validateSortOrder(this, tSortOrder, ALLOWED_SORT_ORDER_LIST); 
				
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
			
			LOGGER.info("Populating the requester with their associated campaigns and roles.");
			UserCampaignServices.populateUserWithCampaignRoleInfo(this, this.getUser());
			
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
			this.surveyResponseList = SurveyResponseReadDao.retrieveSurveyResponses(this, this.userList,
					this.campaignUrn, this.promptIdList, this.surveyIdList, this.startDate, this.endDate, this.sortOrder, 
					this.configuration);
			
			LOGGER.info("Found " + surveyResponseList.size() + " results");
			
			LOGGER.info("Filtering survey response results according to our privacy rules and the requester's role.");
			SurveyResponseReadServices.performPrivacyFilter(this.getUser(), this.campaignUrn, surveyResponseList, this.privacyState);
			
			LOGGER.info("Found: " + surveyResponseList.size() + " results after filtering.");
		}
		
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
		catch(DataAccessException e) {
			e.logException(LOGGER);
		}
	}
	
	/**
	 * Builds the output depending on the state of this request and whatever
	 * output format the requester selected.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		Writer writer = null;
		
		try {
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(httpRequest, httpResponse)));
			
			// Sets the HTTP headers to disable caching
			expireResponse(httpResponse);
			
			// Don't attempt to generate output if the request has failed.
			// If the request ever failed, write an error message.
			if(isFailed()) {
				
				writer.write(getFailureMessage());
				
			} else {
				
				// Prepare for sending the response to the client
				String responseText = null;
				
				final String token = this.getUser().getToken(); 
				if(token != null) {
					CookieUtils.setCookieValue(httpResponse, InputKeys.AUTH_TOKEN, token, (int) (UserBin.getTokenRemainingLifetimeInMillis(token) / MILLIS_IN_A_SECOND));
				}
				
				// Set the content type depending on the output format the 
				// requester requested.
				if(OUTPUT_FORMAT_CSV.equals(this.outputFormat)) {
					
					httpResponse.setContentType("text/csv");
					httpResponse.setHeader("Content-Disposition", "attachment; f.txt");
					
				} else {
					
					httpResponse.setContentType("application/json");
				}
				
				// Build the appropriate response 
				if(! isFailed()) {
					
					List<String> columnList = this.columnList;
					List<String> outputColumns = new ArrayList<String>();
					List<SurveyResponseReadIndexedResult> indexedResultList = new ArrayList<SurveyResponseReadIndexedResult>();
					
					// Build the column headers
					// Each column is a Map with a list containing the values for each row
					
					if(URN_SPECIAL_ALL.equals(columnList.get(0))) {
						outputColumns.addAll(ALLOWED_COLUMN_URN_LIST);
					} else {
						outputColumns.addAll(columnList);
					}
					
					if(columnList.contains(URN_PROMPT_RESPONSE) || URN_SPECIAL_ALL.equals(columnList.get(0))) {
						// The logic here is that if the user is requesting results for survey ids, they want all of the prompts
						// for those survey ids
						// So, loop through the results and find all of the unique prompt ids by forcing them into a Set
						Set<String> promptIdSet = new HashSet<String>();
						
						if(0 != surveyResponseList.size()) {
							for(SurveyResponseReadResult result : surveyResponseList) {
								
								promptIdSet.add(URN_PROMPT_ID_PREFIX + result.getPromptId());
							}
							outputColumns.addAll(promptIdSet);
						}
					}
					
					// get rid of urn:ohmage:prompt:response because it has been replaced with specific prompt ids
					// the list will be unchanged if it didn't already contain urn:ohmage:prompt:response 
					outputColumns.remove(URN_PROMPT_RESPONSE);
					
					// For every result found by the query, the prompt responses need to be rolled up so they are all stored
					// with their associated survey response and metadata. Each prompt response is returned from the db in its
					// own row and the rows can have different sort orders.
					
					boolean isCsv = OUTPUT_FORMAT_CSV.equals(this.outputFormat);
					
					for(SurveyResponseReadResult result : surveyResponseList) {
						
						if(indexedResultList.isEmpty()) { // first time thru 
							indexedResultList.add(new SurveyResponseReadIndexedResult(result, isCsv));
						}
						else {
							int numberOfIndexedResults = indexedResultList.size();
							boolean found = false;
							for(int i = 0; i < numberOfIndexedResults; i++) {
								if(indexedResultList.get(i).getKey().keysAreEqual(result.getUsername(),
										                                          result.getTimestamp(),
																				  result.getEpochMillis(),
										                                          result.getSurveyId(),
										                                          result.getRepeatableSetId(),
										                                          result.getRepeatableSetIteration())) {
									
									found = true;
									indexedResultList.get(i).addPromptResponse(result, isCsv);
								}
							}
							if(! found) {
								indexedResultList.add(new SurveyResponseReadIndexedResult(result, isCsv));
							}
						}
					}
					
					// For csv and json-columns output, the custom choices need to be converted
					// into unique-ified list in order for visualiztions and export to work
					// properly. The key is the prompt id.
					Map<String, List<CustomChoiceItem>> uniqueCustomChoiceMap = null; // will be converted into a choice glossary
					                                                                  // for the custom types
					Map<String, Integer> uniqueCustomChoiceIndexMap = null;
					
					// Now find the custom choice prompts (if there are any) and 
					// unique-ify the entries for their choice glossaries, create 
					// their choice glossaries, and clean up the display value 
					// (i.e., remove all the custom_choices stuff and leave only
					// the value or values the user selected).
						
					for(SurveyResponseReadIndexedResult result : indexedResultList) {
						Map<String, PromptResponseMetadata> promptResponseMetadataMap = result.getPromptResponseMetadataMap();
						Iterator<String> responseMetadataKeyIterator = promptResponseMetadataMap.keySet().iterator();
						
						while(responseMetadataKeyIterator.hasNext()) {
							String promptId = (responseMetadataKeyIterator.next());
							PromptResponseMetadata metadata = promptResponseMetadataMap.get(promptId);
							
							if(SINGLE_CHOICE_CUSTOM.equals(metadata.getPromptType()) || MULTI_CHOICE_CUSTOM.equals(metadata.getPromptType())) {
								
								List<CustomChoiceItem> customChoiceItems = null;
								
								if(null == uniqueCustomChoiceMap) { // lazily initialized in case there are no custom choices
									uniqueCustomChoiceMap = new HashMap<String, List<CustomChoiceItem>>();
								} 
								
								if(! uniqueCustomChoiceMap.containsKey(promptId)) {
									customChoiceItems = new ArrayList<CustomChoiceItem>();
									uniqueCustomChoiceMap.put(promptId, customChoiceItems);
								} 
								else {
									customChoiceItems = uniqueCustomChoiceMap.get(promptId);
								}
								
								// All of the data for the choice_glossary for custom types is stored in its JSON response
								
								JSONObject customChoiceResponse = (JSONObject) (result.getPromptResponseMap().get(promptId));								
								
								Integer singleChoiceValue = JsonUtils.getIntegerFromJsonObject(customChoiceResponse, VALUE);
								if(null != singleChoiceValue) {
									result.getPromptResponseMap().put(promptId, singleChoiceValue);
								} 
								else if(null != JsonUtils.getStringFromJsonObject(customChoiceResponse, VALUE)) {
									result.getPromptResponseMap().put(promptId, JsonUtils.getStringFromJsonObject(customChoiceResponse, VALUE));
								}
								else {
									result.getPromptResponseMap().put(promptId, JsonUtils.getJsonArrayFromJsonObject(customChoiceResponse, VALUE));
								}								
 
								// Since the glossary will not contain the custom choices, the result's display value 
								// can simply be the values the user chose.
								
								JSONArray customChoices = JsonUtils.getJsonArrayFromJsonObject(customChoiceResponse, CUSTOM_CHOICES);
								
								if(customChoices != null) {
								
									for(int i = 0; i < customChoices.length(); i++) {
										JSONObject choice = JsonUtils.getJsonObjectFromJsonArray(customChoices, i);
	
										// If the choice_id is >= 100, it means that is it a choice that the user added
										// In the current system, users cannot remove choices
										int originalId = choice.getInt(CHOICE_ID);
										CustomChoiceItem cci = null;
										boolean isGlobal = false;
										if(originalId < MAGIC_CUSTOM_CHOICE_INDEX) {										
											cci = new CustomChoiceItem(originalId, result.getUsername(), choice.getString(CHOICE_VALUE), GLOBAL);
											isGlobal = true;
										} 
										else {
											cci = new CustomChoiceItem(originalId, result.getUsername(), choice.getString(CHOICE_VALUE), CUSTOM);
										}
										
										if(! customChoiceItems.contains(cci)) {
											if(isGlobal) {
												cci.setId(cci.getOriginalId());
												customChoiceItems.add(cci);
											}
											else {
												if(null == uniqueCustomChoiceIndexMap) {
													uniqueCustomChoiceIndexMap = new HashMap<String, Integer>();												
												}
												
												if(! uniqueCustomChoiceIndexMap.containsKey(promptId)) {
													uniqueCustomChoiceIndexMap.put(promptId, MAGIC_CUSTOM_CHOICE_INDEX - 1);
												}
												
												int uniqueId = uniqueCustomChoiceIndexMap.get(promptId) + 1;
												cci.setId(uniqueId);
												customChoiceItems.add(cci);
												uniqueCustomChoiceIndexMap.put(promptId, uniqueId);
											}
										}	
									}
								}
							}
						}
	 				}
					
					int numberOfSurveys = indexedResultList.size();
					int numberOfPrompts = this.surveyResponseList.size();
					
					// Delete the original result list
					this.surveyResponseList.clear();
					this.surveyResponseList = null;
					
					if(OUTPUT_FORMAT_JSON_ROWS.equals(this.outputFormat)) {
						
						responseText = SurveyResponseReadServices.generateJsonRowsOutput(this, numberOfSurveys, numberOfPrompts, indexedResultList, outputColumns, uniqueCustomChoiceMap);
						
					}
					else if(OUTPUT_FORMAT_JSON_COLUMNS.equals(this.outputFormat)) {
						
						if(indexedResultList.isEmpty()) {
							
							responseText = SurveyResponseReadServices.generateZeroResultJsonColumnOutput(this, outputColumns);
							
						} else {
							
							responseText = SurveyResponseReadServices.generateMultiResultJsonColumnOutput(this, numberOfSurveys, numberOfPrompts, indexedResultList, outputColumns, uniqueCustomChoiceMap);
						}
					}
					
					else if(OUTPUT_FORMAT_CSV.equals(this.outputFormat)) {
						
						if(indexedResultList.isEmpty()) {
							
							responseText = SurveyResponseReadServices.generateZeroResultCsvOutput(this, outputColumns);
							
						} else {
							
							responseText = SurveyResponseReadServices.generateMultiResultCsvOutput(this, numberOfSurveys, numberOfPrompts, indexedResultList, outputColumns, uniqueCustomChoiceMap);
						}
					}
				} 
				else {
					
					// Even for CSV output, the error messages remain JSON
					responseText = getFailureMessage();
				}
				
				LOGGER.info("Writing survey response read output.");
				writer.write(responseText);
			}
		}
		
		// FIXME and catch the actual exceptions
		catch(Exception e) {
			
			LOGGER.error("An unrecoverable exception occurred while generating a survey response read response", e);
			try {
				writer.write(getFailureMessage());
			} catch (Exception ee) {
				LOGGER.error("Caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} 
		finally {
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					LOGGER.error("Caught IOException when attempting to free resources", ioe);
					
				}
			}
		}
	}
	
	/* Methods for retrieving output formatting variables */
	
	// FIXME: these should just be passed (within one object) to the methods that write the output 
	
	public Boolean getPrettyPrint() {
		return prettyPrint;
	}
	
	public Boolean getSuppressMetadata() {
		return suppressMetadata;
	}
	
	public Boolean getReturnId() {
		return returnId;
	}
	
	public Boolean getCollapse() {
		return collapse;
	}
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	public String getCampaignUrn() {
		return campaignUrn;
	}
}
