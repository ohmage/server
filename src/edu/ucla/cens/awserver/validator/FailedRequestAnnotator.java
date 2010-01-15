package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * An implementation of AwRequestAnnotator for general validation failures.
 * 
 * @author selsky
 */
public class FailedRequestAnnotator implements AwRequestAnnotator {
	private static Logger _logger = Logger.getLogger(FailedRequestAnnotator.class);
	
	/**
     * On the AwRequest, sets both the failedRequest property to true and the failedRequestErrorMessage property to the passed-in
     * message. 
	 */
	public void annotate(AwRequest request, String message) {
		request.setFailedRequest(true);
		request.setFailedRequestErrorMessage(message);
		
		if(_logger.isDebugEnabled()) {
			_logger.debug(message);
		}
	}
}
