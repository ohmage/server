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
package org.ohmage.domain.configuration;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 * Wrapper for the items in a survey: survey title, survey description,
 * and all of the prompts and repeatable sets in a survey. 
 * 
 * @author Joshua Selsky
 */
public class Survey extends AbstractSurveyItem {
	private Map<String, SurveyItem> surveyItemMap; // prompts and repeatableSets
	private String title;
	private String description;
	
	public Survey(String surveyId, String title, String description, Map<String, SurveyItem> surveyMap) {
		super(surveyId);
		surveyItemMap = surveyMap; // TODO really need a deep copy here, but so far the creator of this Map does not change it
		this.title = title;
		this.description = description;
	}

	public Map<String, SurveyItem> getSurveyItemMap() {
		return Collections.unmodifiableMap(surveyItemMap);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getRepeatableSetIdForPromptId(String promptId) {
		Iterator<String> iterator = surveyItemMap.keySet().iterator();
		while(iterator.hasNext()) {
			SurveyItem si = surveyItemMap.get(iterator.next());
			if(si instanceof RepeatableSet) {
				RepeatableSet repeatableSet = (RepeatableSet) si;
				
				if(repeatableSet.getPromptMap().containsKey(promptId)) {
					return repeatableSet.getId();
				}
			}
		}
		
		return null;
	}

	@Override
	public String toString() {
		return "Survey [description=" + description + ", surveyItemMap="
				+ surveyItemMap + ", title=" + title + "]";
	}
}
