package edu.ucla.cens.awserver.service;

import org.json.JSONArray;
import org.json.JSONException;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Service-level validation of in-bound JSON messages.
 * 
 * @author selsky
 */
public class JsonMessageSyntaxValidationService implements Service {
	private AwRequestAnnotator _awRequestAnnotator;
	
	/**
	 * @throws IllegalArgumentException if the provided message is null, empty, or all whitespace 
	 */
	public JsonMessageSyntaxValidationService(AwRequestAnnotator awRequestAnnotator) {
		if(null == awRequestAnnotator) {
			throw new IllegalArgumentException("a non-null AwRequestAnnotator is required");
		}
		_awRequestAnnotator = awRequestAnnotator;
	}
	
	/**
	 * Checks the structure and contents of JSON messages. Extra validation is performed here instead of in the validation layer
	 * for because it is not the job of the first-level of validation to parse JSON and place the parsed results back into the 
	 * request. The idea is to keep the purpose/functionality of each app layer as segmented and specific as possible. 
	 * 
	 * If the message is syntactically correct JSON, it gets placed into the AwRequest as a JSONArray.
	 */
	public void execute(AwRequest awRequest) {
		JSONArray jsonArray =  null;
		
		try {
			String jsonDataString = awRequest.getJsonDataAsString();
			jsonArray = new JSONArray(jsonDataString);
			awRequest.setJsonDataAsString(null); // free the reference to the (potentially huge) string 
			awRequest.setJsonDataAsJsonArray(jsonArray);
		
		} catch(JSONException jsone) { // the message is not syntactically correct JSON (the new JSONArray() failed because of 
			                           // a JSON syntax error)
			
			_awRequestAnnotator.annotate(awRequest, "invalid JSON");
			
		}
	}
}
