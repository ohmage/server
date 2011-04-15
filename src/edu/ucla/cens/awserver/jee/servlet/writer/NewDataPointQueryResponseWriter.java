package edu.ucla.cens.awserver.jee.servlet.writer;

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

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.domain.NewDataPointQueryFormattedResult;
import edu.ucla.cens.awserver.domain.NewDataPointQueryResult;
import edu.ucla.cens.awserver.domain.PromptContext;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.util.DateUtils;
import edu.ucla.cens.awserver.util.JsonUtils;

/** 
 * Giant writer for the new data point API. This class needs to be refactored so its constituent parts can be used in other 
 * output writers (e.g., csv).
 * 
 * TODO - the *ResponseWriter classes do more than just write output. The creation of the formatted output should be separated
 * out from any kind of I/O in order to allow refactoring to use different kinds of I/O e.g., streaming/comet.  
 * 
 * @author selsky
 */
public class NewDataPointQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryResponseWriter.class);
	private Map<String, NewDataPointQueryOutputBuilder> _outputBuilderMap;
	private List<String> _columnNames;
	
	public NewDataPointQueryResponseWriter(ErrorResponse errorResponse, 
			                               List<String> columnNames,
			                               Map<String, NewDataPointQueryOutputBuilder> outputBuilderMap) {
		super(errorResponse);
		if(null == columnNames || columnNames.size() == 0) {
			throw new IllegalArgumentException("a non-null, non-empty columnNames list is required");
		}
		if(null == outputBuilderMap || outputBuilderMap.isEmpty()) {
			throw new IllegalArgumentException("a non-null, non-empty output builder map is required");
		}
		_columnNames = columnNames;
		_outputBuilderMap = outputBuilderMap;
	}
	
	/**
	 * Creates column-based output based on the user's query selections.
	 */
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
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
				List<NewDataPointQueryResult> results = (List<NewDataPointQueryResult>) req.getResultList();
				
				// Build the column headers
				// Each column is a Map with a list containing the values for each row
				
				if("urn:awm:special:all".equals(columnList.get(0))) {
					outputColumns.addAll(_columnNames);
				} else {
					outputColumns.addAll(columnList);
				}
				
				if(columnList.contains("urn:awm:prompt:response") || "urn:awm:special:all".equals(columnList.get(0))) {
					// The logic here is that if the user is requesting results for survey ids, they want all of the prompts
					// for those survey ids
					// So, loop through the results and find all of the unique prompt ids by forcing them into a Set
					Set<String> promptIdSet = new HashSet<String>();
					
					if(0 != results.size()) {
						for(NewDataPointQueryResult result : results) {
							
							// _logger.info("urn:awm:prompt:id:" + result.getPromptId());
							promptIdSet.add("urn:awm:prompt:id:" + result.getPromptId());
						}
						outputColumns.addAll(promptIdSet);
					}
				}
				
				// get rid of urn:awm:prompt:response because it has been replaced with specific prompt ids
				// the list will be unchanged if it didn't already contain urn:awm:prompt:response 
				outputColumns.remove("urn:awm:prompt:response");
				
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
					NewDataPointQueryFormattedResult currentFormattedResult = new NewDataPointQueryFormattedResult();
					List<NewDataPointQueryFormattedResult> formattedResultList = new ArrayList<NewDataPointQueryFormattedResult>();
					Map<String, PromptContext> promptContextMap = new HashMap<String, PromptContext>();
					
					copyToFormattedResult(results.get(0), currentFormattedResult, true, promptContextMap);
					formattedResultList.add(currentFormattedResult);
					results.remove(0); // will this break the loop below if there is only one result?
					
					for(NewDataPointQueryResult result : results) {
							
						if( ! currentLoginId.equals(result.getLoginId())
							|| ! currentTimestamp.equals(result.getTimestamp())
							|| ! currentSurveyId.equals(result.getSurveyId())
							|| ((null == currentRepeatableSetId && result.getRepeatableSetId() != null) 
								|| (currentRepeatableSetId != null && ! currentRepeatableSetId.equals(result.getRepeatableSetId())))
							|| ((null == currentRepeatableSetIteration && result.getRepeatableSetIteration() != null) 
								|| (currentRepeatableSetIteration != null && ! currentRepeatableSetIteration.equals(result.getRepeatableSetIteration())))) {
							
							currentFormattedResult = new NewDataPointQueryFormattedResult();
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
					for(NewDataPointQueryFormattedResult result : formattedResultList) {
						for(String key : columnMapKeySet) {
							
							addItemToList(key, columnMap, result);
						}
					}
					
					responseText = _outputBuilderMap.get(req.getOutputFormat()).createMultiResultOutput(totalNumberOfResults, req, promptContextMap, columnMap);
					
				} else { // no results
					
					responseText = _outputBuilderMap.get(req.getOutputFormat()).createZeroResultOutput(req, columnMap);
				}
				
			} else {
				
				// Even for CSV output, the error messages remain JSON
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
			}
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("an unrecoverable exception occurred while generating a response", e);
			try {
				writer.write(generalJsonErrorMessage());
			} catch (Exception ee) {
				_logger.error("caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			if(null != writer) {
				try {
					writer.flush();
					writer.close();
					writer = null;
				} catch (IOException ioe) {
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}
	
	/**
	 * Adds a value from the formatted result to the appropriate column list based on the column name. 
	 */
	private void addItemToList(String columnName, 
			                   Map<String, List<Object>> columnMap,
			                   NewDataPointQueryFormattedResult result) throws JSONException { 
		
		if("urn:awm:user:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLoginId());
			
		} else if("urn:awm:context:client".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getClient());
			
		} else if("urn:awm:context:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimestamp());
			
		} else if("urn:awm:context:timezone".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimezone());
			
		} else if("urn:awm:context:utc_timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getUtcTimestamp() == null ? "NA" : result.getUtcTimestamp());
			
		} else if("urn:awm:context:launch_context_long".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLaunchContext() == null ? "NA" : new JSONObject(result.getLaunchContext()));
			
		}
		else if("urn:awm:context:launch_context_short".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLaunchContext() == null ? "NA" : shortLaunchContext(result.getLaunchContext()));
			
		} else if("urn:awm:context:location:status".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationStatus());
			
		} else if("urn:awm:context:location:latitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLatitude() == null ? "NA" : result.getLatitude());
			
		} else if("urn:awm:context:location:longitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLongitude() == null ? "NA" : result.getLongitude());
			
		} else if("urn:awm:context:location:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationTimestamp() == null ? "NA" : result.getLocationTimestamp());
			
		} else if("urn:awm:context:location:accuracy".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getAccuracy() == null ? "NA" : result.getAccuracy());
			
		} else if("urn:awm:context:location:provider".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getProvider() == null ? "NA" : result.getProvider());
		
		} else if("urn:awm:survey:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyId());
			
		} else if("urn:awm:survey:title".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyTitle());
		
		} else if("urn:awm:survey:description".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getSurveyDescription());
			
		} else if("urn:awm:repeatable_set:id".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getRepeatableSetId() == null ? "NA" : result.getRepeatableSetId());
			
		} else if("urn:awm:repeatable_set:iteration".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getRepeatableSetIteration() == null ? "NA" : result.getRepeatableSetIteration());
			
		} else if (columnName.startsWith("urn:awm:prompt:id:")) {
			
			String promptId = columnName.substring("urn:awm:prompt:id:".length());
			
			if(null != result.getPromptDisplayValueMap().get(promptId)) {
				columnMap.get(columnName).add(result.getPromptDisplayValueMap().get(promptId));
			} else {
				columnMap.get(columnName).add("NA"); 
			}
		}
	}
	
	private String generateUtcTimestamp(NewDataPointQueryResult result) {
		return DateUtils.timestampStringToUtc(result.getTimestamp(), result.getTimezone());
	}	
	
	/**
	 * Squash a row of output into a meta-row containing each prompt that shares the same metadata.
	 *  
	 * @throws JSONException if the location object (retrieved from the db) is invalid JSON - bad!!
	 */
	private void copyToFormattedResult(NewDataPointQueryResult result,
			                           NewDataPointQueryFormattedResult formattedResult,
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
