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
package org.ohmage.request.survey;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.domain.Location;
import org.ohmage.domain.Location.LocationColumnKey;
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
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.omh.OmhReadResponder;
import org.ohmage.util.TimeUtils;
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
 *         or the value {@value #URN_SPECIAL_ALL}. If 
 *         {@value #URN_SPECIAL_ALL} is not used, the only allowed values must
 *         be from {@link org.ohmage.domain.campaign.SurveyResponse.ColumnKey}.
 *         </td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#OUTPUT_FORMAT}</td>
 *     <td>The desired output format of the results. Must be one of 
 *     {@value #OUTPUT_FORMAT_JSON_ROWS}, {@value #OUTPUT_FORMAT_JSON_COLUMNS},
 *     or, {@value #OUTPUT_FORMAT_CSV}</td>
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
 *     updates.<br />
 *     <br />
 *     Deprecated: Instead, users should now use the survey response ID key.
 *     </td>
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
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_ID_LIST}</td>
 *     <td>Filters the results to only those whose UUID is in the given list.
 *       </td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author Joshua Selsky
 */
public final class SurveyResponseReadRequest
		extends SurveyResponseRequest
		implements OmhReadResponder {
	
	public static final Logger LOGGER = 
			Logger.getLogger(SurveyResponseReadRequest.class);
	
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
	
	private final Collection<SurveyResponse.ColumnKey> columns;
	private final SurveyResponse.OutputFormat outputFormat;
	private final List<SortParameter> sortOrder;

	final Boolean collapse;
	private final Boolean prettyPrint;
	private final Boolean returnId;
	private final Boolean suppressMetadata;
	
	final long surveyResponsesToSkip;
	final long surveyResponsesToProcess;
	
	/**
	 * Creates a survey response read request. The 'httpRequest', 'parameters',
	 * and 'campaignId' parameters are required. The rest are optional and will
	 * limit the results.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters from the HTTP request.
	 * 
	 * @param campaignId The campaign's unique identifier. Required.
	 * 
	 * @param usernames A set of usernames.
	 * 
	 * @param surveyIds A set of survey IDs.
	 * 
	 * @param promptIds A set of prompt IDs.
	 * 
	 * @param surveyResponseIds A set of survey response IDs.
	 * 
	 * @param startDate Limits the responses to only those on or after this
	 * 					date.
	 * 
	 * @param endDate Limits the responses to only those on or before this 
	 * 				  date.
	 * 
	 * @param privacyState A survey response privacy state.
	 * 
	 * @param promptResponseSearchTokens A set of tokens which must match the
	 * 									 prompt responses.
	 * 
	 * @param columns The columns of data to return.
	 * 
	 * @param outputFormat The format of the output.
	 * 
	 * @param sortOrder How to sort the parameters.
	 * 
	 * @param collapse Whether or not to collapse the results.
	 * 
	 * @param prettyPrint Whether or not to format the results with whitespace.
	 * 
	 * @param returnId Whether or not to return the response's unique 
	 * 				   identifier with the data.
	 * 
	 * @param suppressMetadata Whether or not to suppress the metadata section.
	 * 
	 * @param numResponsesToSkip The number of survey responses to skip.
	 * 
	 * @param numResponsesToReturn The number of survey responses to return.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws IllegalArgumentException Thrown if a required parameter is 
	 * 									missing.
	 */
	public SurveyResponseReadRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final String campaignId,
			final Collection<String> usernames,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final Set<UUID> surveyResponseIds,
			final DateTime startDate,
			final DateTime endDate,
			final SurveyResponse.PrivacyState privacyState,
			final Set<String> promptResponseSearchTokens,
			final Collection<SurveyResponse.ColumnKey> columns,
			final SurveyResponse.OutputFormat outputFormat,
			final List<SortParameter> sortOrder,
			final Boolean collapse,
			final Boolean prettyPrint,
			final Boolean returnId,
			final Boolean suppressMetadata,
			final Long numResponsesToSkip,
			final Long numResponsesToReturn)
			throws IOException, InvalidRequestException {
		
		super(
			httpRequest, 
			parameters, 
			campaignId, 
			usernames, 
			surveyIds, 
			promptIds, 
			surveyResponseIds, 
			startDate, 
			endDate, 
			privacyState, 
			promptResponseSearchTokens);
		
		this.columns = columns;
		this.outputFormat = outputFormat;
		this.sortOrder = sortOrder;
		this.collapse = collapse;
		this.prettyPrint = prettyPrint;
		this.returnId = returnId;
		this.suppressMetadata = suppressMetadata;
		
		if(numResponsesToSkip == null) {
			this.surveyResponsesToSkip = 0;
		}
		else {
			this.surveyResponsesToSkip = numResponsesToSkip;
		}
		
		if(numResponsesToReturn == null) {
			long tNumResponsesToReturn = 0;
			try {
				tNumResponsesToReturn = 
					Long.decode(
						PreferenceCache.instance().lookup(
							PreferenceCache.KEY_MAX_SURVEY_RESPONSE_PAGE_SIZE));
				
				if(tNumResponsesToReturn == -1) {
					tNumResponsesToReturn = Long.MAX_VALUE;
				}
			}
			catch(CacheMissException e) {
				LOGGER.error("The cache is missing the max page size.", e);
				setFailed();
			}
			catch(NumberFormatException e) {
				LOGGER.error("The max page size is not a number.", e);
				setFailed();
			}
			this.surveyResponsesToProcess = tNumResponsesToReturn;
		}
		else {
			this.surveyResponsesToProcess = numResponsesToReturn;
		}
	}
	
	/**
	 * Creates a survey response read request.
	 * 
	 * @param httpRequest  The request to retrieve parameters from.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public SurveyResponseReadRequest(
			final HttpServletRequest httpRequest)
			throws IOException, InvalidRequestException {
		
		super(httpRequest);
		
		Set<SurveyResponse.ColumnKey> tColumns = null;
		SurveyResponse.OutputFormat tOutputFormat = null;
		List<SortParameter> tSortOrder = null;

		Boolean tCollapse = null;
		Boolean tPrettyPrint = null;
		Boolean tReturnId = null;
		Boolean tSuppressMetadata = null;
		
		long tSurveyResponsesToSkip = 0;
		long tSurveyResponsesToProcess = -1;
		try {
			tSurveyResponsesToProcess = 
					Long.decode(
							PreferenceCache.instance().lookup(
									PreferenceCache.KEY_MAX_SURVEY_RESPONSE_PAGE_SIZE));
			
			if(tSurveyResponsesToProcess == -1) {
				tSurveyResponsesToProcess = Long.MAX_VALUE;
			}
		}
		catch(CacheMissException e) {
			LOGGER.error("The cache is missing the max page size.", e);
			setFailed();
		}
		catch(NumberFormatException e) {
			LOGGER.error("The max page size is not a number.", e);
			setFailed();
		}
		
		if(! isFailed()) {
			
			LOGGER.info("Creating a survey response read request.");
			String[] t;
			
			try {
				// Column List
				t = getParameterValues(InputKeys.COLUMN_LIST);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_COLUMN_ID, 
							"The required column list was missing: " + 
								InputKeys.COLUMN_LIST);
				}
				else if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_COLUMN_ID, 
							"Multiple column lists were given: " + 
								InputKeys.COLUMN_LIST);
				}
				else {
					tColumns = 
							SurveyResponseValidators.validateColumnList(t[0]);
					
					if(tColumns == null) {
						throw new ValidationException(
								ErrorCode.SURVEY_INVALID_COLUMN_ID, 
								"The required column list was missing: " + 
									InputKeys.COLUMN_LIST);
					}
				}
				
				// Output Format
				t = getParameterValues(InputKeys.OUTPUT_FORMAT);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, 
							"The output format is missing: " + 
								InputKeys.OUTPUT_FORMAT);
				}
				else if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, 
							"Multiple output formats were given: " + 
								InputKeys.OUTPUT_FORMAT);
				}
				else {
					tOutputFormat = 
							SurveyResponseValidators.validateOutputFormat(
									t[0]);
					
					if(tOutputFormat == null) {
						throw new ValidationException(
								ErrorCode.SURVEY_INVALID_OUTPUT_FORMAT, 
								"The output format is missing: " + 
									InputKeys.OUTPUT_FORMAT);
					}
				}
				
				// Sort Order
				t = getParameterValues(InputKeys.SORT_ORDER);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SORT_ORDER, 
							"Multiple sort order lists were given: " + 
								InputKeys.SORT_ORDER);
				}
				else if(t.length == 1) {
					tSortOrder = 
							SurveyResponseValidators.validateSortOrder(t[0]);
				}
				
				// Collapse
				t = getParameterValues(InputKeys.COLLAPSE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_COLLAPSE_VALUE, 
							"Multiple collapse values were given: " + 
								InputKeys.COLLAPSE);
				}
				else if(t.length == 1) {
					tCollapse = 
							SurveyResponseValidators.validateCollapse(t[0]);
				}
				
				// Pretty print
				t = getParameterValues(InputKeys.PRETTY_PRINT);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_PRETTY_PRINT_VALUE, 
							"Multiple pretty print values were given: " + 
								InputKeys.PRETTY_PRINT);
				}
				else if(t.length == 1) {
					tPrettyPrint = 
							SurveyResponseValidators.validatePrettyPrint(t[0]);
				}
				
				// Return ID
				t = getParameterValues(InputKeys.RETURN_ID);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_RETURN_ID, 
							"Multiple return ID values were given: " + 
								InputKeys.RETURN_ID);
				}
				else if(t.length == 1) {
					tReturnId = 
							SurveyResponseValidators.validateReturnId(t[0]);
				}
				
				// Suppress metadata
				t = getParameterValues(InputKeys.SUPPRESS_METADATA);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SUPPRESS_METADATA_VALUE, 
							"Multiple suppress metadata values were given: " + 
								InputKeys.SUPPRESS_METADATA);
				}
				else if(t.length == 1) {
					tSuppressMetadata = 
							SurveyResponseValidators.validateSuppressMetadata(
									t[0]);
				}
				
				// Number of survey responses to skip.
				t = getParameterValues(InputKeys.NUM_TO_SKIP);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_SKIP, 
							"Multiple values were given for the number of survey responses to skip: " + 
								InputKeys.NUM_TO_SKIP);
				}
				else if(t.length == 1) {
					tSurveyResponsesToSkip = 
							SurveyResponseValidators
								.validateNumSurveyResponsesToSkip(t[0]);
				}
				
				// Number of survey responses to process.
				t = getParameterValues(InputKeys.NUM_TO_RETURN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_NUM_TO_RETURN, 
							"Multiple values were given for the number of survey responses to process: " + 
								InputKeys.NUM_TO_RETURN);
				}
				else if(t.length == 1) {
					tSurveyResponsesToProcess = 
							SurveyResponseValidators
								.validateNumSurveyResponsesToProcess(
										t[0], 
										tSurveyResponsesToProcess);
				}
			}
			catch (ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		columns = tColumns;
		outputFormat = tOutputFormat;
		sortOrder = tSortOrder;
		
		collapse = tCollapse;
		prettyPrint = tPrettyPrint;
		returnId = tReturnId;
		suppressMetadata = tSuppressMetadata;
		
		surveyResponsesToSkip = tSurveyResponsesToSkip;
		surveyResponsesToProcess = tSurveyResponsesToProcess;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a survey response read request.");
		super.service(
				columns, 
				null, 
				sortOrder,
				collapse, 
				surveyResponsesToSkip, 
				surveyResponsesToProcess);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
	 */
	@Override
	public long getNumDataPoints() {
		return getSurveyResponseCount();
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
		
		// Set the CORS headers.
		handleCORS(httpRequest, httpResponse);
		
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
					for(SurveyResponse surveyResponse : getSurveyResponses()) {
						uniqueSurveyIds.add(surveyResponse.getSurvey().getId());
						uniquePromptIds.addAll(surveyResponse.getPromptIds());
						
						JSONObject currResult;
						currResult = surveyResponse.toJson(
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
								(((returnId == null) ? false : returnId) ||
								 allColumns ||
								 columns.contains(ColumnKey.SURVEY_RESPONSE_ID)
								),
								((collapse != null) && collapse)
							);
						
						if(allColumns || columns.contains(ColumnKey.CONTEXT_DATE)) {
							currResult.put(
									"date", 
									TimeUtils.getIso8601DateString(
											surveyResponse.getDate(),
											false));
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
							currResult.put(
									"timestamp", 
									TimeUtils.getIso8601DateString(
											surveyResponse.getDate(),
											true));
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_UTC_TIMESTAMP)) {
							Calendar tmpCalendar = 
									Calendar.getInstance(
											surveyResponse.getTimezone().toTimeZone());
							tmpCalendar.setTimeInMillis(
									surveyResponse.getTime());
							
							currResult.put(
									"utc_timestamp",
									TimeUtils.getIso8601DateString(
										new DateTime(
											surveyResponse.getTime(), 
											DateTimeZone.UTC),
										true));
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_ACCURACY)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.LocationColumnKey.ACCURACY.toString(false), JSONObject.NULL);
							}
							else {
								double accuracy = location.getAccuracy();
								
								if(Double.isInfinite(accuracy) || Double.isNaN(accuracy)) {
									currResult.put(Location.LocationColumnKey.ACCURACY.toString(false), JSONObject.NULL);
								}
								else {
									currResult.put(Location.LocationColumnKey.ACCURACY.toString(false), accuracy);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LATITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.LocationColumnKey.LATITUDE.toString(false), JSONObject.NULL);
							}
							else {
								double latitude = location.getLatitude();
								
								if(Double.isInfinite(latitude) || Double.isNaN(latitude)) {
									currResult.put(Location.LocationColumnKey.LATITUDE.toString(false), JSONObject.NULL);
								}
								else {
									currResult.put(Location.LocationColumnKey.LATITUDE.toString(false), latitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_LONGITUDE)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.LocationColumnKey.LONGITUDE.toString(false), JSONObject.NULL);
							}
							else {
								double longitude = location.getLongitude();
								
								if(Double.isInfinite(longitude) || Double.isNaN(longitude)) {
									currResult.put(Location.LocationColumnKey.LONGITUDE.toString(false), JSONObject.NULL);
								}
								else {
									currResult.put(Location.LocationColumnKey.LONGITUDE.toString(false), longitude);
								}
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_PROVIDER)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put(Location.LocationColumnKey.PROVIDER.toString(false), JSONObject.NULL);
							}
							else {
								currResult.put(Location.LocationColumnKey.PROVIDER.toString(false), location.getProvider());
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put("location_timestamp", JSONObject.NULL);
							}
							else {
								currResult.put("location_timestamp", location.getTime());
							}
						}
						if(allColumns || columns.contains(ColumnKey.CONTEXT_LOCATION_TIMESTAMP)) {
							Location location = surveyResponse.getLocation();
							
							if(location == null) {
								currResult.put("location_timezone", JSONObject.NULL);
							}
							else {
								currResult.put("location_timezone", location.getTimeZone().getID());
							}
						}
						
						results.put(currResult);
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
						
						// Add the total count to the metadata.
						metadata.put(
								JSON_KEY_TOTAL_NUM_RESULTS, 
								getSurveyResponseCount());
						
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
					JSONArray dates = new JSONArray();
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
					JSONArray surveyResponseIds = new JSONArray();
					JSONArray counts = new JSONArray();
					Map<String, JSONObject> prompts = new HashMap<String, JSONObject>();
					
					// If the user requested to know information about prompt
					// responses, populate the prompt contexts with the 
					// information about each of the prompts that were 
					// requested.
					if(allColumns ||
							columns.contains(ColumnKey.PROMPT_RESPONSE)) {
						
						// If the user-supplied list of survey IDs is present,
						if(getSurveyIds() != null) {
							Map<String, Survey> campaignSurveys = getCampaign().getSurveys();
							// If the user asked for all surveys for this
							// campaign, then populate the prompt information
							// with all of the data about all of the prompts in
							// all of the surveys in this campaign.
							if(getSurveyIds().equals(URN_SPECIAL_ALL_LIST)) {
								for(Survey currSurvey : campaignSurveys.values()) {
									populatePrompts(currSurvey.getSurveyItems(), prompts);
								}
							}
							// Otherwise, populate the prompt information only
							// with the data about the requested surveys.
							else {
								for(String surveyId : this.getSurveyIds()) {
									populatePrompts(campaignSurveys.get(surveyId).getSurveyItems(), prompts);
								}
							}
						}
						// If the user-supplied list of prompt IDs is present,
						else if(getPromptIds() != null) {
							// If the user asked for all prompts for this
							// campaign, then populate the prompt information
							// with all of the data about all of the prompts in
							// this campaign.
							if(getPromptIds().equals(URN_SPECIAL_ALL_LIST)) {
								for(Survey currSurvey : getCampaign().getSurveys().values()) {
									populatePrompts(currSurvey.getSurveyItems(), prompts);
								}
							}
							// Otherwise, populate the prompt information with
							// the data about only the requested prompts.
							else {
								int currNumPrompts = 0;
								Map<Integer, SurveyItem> tempPromptMap = 
										new HashMap<Integer, SurveyItem>(getPromptIds().size());
								
								for(String promptId : getPromptIds()) {
									try {
										tempPromptMap.put(
												currNumPrompts, 
												getCampaign().getPrompt(
														getCampaign().getSurveyIdForPromptId(
																promptId), 
														promptId));
									}
									catch(DomainException e) {
										LOGGER.error(
												"A prompt ID that should have already been validated, appears to no longer exist.",
												e);
										setFailed();
										super.respond(
												httpRequest, 
												httpResponse, 
												null);
									}
									currNumPrompts++;
								}
								
								populatePrompts(tempPromptMap, prompts);
							}
						}
					}
					
					// Process each of the survey responses and keep track of
					// the number of prompt responses.
					int numSurveyResponses = getSurveyResponses().size();
					int numPromptResponses = 0;
					for(SurveyResponse surveyResponse : getSurveyResponses()) {
						try {
							numPromptResponses += processResponses(allColumns, 
									surveyResponse, 
									surveyResponse.getResponses(), 
									prompts, 
									usernames, clients, privacyStates, 
									dates, timestamps, utcTimestamps, 
									epochMillisTimestamps, timezones, 
									locationStatuses, locationLongitude, 
									locationLatitude, locationTimestamp, 
									locationTimeZone,
									locationAccuracy, locationProvider,
									surveyIds, surveyTitles, surveyDescriptions, 
									launchContexts, surveyResponseIds, counts
								);
						} 
						catch(DomainException e) {
							LOGGER.error(
									"There was a problem aggregating the responses.",
									e);
							setFailed();
							super.respond(httpRequest, httpResponse, null);
						}
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
					if(allColumns || columns.contains(ColumnKey.CONTEXT_DATE)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, timestamps);
						result.put(ColumnKey.CONTEXT_DATE.toString(), values);
						keysOrdered.put(ColumnKey.CONTEXT_DATE.toString());
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
					if(allColumns || columns.contains(ColumnKey.SURVEY_RESPONSE_ID)) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, surveyResponseIds);
						result.put(ColumnKey.SURVEY_RESPONSE_ID.toString(), values);
						keysOrdered.put(ColumnKey.SURVEY_RESPONSE_ID.toString());
					}
					if((collapse != null) && collapse) {
						JSONObject values = new JSONObject();
						values.put(JSON_KEY_VALUES, counts);
						result.put("urn:ohmage:context:count", values);
						keysOrdered.put("urn:ohmage:context:count");
					}
					
					// If metadata is not suppressed, create it.
					JSONObject metadata = null;
					if((suppressMetadata == null) || (! suppressMetadata)) {
						metadata = new JSONObject();
						
						metadata.put(InputKeys.CAMPAIGN_URN, getCampaignId());
						metadata.put(JSON_KEY_NUM_SURVEYS, getSurveyResponses().size());
						metadata.put(JSON_KEY_NUM_PROMPTS, numPromptResponses);
						
						// Add the total count to the metadata.
						metadata.put(
								JSON_KEY_TOTAL_NUM_RESULTS, 
								getSurveyResponseCount());
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
						}

						// Get the number of keys.
						int keyLength = keysOrdered.length();
						
						// Create a comma-separated list of the header names.
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
						
						if((suppressMetadata == null) || (! suppressMetadata)) {
							resultBuilder.append("## end data");
						}
						
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
			catch(DomainException e) {
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
			LOGGER.warn("Unable to write response message. Aborting.", e);
		}
		
		// Close it.
		try {
			writer.close();
		}
		catch(IOException e) {
			LOGGER.warn("Unable to close the writer.", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void respond(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException, DomainException {
		
		for(SurveyResponse surveyResponse : getSurveyResponses()) {
			// Start the object.
			generator.writeStartObject();
			
			// Write the data point's metadata.
			generator.writeObjectFieldStart("metadata");
			
			// Write the unique identifier for this point.
			generator.writeStringField(
				"id",
				surveyResponse.getSurveyResponseId().toString());
			
			// Write the timestamp for this point.
			generator.writeStringField(
				"timestamp",
				ISODateTimeFormat
					.dateTime()
					.print(
						new DateTime(
							surveyResponse.getTime(),
							surveyResponse.getTimezone())));
			
			// Write the location for this point.
			Location location = surveyResponse.getLocation();
			if(location != null) {
				generator.writeObjectFieldStart("location");
				location.streamJson(
					generator, 
					false, 
					LocationColumnKey.ALL_COLUMNS);
				generator.writeEndObject();
			}
			
			// End the metadata.
			generator.writeEndObject();
			
			// Write the data point's data.
			generator.writeObjectFieldStart("data");
			
			// Write the survey's ID.
			generator.writeStringField(
				SurveyResponse.JSON_KEY_SURVEY_ID,
				surveyResponse.getSurvey().getId());
			
			// Write the launch context.
			generator.writeObjectFieldStart(
				SurveyResponse.JSON_KEY_SURVEY_LAUNCH_CONTEXT);
			
			// Write the launch context's time.
			generator.writeNumberField(
				SurveyResponse.LaunchContext.JSON_KEY_LAUNCH_TIME,
				surveyResponse.getLaunchContext().getLaunchTime());
			
			// Write the launch context's time zone.
			generator.writeStringField(
				SurveyResponse.LaunchContext.JSON_KEY_LAUNCH_TIMEZONE,
				surveyResponse.getLaunchContext().getTimeZone().getID());
			
			// Write the launch context's active triggers.
			generator.writeArrayFieldStart(
				SurveyResponse.LaunchContext.JSON_KEY_ACTIVE_TRIGGERS);
			
			// Add all of the active triggers.
			JSONArray activeTriggers = 
				surveyResponse.getLaunchContext().getActiveTriggers();
			int numActiveTriggers = activeTriggers.length();
			for(int i = 0; i < numActiveTriggers; i++) {
				try {
					generator.writeString(activeTriggers.getString(i));
				}
				catch(JSONException e) {
					LOGGER.warn(
						"Could not serialize one of the trigger names.",
						e);
				}
			}
			
			// End the launch context's active triggers array.
			generator.writeEndArray();
			
			// End the launch context.
			generator.writeEndObject();
			
			// Write the responses array.
			generator.writeArrayFieldStart(SurveyResponse.JSON_KEY_RESPONSES);
			Map<Integer, Response> responses = surveyResponse.getResponses();
			List<Integer> indices =
				new ArrayList<Integer>(responses.keySet());
			Collections.sort(indices);
			for(Integer index : indices) {
				// Get the response.
				Response response = responses.get(index);
				
				// Start the response.
				generator.writeStartObject();
				
				// Write the response's ID.
				generator.writeStringField(
					PromptResponse.JSON_KEY_PROMPT_ID,
					response.getId());
				
				// Write the response.
				generator.writeObjectField(
					PromptResponse.JSON_KEY_RESPONSE,
					response.getResponse());
				
				// End the response.
				generator.writeEndObject();
			}
			generator.writeEndArray();
			
			// End the data field.
			generator.writeEndObject();
			
			// End the object.
			generator.writeEndObject();
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
					
					prompts.put(prompt.getId() + ":key", promptJsonKey);
					prompts.put(prompt.getId() + ":label", promptJsonLabel);
					
					if(prompt.hasValues()) {
						JSONObject promptJsonValue = new JSONObject();
						promptJsonValue.put(JSON_KEY_CONTEXT, prompt.toJson());
						promptJsonValue.put(JSON_KEY_VALUES, new JSONArray());

						prompts.put(prompt.getId() + ":value", promptJsonValue);
					}
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
	 * 
	 * @throws DomainException There was a problem aggregating the data.
	 */
	private int processResponses(final boolean allColumns, 
			final SurveyResponse surveyResponse,
			final Map<Integer, Response> responses, 
			Map<String, JSONObject> prompts,
			JSONArray usernames, JSONArray clients, JSONArray privacyStates,
			JSONArray dates, JSONArray timestamps, JSONArray utcTimestamps, 
			JSONArray epochMillisTimestamps, JSONArray timezones,
			JSONArray locationStatuses, JSONArray locationLongitude,
			JSONArray locationLatitude, JSONArray locationTime,
			JSONArray locationTimeZone,
			JSONArray locationAccuracy, JSONArray locationProvider,
			JSONArray surveyIds, JSONArray surveyTitles, 
			JSONArray surveyDescriptions, JSONArray launchContexts,
			JSONArray surveyResponseIds, JSONArray counts) 
			throws JSONException, DomainException {

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
		if(allColumns || columns.contains(ColumnKey.CONTEXT_DATE)) {
			dates.put(
					TimeUtils.getIso8601DateString(
						surveyResponse.getDate(), false));
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_TIMESTAMP)) {
			timestamps.put(
					TimeUtils.getIso8601DateString(
						surveyResponse.getDate(), true));
		}
		if(allColumns || columns.contains(ColumnKey.CONTEXT_UTC_TIMESTAMP)) {
			utcTimestamps.put(
					TimeUtils.getIso8601DateString(
						new DateTime(
							surveyResponse.getTime(), 
							DateTimeZone.UTC), 
						true));
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
		if(allColumns || columns.contains(ColumnKey.SURVEY_RESPONSE_ID)) {
			surveyResponseIds.put(surveyResponse.getSurveyResponseId().toString());
		}
		if((collapse != null) && collapse) {
			counts.put(surveyResponse.getCount());
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
						Object responseObject = response.getResponse();
						
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
							
							if(((ChoicePrompt) prompt).hasValues()) {
								JSONArray values =
										prompts.get(responseId + ":value")
											.getJSONArray(JSON_KEY_VALUES);
								values.put(JSONObject.NULL);
							}
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
								Collection<Integer> keys = (Collection <Integer>) responseObject;
								
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
							
							if(choicePrompt.hasValues()) {
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
						
						Object responseValue = response.getResponse();
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
