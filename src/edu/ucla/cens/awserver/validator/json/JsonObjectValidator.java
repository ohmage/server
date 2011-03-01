package edu.ucla.cens.awserver.validator.json;


import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Validator interface for validation against JSONObjects where the validation may result in changes to the AwRequest.
 * 
 * @author selsky
 */
public interface JsonObjectValidator {

	boolean validate(AwRequest awRequest, JSONObject object);
	
}
