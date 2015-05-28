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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.IMedia;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a document prompt.
 * 
 * @author Hongsuda T. 
 */
public abstract class MediaPrompt extends Prompt {
	
	/**
	 * The JSON key for the maximum file size.
	 */
	public static final String JSON_KEY_MAX_FILESIZE = "max_filesize";
	
	/**
	 * The campaign configuration property key for the maximum file size 
	 * allowed.
	 */
	public static final String XML_KEY_MAX_FILESIZE = "maxFileSize";
	
	/**
	 * The maximum file size that can be uploaded to the server.
	 */
	protected final Long maxFileSize;
	
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
	public MediaPrompt(
			final String id, 
			final String condition, 
			final String unit, 
			final String text, 
			final String explanationText,
			final boolean skippable, 
			final String skipLabel,
			final String displayLabel,
			final Prompt.Type type,
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
			index);
		
		if((maxFileSize != null) && (maxFileSize < 0)){ 
				throw new DomainException(
					"The maximum filesize cannot be negative.");
		}
		
		this.maxFileSize = maxFileSize;
	}
	
	/**
	 * Returns the maximum allowed dimension of an image.
	 * 
	 * @return The maximum allowed dimension of an image or null if it
	 * 		   was not given.
	 */
	public Long getMaxFileSize() {
		return maxFileSize;
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
	 * Validates that a given value is valid and, if so, converts it into an
	 * appropriate object.
	 * 
	 * @param value The value to be validated. This must be one of the  
	 * 				following:<br />
	 * 				<ul>
	 * 				<li>{@link NoResponse}</li>
	 * 				<li>{@link UUID}</li>
	 * 				<li>{@link String} that represents:</li>
	 * 				  <ul>
	 * 				    <li>{@link NoResponse}</li>
	 * 				    <li>{@link UUID}</li>
	 * 				  <ul>
	 * 				</ul>
	 * 
	 * @return A {@link UUID} object or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	@Override
	public Object validateValue(final Object value) throws DomainException {
		// If it's already a NoResponse value, then return make sure that if it
		// was skipped that it as skippable.
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
						"The prompt, '" +
							getId() +
							"', was skipped, but it is not skippable.");
			}
			
			return value;
		}
		// If it is already a UUID value, then return it.
		else if(value instanceof UUID) {
			return value;
		}
		// If it is a String value then attempt to decode it into a NoResponse
		// value or a UUID value.
		else if(value instanceof String) {
			String valueString = (String) value;
			
			try {
				return NoResponse.valueOf(valueString);
			}
			catch(IllegalArgumentException notNoResponse) {
				try {
					return UUID.fromString(valueString);
				}
				catch(IllegalArgumentException notUuid) {
					throw new DomainException(
							"The string response value was not " +
									"decodable into a UUID for prompt '" +
									getId() +
									"': " +
									valueString);
				}
			}
		}	
		else {
			throw new DomainException(
					"The value is not decodable as a reponse value for prompt '" +
						getId() + 
						"'.");
		}
	}
	
	
	/**
	 * Validates that the corresponding media object. 
	 * 
	 * @return A {@link UUID} object or a {@link NoResponse} object.
	 * 
	 * @throws DomainException The value is invalid.
	 */
	public void validateMediaFileSize(final IMedia media) throws DomainException {
		if (media == null) {
			throw new DomainException(
					"The media content is null: " +	getId() + "'.");
		}
		
		if ((maxFileSize != null) && (media.getFileSize() > maxFileSize))
			throw new DomainException(ErrorCode.MEDIA_INVALID_DATA, 
					"The file size is larger than its specified maximum: " + maxFileSize);	
	}
	

	public abstract PromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response) 
			throws DomainException;


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
		
		if(maxFileSize != null) {
			result.put(JSON_KEY_MAX_FILESIZE, maxFileSize);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.SurveyItem#toConcordia(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void toConcordia(
			final JsonGenerator generator)
			throws JsonGenerationException, IOException {
		
		// The response is always an object.
		generator.writeStartObject();
		generator.writeStringField("type", "object");
		
		// The fields array.
		generator.writeArrayFieldStart("schema");
		
		// The first field in the object is the prompt's ID.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_PROMPT_ID);
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		// The second field in the object is the response's value.
		generator.writeStartObject();
		generator.writeStringField("name", PromptResponse.JSON_KEY_RESPONSE);
		generator.writeStringField("type", "string");
		generator.writeEndObject();
		
		// End the array of fields.
		generator.writeEndArray();
		
		// End the object.
		generator.writeEndObject();
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result =
			prime *
				result +
				((maxFileSize == null) ? 0 : maxFileSize.hashCode());
		return result;
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
		if(!(obj instanceof MediaPrompt)) {
			return false;
		}
		MediaPrompt other = (MediaPrompt) obj;
		if (maxFileSize == null) {
			if (other.maxFileSize != null)
				return false;
		} else if(! maxFileSize.equals(other.maxFileSize)) {
			return false;
		}
		return true;

	}
	
}