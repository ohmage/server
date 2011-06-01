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
package org.ohmage.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.ohmage.dao.Dao;
import org.ohmage.domain.Configuration;
import org.ohmage.domain.DataPointQueryResult;
import org.ohmage.domain.PromptTypeUtils;
import org.ohmage.request.AwRequest;
import org.ohmage.request.DataPointQueryAwRequest;
import org.ohmage.util.JsonUtils;


/**
 * @author selsky
 */
public class DataPointQueryService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(DataPointQueryService.class);
	
	public DataPointQueryService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Retrieves data points and creates a result list of DataPointQueryResults. The data points are responses for promptIds from a 
	 * campaign's XML configuration. In addition to looking up the prompt responses for each promptId, the metadata responses
	 * (displayType == metadata) from the same survey as each prompt response are also retrieved.   
	 */
	@Override
	public void execute(AwRequest awRequest) {
		DataPointQueryAwRequest req = (DataPointQueryAwRequest) awRequest;
		
		// 1. Find the metadata data point ids: these are ids with a display type of metadata from the same survey as the prompt 
		// ids that are being queried against. The metadata values are added to the results list as distinct query results.
		Configuration config = req.getConfiguration();
		List<String> metadataPromptIds = new ArrayList<String>();
		String[] dataPointIds = req.getDataPointIds();
		
		for(String dataPointId : dataPointIds) {
			List<String> list = config.getMetadataPromptIds(dataPointId); 
			if(! metadataPromptIds.containsAll(list)) {
				metadataPromptIds.addAll(list);
			}
		}
		
		req.setMetadataPromptIds(metadataPromptIds);	
		
		// 2. Pass all ids to the data access object and run the query
		getDao().execute(req);
		
		// 3. Post-process
		// Label the data points that are metadata
		// Set the displayLabel and the unit from the survey config if either are available
		List<?> results = req.getResultList();
		int numberOfResults = results.size();
		
		for(int i = 0; i < numberOfResults; i++) {
			DataPointQueryResult result = (DataPointQueryResult) results.get(i);
			
			if(metadataPromptIds.contains(result.getPromptId())) {
				result.setIsMetadata(true);
			}
			
			if(result.isRepeatableSetResult()) {
				
				result.setUnit(config.getUnitFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId()));
				result.setDisplayLabel(
					config.getDisplayLabelFor(result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
				);
				result.setDisplayType(config.getDisplayTypeFor(
					result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId())
				);
				
				if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
					
					setDisplayValueFromSingleChoice(result, config, true);	
					
				} else if(PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
					
					setDisplayValueFromMultiChoice(result, config, true);
					
				} else { 
						
					result.setDisplayValue(result.getResponse());
				}
				
			} else {
				
				result.setUnit(config.getUnitFor(result.getSurveyId(), result.getPromptId()));
				result.setDisplayLabel(config.getDisplayLabelFor(result.getSurveyId(), result.getPromptId()));
				result.setDisplayType(config.getDisplayTypeFor(result.getSurveyId(), result.getPromptId()));
				
				if(PromptTypeUtils.isSingleChoiceType(result.getPromptType())) {
				
					setDisplayValueFromSingleChoice(result, config, false);
				
				} else if (PromptTypeUtils.isMultiChoiceType(result.getPromptType())) {
					
					setDisplayValueFromMultiChoice(result, config, false);
										
				} else {
					
					result.setDisplayValue(result.getResponse());
				}
			}
		}
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(req.getResultList());
		}
	}
	
	private void setDisplayValueFromSingleChoice(DataPointQueryResult result, Configuration config, boolean isRepeatableSetItem) {
		String value = null;
		
		if(isRepeatableSetItem) {
			value = config.getValueForChoiceKey(
				result.getSurveyId(), result.getRepeatableSetId(), result.getPromptId(), String.valueOf(result.getResponse())
			);
		} else {
			value = config.getValueForChoiceKey(
				result.getSurveyId(), result.getPromptId(), String.valueOf(result.getResponse())
			);
		}
		
		if(null != value) {
			result.setDisplayValue(value);
		} else {
			result.setDisplayValue(result.getResponse());
		}
	}
	
	private void setDisplayValueFromMultiChoice(DataPointQueryResult result, Configuration config, boolean isRepeatableSetItem) {
	
		JSONArray responseArray = JsonUtils.getJsonArrayFromString(String.valueOf(result.getResponse()));
		
		if(null != responseArray) {
			JSONArray valueArray = new JSONArray();
			int length = responseArray.length();
			
			for(int j = 0; j < length; j++) {
				
				Object value = null;
				
				if(isRepeatableSetItem) {
					value = config.getValueForChoiceKey(result.getSurveyId(), result.getRepeatableSetId(), 
						result.getPromptId(), JsonUtils.getStringFromJsonArray(responseArray, j)
					);
				} else {
					value = config.getValueForChoiceKey(
						result.getSurveyId(), result.getPromptId(), JsonUtils.getStringFromJsonArray(responseArray, j)
					);
				}
				
				if(null == value) {
					break;
				} else {
					valueArray.put(value);
				}
			}
			
			if(valueArray.length() == length) {
				result.setDisplayValue(valueArray);
			} else {				
				result.setDisplayValue(result.getResponse());
			}
		
		} else {
			result.setDisplayValue(result.getResponse());
		}
	}
}
