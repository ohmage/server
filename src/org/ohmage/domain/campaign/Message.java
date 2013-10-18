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

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

public final class Message extends SurveyItem {
	private static final String JSON_KEY_TEXT = "text";
	
	/**
	 * The text to be displayed to the user.
	 */
	private final String text;
	
	/**
	 * Creates a Message object.
	 * 
	 * @param id The message's unqiue identifier.
	 * 
	 * @param condition A string representing the conditions under which this 
	 * 					message should be shown.
	 * 
	 * @param index The messages index in its group of survey items.
	 * 
	 * @param text The text to display to the user.
	 * 
	 * @throws DomainException Thrown if the text is null or whitespace only.
	 */
	public Message(
			final String id, 
			final String condition, 
			final int index,
			final String text) 
			throws DomainException {
		
		super(id, condition, index);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new DomainException(
					"The text cannot be null or whitespace only.");
		}
		
		this.text = text;
	}
	
	/**
	 * Returns the text of this message.
	 * 
	 * @return The text of this message.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Conditions are not allowed for messages unless they are
	 * {@link NoResponse} values.
	 */
	@Override
	public void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		throw
			new DomainException(
				"Conditions are not allowed on messages unless the SKIPPED or NOT_DISPLAYED is the value being compared.");
	}
	
	/**
	 * Returns the number of survey items that this represents which is always
	 * exactly 1.
	 * 
	 * @return 1
	 */
	@Override
	public int getNumSurveyItems() {
		return 1;
	}
	
	/**
	 * Always returns 0 as this is not a prompt and doesn't contain any
	 * subprompts.
	 * 
	 * @return 0
	 */
	@Override
	public int getNumPrompts() {
		return 0;
	}
	
	/**
	 * Creates a JSONObject that represents this message.
	 * 
	 * @return A JSONObject that represents this message.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_TEXT, text);
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.SurveyItem#toConcordia(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void toConcordia(
			final JsonGenerator geneartor)
			throws JsonGenerationException, IOException {
		
		throw new UnsupportedOperationException(
			"Messages have no output format");
	}

	/**
	 * Creates a hash code for this message.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/**
	 * Determines if this Message object is equal to another object.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
