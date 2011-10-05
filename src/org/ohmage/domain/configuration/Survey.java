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
import java.util.Map;

import org.ohmage.util.StringUtils;

/**
 * Wrapper for the items in a survey.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class Survey {
	/**
	 */
	private final String id;
	/**
	 */
	private final String title;
	/**
	 */
	private final String description;
	
	/**
	 */
	private final String introText;
	/**
	 */
	private final String submitText;
	
	/**
	 */
	private final boolean showSummary;
	/**
	 */
	private final Boolean editSummary;
	/**
	 */
	private final String summaryText;
	
	/**
	 */
	private final boolean anytime;
	
	/**
	 */
	private final Map<String, SurveyItem> surveyItems;
	
	public Survey(final String id, final String title, final String description,
			final String introText, final String submitText,
			final boolean showSummary, final Boolean editSummary, final String summaryText,
			final boolean anytime, final Map<String, SurveyItem> surveyItems) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			throw new IllegalArgumentException("The ID cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(title)) {
			throw new IllegalArgumentException("The title cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(submitText)) {
			throw new IllegalArgumentException("The submit text cannot be null.");
		}
		if(showSummary && (editSummary == null)) {
			throw new IllegalArgumentException("Edit summary cannot be null if show summary is true.");
		}
		if(showSummary && StringUtils.isEmptyOrWhitespaceOnly(summaryText)) {
			throw new IllegalArgumentException("The summary text cannot be null if show summary is true.");
		}
		if((surveyItems == null) || (surveyItems.size() == 0)) {
			throw new IllegalArgumentException("The surveyItems list cannot be null or empty.");
		}
		
		this.id = id;
		this.title = title;
		this.description = description;
		this.introText = introText;
		this.submitText = submitText;
		this.showSummary = showSummary;
		this.editSummary = editSummary;
		this.summaryText = summaryText;
		this.anytime = anytime;
		// FIXME: Deep copy.
		this.surveyItems = surveyItems;
	}
	
	/**
	 * @return
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @return
	 */
	public String getIntroText() {
		return introText;
	}
	
	/**
	 * @return
	 */
	public String getSubmitText() {
		return submitText;
	}
	
	public boolean showSummary() {
		return showSummary;
	}
	
	public Boolean editSummary() {
		return editSummary;
	}
	
	public String summaryText() {
		return summaryText;
	}
	
	public boolean anytime() {
		return anytime;
	}
	
	public int getNumSurveyItems() {
		return surveyItems.size();
	}
	
	public int getNumPrompts() {
		int total = 0;
		
		for(SurveyItem prompt : surveyItems.values()) {
			total += prompt.getNumPrompts();
		}
		
		return total;
	}

	public Map<String, SurveyItem> getSurveyItems() {
		return Collections.unmodifiableMap(surveyItems);
	}
	
	public SurveyItem getSurveyItem(final String surveyItemId) {
		for(SurveyItem prompt : surveyItems.values()) {
			if(prompt instanceof Prompt) {
				if(((Prompt) prompt).getId().equals(surveyItemId)) {
					return prompt;
				}
			}
			else if(prompt instanceof RepeatableSet) {
				if(((RepeatableSet) prompt).getPrompt(surveyItemId) != null) {
					return prompt;
				}
			}
		}
		
		return null;
	}
	
	public SurveyItem getSurveyItem(final int index) {
		for(SurveyItem surveyItem : surveyItems.values()) {
			if(surveyItem.getIndex() == index) {
				return surveyItem;
			}
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((surveyItems == null) ? 0 : surveyItems.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Survey other = (Survey) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (surveyItems == null) {
			if (other.surveyItems != null)
				return false;
		} else if (!surveyItems.equals(other.surveyItems))
			return false;
		return true;
	}
}