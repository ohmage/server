package edu.ucla.cens.awserver.validator;

import java.lang.reflect.Field;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates that the value found for the provided InputKey is a positive integer.
 * 
 * @author Joshua Selsky
 */
public class InputKeyIsIntegerValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(InputKeyIsIntegerValidator.class);
	private String _inputKey;
	
	public InputKeyIsIntegerValidator(String inputKey, AwRequestAnnotator awRequestAnnotator) throws IllegalAccessException {
		super(awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(inputKey)) {
			throw new IllegalArgumentException("a non-null non-empty input key is required");
		}
		Field[] fields = InputKeys.class.getFields();
		boolean found = false;
		for(Field field : fields) {
			if(field.get(InputKeys.class).equals(inputKey)) { // InputKeys.class is used because it is a singleton containing only
				                                              // constant values. Normally, you would use an actual object instance 
				                                              // here.
				found = true;
			}
		}
		if(! found) {
			throw new IllegalArgumentException("the input key " + inputKey + " does not exist in the system");
		}
		_inputKey = inputKey;
	}
	
	/**
     * Checks that the value retrieved from the input key on construction is a valid positive integer.
	 */
	public boolean validate(AwRequest awRequest) throws ValidatorException {
		_logger.info("Validating that " + _inputKey + " is a valid positive integer");
		
		Object value = awRequest.getToValidateValue(_inputKey);
		
		if(null == value) {
			getAnnotator().annotate(awRequest, "input key " + _inputKey + " not found");
			return false;
		}
		
		int integer = -1;
		
		try {
			
			integer = Integer.parseInt((String) value);
			
		} catch (NumberFormatException nfe) {
			
			getAnnotator().annotate(awRequest, _inputKey + " is not a parseable integer");
			return false;
		}
		
		if(integer <= 0) {
			getAnnotator().annotate(awRequest, _inputKey + " must be greater than zero");
			return false;
		}
		
		awRequest.addToProcess(_inputKey, value, true);
		return true;
	}
}