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
package org.ohmage.domain.campaign;

import java.util.Collections;
import java.util.Map;

import org.ohmage.util.StringUtils;

/**
 * Wrapper for a survey object.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class Survey {
	/**
	 * The surveys unique identifier.
	 */
	private final String id;
	/**
	 * The title of the survey.
	 */
	private final String title;
	/**
	 * A description of this survey.
	 */
	private final String description;
	
	/**
	 * The text to be displayed to the user when the survey begins.
	 */
	private final String introText;
	/**
	 * The text to be displayed to the user just before they submit the survey.
	 */
	private final String submitText;
	
	/**
	 * Whether or not to show the summary text.
	 */
	private final boolean showSummary;
	/**
	 * Whether or not the user is allowed to edit the summary.
	 */
	private final Boolean editSummary;
	/**
	 * The text that is displayed to the user upon completing the survey.
	 */
	private final String summaryText;
	
	/**
	 * Whether or not this survey may be taken at any time or only when a 
	 * trigger has made it available.
	 */
	private final boolean anytime;
	
	/**
	 * The map of survey item IDs its actual SurveyItem object.
	 */
	private final Map<Integer, SurveyItem> surveyItems;
	
	/**
	 * Creates a new survey.
	 * 
	 * @param id The survey's unique identifier.
	 * 
	 * @param title The title of the survey.
	 * 
	 * @param description The description of the survey.
	 * 
	 * @param introText The text to be displayed to the user when they begin 
	 * 					the survey.
	 * 
	 * @param submitText The text to be displayed to the user just before they
	 * 					 submit the survey.
	 * 
	 * @param showSummary Whether or not to show the summary.
	 * 
	 * @param editSummary Whether or not the user is allowed to edit the 
	 * 					  summary.
	 * 
	 * @param summaryText The survey's summary text.
	 * 
	 * @param anytime Whether the user is allowed to take this survey at any
	 * 				  time or if they may only take it when a trigger has made
	 * 				  it available.
	 * 
	 * @param surveyItems A map of the survey item's unique identifier to their
	 * 					  actual SurveyItem object.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the values are null or
	 * 									obviously invalid such as a string 
	 * 									being only whitespace or the map of
	 * 									survey items being empty. Also, thrown
	 * 									if 'showSummary' is true, but 
	 * 									'editSummary' is null and/or 
	 * 									'summaryText' is null or whitespace 
	 * 									only.
	 */
	public Survey(final String id, final String title, final String description,
			final String introText, final String submitText,
			final boolean showSummary, final Boolean editSummary, final String summaryText,
			final boolean anytime, final Map<Integer, SurveyItem> surveyItems) {
		
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
	 * Returns the unique identifier for this survey.
	 * 
	 * @return This survey's unique identifier.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the title for this survey.
	 * 
	 * @return This survey's title.
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Returns the description for this survey.
	 * 
	 * @return This survey's description.
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Returns the intro text for this survey.
	 * 
	 * @return This survey's intro text.
	 */
	public String getIntroText() {
		return introText;
	}
	
	/**
	 * Returns the submit text for this survey.
	 * 
	 * @return This survey's submit text.
	 */
	public String getSubmitText() {
		return submitText;
	}
	
	/**
	 * Returns whether or not to show the summary for this survey.
	 * 
	 * @return Whether or not to show this survey's summary.
	 */
	public boolean showSummary() {
		return showSummary;
	}
	
	/**
	 * Returns whether or not to allow the user to edit the summary for this
	 * survey.
	 * 
	 * @return Whether or not to allow the user to edit this survey's summary
	 * 		   or null if the 'showSummary' is set to false.
	 * 
	 * @see #showSummary()
	 */
	public Boolean editSummary() {
		return editSummary;
	}
	
	/**
	 * Returns the summary text for this survey.
	 * 
	 * @return This survey's summary text or null if 'showSummary' is false.
	 * 
	 * @see #showSummary()
	 */
	public String summaryText() {
		return summaryText;
	}
	
	/**
	 * Returns whether or not this survey may be taken at any time or if it may
	 * only be taken when a trigger has enabled it.
	 * 
	 * @return Whether or not this survey may be taken at any time or only when
	 * 		   a trigger has enabled it.
	 */
	public boolean anytime() {
		return anytime;
	}
	
	/**
	 * Returns the number of survey items contained within this survey.
	 * 
	 * @return The number of survey items contained within this survey.
	 */
	public int getNumSurveyItems() {
		int total = 0;
		
		for(SurveyItem surveyItem : surveyItems.values()) {
			total += surveyItem.getNumSurveyItems();
		}
		
		return total;
	}
	
	/**
	 * Returns the number of prompts contained within this survey.
	 * 
	 * @return The number of prompts contained within this survey.
	 */
	public int getNumPrompts() {
		int total = 0;
		
		for(SurveyItem prompt : surveyItems.values()) {
			total += prompt.getNumPrompts();
		}
		
		return total;
	}

	/**
	 * Returns an unmodifiable map of all of the survey items.
	 * 
	 * @return An unmodifiable map of all of the survey items.
	 */
	public Map<Integer, SurveyItem> getSurveyItems() {
		return Collections.unmodifiableMap(surveyItems);
	}
	
	/**
	 * Returns a survey item from the list of survey items based on the unique
	 * identifier.
	 * 
	 * @param surveyItemId The survey item's unique identifier.
	 * 
	 * @return The SurveyItem object representing the desired survey item.
	 * 
	 * @throws IllegalArgumentException Thrown if the survey item ID is null.
	 */
	public SurveyItem getSurveyItem(final String surveyItemId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyItemId)) {
			throw new IllegalArgumentException("The survey item ID is null.");
		}
		
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
	
	/**
	 * Returns a survey item from the list of survey items based on its index
	 * in the list.
	 * 
	 * @param index The survey item's index in the list of survey items.
	 * 
	 * @return Returns the survey item at the given index. 
	 */
	public SurveyItem getSurveyItem(final int index) {
		for(SurveyItem surveyItem : surveyItems.values()) {
			if(surveyItem.getIndex() == index) {
				return surveyItem;
			}
		}
		
		return null;
	}

	/**
	 * Generates a hash code for this survey.
	 * 
	 * @return A hash code for this survey.
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

	/**
	 * Determines if this survey logically equals another object.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if this survey logically equals the other object; false,
	 * 		   otherwise.
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