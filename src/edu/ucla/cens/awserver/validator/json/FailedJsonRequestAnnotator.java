package edu.ucla.cens.awserver.validator.json;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SensorUploadAwRequest;
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
     * 
     * Since the upload URL is public to the internet, there are cases where no data is posted because someone (malicious or not)
     * is hitting the URL. In this case, only the request URL is logged. 
     * 
	 */
	public void annotate(AwRequest awRequest, String message) {
		awRequest.setFailedRequest(true);
		JSONObject jsonObject = null;
		
		try {
			
			jsonObject = new JSONObject(_jsonErrorMessage);
			
			// TODO
			// This is ugly because it assumes the structure of the error message. The way errors should be configured is
			// that error codes and text should be configured instead of a JSON string
			
			JSONArray errorArray = JsonUtils.getJsonArrayFromJsonObject(jsonObject, "errors");
			JSONObject errorObject = JsonUtils.getJsonObjectFromJsonArray(errorArray, 0);
			
			if(awRequest instanceof SensorUploadAwRequest) { // hackeroo!
			
				if(-1 != awRequest.getCurrentMessageIndex()) {			
				
					errorObject.put("at_record_number", awRequest.getCurrentMessageIndex());
				
				}
				
				if(-1 != awRequest.getCurrentPromptId()) { // a prompt upload is being handled.
					
					errorObject.put("at_prompt_id", awRequest.getCurrentPromptId());
				}
				
				// Now add the original request URL and the original JSON input message to the error output
				// If the JSON data is longer than 250 characters, an info message is sent back instead in order to 
				// avoid echoing extremely large messages back to the client and into the server logs
				jsonObject.put("request_url", awRequest.getRequestUrl());
				
				String data = awRequest.getJsonDataAsString();
				if(null != data) {
					
					jsonObject.put("request_json", getDataTruncatedMessage(data));
									
				} else {
					
					jsonObject.put("request_json", getDataTruncatedMessage(awRequest.getJsonDataAsJsonArray().toString()));
				}
			}
			
		} catch(JSONException jsone) {  // invalid JSON at this point is a logical error
		
			throw new IllegalStateException(jsone);
		}
		
		awRequest.setFailedRequestErrorMessage(jsonObject.toString());
				
		if(logger.isDebugEnabled()) {
			logger.debug(message);
		}
	}
	
	/**
	 * @return "check server logs for input data" if the input string is over 250 characters
	 */
	private String getDataTruncatedMessage(String string) {
		if(null != string) {
			if(string.length() > 250) {
				return "check upload file dump for input data";
			}
		}
		return string;
	}
}
