package edu.ucla.cens.awserver.validator.json;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SensorUploadAwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * An implementation of AwRequestAnnotator for validation failures which ultimately result in JSON output (initially used 
 * in response to phone/device data uploads).
 * 
 * @author selsky
 */
public class FailedJsonRequestAnnotator implements AwRequestAnnotator {
	private static Logger logger = Logger.getLogger(FailedJsonRequestAnnotator.class);
	private ErrorResponse _errorResponse;
	
	/**
	 * The provided error message will be used as JSON output and must be a syntactically valid JSON Object.
	 * 
	 * @throws IllegalArgumentException if jsonErrorMessage is null
	 * @throws IllegalArgumentException if jsonErrorMessage string cannot be parsed to syntactically correct JSON (it must be a 
	 * valid JSON array.)
	 */
	public FailedJsonRequestAnnotator(ErrorResponse errorResponse) {
		if(null == errorResponse) {
			throw new IllegalArgumentException("a null ErrorResponse is not allowed");
		}
		_errorResponse = errorResponse;
	}
	
	/**
     * Annotates the request with a failed error message using JSON syntax. The JSON message is of the form: 
     * 
     * <pre>
     *   {
     *     "response":"failure",
     *     "errors":[
     *       {
     *         "code":"0000",
     *         "text":"text",
     *         "at_record_number":0,
     *         "at_prompt_id":0
     *       }
     *     ]
     *   }
     * </pre>
     * 
     * The message passed in is used for debug output only. For cases where the JSONObject representing the error output message 
     * must be passed into this method, @see FailedJsonSuppliedMessageRequestAnnotator.
	 */
	public void annotate(AwRequest awRequest, String message) {
		awRequest.setFailedRequest(true);
		
		try { // TODO - the response JSONObjects can be cached because they are not unique to a request
			
			JSONObject responseJsonObject = new JSONObject();
			JSONObject errorJsonObject = new JSONObject();
			JSONArray errorJsonArray = new JSONArray();
			
			responseJsonObject.put("result", "failure");
			
			errorJsonObject.put("code", _errorResponse.getCode());
			errorJsonObject.put("text", _errorResponse.getText());
			
			if(awRequest instanceof SensorUploadAwRequest) { // hackeroo!
			
				if(-1 != awRequest.getCurrentMessageIndex()) {
					errorJsonObject.put("at_record_number", awRequest.getCurrentMessageIndex());
				}
				
				if(-1 != awRequest.getCurrentPromptId()) { // a prompt upload is being handled.
					errorJsonObject.put("at_prompt_id", awRequest.getCurrentPromptId());
				}
			}
			
			errorJsonArray.put(errorJsonObject);
			responseJsonObject.put("errors", errorJsonArray);
			awRequest.setFailedRequestErrorMessage(responseJsonObject.toString());
			
			if(logger.isDebugEnabled()) {
				logger.debug(message);
			}
			
		} catch(JSONException jsone) {  // invalid JSON at this point is a logical error
		
			throw new IllegalStateException(jsone);
		}
	}
}
