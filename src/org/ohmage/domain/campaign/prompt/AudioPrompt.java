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
import org.ohmage.domain.campaign.response.AudioPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * This class represents an audio prompt.
 * </p>
 *
 * @author John Jenkins
 */
public class AudioPrompt extends Prompt {
	/**
	 * The JSON key for the maximum duration.
	 */
	public static final String JSON_KEY_MAX_DURATION = "max_duration";
	/**
	 * The JSON key for the maximum duration.
	 */
	public static final String XML_KEY_MAX_DURATION = "maxDuration";
	
	/**
	 * The maximum number of milliseconds that the recording may last.
	 */
	private final Long maxDuration;
	
	/**
	 * Creates an audio prompt.
	 * 
	 * @param id
	 *        The unique identifier for the prompt within its survey item
	 *        group.
	 * 
	 * @param condition
	 *        The condition determining if this prompt should be displayed.
	 * 
	 * @param unit
	 *        The unit value for this prompt.
	 * 
	 * @param text
	 *        The text to be displayed to the user for this prompt.
	 * 
	 * @param explanationText
	 *        A more-verbose version of the text to be displayed to the user
	 *        for this prompt.
	 * 
	 * @param skippable
	 *        Whether or not this prompt may be skipped.
	 * 
	 * @param skipLabel
	 *        The text to show to the user indicating that the prompt may be
	 *        skipped.
	 * 
	 * @param displayLabel
	 *        The display label for this prompt.
	 * 
	 * @param index
	 *        This prompt's index in its container's list of survey items.
	 *        
	 * @param maxDuration
	 * 
	 * @throws DomainException
	 *         Thrown if the maximum duration is negative.
	 */
	public AudioPrompt(
		final String id,
		final String condition,
		final String unit,
		final String text,
		final String explanationText,
		final boolean skippable,
		final String skipLabel,
		final String displayLabel,
		final int index,
		final Long maxDuration)
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
			Type.AUDIO,
			index);
		
		// Validate the maximum duration.
		if((maxDuration != null) && (maxDuration <= 0)) {
			throw
				new DomainException("The maximum duration must be positive.");
		}
		this.maxDuration = maxDuration;
	}
	
	/**
	 * Returns the maximum allowed duration for a recording from this prompt.
	 * 
	 * @return The maximum allowed duration for a recording from this prompt.
	 */
	public long getMaxDuration() {
		return maxDuration;
	}
	
	/**
	 * Conditions are not allowed for audio prompts unless they are
	 * {@link NoResponse} values.
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
				"Conditions are not allowed for audio prompts.");
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.Prompt#validateValue(java.lang.Object)
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
	public AudioPromptResponse createResponse(
		final Integer repeatableSetIteration,
		final Object response)
		throws DomainException {
		
		return 
			new AudioPromptResponse(
				this,
				repeatableSetIteration,
				response);
	}
	
	/**
	 * Creates a JSONObject that represents this photo prompt.
	 * 
	 * @return A JSONObject that represents this photo prompt.
	 * 
	 * @throws JSONException There was a problem creating the JSONObject.
	 */
	@Override
	public JSONObject toJson() throws JSONException {
		JSONObject result = super.toJson();
		
		if(maxDuration != null) {
			result.put(JSON_KEY_MAX_DURATION, maxDuration);
		}
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.campaign.SurveyItem#toConcordia(org.codehaus.jackson.JsonGenerator)
	 */
	@Override
	public void toConcordia(JsonGenerator generator)
		throws JsonGenerationException,
		IOException {
		
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
}