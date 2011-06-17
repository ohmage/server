package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;

/**
 * Validates that a username parameter follows our conventions.
 * 
 * @author John Jenkins
 */
public class UserNameParamValidator extends AbstractAnnotatingRegexpValidator {
	private static final Logger _logger = Logger.getLogger(UserNameParamValidator.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this validator.
	 * 
	 * @param regexp The regular expression to use to validate the usernames.
	 * 
	 * @param annotator The annotator to respond with should the validation
	 * 					fail.
	 * 
	 * @param key The key to use to get the username parameter from the 
	 * 			  request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public UserNameParamValidator(String regexp, AwRequestAnnotator annotator, String key, boolean required) {
		super(regexp, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Checks that the username parameter follows our conventions.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		String user;
		try {
			user = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException outerException) {
			try {
				user = (String) awRequest.getToValidateValue(_key);
				
				if(user == null) {
					if(_required) {
						_logger.error("Missing required key: " + _key);
						throw new ValidatorException("Missing required key: " + _key);
					}
					else {
						return true;
					}
				}
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					_logger.error("Missing required key: " + _key);
					throw new ValidatorException("Missing required key: " + _key);
				}
				else {
					return true;
				}
			}
		}
		
		_logger.info("Validating that a username parameter is valid.");
		
		if(! _regexpPattern.matcher(user).matches()) {
			getAnnotator().annotate(awRequest, "The username doesn't follow our conventions.");
			awRequest.setFailedRequest(true);
			return false;
		}
		
		awRequest.addToProcess(InputKeys.USER, user, true);
		return true;
	}
}