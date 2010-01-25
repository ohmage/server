package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public class ArrayBooleanValidator extends NullValidator {
	private static Logger _logger = Logger.getLogger(ArrayBooleanValidator.class);
	private int _requiredArrayLength;
	
	/**
	 * @throws IllegalArgumentException if the provided string is not an integer or is an integer less than one
	 */
	public ArrayBooleanValidator(String requiredArrayLength) {
		int reqLength = 0;
		try {
			
			reqLength = Integer.parseInt(requiredArrayLength);
			
		} catch (NumberFormatException nfe) {
			
			throw new IllegalArgumentException("required array length must be an integer");
		}
		
		if(reqLength < 1) {
			
			throw new IllegalArgumentException("invalid array length: " + requiredArrayLength);
		}
		
		_requiredArrayLength = reqLength;
	}
	
	/**
	 * Validates that the provided string is a JSON of the correct length and that it's elements are all t or f values.
	 * 
	 * @return true if the provided string is "null" or a valid military time
	 * @return false otherwise
	 */
	public boolean validate(String response) {
		if(super.validate(response)) {
			return true;
		}
		
		JSONArray jsonArray = JsonUtils.getJsonArrayFromString(response);
		
		if(null == jsonArray) {
			
			_logger.info("syntactically invalid JSON array");
			return false;
			
		} 
		
		int arrayLength = jsonArray.length();
		
		if(arrayLength != _requiredArrayLength) {
			
			_logger.info("invalid JSON array");
			return false;
			
		} 
			
		for(int i = 0; i < arrayLength; i++) {
			
			String value = JsonUtils.getStringFromJsonArray(jsonArray, i);
			
			if(null == value) {
				
				_logger.info("invalid JSON array element at index " + i);
				return false;
				
			}
			
			if(! ("t".equals(value) || "f".equals(value))) {
				
				_logger.info("invalid JSON boolean array element (not t or f) at index " + i);
				return false;
				
			}	
		}
			
		return true;
	}
}
