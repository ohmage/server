package edu.ucla.cens.awserver.validator.json;


import org.json.JSONObject;

import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Validator interface for validation against JSONObjects where the validation may result in changes to the AwRequest.
 * 
 * @author selsky
 */
public interface JsonObjectValidator {

	public boolean validate(AwRequest request, JSONObject object);
	
}
