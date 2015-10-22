package org.ohmage.validator;

import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about videos.
 * 
 * @author John Jenkins
 */
public final class VideoValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private VideoValidators() {}
	
	/**
	 * Validates that an video ID is a valid video ID. If it is null or 
	 * whitespace only, null is returned. Otherwise, the video is returned or
	 * an exception is thrown.
	 * 
	 * @param videoId The video's ID.
	 * 
	 * @return Returns null if the video ID is null or whitespace only; 
	 * 		   otherwise, the video ID is returned.
	 * 
	 * @throws ValidationException Thrown if the video ID is not null, not
	 * 							   whitespace only, and not a valid video ID.
	 */
	public static UUID validateId(
			final String id) 
			throws ValidationException {
		
		try {
			return MediaValidators.validateId(id);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.VIDEO_INVALID_ID, 
					"The video ID is not a valid video ID: " + id);
		}
	}
}
