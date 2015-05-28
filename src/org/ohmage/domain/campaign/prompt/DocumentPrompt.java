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
package org.ohmage.domain.campaign.prompt;

import java.io.IOException;
import java.util.UUID;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.Media;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.DocumentPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a document prompt.
 * 
 * @author Hongsuda T. 
 */
public class DocumentPrompt extends MediaPrompt {
	
	
	/**
	 * Creates a document prompt.
	 * 
	 * @param id The unique identifier for the prompt within its survey item
	 * 			 group.
	 * 
	 * @param condition The condition determining if this prompt should be
	 * 					displayed.
	 * 
	 * @param unit The unit value for this prompt.
	 * 
	 * @param text The text to be displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param maxFileSize The maximum allowed dimension for a photo.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if the vertical resolution is negative.
	 */
	public DocumentPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final int index,
			final Long maxFileSize) 
			throws DomainException {
		
		super(
			id,
			condition,
			unit,
			text,
			explanationText,
			skippable,
			skipLabel,
			displayLabel,
			Type.DOCUMENT,
			index,
			maxFileSize);
	
	}
	
	
	/**
	 * Conditions are not allowed within the document prompt. Use the parent's validation.
	 * 
	 * @param pair The pair to validate.
	 * 
	 * @throws DomainException Always thrown because conditions are not allowed
	 * 						   for photo prompts.
	 */
	@Override
	public void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		throw
			new DomainException(
				"Conditions are not allowed in document prompts.");
	}

	
	/**
	 * Validates that the corresponding media object. 
	 * 
	 * @return A {@link UUID} object or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	public void validateMedia(final Media media) throws DomainException {
		validateMediaFileSize(media);
		
	}
	

	/**
	 * Creates a response to this prompt based on a response value.
	 * 
	 * @param response The response from the user as an Object.
	 * 
	 * @param repeatableSetIteration If this prompt belongs to a repeatable 
	 * 								 set, this is the iteration of that 
	 * 								 repeatable set on which the response to
	 * 								 this prompt was made.
	 * 
	 * @throws DomainException Thrown if this prompt is part of a repeatable 
	 * 						   set but the repeatable set iteration value is 
	 * 						   null, if the repeatable set iteration value is 
	 * 						   negative, or if the value is not a valid 
	 * 						   response value for this prompt.
	 */
	@Override
	public DocumentPromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException {
		
		return new DocumentPromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this prompt.
	 * 
	 * @return A JSONObject that represents this prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		return result;
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!super.equals(obj)) {
			return false;
		}
		if(!(obj instanceof DocumentPrompt)) {
			return false;
		}
	
		return true;
	}
}