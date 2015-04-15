package org.ohmage.validator;

import java.util.UUID;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for validating information about document prompt data.
 * 
 * @author HT
 */
public class DocumentPValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private DocumentPValidators() {}

	/**
	 * Validates that an document ID is a valid document ID. If it is null or
	 * whitespace only, null is returned. Otherwise, the document is returned or
	 * an exception is thrown.
	 * 
	 * @param id
	 *        The document ID.
	 * 
	 * @return Returns null if the document ID is null or whitespace only;
	 *         otherwise, the document ID is returned.
	 * 
	 * @throws ValidationException
	 *         Thrown if the document ID is not null, not whitespace only, and not
	 *         a valid document ID.
	 */
	public static UUID validateId(
		final String id)
		throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(id)) {
			return null;
		}

		try {
			return UUID.fromString(id);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.MEDIA_INVALID_ID, 
					"The document ID is not a valid ID: " + id);
		}
	}
}