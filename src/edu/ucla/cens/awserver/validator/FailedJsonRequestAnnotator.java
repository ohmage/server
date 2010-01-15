package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * An implementation of AwRequestAnnotator for validation failures which ultimately result in JSON output (initially used 
 * in response to phone/device data uploads).
 * 
 * @author selsky
 */
public class FailedJsonRequestAnnotator implements AwRequestAnnotator {
	private static Logger logger = Logger.getLogger(FailedJsonRequestAnnotator.class);
	private String _jsonErrorMessage;
	
	/**
	 * @throws IllegalArgumentException if jsonErrorMessage is null
	 * @throws IllegalArgumentException if jsonErrorMessage string cannot be parsed to syntactically correct JSON (it must be a 
	 * valid JSON array.)
	 */
	public FailedJsonRequestAnnotator(String jsonErrorMessage) {
		if(null == jsonErrorMessage) {
			throw new IllegalArgumentException("a null jsonErrorObject string is not allowed");
		}
		try {
			new JSONArray(jsonErrorMessage); // No variable assignment because all that's needed is the parse implicit in the 
			                                 // constructor. Unfortunately, this particular JSON API does not contain static 
			                                 // methods such as JSONArray.isValid(String jsonString) so the constructor is abused
			                                 // instead.
		} catch (JSONException jsonException) {
			throw new IllegalArgumentException("the jsonErrorObject is invalid JSON");
		}
		
		_jsonErrorMessage = jsonErrorMessage;
	}
	
	/**
     * Sets failed request properties on the AwRequest.
	 */
	public void annotate(AwRequest request, String message) {
		request.setFailedRequest(true);
		request.setFailedRequestErrorMessage(_jsonErrorMessage);
				
		if(logger.isDebugEnabled()) {
			logger.debug(message);
		}
		
	}
}
