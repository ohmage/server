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
package org.ohmage.domain.campaign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * Maps a repeatable set id to the Prompts contained with in the set.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class RepeatableSet extends SurveyItem {
	private static final String JSON_KEY_TERMINATION_QUESTION = "termination_question";
	private static final String JSON_KEY_TERMINATION_TRUE_LABEL = "termination_true_label";
	private static final String JSON_KEY_TERMINATION_FALSE_LABEL = "termination_false_label";
	private static final String JSON_KEY_TERMINATION_SKIP_ENABLED = "termination_skip_enabled";
	private static final String JSON_KEY_TERMINATION_SKIP_LABEL = "termination_skip_label";
	private static final String JSON_KEY_PROMPTS = "surveyItems";
	
	private final String terminationQuestion;
	private final String terminationTrueLabel;
	private final String terminationFalseLabel;
	
	private final boolean terminationSkipEnabled;
	private final String terminationSkipLabel;
	
	private final Map<Integer, SurveyItem> surveyItems;
	
	/**
	 * Creates a new repeatable set.
	 * 
	 * @param id The repeatable set's unique identifier.
	 * 
	 * @param condition The condition string which is used to determine if this
	 * 					repeatable set should be displayed or not.
	 * 
	 * @param terminationQuestion The text to ask the user if the repeatable
	 * 							  set should be run again.
	 * 
	 * @param terminationTrueLabel The text to display to the the user that
	 * 							   suggests that the repeatable set should be
	 * 							   run again.
	 * 
	 * @param terminationFalseLabel The text to display to the user that
	 * 								suggests that the repeatable set should not
	 * 								be run again.
	 * 
	 * @param terminationSkipEnabled Whether the user is allowed to skip the
	 * 								 entire repeatable set.
	 * 
	 * @param terminationSkipLabel The label to display to the user suggesting
	 * 							   that they may skip the entire repeatable 
	 * 							   set.
	 * 
	 * @param surveyItems A map of the survey item's index to the actual survey
	 * 				  item.
	 * 
	 * @param index The repeatable set's index in its parent group of survey
	 * 				items.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the parameters are
	 * 									invalid.
	 */
	public RepeatableSet(
			final String id, 
			final String condition,
			final String terminationQuestion, 
			final String terminationTrueLabel, 
			final String terminationFalseLabel,
			final boolean terminationSkipEnabled, 
			final String terminationSkipLabel,
			final Map<Integer, SurveyItem> surveyItems,
			final int index) 
			throws DomainException {
		
		super(id, condition, index);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationQuestion)) {
			throw new DomainException("The termination question cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationTrueLabel)) {
			throw new DomainException("The termination true label cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationFalseLabel)) {
			throw new DomainException("The termination false label cannot be null.");
		}
		if(terminationSkipEnabled && StringUtils.isEmptyOrWhitespaceOnly(terminationSkipLabel)) {
			throw new DomainException("The termination skip is enabled but the label is null.");
		}
		if((surveyItems == null) || (surveyItems.size() == 0)) {
			throw new DomainException("The prompt list cannot be null or empty.");
		}
		
		this.terminationQuestion = terminationQuestion;
		this.terminationTrueLabel = terminationTrueLabel;
		this.terminationFalseLabel = terminationFalseLabel;
		
		this.terminationSkipEnabled = terminationSkipEnabled;
		this.terminationSkipLabel = terminationSkipLabel;
		
		this.surveyItems = new HashMap<Integer, SurveyItem>(surveyItems.size());
		for(SurveyItem surveyItem : surveyItems.values()) {
			surveyItem.setParent(this);
			this.surveyItems.put(surveyItem.getIndex(), surveyItem);
		}
	}

	/**
	 * Returns the termination question.
	 * 
	 * @return The termination question.
	 */
	public final String getTerminationQuestion() {
		return terminationQuestion;
	}

	/**
	 * Returns the text to be displayed to the user indicating that they want
	 * to take the repeatable set again.
	 * 
	 * @return The text to be displayed to the user indicating that they want
	 * 		   to take the repeatable set again.
	 */
	public final String getTermiantionTrueLabel() {
		return terminationTrueLabel;
	}

	/**
	 * Returns the text to be displayed to the user indicating that they do not
	 * want to take the repeatable set again.
	 * 
	 * @return The text to be displayed to the user indicating that they do not
	 * 		   want to take the repeatable set again.
	 */
	public final String getTerminationFalseLabel() {
		return terminationFalseLabel;
	}

	/**
	 * Returns whether or not the user is allowed to skip the entire repeatable
	 * set.
	 * 
	 * @return Whether or not the user is allowed to skip the entire repeatable
	 * 		   set.
	 */
	public final boolean isTerminationSkipEnabled() {
		return terminationSkipEnabled;
	}

	/**
	 * Returns the text to be displayed to the user indicating that they may
	 * skip the repeatable set.
	 * 
	 * @return The text to be displayed to the user indicating that they may
	 * 		   skip the repeatable set.
	 */
	public final String getTerminationSkipLabel() {
		return terminationSkipLabel;
	}

	/**
	 * Returns the map of survey item indexes to their SurveyItem objects for
	 * this repeatable set.
	 * 
	 * @return The map of survey item indexes to their SurveyItem objects for
	 * 		   this repeatable set.
	 */
	public final Map<Integer, SurveyItem> getSurveyItems() {
		return Collections.unmodifiableMap(surveyItems);
	}
	
	/**
	 * Returns a prompt from this repeatable set or from any sub repeatable 
	 * sets.
	 * 
	 * @param promptId The prompt's unique identifier.
	 * 
	 * @return A Prompt object representing the prompt if it exists in this
	 * 		   repeatable set or any of its sub repeatable sets; otherwise, 
	 * 		   null is returned. 
	 */
	public final Prompt getPrompt(final String promptId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			return null;
		}
		
		for(SurveyItem prompt : surveyItems.values()) {
			if(prompt instanceof RepeatableSet) {
				Prompt result = ((RepeatableSet) prompt).getPrompt(promptId);
				if(result != null) {
					return result;
				}
			}
			else if(prompt instanceof Prompt) {
				if(((Prompt) prompt).getId().equals(promptId)) {
					return (Prompt) prompt;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a repeatable set from this repeatable set or from any sub
	 * repeatable sets.
	 * 
	 * @param repeatableSetId The unique identifier for the desired repeatable
	 * 						  set.
	 * 						  
	 * @return The RepeatableSet object representing the repeatable set if it 
	 * 		   was found; otherwise, null is returned.
	 */
	public final RepeatableSet getRepeatableSet(final String repeatableSetId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(repeatableSetId)) {
			return null;
		}
		
		for(SurveyItem prompt : surveyItems.values()) {
			if(prompt instanceof RepeatableSet) {
				RepeatableSet repeatableSet = (RepeatableSet) prompt;
				
				if(repeatableSet.getId().equals(repeatableSetId)) {
					return repeatableSet;
				}
				
				RepeatableSet result = repeatableSet.getRepeatableSet(repeatableSetId);
				if(result != null) {
					return result;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Returns a survey item from this repeatable set, but not from any of the
	 * sub repeatable sets.
	 * 
	 * @param surveyItemId The survey item's unique identifier.
	 * 
	 * @return The SurveyItem in question or null if it doesn't exist.
	 */
	public final SurveyItem getSurveyItem(final String surveyItemId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(surveyItemId)) {
			return null;
		}
		
		for(SurveyItem surveyItem : surveyItems.values()) {
			if(surveyItem.getId().equals(surveyItemId)) {
				return surveyItem;
			}
		}
		
		return null;
	}
	
	/**
	 * Creates a JSONObject that represents this repeatable set and all of its
	 * survey items.
	 * 
	 * @return A JSONObject representing this repeatable set and it survey 
	 * 		   items or null if there is an error.
	 * 
	 * @throws JSONException There was an error creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		if(result == null) {
			// FIXME: Ignore the exception and let it propagate.
			return null;
		}
		
		result.put(JSON_KEY_TERMINATION_QUESTION, terminationQuestion);
		result.put(JSON_KEY_TERMINATION_TRUE_LABEL, terminationTrueLabel);
		result.put(JSON_KEY_TERMINATION_FALSE_LABEL, terminationFalseLabel);
		result.put(JSON_KEY_TERMINATION_SKIP_ENABLED, terminationSkipEnabled);
		result.put(JSON_KEY_TERMINATION_SKIP_LABEL, terminationSkipLabel);
		
		List<Integer> indices = new ArrayList<Integer>(surveyItems.keySet());
		Collections.sort(indices);
		
		JSONArray surveyItemsJson = new JSONArray();
		for(Integer index : indices) {
			surveyItemsJson.put(surveyItems.get(index).toJson());
		}
		result.put(JSON_KEY_PROMPTS, surveyItemsJson);
		
		return result;
	}

	/**
	 * Returns the number of survey items that this repeatable set contains,
	 * plus all of the survey items in any of the repeatable sets that it
	 * contains, plus one for itself.
	 * 
	 * @return The total number of survey items contained by this repeatable
	 * 		   set and all sub repeatable sets plus one for itself.
	 */
	@Override
	public int getNumSurveyItems() {
		// Start at 1 to include yourself.
		int total = 1;
		
		for(SurveyItem prompt : surveyItems.values()) {
			total += prompt.getNumSurveyItems();
		}
		
		return total;
	}
	
	/**
	 * Returns the number of surveyItems contained by this repeatable set and all
	 * those in sub repeatable sets.
	 * 
	 * @return The total number of surveyItems contained in this repeatable set and
	 * 		   contained in all of its sub repeatable sets.
	 */
	@Override
	public int getNumPrompts() {
		int total = 0;
		
		for(SurveyItem prompt : surveyItems.values()) {
			total += prompt.getNumPrompts();
		}
		
		return total;
	}

	/**
	 * Generates a hash code for this repeatable set.
	 * 
	 * @return A hash code for this repeatable set.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((surveyItems == null) ? 0 : surveyItems.hashCode());
		result = prime
				* result
				+ ((terminationTrueLabel == null) ? 0 : terminationTrueLabel
						.hashCode());
		result = prime
				* result
				+ ((terminationFalseLabel == null) ? 0 : terminationFalseLabel
						.hashCode());
		result = prime
				* result
				+ ((terminationQuestion == null) ? 0 : terminationQuestion
						.hashCode());
		result = prime * result + (terminationSkipEnabled ? 1231 : 1237);
		result = prime
				* result
				+ ((terminationSkipLabel == null) ? 0 : terminationSkipLabel
						.hashCode());
		return result;
	}

	/**
	 * Compares this repeatable set with another object and returns true if 
	 * they are logically equivalent.
	 * 
	 * @param obj The other object.
	 * 
	 * @return True if the object is logically equivalent to this repeatable 
	 * 		   set; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RepeatableSet other = (RepeatableSet) obj;
		if (surveyItems == null) {
			if (other.surveyItems != null)
				return false;
		} else if (!surveyItems.equals(other.surveyItems))
			return false;
		if (terminationTrueLabel == null) {
			if (other.terminationTrueLabel != null)
				return false;
		} else if (!terminationTrueLabel.equals(other.terminationTrueLabel))
			return false;
		if (terminationFalseLabel == null) {
			if (other.terminationFalseLabel != null)
				return false;
		} else if (!terminationFalseLabel.equals(other.terminationFalseLabel))
			return false;
		if (terminationQuestion == null) {
			if (other.terminationQuestion != null)
				return false;
		} else if (!terminationQuestion.equals(other.terminationQuestion))
			return false;
		if (terminationSkipEnabled != other.terminationSkipEnabled)
			return false;
		if (terminationSkipLabel == null) {
			if (other.terminationSkipLabel != null)
				return false;
		} else if (!terminationSkipLabel.equals(other.terminationSkipLabel))
			return false;
		return true;
	}
}
