package edu.ucla.cens.awserver.validator;

import java.util.UUID;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Validates that a UUID is a valid UUID.
 * 
 * @author John Jenkins
 */
public class UuidValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(UuidValidator.class);
	
	private String _key;
	private boolean _required;
	
	/**
	 * Constructor for this validator.
	 * 
	 * @param annotator The annotator to respond with should the validation
	 * 					fail.
	 * 
	 * @param regexp The regular expression to validate a UUID against.
	 */
	public UuidValidator(AwRequestAnnotator annotator, String key, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Validates that, if the UUID is required that it exists. Then, checks
	 * that the UUID is a valid UUID.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String uuid;
		try {
			uuid = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			uuid = (String) awRequest.getToValidateValue(_key);
			
			if(uuid == null) {
				if(_required) {
					throw new IllegalArgumentException("Missing required key '" + _key + "'.");
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating UUID for key '" + _key + "'.");

		try {
			UUID.fromString(uuid);
		}
		catch(IllegalArgumentException e) {
			_logger.info("The given UUID isn't a valid UUID.");
			return false;
		}
		
		awRequest.addToProcess(_key, uuid, true);
		return true;
	}
}