package org.ohmage.validator;

import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about media.
 * 
 * @author Hongsuda T. 
 */
public final class MediaValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private MediaValidators() {}
	
	/**
	 * Validates that a media ID is a valid ID. If it is null or 
	 * whitespace only, null is returned. Otherwise, the UUID is returned or
	 * an exception is thrown.
	 * 
	 * @param mediaId The media's ID.
	 * 
	 * @return Returns null if the media ID is null or whitespace only; 
	 * 		   otherwise, the media ID is returned.
	 * 
	 * @throws ValidationException Thrown if the media ID is not null, not
	 * 							   whitespace only, and not a valid media ID.
	 */
	public static UUID validateId(
			final String mediaId) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(mediaId)) {
			return null;
		}
		
		try {
			return UUID.fromString(mediaId);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.MEDIA_INVALID_ID, 
					"The media ID is not a valid ID: " + mediaId);
		}
	}
}
