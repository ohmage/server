package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Implementations of this interface are intended to validate some portion of an AwRequest instance.
 * 
 * @author selsky
 */
public interface Validator {
	
	/**
	 * Runs validation against attributes of the provided AwRequest.
	 */
	public boolean validate(AwRequest awRequest);
	
}
