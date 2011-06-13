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
package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.domain.PromptContext;
import org.ohmage.domain.SurveyResponseReadFormattedResult;
import org.ohmage.domain.SurveyResponseReadResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.DateUtils;
import org.ohmage.util.JsonUtils;


/** 
 * Giant writer for the new data point API. This class needs to be refactored so its constituent parts can be used in other 
 * output writers (e.g., csv).
 * 
 * TODO - the *ResponseWriter classes do more than just write output. The creation of the formatted output should be separated
 * out from any kind of I/O in order to allow refactoring to use different kinds of I/O e.g., streaming/comet.  
 * 
 * @author selsky
 */
public class SurveyResponseReadResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadResponseWriter.class);
	private Map<String, SurveyResponseReadColumnOutputBuilder> _columnOutputBuilderMap;
	private SurveyResponseReadJsonRowBasedOutputBuilder _rowBasedOutputBuilder;
	
	private List<String> _columnNames;
	
	public SurveyResponseReadResponseWriter(ErrorResponse errorResponse, 
			                               List<String> columnNames,
			                               Map<String, SurveyResponseReadColumnOutputBuilder> columnOutputBuilderMap,
			                               SurveyResponseReadJsonRowBasedOutputBuilder rowBasedOutputBuilder) {
		super(errorResponse);
		if(null == columnNames || columnNames.size() == 0) {
			throw new IllegalArgumentException("A non-null, non-empty columnNames list is required");
		}
		if(null == columnOutputBuilderMap || columnOutputBuilderMap.isEmpty()) {
			throw new IllegalArgumentException("A non-null, non-empty output builder map is required");
		}
		if(null == rowBasedOutputBuilder) {
			throw new IllegalArgumentException("A non-null row-based output builder is required");
		}
		_columnNames = columnNames;
		_columnOutputBuilderMap = columnOutputBuilderMap;
		_rowBasedOutputBuilder = rowBasedOutputBuilder;
	}
	
	/**
	 * Creates column-based output based on the user's query selections.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
			
			// Set the content type
			if("csv".equals(req.getOutputFormat())) {
				response.setContentType("text/csv");
				response.setHeader("Content-Disposition", "attachment; f.txt");
			} else {
				response.setContentType("application/json");
			}
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				List<String> columnList = req.getColumnList();
				List<String> outputColumns = new ArrayList<String>();
				@SuppressWarnings("unchecked")
				List<SurveyResponseReadResult> results = (List<SurveyResponseReadResult>) req.getResultList();
				
				// Build the column headers
				// Each column is a Map with a list containing the values for each row
				
				if("urn:ohmage:special:all".equals(columnList.get(0))) {
					outputColumns.addAll(_columnNames);
				} else {
					outputColumns.addAll(columnList);
				}
				
				if(columnList.contains("urn:ohmage:prompt:response") || "urn:ohmage:special:all".equals(columnList.get(0))) {
					// The logic here is that if the user is requesting results for survey ids, they want all of the prompts
					// for those survey ids
					// So, loop through the results and find all of the unique prompt ids by forcing them into a Set
					Set<String> promptIdSet = new HashSet<String>();
					
					if(0 != results.size()) {
						for(SurveyResponseReadResult result : results) {
							
							// _logger.info("urn:ohmage:prompt:id:" + result.getPromptId());
							promptIdSet.add("urn:ohmage:prompt:id:" + result.getPromptId());
						}
						outputColumns.addAll(promptIdSet);
					}
				}
				
				// get rid of urn:ohmage:prompt:response because it has been replaced with specific prompt ids
				// the list will be unchanged if it didn't already contain urn:ohmage:prompt:response 
				outputColumns.remove("urn:ohmage:prompt:response");
				
				
				if(! "json-rows".equals(req.getOutputFormat())) { 
				
					Map<String, List<Object>> columnMap = new HashMap<String, List<Object>> ();
					
					// initialize the map with a bunch of empty lists in order to avoid a null list check when retrieving the 
					// lists for each column below
					
					for(String columnName : outputColumns) {
						List<Object> list = new ArrayList<Object>();
						columnMap.put(columnName, list);
					}
					
					Set<String> columnMapKeySet = columnMap.keySet();
					
					// TODO convert all of the results to UTC first just in case they span timezones
					// this means there could be results that seem out of order if they actually do span timezones
					// and it means the sort should occur here instead of the SQL
					
					// Now flip/squash the database rows into columns
					// For each user-date-surveyId-repeatableSetId-repeatableSetIteration combination, the metadata will be the same
					// Assume that the results are ordered by user-date-surveyId-promptId (brittle dependency on SQL order by)
					
					if(0 != results.size()) {
						
						int totalNumberOfResults = results.size();
						String currentLoginId = results.get(0).getLoginId();
						String currentTimestamp = results.get(0).getTimestamp();
						String currentSurveyId = results.get(0).getSurveyId();
						String currentRepeatableSetId = results.get(0).getRepeatableSetId();
						Integer currentRepeatableSetIteration = results.get(0).getRepeatableSetIteration();
						SurveyResponseReadFormattedResult currentFormattedResult = new SurveyResponseReadFormattedResult();
						List<SurveyResponseReadFormattedResult> formattedResultList = new ArrayList<SurveyResponseReadFormattedResult>();
						Map<String, PromptContext> promptContextMap = new HashMap<String, PromptContext>();
						
						copyToFormattedResult(results.get(0), currentFormattedResult, true, promptContextMap);
						formattedResultList.add(currentFormattedResult);
						results.remove(0); // TODO will this break the loop below if there is only one result?
						
						for(SurveyResponseReadResult result : results) {
								
							if( ! currentLoginId.equals(result.getLoginId())
								|| ! currentTimestamp.equals(result.getTimestamp())
								|| ! currentSurveyId.equals(result.getSurveyId())
								|| ((null == currentRepeatableSetId && result.getRepeatableSetId() != null) 
									|| (currentRepeatableSetId != null && ! currentRepeatableSetId.equals(result.getRepeatableSetId())))
								|| ((null == currentRepeatableSetIteration && result.getRepeatableSetIteration() != null) 
									|| (currentRepeatableSetIteration != null && ! currentRepeatableSetIteration.equals(result.getRepeatableSetIteration())))) {
								
								currentFormattedResult = new SurveyResponseReadFormattedResult();
								copyToFormattedResult(result, currentFormattedResult, true, promptContextMap);
								formattedResultList.add(currentFormattedResult);
								currentLoginId = currentFormattedResult.getLoginId();
								currentSurveyId = currentFormattedResult.getSurveyId();
								currentTimestamp = currentFormattedResult.getTimestamp();
								currentRepeatableSetId = currentFormattedResult.getRepeatableSetId();
								currentRepeatableSetIteration = currentFormattedResult.getRepeatableSetIteration();
								
							} else {
								
								copyToFormattedResult(result, currentFormattedResult, false, promptContextMap);
							}
						}
						
						// Column-ify the data with only the columns that the user requested
						for(SurveyResponseReadFormattedResult result : formattedResultList) {
							for(String key : columnMapKeySet) {
								
								addItemToList(key, columnMap, result);
							}
						}
						
						responseText = _columnOutputBuilderMap.get(req.getOutputFormat()).createMultiResultOutput(totalNumberOfResults, req, promptContextMap, columnMap);
						
					} else { // no results
						
						responseText = _columnOutputBuilderMap.get(req.getOutputFormat()).createZeroResultOutput(req, columnMap);
					}
				
				} else { // the client wants row-based output
					
					responseText = _rowBasedOutputBuilder.buildOutput(req, results, outputColumns);
				}
				
			} else {
				
				// Even for CSV output, the error messages remain JSON
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
			}
			
			_logger.info("Generating survey response read output.");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("An unrecoverable exception occurred while generating a response", e);
			try {
				writer.write(generalJsonErrorMessage());
			} catch (Exception ee) {
				_logger.error("Caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			if(null != writer) {
				try {
					writer.flush();
					writer.close();
					writer = null;
				} catch (IOException ioe) {
					_logger.error("Caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}
	
	/**
	 * Adds a value from the formatted result to the appropriate column list based on the column name. 
	 */
	private void addItemToList(String columnName, 
			                   Map<String, List<Object>> columnMap,
			                   SurveyResponseReadFormattedResult result) throws JSONException { 
		
		if("urn:ohmage:user:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLoginId());
			
		} else if("urn:ohmage:context:client".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getClient());
			
		} else if("urn:ohmage:context:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimestamp());
			
		} else if("urn:ohmage:context:timezone".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimezone());
			
		} else if("urn:ohmage:context:utc_timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getUtcTimestamp());
			
		} else if("urn:ohmage:context:launch_context_long".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLaunchContext() == null ? null : new JSONObject(result.getLaunchContext()));
			
		}
		else if("urn:ohmage:context:launch_context_short".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLaunchContext() == null ? null : shortLaunchContext(result.getLaunchContext()));
			
		} else if("urn:ohmage:context:location:status".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationStatus());
			
		} else if("urn:ohmage:context:location:latitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLatitude());
			
		} else if("urn:ohmage:context:location:longitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLongitude());
			
		} else if("urn:ohmage:context:location:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationTimestamp());
			
		} else if("urn:ohmage:context:location:accuracy".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getAccuracy());
			
		} else if("urn:ohmage:context:location:provider".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getProvider());
		
		} else if("urn:ohmage:survey:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyId());
			
		} else if("urn:ohmage:survey:title".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyTitle());
		
		} else if("urn:ohmage:survey:description".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyDescription());
		
		} else if("urn:ohmage:survey:privacy_state".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getPrivacyState());
			
		} else if("urn:ohmage:repeatable_set:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getRepeatableSetId());
			
		} else if("urn:ohmage:repeatable_set:iteration".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getRepeatableSetIteration());
			
		} else if (columnName.startsWith("urn:ohmage:prompt:id:")) {
			
			String promptId = columnName.substring("urn:ohmage:prompt:id:".length());
			
			if(null != result.getPromptDisplayValueMap().get(promptId)) {
				columnMap.get(columnName).add(result.getPromptDisplayValueMap().get(promptId));
			} else {
				columnMap.get(columnName).add(null); 
			}
		} 
	}
	
	private String generateUtcTimestamp(SurveyResponseReadResult result) {
		return DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone());
	}	
	
	/**
	 * Squash a row of output into a meta-row containing each prompt that shares the same metadata.
	 *  
	 * @throws JSONException if the location object (retrieved from the db) is invalid JSON - bad!!
	 */
	private void copyToFormattedResult(SurveyResponseReadResult result,
			                           SurveyResponseReadFormattedResult formattedResult,
			                           boolean isNewRow,
			                           Map<String, PromptContext> promptContextMap) 
		throws JSONException {
		
		if(isNewRow) {
			formattedResult.setClient(result.getClient());
			formattedResult.setLaunchContext(result.getLaunchContext());
			formattedResult.setLocationStatus(result.getLocationStatus());
			
			if(! "unavailable".equals(result.getLocationStatus())) { // flatten the location object
				JSONObject locationObject = new JSONObject(result.getLocation());
				
				if(! Double.isNaN(locationObject.optDouble("accuracy"))) {
					formattedResult.setAccuracy(locationObject.optDouble("accuracy"));
				} else {
					formattedResult.setAccuracy(null);
				}
				
				if(! Double.isNaN(locationObject.optDouble("latitude"))) {
					formattedResult.setLatitude(locationObject.optDouble("latitude"));
				} else {
					formattedResult.setLatitude(null);
				}
				
				if(! Double.isNaN(locationObject.optDouble("longitude"))) {
					formattedResult.setLongitude(locationObject.optDouble("longitude"));
				} else {
					formattedResult.setLongitude(null);
				}
				
				if(! "".equals(locationObject.optString("provider")) ) {
					formattedResult.setProvider(locationObject.optString("provider"));
				} else {
					formattedResult.setProvider(null);
				}
				
				if(! "".equals(locationObject.optString("timestamp")) ) {
					formattedResult.setLocationTimestamp(locationObject.optString("timestamp"));
				} else {
					formattedResult.setLocationTimestamp(null);
				}
			}
			
			// _logger.info("new formatted result key: " + result.getLoginId() + ":" + result.getTimestamp() + ":" + result.getSurveyId() + ":" 
			//		+ result.getRepeatableSetId() + ":" + result.getRepeatableSetIteration());
			
			formattedResult.setLoginId(result.getLoginId());
			formattedResult.setRepeatableSetId(result.getRepeatableSetId());
			formattedResult.setRepeatableSetIteration(result.getRepeatableSetIteration());
			formattedResult.setSurveyId(result.getSurveyId());
			formattedResult.setSurveyTitle(result.getSurveyTitle());
			formattedResult.setSurveyDescription(result.getSurveyDescription());
			formattedResult.setTimestamp(result.getTimestamp());
			formattedResult.setTimezone(result.getTimezone());
			formattedResult.setUtcTimestamp(generateUtcTimestamp(result));
			formattedResult.setPrivacyState(result.getPrivacyState());
		} 
			
		Map<String, Object> promptDisplayValueMap = formattedResult.getPromptDisplayValueMap();
		if(null == promptDisplayValueMap) {
			promptDisplayValueMap = new HashMap<String, Object>();
			formattedResult.setPromptDisplayValueMap(promptDisplayValueMap);
		}
		
		promptDisplayValueMap.put(result.getPromptId(), result.getDisplayValue());
		
		// Create the context object - only need one for each prompt in the output
		
		if(null == promptContextMap.get(result.getPromptId())) {
			PromptContext pc = new PromptContext();
			pc.setDisplayLabel(result.getDisplayLabel());
			pc.setDisplayType(pc.getDisplayType());
			pc.setId(result.getPromptId());
			pc.setType(result.getPromptType());
			pc.setChoiceGlossary(result.getChoiceGlossary());
			pc.setText(result.getPromptText());
			promptContextMap.put(result.getPromptId(), pc);
		}
	}
	
	private JSONObject shortLaunchContext(String launchContext) throws JSONException {
		JSONObject lc = new JSONObject(launchContext);
		JSONObject shortLc = new JSONObject();
		
		String launchTime = JsonUtils.getStringFromJsonObject(lc, "launch_time");
		if(null != launchTime) {
			shortLc.put("launch_time", launchTime);
		}
		
		JSONArray activeTriggers = JsonUtils.getJsonArrayFromJsonObject(lc, "active_triggers");
		if(null != activeTriggers) {
			JSONArray shortArray = new JSONArray();
			for(int i = 0; i < activeTriggers.length(); i++) {
				JSONObject shortArrayEntry = new JSONObject();
				JSONObject longArrayEntry = JsonUtils.getJsonObjectFromJsonArray(activeTriggers, i);
				if(null != longArrayEntry) {
					String triggerType = JsonUtils.getStringFromJsonObject(longArrayEntry, "trigger_type");
					if(null != triggerType) {
						shortArrayEntry.put("trigger_type", triggerType);
					}
					JSONObject runtimeDescription = JsonUtils.getJsonObjectFromJsonObject(longArrayEntry, "runtime_description");
					if(null != runtimeDescription) {
						String triggerTime = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timestamp");
						if(null != triggerTime) {
							shortArrayEntry.put("trigger_timestamp", triggerTime);
						}
						String triggerTimezone = JsonUtils.getStringFromJsonObject(runtimeDescription, "trigger_timezone");
						if(null != triggerTimezone) {
							shortArrayEntry.put("trigger_timezone", triggerTimezone);
						}
					}
				}
				shortArray.put(shortArrayEntry);
			}
		}
		return shortLc;
	}
}
