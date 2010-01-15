package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Handler for annotating an AwRequest when post-processing validation. 
 * 
 * @author selsky
 */
public interface AwRequestAnnotator {
	
	/**
	 * Annotates the AwRequest with the provided message. Implementations of this interface are free to annotate the 
	 * request in ways specific to a feature or usage scenario (e.g., failed validation, creation of custom error messages, etc). 
	 */
	public void annotate(AwRequest request, String message);
	
}
