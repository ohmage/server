package org.ohmage.domain.campaign.prompt;

import java.io.IOException;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.Prompt;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.Response.NoResponse;
import org.ohmage.domain.campaign.response.VideoPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * This class represents a video prompt.
 *
 * @author John Jenkins
 */
public class VideoPrompt extends Prompt {
	private static final String JSON_KEY_MAX_SECONDS = "max_seconds";
	
	/**
	 * The key for the properties to retrieve the maximum seconds value.
	 */
	public static final String XML_MAX_SECONDS = "max_seconds";
	
	private final int maxSeconds;

	/**
	 * Creates a new video prompt.
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
	 * @param abbreviatedText An abbreviated version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param explanationText A more-verbose version of the text to be 
	 * 						  displayed to the user for this prompt.
	 * 
	 * @param skippable Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel The text to show to the user indicating that the prompt
	 * 					may be skipped.
	 * 
	 * @param displayType This prompt's
	 * 					 {@link org.ohmage.domain.campaign.Prompt.DisplayType}.
	 * 
	 * @param displayLabel The display label for this prompt.
	 * 
	 * @param maxSeconds The maximum number of seconds allowed for this video.
	 * 
	 * @param index This prompt's index in its container's list of survey 
	 * 				items.
	 * 
	 * @throws DomainException Thrown if the maximum number of seconds is 
	 * 						   negative.
	 */
	public VideoPrompt(
			final String id,
			final String condition,
			final String unit,
			final String text,
			final String abbreviatedText,
			final String explanationText,
			final boolean skippable,
			final String skipLabel,
			final DisplayType displayType,
			final String displayLabel,
			final int maxSeconds,
			final int index) 
			throws DomainException {
		
		super(
			id,
			condition,
			unit,
			text,
			abbreviatedText,
			explanationText,
			skippable,
			skipLabel,
			displayType,
			displayLabel,
			Type.VIDEO,
			index);
		
		if(maxSeconds <= 0) {
			throw new DomainException(
				"The maximum number of seconds must be a positive integer.");
		}
		this.maxSeconds = maxSeconds;
	}
	
	/**
	 * Returns the maximum number of seconds of video allowed.
	 * @return
	 */
	public int getMaxSeconds() {
		return maxSeconds;
	}

	/**
	 * Conditions are not allowed for this prompt type unless they are
	 * {@link NoResponse} values.
	 */
	@Override
	public void validateConditionValuePair(
			final ConditionValuePair pair)
			throws DomainException {
		
		// If the value is a valid NoResponse value, then it is acceptable to
		// compare against this prompt.
		if(checkNoResponseConditionValuePair(pair)) {
			return;
		}
		
		throw new DomainException(
			"Conditions are not allowed for video prompts.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.Prompt#validateValue(java.lang.Object)
	 */
	@Override
	public Object validateValue(
			final Object value)
			throws DomainException {
		
		if(value instanceof NoResponse) {
			if(NoResponse.SKIPPED.equals(value) && (! skippable())) {
				throw new DomainException(
					"The prompt, '" +
						getId() +
						"', was skipped, but it is not skippable.");
			}
			
			return value;
		}
		else if(value instanceof UUID) {
			return value;
		}
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
							"The string response value was not decodable into a UUID for prompt '" +
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

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.Prompt#createResponse(java.lang.Integer, java.lang.Object)
	 */
	@Override
	public PromptResponse createResponse(
			final Integer repeatableSetIteration,
			final Object response)
			throws DomainException {
		
		return new VideoPromptResponse(
			this,
			repeatableSetIteration,
			response);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.Prompt#toJson()
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		result.put(JSON_KEY_MAX_SECONDS, maxSeconds);
		
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
		result = prime * result + maxSeconds;
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
		if(!(obj instanceof VideoPrompt)) {
			return false;
		}
		VideoPrompt other = (VideoPrompt) obj;
		if(maxSeconds != other.maxSeconds) {
			return false;
		}
		return true;
	}
}
