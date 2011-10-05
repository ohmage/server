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
import java.util.HashMap;
import java.util.Map;

import org.ohmage.util.StringUtils;

/**
 * Maps a repeatable set id to the Prompts contained with in the set.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class RepeatableSet extends SurveyItem {
	/**
	 */
	private final String terminationQuestion;
	/**
	 */
	private final String termiantionTrueLabel;
	/**
	 */
	private final String terminationFalseLabel;
	
	/**
	 */
	private final boolean terminationSkipEnabled;
	/**
	 */
	private final String terminationSkipLabel;
	
	/**
	 */
	private final Map<String, SurveyItem> prompts;
	
	public RepeatableSet(final String id, final String condition,
			final String terminationQuestion, 
			final String terminationTrueLabel, 
			final String terminationFalseLabel,
			final boolean terminationSkipEnabled, 
			final String terminationSkipLabel,
			final Map<String, SurveyItem> prompts,
			final int index) {
		
		super(id, condition, index);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationQuestion)) {
			throw new IllegalArgumentException("The termination question cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationTrueLabel)) {
			throw new IllegalArgumentException("The termination true label cannot be null.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(terminationFalseLabel)) {
			throw new IllegalArgumentException("The termination false label cannot be null.");
		}
		if(terminationSkipEnabled && StringUtils.isEmptyOrWhitespaceOnly(terminationSkipLabel)) {
			throw new IllegalArgumentException("The termination skip is enabled but the label is null.");
		}
		if((prompts == null) || (prompts.size() == 0)) {
			throw new IllegalArgumentException("The prompt list cannot be null or empty.");
		}
		
		this.terminationQuestion = terminationQuestion;
		this.termiantionTrueLabel = terminationTrueLabel;
		this.terminationFalseLabel = terminationFalseLabel;
		
		this.terminationSkipEnabled = terminationSkipEnabled;
		this.terminationSkipLabel = terminationSkipLabel;
		
		this.prompts = new HashMap<String, SurveyItem>(prompts.size());
		for(SurveyItem surveyItem : prompts.values()) {
			surveyItem.setParent(this);
			this.prompts.put(surveyItem.getId(), surveyItem);
		}
	}

	/**
	 * @return
	 */
	public final String getTerminationQuestion() {
		return terminationQuestion;
	}

	/**
	 * @return
	 */
	public final String getTermiantionTrueLabel() {
		return termiantionTrueLabel;
	}

	/**
	 * @return
	 */
	public final String getTerminationFalseLabel() {
		return terminationFalseLabel;
	}

	/**
	 * @return
	 */
	public final boolean isTerminationSkipEnabled() {
		return terminationSkipEnabled;
	}

	/**
	 * @return
	 */
	public final String getTerminationSkipLabel() {
		return terminationSkipLabel;
	}

	public final Map<String, SurveyItem> getPrompts() {
		return Collections.unmodifiableMap(prompts);
	}
	
	public final Prompt getPrompt(final String promptId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			return null;
		}
		
		for(SurveyItem prompt : prompts.values()) {
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
	
	public final RepeatableSet getRepeatableSet(final String repeatableSetId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(repeatableSetId)) {
			return null;
		}
		
		for(SurveyItem prompt : prompts.values()) {
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
	
	@Override
	public int getNumPrompts() {
		int total = 0;
		
		for(SurveyItem prompt : prompts.values()) {
			total += prompt.getNumPrompts();
		}
		
		return total;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((prompts == null) ? 0 : prompts.hashCode());
		result = prime
				* result
				+ ((termiantionTrueLabel == null) ? 0 : termiantionTrueLabel
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
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
		if (prompts == null) {
			if (other.prompts != null)
				return false;
		} else if (!prompts.equals(other.prompts))
			return false;
		if (termiantionTrueLabel == null) {
			if (other.termiantionTrueLabel != null)
				return false;
		} else if (!termiantionTrueLabel.equals(other.termiantionTrueLabel))
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