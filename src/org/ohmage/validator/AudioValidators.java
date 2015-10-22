package org.ohmage.validator;

import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about audio data.
 * 
 * @author John Jenkins
 */
public class AudioValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private AudioValidators() {}

	/**
	 * Validates that an audio ID is a valid audio ID. If it is null or
	 * whitespace only, null is returned. Otherwise, the audio is returned or
	 * an exception is thrown.
	 * 
	 * @param id
	 *        The audio ID.
	 * 
	 * @return Returns null if the audio ID is null or whitespace only;
	 *         otherwise, the audio ID is returned.
	 * 
	 * @throws ValidationException
	 *         Thrown if the audio ID is not null, not whitespace only, and not
	 *         a valid audio ID.
	 */
	public static UUID validateId(
		final String id)
		throws ValidationException {
		
		try {
			return MediaValidators.validateId(id);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.AUDIO_INVALID_ID, 
					"The video ID is not a valid video ID: " + id);
		}
	}
}