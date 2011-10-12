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
package org.ohmage.domain.survey.read;

import java.util.HashMap;
import java.util.Map;

import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.domain.configuration.PromptProperty;

/**
 * Allow survey response read results to be indexed by their key. This class is needed in order to bucketize interleaved results.
 * When running the query for survey responses, prompt responses for the same survey result may be interleaved depending on the
 * sort order (ORDER BY) that was used (the API allows an optional sort_order parameter). In order to properly serialize the 
 * results, the prompt responses for each survey response need to be "rolled up" into their correct IndexedResult.  
 *  
 * @author Joshua Selsky
 */
public class SurveyResponseReadIndexedResult {
	// Key
	private SurveyResponseReadIndexedResultKey _key;
	
	// Prompt responses and prompt metadata
	private Map<String, Object> _promptResponseMap;
	private Map<String, PromptResponseMetadata> _promptResponseMetadataMap;
	private Map<String, Map<String, PromptProperty>> _choiceGlossaryMap;
	
	// Keep the original result around for metadata/result context
	private SurveyResponseReadResult _originalResult;
	
	public SurveyResponseReadIndexedResult(SurveyResponseReadResult result, boolean isCsv) {
		
		_key = new SurveyResponseReadIndexedResultKey(result.getUsername(), result.getTimestamp(), result.getEpochMillis(), 
			result.getSurveyId(), result.getRepeatableSetId(), result.getRepeatableSetIteration());	
		
		_originalResult = result;
		
		PromptResponseMetadata promptResponseMetadata 
			= new PromptResponseMetadata(result.getDisplayLabel(),
					                     result.getDisplayType(),
					                     result.getPromptText(),
					                     result.getPromptType(),
					                     result.getUnit());
		
		_promptResponseMetadataMap = new HashMap<String, PromptResponseMetadata>();
		_promptResponseMetadataMap.put(result.getPromptId(), promptResponseMetadata);
		_promptResponseMap = new HashMap<String, Object>();
		
		if("single_choice".equals(result.getPromptType()) && isCsv) {
			_promptResponseMap.put(result.getPromptId(), new SingleChoicePromptValueAndLabel(result.getSingleChoiceOrdinalValue(), result.getSingleChoiceLabel()));
		}
		else {
			_promptResponseMap.put(result.getPromptId(), result.getDisplayValue());
		}
		
		_choiceGlossaryMap = new HashMap<String, Map<String, PromptProperty>>();
		
		if(null != result.getChoiceGlossary()) {
			_choiceGlossaryMap.put(result.getPromptId(), result.getChoiceGlossary());
		}
	}
	
	public void addPromptResponse(SurveyResponseReadResult result, boolean isCsv) {
		
		if("single_choice".equals(result.getPromptType()) && isCsv) {
			_promptResponseMap.put(result.getPromptId(), new SingleChoicePromptValueAndLabel(result.getSingleChoiceOrdinalValue(), result.getSingleChoiceLabel()));
		}
		else {
			_promptResponseMap.put(result.getPromptId(), result.getDisplayValue());
		}
		
		PromptResponseMetadata promptResponseMetadata 
			= new PromptResponseMetadata(result.getDisplayLabel(),
										 result.getDisplayType(),
				                         result.getPromptText(),
				                         result.getPromptType(),
				                         result.getUnit());
	    
		_promptResponseMetadataMap.put(result.getPromptId(), promptResponseMetadata);
		
		if(null != result.getChoiceGlossary()) {
			_choiceGlossaryMap.put(result.getPromptId(), result.getChoiceGlossary());
		}
	}
	
	
	public Map<String, Map<String, PromptProperty>> getChoiceGlossaryMap() {
		return _choiceGlossaryMap;
	}
	
	public String getClient() {
		return _originalResult.getClient();
	}
	
	public SurveyResponseReadIndexedResultKey getKey() {
		return _key;
	}
	
	public String getLaunchContext() {
		return _originalResult.getLaunchContext();
	}

	public String getLocation() {
		return _originalResult.getLocation();
	}

	public String getLocationStatus() {
		return _originalResult.getLocationStatus();
	}
	
	public SurveyResponsePrivacyStateCache.PrivacyState getPrivacyState() {
		return _originalResult.getPrivacyState();
	}
	
	public Map<String, Object> getPromptResponseMap() {
		return _promptResponseMap;
	}
	
	public Map<String, PromptResponseMetadata> getPromptResponseMetadataMap() {
		return _promptResponseMetadataMap;
	}
	
	public String getRepeatableSetId() {
		return _key.getRepeatableSetId();
	}
	
	public Integer getRepeatableSetIteration() {
		return _key.getRepeatableSetIteration();
	}
	
	public Object getSingleChoiceOrdinalValue() {
		return _originalResult.getSingleChoiceOrdinalValue();
	}
	
	public Object getSingleChoiceLabel() {
		return _originalResult.getSingleChoiceLabel();
	}
	
	public String getSurveyId() {
		return _originalResult.getSurveyId();
	}
	
	public int getSurveyPrimaryKeyId() {
		return _originalResult.getSurveyPrimaryKeyId();
	}

	public String getSurveyDescription() {
		return _originalResult.getSurveyDescription();
	}

	public String getSurveyTitle() {
		return _originalResult.getSurveyTitle();
	}

	public String getTimestamp() {
		return _originalResult.getTimestamp();
	}
	
	public String getTimezone() {
		return _originalResult.getTimezone();
	}

	public String getUsername() {
		return _originalResult.getUsername();
	}
	
	public String getUtcTimestamp() {
		return _originalResult.getUtcTimestamp();
	}

	@Override
	public String toString() {
		return "SurveyResponseReadIndexedResult [_key=" + _key
				+ ", _promptResponseMap=" + _promptResponseMap
				+ ", _promptResponseMetadataMap=" + _promptResponseMetadataMap
				+ ", _choiceGlossaryMap=" + _choiceGlossaryMap
				+ ", _originalResult=" + _originalResult + "]";
	}
}
