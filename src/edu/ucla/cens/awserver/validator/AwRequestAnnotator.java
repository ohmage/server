package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Handler for annotating an AwRequest when post-processing validation. 
 * 
 * @author selsky
 */
public interface AwRequestAnnotator {
	
	/**
	 * Annotates the provided AwRequest with details of the failed validation. 
	 */
	public void annotate(AwRequest request, String message);
	
}
