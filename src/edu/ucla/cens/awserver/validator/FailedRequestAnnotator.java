package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * An implementation of AwRequestAnnotator for validation failures.
 * 
 * @author selsky
 */
public class FailedRequestAnnotator implements AwRequestAnnotator {

	/**
     * Sets failed request properties on the AwRequest.
	 */
	public void annotate(AwRequest request, String message) {
		request.setFailedRequest(true);
		request.setFailedRequestErrorMessage(message);
	}
}
