package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;

/**
 * Validates that the parameterized JSON that is received is a valid 
 * JSONObject.
 * 
 * @author John Jenkins
 */
public class JsonValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(JsonValidator.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this validator.
	 * 
	 * @param annotator The annotator to use if the parameterized value is not
	 * 					valid JSON.
	 */
	public JsonValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that the parameterized value is valid JSON.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String jsonData;
		try {
			jsonData = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				jsonData = (String) awRequest.getToValidateValue(_key);
				
				if(jsonData == null) {
					if(_required) {
						throw new ValidatorException("The required key is missing: " + _key);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("The required key is missing: " + _key);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating that the parameterized JSON data is a JSONObject: " + _key);
		
		try {
			new JSONObject(jsonData);
		}
		catch(JSONException e) {
			getAnnotator().annotate(awRequest, "The JSON parameter is invalid: " + e);
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(_key, jsonData, true);
		return true;
	}
}