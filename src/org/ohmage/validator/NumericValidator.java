package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;

public class NumericValidator extends AbstractAnnotatingValidator {
	private static final Logger _logger = Logger.getLogger(NumericValidator.class);
	
	private final String _key;
	
	private final boolean _negativeAllowed;
	private final boolean _zeroAllowed;
	private final boolean _positiveAllowed;
	
	private final boolean _required;
	
	/**
	 * Creates this validator.
	 * 
	 * @param annotator The annotator to respond with should the validation 
	 * 					fail.
	 * 
	 * @param key The key to use to lookup the value in the request.
	 * 
	 * @param negativeAllowed Whether or not a negative number is allowed.
	 * 
	 * @param zeroAllowed Whether or not a value of zero is allowed.
	 * 
	 * @param positiveAllowed Whether or not positive values are allowed.
	 * 
	 * @param required Whether or not this validation is required.
	 * 
	 * @throws IllegalArgumentException Thrown if the key is null or whitespace
	 * 									only.
	 */
	public NumericValidator(AwRequestAnnotator annotator, String key, 
			boolean negativeAllowed, boolean zeroAllowed, boolean positiveAllowed, boolean required) throws IllegalArgumentException {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		
		_negativeAllowed = negativeAllowed;
		_zeroAllowed = zeroAllowed;
		_positiveAllowed = positiveAllowed;
		
		_required = required;
	}

	/**
	 * Validates that the number satisfies the constraints.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		// Get the value from the request.
		String sValue;
		try {
			sValue = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				sValue = (String) awRequest.getToValidateValue(_key);
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					throw new IllegalArgumentException("Missing required key: " + _key);
				}
				else {
					return true;
				}
			}
			
			if(sValue == null) {
				if(_required) {
					throw new IllegalArgumentException("Missing required key: " + _key);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating numeric value with key: " + _key);
		
		// Attempt to decode the value then check that it meets the setup
		// requirements.
		try {
			int value = Integer.decode(sValue);
			
			if((! _negativeAllowed) && (value < 0)) {
				getAnnotator().annotate(awRequest, "The value is negative but negative values are dissallowed.");
				return false;
			}
			else if((! _zeroAllowed) && (value == 0)) {
				getAnnotator().annotate(awRequest, "The value is zero but zero is not allowed.");
				return false;
			}
			else if((! _positiveAllowed) && (value > 0)) {
				getAnnotator().annotate(awRequest, "The value is positive but positive values are dissallowed.");
				return false;
			}
		}
		catch(NumberFormatException e) {
			getAnnotator().annotate(awRequest, "The value is not a valid numeric value.");
			return false;
		}
		
		awRequest.addToProcess(_key, sValue, true);
		return true;
	}

}
