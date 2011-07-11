package org.ohmage.validator;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class validates UUID and UUID-related values.
 * 
 * @author John Jenkins
 */
public final class UuidValidators {
	private static final Logger LOGGER = Logger.getLogger(UuidValidators.class);
	
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private UuidValidators() {}
	
	/**
	 * Validates that a UUID String is a valid UUID.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param uuid The UUID as a String to validate.
	 * 
	 * @return Returns null if the UUID String is null or whitespace only.
	 * 		   Otherwise, it returns the parameterized UUID value.
	 * 
	 * @throws ValidationException Thrown if the UUID value is not null, not
	 * 							   whitespace only, and not a valid UUID.
	 */
	public static String validateUuid(Request request, String uuid) throws ValidationException {
		LOGGER.info("Validating that a UUID is a valid UUID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(uuid)) {
			return null;
		}
		
		try {
			UUID.fromString(uuid);
			
			return uuid;
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.MALFORMED_UUID, "The document's ID is not a valid UUID.");
			throw new ValidationException("The document's ID is not a valid UUID.");
		}
	}
}
