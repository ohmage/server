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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.domain.SurveyResponseReadIndexedResult;
import org.ohmage.domain.SurveyResponseReadResult;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;


/** 
 * Builds survey response read output by dispatching to different output builders depending on the value of the output_format
 * provided in the request.
 * 
 * @author Joshua Selsky
 */
public class SurveyResponseReadResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadResponseWriter.class);
	private SurveyResponseReadCsvOutputBuilder _csvOutputBuilder;
	private SurveyResponseReadJsonColumnOutputBuilder _jsonColumnOutputBuilder;
	private SurveyResponseReadJsonRowBasedOutputBuilder _jsonRowBasedOutputBuilder;
	
	private List<String> _columnNames;
	
	public SurveyResponseReadResponseWriter(ErrorResponse errorResponse, 
			                               List<String> columnNames,
			                               SurveyResponseReadJsonRowBasedOutputBuilder rowBasedOutputBuilder,
			                               SurveyResponseReadJsonColumnOutputBuilder jsonColumnOutputBuilder,
			                               SurveyResponseReadCsvOutputBuilder csvOutputBuilder) {
		super(errorResponse);
		if(null == columnNames || columnNames.size() == 0) {
			throw new IllegalArgumentException("A non-null, non-empty columnNames list is required");
		}
		if(null == rowBasedOutputBuilder) {
			throw new IllegalArgumentException("A non-null SurveyResponseReadJsonRowBasedOutputBuilder is required");
		}
		if(null == jsonColumnOutputBuilder) {
			throw new IllegalArgumentException("A non-null SurveyResponseReadJsonColumnOutputBuilder is required");
		}
		if(null == csvOutputBuilder) {
			throw new IllegalArgumentException("A non-null SurveyResponseReadCsvColumnOutputBuilder is required");
		}
		
		_columnNames = columnNames;
		_jsonRowBasedOutputBuilder = rowBasedOutputBuilder;
		_jsonColumnOutputBuilder = jsonColumnOutputBuilder;
		_csvOutputBuilder = csvOutputBuilder;
	}
	
	/**
	 * Performs a sort on the query results in order to "roll up" interleaved prompt responses into their associated prompt
	 * response and then dispatches to the appropriate output builder to generate output. Finally, writes the output
	 * to the HttpServletResponse's output stream.
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
				List<SurveyResponseReadIndexedResult> indexedResults = new ArrayList<SurveyResponseReadIndexedResult>();
				
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
				
				
				// For every result found by the query, the prompt responses need to be rolled up so they are all stored
				// with their associated survey response and metadata. Each prompt response is returned from the db in its
				// own row and the rows can have different sort orders.
				
				boolean isCsv = "csv".equals(req.getOutputFormat());
				
				for(SurveyResponseReadResult result : results) {
					
					if(indexedResults.isEmpty()) { // first time thru
						indexedResults.add(new SurveyResponseReadIndexedResult(result, isCsv));
					}
					else {
						int numberOfIndexedResults = indexedResults.size();
						boolean found = false;
						for(int i = 0; i < numberOfIndexedResults; i++) {
							if(indexedResults.get(i).getKey().keysAreEqual(result.getUsername(),
									                                       result.getTimestamp(),
									                                       result.getSurveyId(),
									                                       result.getRepeatableSetId(),
									                                       result.getRepeatableSetIteration())) {
								
								found = true; //_logger.info("found");
								indexedResults.get(i).addPromptResponse(result, isCsv);
							}
						}
						if(! found) {
							indexedResults.add(new SurveyResponseReadIndexedResult(result, isCsv));
						}
					}
				}
				
				int numberOfSurveys = indexedResults.size();
				int numberOfPrompts = results.size();
				
				// Delete the original result list
				results.clear();
				results = null;
				
				if("json-rows".equals(req.getOutputFormat())) {
					responseText = _jsonRowBasedOutputBuilder.buildOutput(numberOfSurveys, numberOfPrompts, req, indexedResults, outputColumns);
				}
				else if("json-columns".equals(req.getOutputFormat())) {
					if(indexedResults.isEmpty()) {
						responseText = _jsonColumnOutputBuilder.createZeroResultOutput(req, outputColumns);
					} else {
						responseText = _jsonColumnOutputBuilder.createMultiResultOutput(numberOfSurveys, numberOfPrompts, req, indexedResults, outputColumns);
					}
				}
				else if("csv".equals(req.getOutputFormat())) {
					if(indexedResults.isEmpty()) {
						responseText = _csvOutputBuilder.createZeroResultOutput(req, outputColumns);
					} else {
						responseText = _csvOutputBuilder.createMultiResultOutput(numberOfSurveys, numberOfPrompts, req, indexedResults, outputColumns);
					}
				}
			} 
			else {
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
}