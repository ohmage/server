package edu.ucla.cens.awserver.validator.json;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

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
	 * The provided error message will be used as JSON output and must be a syntactically valid JSON Object.
	 * 
	 * @throws IllegalArgumentException if jsonErrorMessage is null
	 * @throws IllegalArgumentException if jsonErrorMessage string cannot be parsed to syntactically correct JSON (it must be a 
	 * valid JSON array.)
	 */
	public FailedJsonRequestAnnotator(String jsonErrorMessage) {
		if(null == jsonErrorMessage) {
			throw new IllegalArgumentException("a null jsonErrorObject string is not allowed");
		}
		try {
			new JSONObject(jsonErrorMessage); // No variable assignment because all that's needed is the parse implicit in the 
			                                  // constructor. Unfortunately, this particular JSON API does not contain static 
			                                  // methods such as JSONObject.isValid(String jsonString) so the constructor is abused
			                                  // instead.
		} catch (JSONException jsonException) {
			throw new IllegalArgumentException("the jsonErrorObject is invalid JSON");
		}
		
		_jsonErrorMessage = jsonErrorMessage;
	}
	
	/**
     * Annotates the request with a failed error message using JSON syntax. The JSON message is of the form: 
     * <pre>
     *   {
     *     "request_url":"user's request url",
     *     "request_json":"the original JSON sent with the request",
     *     "errors":[
     *       {
     *          A JSON Object that is represented by the message supplied on construction of this class. 
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
		JSONObject jsonObject = null;
		
		try {
			
			jsonObject = new JSONObject(_jsonErrorMessage);
			
			// this is ugly because it assumes the structure of the error message, the way errors should be configured is
			// that error codes and text should be set instead of a JSON string TODO
			JSONArray errorArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, "errors");
			JSONObject errorObject = JsonUtils.getJsonObjectFromJsonArray(errorArray, 0);
			
			errorObject.put("at_record_number", (Integer) awRequest.getAttribute("currentMessageIndex"));
			
			if(null != awRequest.getAttribute("currentPromptId")) { // a prompt upload is being handled.
				
				errorObject.put("at_prompt_id", (Integer) awRequest.getAttribute("currentPromptId"));
			}
			
			// now add the original request URL and the original JSON input message to the error output
			jsonObject.put("request_url", awRequest.getAttribute("requestUrl"));
			jsonObject.put("request_json", awRequest.getAttribute("jsonData"));
						
		
		} catch(JSONException jsone) {  // invalid JSON at this point is a logical error
		
			throw new IllegalStateException(jsone);
		}
		
		awRequest.setFailedRequestErrorMessage(jsonObject.toString());
				
		if(logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}
}
