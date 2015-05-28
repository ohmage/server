package org.ohmage.domain.campaign.prompt;

import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.domain.campaign.response.AudioPromptResponse;
import org.ohmage.exception.DomainException;

/**
 * <p>
 * This class represents an audio prompt.
 * </p>
 *
 * @author John Jenkins
 * @author Hongsuda T. 
 */
public class AudioPrompt extends MediaPrompt {
	/**
	 * The JSON key for the maximum duration.
	 */
	public static final String JSON_KEY_MAX_DURATION = "max_milliseconds";
	/**
	 * The JSON key for the maximum duration.
	 */
	public static final String XML_KEY_MAX_DURATION = "maxMilliseconds";
	
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
	 * 		  The maxDuration of the audio object
	 * 
	 * @param maxFileSize 
	 * 		  The maximum file size of the audio object
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
		final Long maxDuration, 
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
			Type.AUDIO,
			index,
			maxFileSize);
		
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
	 * Conditions are not allowed for audio prompts. Use parent's method.
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
		
		if(maxFileSize != null) {
			result.put(DocumentPrompt.JSON_KEY_MAX_FILESIZE, maxFileSize);
		}
		
		return result;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result +
				((maxDuration == null) ? 0 : maxDuration.hashCode());
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
		if(!(obj instanceof AudioPrompt)) {
			return false;
		}
		AudioPrompt other = (AudioPrompt) obj;
		if (maxDuration == null) {
			if (other.maxDuration != null)
				return false;
		} else {
			if(! maxDuration.equals(other.maxDuration)) {
				return false;
			}
		}
		return true;
	}
}