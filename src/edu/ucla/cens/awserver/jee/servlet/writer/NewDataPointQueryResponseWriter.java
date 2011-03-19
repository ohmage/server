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
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.domain.NewDataPointQueryFormattedResult;
import edu.ucla.cens.awserver.domain.NewDataPointQueryResult;
import edu.ucla.cens.awserver.domain.PromptOutput;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.util.DateUtils;

/** 
 * Giant writer for the new data point API. This class needs to be refactored so its constituent parts can be used in other 
 * output writers (e.g., csv).
 * 
 * @author selsky
 */
public class NewDataPointQueryResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryResponseWriter.class);
	private List<String> _columnNames;
	
	public NewDataPointQueryResponseWriter(ErrorResponse errorResponse, List<String> columnNames) {
		super(errorResponse);
		if(null == columnNames || columnNames.size() == 0) {
			throw new IllegalArgumentException("a non-empty columnNames list is required");
		}
		_columnNames = columnNames;
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				// 1. Find the column headers
				// a. all columns?
				// 2. Results are already sorted by user, date, survey id, prompt id
				// 3. Create map of JSONArrays with the column name as keys (urn - result getter mapping)
				// 4. Loop over results:
				// 5. Loop over columns: 
				// For each column, add the appropriate value to the collection from the appropriate map. Missing columns get "NA".
				
				NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
				
				List<String> columnList = req.getColumnList();
				List<String> outputColumns = new ArrayList<String>();
				List<NewDataPointQueryResult> results = (List<NewDataPointQueryResult>) req.getResultList();
				
				// TODO? Need to make sure that user, date, survey id, prompt id, and prompt response are
				// always included?
				
				// TODO
				// Build metadata section
				// number of results, column headers
				
				
				
				// Build the column headers
				// Each column is a Map with a list containing the values for each row
				
				if("urn:sys:special:all".equals(columnList.get(0))) {
					outputColumns.addAll(_columnNames);
				} else {
					outputColumns.addAll(columnList);
				}
				
				if(null != req.getSurveyIdList() || "urn:sys:special:all".equals(req.getPromptIdList().get(0))) {
					// The logic here is that if the user is requesting results for survey ids, they want all of the prompts
					// for those survey ids
					// So, loop through the results and find all of the unique prompt ids by forcing them into a Set
					Set<String> promptIdSet = new HashSet<String>();
					
					if(0 != results.size()) {
						for(NewDataPointQueryResult result : results) {
							promptIdSet.add("urn:sys:upload:data:prompt:id:" + result.getPromptId());
						}
						outputColumns.addAll(promptIdSet);
					}
					
				} else {
					
					outputColumns.addAll(req.getPromptIdList());
				}
				
				Map<String, List<Object>> columnMap = new HashMap<String, List<Object>> ();
				
				// initialize the map with a bunch of empty lists in order to avoid a null list check when retrieving the 
				// lists for each column below
				
				for(String columnName : outputColumns) {
					List<Object> list = new ArrayList<Object>();
					columnMap.put(columnName, list);
				}
				
				// TODO Convert all of the results to UTC first just in case they span timezones ?
				
				
				// Now flip/squash the database rows into columns
				// For each user-date-surveyId combination, the metadata will be the same
				// Assume that the results are ordered by user-date-surveyId-promptId (brittle dependency on SQL order by)
				
				if(0 != results.size()) {
					
					String currentLoginId = results.get(0).getLoginId();
					String currentTimestamp = results.get(0).getTimestamp();
					String currentSurveyId = results.get(0).getSurveyId();
					String currentRepeatableSetId = results.get(0).getRepeatableSetId();
					Integer currentRepeatableSetIteration = results.get(0).getRepeatableSetIteration();
					NewDataPointQueryFormattedResult currentFormattedResult = null;
					List<NewDataPointQueryFormattedResult> formattedResultList = new ArrayList<NewDataPointQueryFormattedResult>();
					
					copyToFormattedResult(results.get(0), currentFormattedResult, true);
					
					for(NewDataPointQueryResult result : results) {
							
						if( ! currentLoginId.equals(result.getLoginId())
							|| ! currentTimestamp.equals(result.getTimestamp())
							|| ! currentSurveyId.equals(result.getSurveyId())
							|| ! currentRepeatableSetId.equals(result.getRepeatableSetId())
							|| ! currentRepeatableSetIteration.equals(result.getRepeatableSetIteration())) {
						
							currentFormattedResult = new NewDataPointQueryFormattedResult();
							formattedResultList.add(currentFormattedResult);
							copyToFormattedResult(result, currentFormattedResult, true);
							
						} else {
							
							copyToFormattedResult(result, currentFormattedResult, false);
						}
					}
					
					Set<String> keySet = columnMap.keySet();
					
					// Column-ify the data
					for(NewDataPointQueryFormattedResult result : formattedResultList) {
						for(String key : keySet) {
							
							addItemToList(key, columnMap, result, req);
						}
					}
					
					// Build output
					for(String key : keySet) {
						int listSize = columnMap.get(key).size();
						for(int i = 0; i < listSize; i++) {
							
							
						}
					}
					
				} else { // no results
					
					
				}
				
			} else {
				
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
	
	private void addItemToList(String columnName, 
			                   Map<String, List<Object>> columnMap,
			                   NewDataPointQueryFormattedResult result,
			                   NewDataPointQueryAwRequest req) {
		
		if("urn:sys:upload:metadata:user".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLoginId());
			
		} else if("urn:sys:upload:metadata:client".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getClient());
			
		} else if("urn:sys:upload:metadata:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimestamp());
			
		} else if("urn:sys:upload:metadata:timezone".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getTimezone());
			
		} else if("urn:sys:upload:metadata:utc_timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getUtcTimestamp());
			
		} else if("urn:sys:upload:metadata:survey_launch_context".equals(columnName)) { // TODO need to flatten the launch context
			
			columnMap.get(columnName).add(result.getLaunchContext());
			
		} else if("urn:sys:upload:metadata:location:status".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationStatus());
			
		} else if("urn:sys:upload:metadata:location:latitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLatitude());
			
		} else if("urn:sys:upload:metadata:location:longitude".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLongitude());
			
		} else if("urn:sys:upload:metadata:location:timestamp".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getLocationTimestamp());
			
		} else if("urn:sys:upload:metadata:location:accuracy".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getAccuracy());
			
		} else if("urn:sys:upload:metadata:location:provider".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getProvider());
			
		} else if("urn:sys:upload:metadata:campaign:name".equals(columnName)) {
			
			columnMap.get(columnName).add(req.getCampaignName());			
			
		} else if("urn:sys:upload:metadata:campaign:version".equals(columnName)) {
			
			columnMap.get(columnName).add(req.getCampaignVersion());
			
		} else if("urn:sys:upload:metadata:repeatable_set:iteration".equals(columnName)) {
			
			columnMap.get(columnName).add(result.getRepeatableSetIteration());
			
		} else if ("urn:sys:upload:data:prompt:id:".startsWith(columnName)) {
			
			String promptId = columnName.substring("urn:sys:upload:data:prompt:id:".length() - 1);
			
			columnMap.get(columnName).add(result.getPromptOutputMap().get(promptId)); // TODO for null values, convert to "NA"?
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
	private void copyToFormattedResult(NewDataPointQueryResult result, NewDataPointQueryFormattedResult formattedResult, boolean isNewRow) 
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
					formattedResult.setTimestamp(locationObject.optString("timestamp"));
				} else {
					formattedResult.setLocationTimestamp(null);
				}
			}
			
			formattedResult.setLoginId(result.getLoginId());
			formattedResult.setRepeatableSetId(result.getRepeatableSetId());
			formattedResult.setRepeatableSetIteration(result.getRepeatableSetIteration());
			formattedResult.setSurveyId(result.getSurveyId());
			formattedResult.setTimestamp(result.getTimestamp());
			formattedResult.setTimezone(result.getTimezone());
			formattedResult.setUtcTimestamp(generateUtcTimestamp(result));
		
		} else { // a row with unchanged metadata, but a prompt response to add
			
			Map<String, PromptOutput> promptOutputMap = formattedResult.getPromptOutputMap();
			if(null == promptOutputMap) {
				promptOutputMap = new HashMap<String, PromptOutput>();
				formattedResult.setPromptOutputMap(promptOutputMap);
			}
			
			PromptOutput po = new PromptOutput();
			po.setDisplayLabel(result.getDisplayLabel());
			po.setDisplayType(po.getDisplayType());
			po.setDisplayValue(result.getDisplayValue());
			po.setId(result.getPromptId());
			po.setType(result.getPromptType());
			promptOutputMap.put(result.getPromptId(), po);
		}
	}
}
