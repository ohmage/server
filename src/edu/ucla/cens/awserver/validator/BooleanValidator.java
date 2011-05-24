package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates that the a value is a boolean value, one of 'true' or 'false'.
 * 
 * @author John Jenkins
 */
public class BooleanValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(BooleanValidator.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Constructs this validator.
	 * 
	 * @param annotator Annotator to respond with should the validation fail.
	 * 
	 * @param key The key to use to retrieve the boolean value from the
	 * 			  request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public BooleanValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that the value for the key is a boolean value of either 'true'
	 * or 'false'.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String booleanValue;
		try {
			booleanValue = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			booleanValue = (String) awRequest.getToValidateValue(_key);
			
			if(booleanValue == null) {
				if(_required) {
					throw new ValidatorException("Missing required key '" + _key + "'.");
				}
				else{
					return true;
				}
			}
		}
		
		_logger.info("Validating boolean value for key: " + _key);
		
		if((! booleanValue.equals("true")) && (! booleanValue.equals("false"))) {
			getAnnotator().annotate(awRequest, "The boolean value is not a valid boolean value: " + booleanValue);
			return false;
		}
		
		awRequest.addToProcess(_key, booleanValue, true);
		return true;
	}
}