package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;

/**
 * Validates that a string value is an acceptable string value.
 * 
 * @author John Jenkins
 */
public class StringValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(StringValidator.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this validator.
	 * 
	 * @param annotator The annotator to respond with should the validation
	 * 					fail.
	 * 
	 * @param key The key to use to get the value out of the toValidate map
	 * 			  and place it back in the toProcess map.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public StringValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * There aren't any requirements for string values, so it doesn't do 
	 * anything for now.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String string;
		try {
			string = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				string = (String) awRequest.getToValidateValue(_key);
				
				if(string == null) {
					if(_required) {
						throw new ValidatorException("Missing required key: " + _key);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new ValidatorException("Missing required key: " + _key);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating that the given string is valid: " + _key);
		
		// For now, there is no String validation, so it will simply pass it to
		// the next map and return true.
		awRequest.addToProcess(_key, string, true);
		
		return true;
	}
}