package org.ohmage.validator;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.ohmage.util.StringUtils;

/**
 * This class validates UUID and UUID-related values. This should not be called
 * by any Requests and should instead be called by other validators which 
 * interpret the results and should fail the request and/or return the value.
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
	 * @param uuid The UUID as a String to validate.
	 * 
	 * @return Returns true if the String is null, whitespace only, or a valid
	 * 		   UUID; otherwise, it returns false.
	 */
	public static boolean validateUuid(String uuid) {
		LOGGER.info("Validating that a UUID is a valid UUID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(uuid)) {
			return true;
		}
		
		try {
			UUID.fromString(uuid);
			
			return true;
		}
		catch(IllegalArgumentException e) {
			return false;
		}
	}
}
